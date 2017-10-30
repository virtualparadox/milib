package com.milaboratory.core.alignment;

import com.milaboratory.core.io.util.IOTestUtil;
import com.milaboratory.core.sequence.NucleotideAlphabetCaseSensitive;
import com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive;
import com.milaboratory.util.GlobalObjectMappers;
import org.junit.Test;

import java.util.HashMap;

import static com.milaboratory.core.sequence.SequenceQuality.BAD_QUALITY_VALUE;
import static com.milaboratory.core.sequence.SequenceQuality.GOOD_QUALITY_VALUE;
import static org.junit.Assert.*;

public class PatternAndTargetAlignmentScoringTest {
    @Test
    public void serializationTest() throws Exception {
        PatternAndTargetAlignmentScoring expected = new PatternAndTargetAlignmentScoring(0, -1,
                -1, true, GOOD_QUALITY_VALUE, BAD_QUALITY_VALUE, -1);
        String s = GlobalObjectMappers.PRETTY.writeValueAsString(expected);
        PatternAndTargetAlignmentScoring scoring = GlobalObjectMappers.ONE_LINE.readValue(s,
                PatternAndTargetAlignmentScoring.class);
        assertEquals(expected, scoring);
        IOTestUtil.assertJavaSerialization(expected);
        IOTestUtil.assertJavaSerialization(scoring);
    }

    @Test
    public void testWildcard() throws Exception {
        PatternAndTargetAlignmentScoring scoring = new PatternAndTargetAlignmentScoring(0, -5,
                -10, false, GOOD_QUALITY_VALUE, BAD_QUALITY_VALUE, 0);

        assertEquals(0, scoring.getScore(NucleotideAlphabetCaseSensitive.A,
                NucleotideAlphabetCaseSensitive.A));
        assertEquals(0, scoring.getScore(NucleotideAlphabetCaseSensitive.a,
                NucleotideAlphabetCaseSensitive.A));
        assertEquals(-5, scoring.getScore(NucleotideAlphabetCaseSensitive.A,
                NucleotideAlphabetCaseSensitive.T));
        assertEquals(-5, scoring.getScore(NucleotideAlphabetCaseSensitive.A,
                NucleotideAlphabetCaseSensitive.t));

        assertEquals(-2, scoring.getScore(NucleotideAlphabetCaseSensitive.c,
                NucleotideAlphabetCaseSensitive.S));
        assertEquals(-2, scoring.getScore(NucleotideAlphabetCaseSensitive.S,
                NucleotideAlphabetCaseSensitive.s));

        assertEquals(-5, scoring.getScore(NucleotideAlphabetCaseSensitive.A,
                NucleotideAlphabetCaseSensitive.S));
        assertEquals(-5, scoring.getScore(NucleotideAlphabetCaseSensitive.W,
                NucleotideAlphabetCaseSensitive.S));
    }

    @Test
    public void testScoreWithQuality() throws Exception {
        PatternAndTargetAlignmentScoring scoring = new PatternAndTargetAlignmentScoring(0, -5,
                -10, false, (byte)12, (byte)2, -10);
        assertEquals(0, scoring.getScore(NucleotideAlphabetCaseSensitive.A,
                NucleotideAlphabetCaseSensitive.A, (byte)20));
        assertEquals(-5, scoring.getScore(NucleotideAlphabetCaseSensitive.A,
                NucleotideAlphabetCaseSensitive.A, (byte)7));
        assertEquals(-10, scoring.getScore(NucleotideAlphabetCaseSensitive.A,
                NucleotideAlphabetCaseSensitive.A, (byte)1));
        assertEquals(-3, scoring.getScore(NucleotideAlphabetCaseSensitive.S,
                NucleotideAlphabetCaseSensitive.s, (byte)11));
    }

    @Test
    public void testGapPenalty() throws Exception {
        PatternAndTargetAlignmentScoring scoring = new PatternAndTargetAlignmentScoring(0, -5,
                -10, false, GOOD_QUALITY_VALUE, BAD_QUALITY_VALUE, 0);
        NucleotideSequenceCaseSensitive pattern = new NucleotideSequenceCaseSensitive("aTTagaCA");
        HashMap<Integer, Integer> expectedPenalties = new HashMap<Integer, Integer>() {{
            put(-1, -10); put(0, -100000000); put(1, -100000000); put(2, -100000000);
            put(3, -100000000); put(4, -10); put(5, -100000000); put(6, -100000000);
            put(7, -100000000); put(8, -100000000);
        }};
        for (HashMap.Entry<Integer, Integer> expectedPenalty : expectedPenalties.entrySet())
            assertEquals((int)expectedPenalty.getValue(), scoring.getGapPenalty(pattern, expectedPenalty.getKey()));
    }

    @Test
    public void testAlignmentScore() throws Exception {
        PatternAndTargetAlignmentScoring scoring1 = new PatternAndTargetAlignmentScoring(0, -5,
                -10, false, GOOD_QUALITY_VALUE, BAD_QUALITY_VALUE, 0);
        PatternAndTargetAlignmentScoring scoring2 = new PatternAndTargetAlignmentScoring(0, -5,
                -10, true, GOOD_QUALITY_VALUE, BAD_QUALITY_VALUE, 0);
        assertEquals(-100, scoring1.calculateAlignmentScore(-100, 10));
        assertEquals(-15, scoring2.calculateAlignmentScore(-100, 100));
        assertEquals(-28, scoring2.calculateAlignmentScore(-100, 10));
        assertEquals(-50, scoring2.calculateAlignmentScore(-100, 3));
        assertEquals(-100, scoring2.calculateAlignmentScore(-100, 1));
    }
}
