package com.milaboratory.core.mutations;

import com.fasterxml.jackson.annotation.*;
import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.AlignmentScoring;
import com.milaboratory.core.alignment.kaligner1.KAlignerParameters;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.primitivio.annotations.Serializable;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class AggregatedMutations<S extends Sequence<S>> {
    final Alphabet<S> alphabet;
    final CoverageCounter coverageWeight;
    final CoverageCounter mutationWeight;
    final TIntObjectHashMap<int[]> mutations;
    final Range range;

    public AggregatedMutations(Alphabet<S> alphabet, CoverageCounter coverageWeight, CoverageCounter mutationWeight,
                               TIntObjectHashMap<int[]> mutations, Range range) {
        this.alphabet = alphabet;
        this.coverageWeight = coverageWeight;
        this.mutationWeight = mutationWeight;
        this.mutations = mutations;
        this.range = range;
    }

    public long coverageWeight(int position) {
        return coverageWeight.totalWeight(position);
    }

    public long mutationWeight(int position) {
        return mutationWeight.totalWeight(position);
    }

    public Mutations<S> mutation(int position) {
        int[] mutations = this.mutations.get(position);
        if (mutations == null)
            return Mutations.empty(alphabet);
        return new Mutations<>(alphabet, mutations, true);
    }

    public Consensus<S> buildAlignments(final S reference,
                                        final QualityProvider qualityProvider,
                                        final AlignmentScoring<S> scoring) {
        final MutationsBuilder<S> mBuilder = new MutationsBuilder<>(alphabet);
        final int from = range.getFrom(), to = range.getTo(), length = range.length();

        int mutationsDelta = 0;
        for (int[] ints : mutations.valueCollection())
            mutationsDelta += MutationsUtil.getLengthDelta(ints);

        final byte[] quality = new byte[length + mutationsDelta];
        Arrays.fill(quality, (byte) (Byte.MAX_VALUE - 70));

        mutationsDelta = 0;
        // position <= to ("=" for trailing insertions)
        for (int position = from; position <= to; ++position) {
            int[] muts = mutations.get(position);

            // In case without trailing insertions
            if (position == to)
                break;

            long coverage = coverageWeight(position);
            if (containInsertions(muts))
                coverage = Math.max(coverage, coverageWeight(position - 1));
            int index = mutationsDelta + position - from;
            byte q = qualityProvider.getQuality(coverage, mutationWeight(position), muts);

            if (muts == null) {
                quality[index] = min(quality[index], q);
            } else {
                int lDelta = MutationsUtil.getLengthDelta(muts);
                if (lDelta == 0) {
                    quality[index] = min(quality[index], q);
                } else if (lDelta < 0) {
                    assert lDelta == -1;
                    if (index >= 1)
                        quality[index - 1] = min(quality[index - 1], q);
                    if (index < quality.length)
                        quality[index] = min(quality[index], q);
                } else
                    for (int i = 0; i < lDelta + 1; i++)
                        quality[index + i] = min(quality[index + i], q);

                mBuilder.append(muts);
                mutationsDelta += lDelta;
            }
        }

        return new Consensus<>(new SequenceQuality(quality), reference,
                new Alignment<>(reference.getRange(from, to), mBuilder.createAndDestroy(), range, scoring));
    }

    public List<int[]> filtered(MutationsFilter filter) {
        TIntObjectIterator<int[]> it = mutations.iterator();
        List<int[]> filtered = new ArrayList<>();
        while (it.hasNext()) {
            it.advance();
            int pos = it.key();
            int[] muts = it.value();
            if (filter.filter(pos, muts, coverageWeight(pos), mutationWeight(pos)))
                filtered.add(muts);
        }
        return filtered;
    }

    public static boolean containInsertions(int[] muts) {
        if (muts == null)
            return false;
        for (int mut : muts)
            if (Mutation.isInsertion(mut))
                return true;
        return false;
    }

    public static byte min(byte a, byte b) {
        return (a <= b) ? a : b;
    }

    public static byte max(byte a, byte b) {
        return (a >= b) ? a : b;
    }

    public interface QualityProvider {
        byte getQuality(long coverageWeight, long mutationCount, int[] mutations);
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = KAlignerParameters.class)
    @JsonSubTypes({@JsonSubTypes.Type(value = SimpleMutationsFilter.class, name = "simple")})
    @Serializable(asJson = true)
    public interface MutationsFilter {
        boolean filter(int position, int[] mutations, long coverageWeight, long mutationWeight);
    }

    public static final class SimpleMutationsFilter implements MutationsFilter {
        public final long minimalCoverage;
        public final double minimalRatio;

        @JsonCreator
        public SimpleMutationsFilter(@JsonProperty("minimalCoverage") long minimalCoverage,
                                     @JsonProperty("minimalRatio") double minimalRatio) {
            this.minimalCoverage = minimalCoverage;
            this.minimalRatio = minimalRatio;
        }

        @Override
        public boolean filter(int position, int[] mutations, long coverageWeight, long mutationWeight) {
            if (coverageWeight < minimalCoverage)
                return false;
            return (1. * mutationWeight / coverageWeight) >= minimalRatio;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SimpleMutationsFilter)) return false;

            SimpleMutationsFilter that = (SimpleMutationsFilter) o;

            if (minimalCoverage != that.minimalCoverage) return false;
            return Double.compare(that.minimalRatio, minimalRatio) == 0;

        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = (int) (minimalCoverage ^ (minimalCoverage >>> 32));
            temp = Double.doubleToLongBits(minimalRatio);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    public static final class Consensus<S extends Sequence<S>> {
        public final SequenceQuality quality;
        public final Sequence<S> sequence;
        public final Alignment<S> alignment;

        public Consensus(SequenceQuality quality, Sequence<S> sequence, Alignment<S> alignment) {
            this.quality = quality;
            this.sequence = sequence;
            this.alignment = alignment;
        }

        public ArrayList<Consensus<S>> split(byte qualityThreshold, AlignmentScoring<S> scoring) {
            ArrayList<Consensus<S>> result = new ArrayList<>();

            int beginIn1 = 0, beginIn2 = 0;
            final Range sequence1Range = alignment.getSequence1Range();
            final int seq1To = sequence1Range.getTo();
            for (int positionIn1 = sequence1Range.getFrom(); positionIn1 < seq1To; ++positionIn1) {
                int positionIn2 = alignment.convertPosition(positionIn1);
                if (positionIn2 >= 0 && quality.value(positionIn2) <= qualityThreshold) {
                    if (positionIn1 > beginIn1 && positionIn2 > beginIn2)
                        result.add(new Consensus<>(quality.getRange(beginIn2, positionIn2),
                                sequence.getRange(beginIn2, positionIn2),
                                alignment.getRange(beginIn1, positionIn1, scoring)));

                    beginIn1 = positionIn1 + 1;
                    beginIn2 = positionIn2 + 1;
                }
            }

            int positionIn2 = alignment.convertPosition(seq1To);
            if (seq1To != beginIn1 && positionIn2 != beginIn2)
                result.add(new Consensus<>(quality.getRange(beginIn2, positionIn2),
                        sequence.getRange(beginIn2, positionIn2),
                        alignment.getRange(beginIn1, seq1To, scoring)));

            return result;
        }
    }
}