package com.milaboratory.core.motif;

import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.NucleotideSequenceBuilder;
import com.milaboratory.core.sequence.WildcardSymbol;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MotifTest {
    @Test
    public void test1() throws Exception {
        Motif<NucleotideSequence> motif = new Motif<>(NucleotideSequence.ALPHABET, "ATTAGACA");
        NucleotideSequence seq = new NucleotideSequence("ACTGCGATAAATTAGACAGTACGTA");
        assertFalse(motif.matches(seq, 9));
        assertTrue(motif.matches(seq, 10));
        assertFalse(motif.matches(seq, 11));
    }

    @Test
    public void test2() throws Exception {
        Motif<NucleotideSequence> motif = new Motif<>(NucleotideSequence.ALPHABET, "NNNNNNNN");
        NucleotideSequence seq = new NucleotideSequence("ACTGCGATAAATTAGACAGTACGTA");
        for (int i = 0; i < seq.size() - motif.size(); ++i)
            assertTrue(motif.matches(seq, i));
    }

    @Test
    public void test3() throws Exception {
        Motif<AminoAcidSequence> motif = new Motif<>(AminoAcidSequence.ALPHABET, "CASSLAP");
        AminoAcidSequence seq = new AminoAcidSequence("LAPGATCASSLAPGAT");
        assertFalse(motif.matches(seq, 5));
        assertTrue(motif.matches(seq, 6));
        assertFalse(motif.matches(seq, 7));
        assertTrue(new Motif<>(seq).matches(seq, 0));
    }

    @Test
    public void testRandom1() throws Exception {
        RandomGenerator rg = new Well19937c();
        for (WildcardSymbol wildcardSymbol : NucleotideSequence.ALPHABET.getAllWildcards()) {
            int seqLength = 20 + rg.nextInt(100);
            int motifSize = rg.nextInt(20);
            StringBuilder builder = new StringBuilder(motifSize);
            for (int i = 0; i < motifSize; ++i)
                builder.append(wildcardSymbol.getSymbol());
            Motif<NucleotideSequence> motif = new Motif<>(NucleotideSequence.ALPHABET, builder.toString());
            NucleotideSequenceBuilder seqBuilder = new NucleotideSequenceBuilder().ensureCapacity(seqLength);
            for (int i = 0; i < seqLength; ++i)
                seqBuilder.append(wildcardSymbol.getUniformlyDistributedSymbol(rg.nextLong()));
            NucleotideSequence seq = seqBuilder.createAndDestroy();
            for (int i = 0; i < seq.size() - motif.size(); ++i)
                assertTrue(motif.matches(seq, i));
        }
    }
}