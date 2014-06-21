package com.milaboratory.core.mutations;

import com.milaboratory.core.alignment.Aligner;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class MutationsTest {

    @Test
    public void testCombine1() throws Exception {
        NucleotideSequence seq1 = new NucleotideSequence("ATTAGACA"),
                seq2 = new NucleotideSequence("CATTACCA"),
                seq3 = new NucleotideSequence("CATAGCCA");

        Mutations<NucleotideSequence> m1 = Aligner.alignGlobal(LinearGapAlignmentScoring.getNucleotideBLASTScoring(), seq1, seq2).getGlobalMutations(),
                m2 = Aligner.alignGlobal(LinearGapAlignmentScoring.getNucleotideBLASTScoring(), seq2, seq3).getGlobalMutations();

        checkMutations(m1);
        checkMutations(m2);
        Mutations<NucleotideSequence> m3 = m1.combineWith(m2);
        assertTrue(MutationsUtil.check(m3));
        Assert.assertEquals(seq3, m3.mutate(seq1));
    }

    @Test
    public void testCombine2() throws Exception {
        NucleotideSequence seq1 = new NucleotideSequence("ACGTGTTACCGGTGATT"),
                seq2 = new NucleotideSequence("AGTTCTTGTTTTTTCCGTAC"),
                seq3 = new NucleotideSequence("ATCCGTAAATTACGTGCTGT");

        Mutations<NucleotideSequence> m1 = Aligner.alignGlobal(LinearGapAlignmentScoring.getNucleotideBLASTScoring(), seq1, seq2).getGlobalMutations(),
                m2 = Aligner.alignGlobal(LinearGapAlignmentScoring.getNucleotideBLASTScoring(), seq2, seq3).getGlobalMutations();

        Mutations<NucleotideSequence> m3 = m1.combineWith(m2);

        assertTrue(MutationsUtil.check(m3));
        checkMutations(m1);
        checkMutations(m2);
        checkMutations(m3);

        Assert.assertEquals(seq3, m3.mutate(seq1));
    }

    @Test
    public void testCombine3() throws Exception {
        NucleotideSequence seq1 = new NucleotideSequence("AACTGCTAACTCGA"),
                seq2 = new NucleotideSequence("CGAACGTTAAGCACAAA"),
                seq3 = new NucleotideSequence("CAAATGTGAGATC");

        Mutations<NucleotideSequence> m1 = Aligner.alignGlobal(LinearGapAlignmentScoring.getNucleotideBLASTScoring(), seq1, seq2).getGlobalMutations(),
                m2 = Aligner.alignGlobal(LinearGapAlignmentScoring.getNucleotideBLASTScoring(), seq2, seq3).getGlobalMutations();

        Mutations<NucleotideSequence> m3 = m1.combineWith(m2);

        assertTrue(MutationsUtil.check(m3));
        checkMutations(m1);
        checkMutations(m2);
        checkMutations(m3);

        Assert.assertEquals(seq3, m3.mutate(seq1));
    }

    @Test
    public void testBS() throws Exception {
        NucleotideSequence
                seq1 = new NucleotideSequence("TGACCCGTAACCCCCCGGT"),
                seq2 = new NucleotideSequence("CGTAACTTCAGCCT");

        Alignment<NucleotideSequence> alignment = Aligner.alignGlobal(LinearGapAlignmentScoring.getNucleotideBLASTScoring(),
                seq1, seq2);

//        AlignmentHelper helper = alignment.getAlignmentHelper();
//        System.out.println(helper);
//
//        int p;
//        for (int i = helper.size() - 1; i >= 0; --i) {
//            if ((p = helper.convertPositionToSeq1(i)) > 0)
//                assertEquals(NucleotideSequence.ALPHABET.symbolFromCode(seq1.codeAt(p)),
//                        helper.getLine1().charAt(i));
//            if ((p = helper.convertPositionToSeq2(i)) > 0)
//                assertEquals(NucleotideSequence.ALPHABET.symbolFromCode(seq2.codeAt(p)),
//                        helper.getLine3().charAt(i));
//        }

        Mutations<NucleotideSequence> mutations = alignment.getGlobalMutations();
        checkMutations(mutations);

        Assert.assertEquals(-1, mutations.firstMutationWithPosition(-1));
        Assert.assertEquals(3, mutations.firstMutationWithPosition(3));
        Assert.assertEquals(4, mutations.firstMutationWithPosition(4));
        Assert.assertEquals(-6, mutations.firstMutationWithPosition(5));
        Assert.assertEquals(5, mutations.firstMutationWithPosition(11));
        Assert.assertEquals(7, mutations.firstMutationWithPosition(12));
        Assert.assertEquals(8, mutations.firstMutationWithPosition(13));
    }

    public static void checkMutations(Mutations mutations) {
        assertEquals("Encode/Decode", mutations, Mutations.decode(mutations.encode(), NucleotideSequence.ALPHABET));
    }

}