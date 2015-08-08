package com.milaboratory.core.alignment.blast;

import cc.redberry.pipe.InputPort;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.OutputPortCloseable;
import cc.redberry.pipe.blocks.Buffer;
import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.batch.*;
import com.milaboratory.core.io.sequence.fasta.FastaWriter;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.mutations.MutationsUtil;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.Sequence;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Integer.parseInt;

public class BlastAligner<S extends Sequence<S>> implements PipedBatchAligner<S, ExternalDBBlastHit<S>> {
    private static final String OUTFMT = "7 btop sstart send qstart qend score bitscore evalue stitle qseq sseq";
    private static final String QUERY_ID_PREFIX = "Q";
    final BlastDB database;
    final Alphabet<S> alphabet;
    final BlastAlignerParameters parameters;
    final int batchSize;

    public BlastAligner(BlastDB database) {
        this(database, null, -1);
    }

    public BlastAligner(BlastDB database, BlastAlignerParameters parameters, int batchSize) {
        this.database = database;
        this.alphabet = (Alphabet<S>) database.getAlphabet();
        this.parameters = parameters;
        this.batchSize = batchSize;
    }

    @Override
    public <Q> OutputPort<PipedAlignmentResult<ExternalDBBlastHit<S>, Q>> align(OutputPort<Q> input, SequenceExtractor<Q, S> extractor) {
        return new BlastWorker<>(input, extractor);
    }

    @Override
    public <Q extends HasSequence<S>> OutputPort<PipedAlignmentResult<ExternalDBBlastHit<S>, Q>> align(OutputPort<Q> input) {
        return new BlastWorker<>(input, BatchAlignmentUtil.DUMMY_EXTRACTOR);
    }

    private class BlastWorker<Q> implements
            OutputPortCloseable<PipedAlignmentResult<ExternalDBBlastHit<S>, Q>> {
        final ConcurrentMap<String, Q> queryMapping = new ConcurrentHashMap<>();
        final Buffer<PipedAlignmentResult<ExternalDBBlastHit<S>, Q>> resultsBuffer;
        final Process process;
        final BlastSequencePusher<Q> pusher;
        final BlastResultsFetcher<Q> fetcher;

        public BlastWorker(OutputPort<Q> source, SequenceExtractor<Q, S> sequenceExtractor) {
            this.resultsBuffer = new Buffer<>(32);
            try {
                ProcessBuilder processBuilder = Blast.getProcessBuilder(
                        Blast.toBlastCommand(database.getAlphabet()), "-db", database.getName(), "-outfmt", OUTFMT);

                processBuilder.redirectErrorStream(false);
                if (batchSize != -1)
                    processBuilder.environment().put("BATCH_SIZE", Integer.toString(batchSize));

                this.process = processBuilder.start();
                this.pusher = new BlastSequencePusher<>(source, sequenceExtractor,
                        queryMapping, this.process.getOutputStream());
                this.fetcher = new BlastResultsFetcher<>(this.resultsBuffer.createInputPort(),
                        queryMapping, this.process.getInputStream());

                this.pusher.start();
                this.fetcher.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public PipedAlignmentResult<ExternalDBBlastHit<S>, Q> take() {
            return resultsBuffer.take();
        }

        @Override
        public void close() {
            if (pusher.source instanceof OutputPortCloseable)
                ((OutputPortCloseable) pusher.source).close();
        }
    }

    private class BlastResultsFetcher<Q> extends Thread {
        final InputPort<PipedAlignmentResult<ExternalDBBlastHit<S>, Q>> resultsInputPort;
        final BufferedReader reader;
        final ConcurrentMap<String, Q> queryMapping;

        public BlastResultsFetcher(InputPort<PipedAlignmentResult<ExternalDBBlastHit<S>, Q>> resultsInputPort,
                                   ConcurrentMap<String, Q> queryMapping, InputStream stream) {
            this.resultsInputPort = resultsInputPort;
            this.reader = new BufferedReader(new InputStreamReader(stream));
            this.queryMapping = queryMapping;
        }

        @Override
        public void run() {
            try {
                String line;
                int num = -1, done = 0;

                Q query = null;
                ArrayList<ExternalDBBlastHit<S>> hits = null;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("hits found")) {
                        num = parseInt(line.replace("#", "").replace("hits found", "").trim());
                        hits = new ArrayList<>(num);
                    } else if (line.contains("Query")) {
                        String qid = line.replace("# Query: ", "").trim();
                        query = queryMapping.remove(qid);
                        if (query == null)
                            throw new RuntimeException();
                    } else if (!line.startsWith("#")) {
                        if (hits == null)
                            throw new RuntimeException();

                        hits.add(parseLine(line));
                    }

                    if (hits != null && hits.size() == num) {
                        if (query == null)
                            throw new RuntimeException();

                        resultsInputPort.put(new PipedAlignmentResultImpl<>(hits, query));

                        query = null;
                        hits = null;
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                // Closing port
                resultsInputPort.put(null);
            }
        }

        private ExternalDBBlastHit<S> parseLine(String line) {
            String[] fields = line.split("\t");
            int i = 0;
            //btop sstart send qstart qend score sseqid qseq sseq
            String btop = fields[i++],
                    sstart = fields[i++],
                    send = fields[i++],
                    qstart = fields[i++],
                    qend = fields[i++],
                    score = fields[i++],
                    bitscore = fields[i++],
                    evalue = fields[i++],
                    stitle = fields[i++],
                    qseq = fields[i++].replace("-", ""),
                    sseq = fields[i++].replace("-", "");

            Mutations<S> mutations = new Mutations<>(alphabet, MutationsUtil.btopDecode(btop, alphabet));
            Alignment<S> alignment = new Alignment<>(alphabet.parse(sseq), mutations,
                    new Range(0, sseq.length()), new Range(parseInt(qstart) - 1, parseInt(qend)),
                    Float.parseFloat(bitscore));
            return new ExternalDBBlastHit<>(alignment, stitle, Double.parseDouble(score), Double.parseDouble(bitscore),
                    Double.parseDouble(evalue));
        }
    }

    private class BlastSequencePusher<Q> extends Thread {
        final AtomicLong counter = new AtomicLong();
        final OutputPort<Q> source;
        final SequenceExtractor<Q, S> sequenceExtractor;
        final ConcurrentMap<String, Q> queryMapping;
        final FastaWriter<S> writer;

        public BlastSequencePusher(OutputPort<Q> source, SequenceExtractor<Q, S> sequenceExtractor,
                                   ConcurrentMap<String, Q> queryMapping,
                                   OutputStream stream) {
            this.source = source;
            this.sequenceExtractor = sequenceExtractor;
            this.queryMapping = queryMapping;
            this.writer = new FastaWriter<S>(stream, FastaWriter.DEFAULT_MAX_LENGTH);
        }

        @Override
        public void run() {
            Q query;

            while ((query = source.take()) != null) {
                S sequence = sequenceExtractor.extract(query);
                String name = QUERY_ID_PREFIX + counter.incrementAndGet();
                queryMapping.put(name, query);
                writer.write(name, sequence);
            }

            writer.close();
        }
    }
}
