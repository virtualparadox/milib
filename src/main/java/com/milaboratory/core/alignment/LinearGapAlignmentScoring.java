package com.milaboratory.core.alignment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Sequence;

import static com.milaboratory.core.alignment.ScoringUtils.getSymmetricMatrix;

/**
 * LinearGapAlignmentScoring - scoring system which uses penalty for gap
 */
public final class LinearGapAlignmentScoring<S extends Sequence<S>> extends AbstractAlignmentScoring<S> {
    /**
     * Penalty for gap, must be < 0
     */
    private final int gapPenalty;

    /**
     * Creates new LinearGapAlignmentScoring. Required for deserialization defaults.
     */
    @SuppressWarnings("unchecked")
    private LinearGapAlignmentScoring() {
        super((Alphabet) NucleotideSequence.ALPHABET);
        gapPenalty = -5;
    }

    /**
     * Creates new LinearGapAlignmentScoring
     *
     * @param alphabet   alphabet to be used
     * @param subsMatrix substitution matrix to be used
     * @param gapPenalty penalty for gap, must be < 0
     */
    @JsonCreator
    public LinearGapAlignmentScoring(
            @JsonProperty("alphabet") Alphabet<S> alphabet,
            @JsonProperty("subsMatrix") int[] subsMatrix,
            @JsonProperty("gapPenalty") int gapPenalty) {
        super(alphabet, subsMatrix);
        if (gapPenalty >= 0)
            throw new IllegalArgumentException();
        this.gapPenalty = gapPenalty;
    }

    /**
     * Returns scoring with uniform match and mismatch scores
     *
     * @param alphabet alphabet
     * @param match    match score > 0
     * @param mismatch mismatch score < 0
     * @param gap      gap penalty < 0
     * @return scoring with uniform match and mismatch scores
     */
    public LinearGapAlignmentScoring(Alphabet<S> alphabet,
                                     int match, int mismatch,
                                     int gap) {
        this(alphabet,
                getSymmetricMatrix(match, mismatch, alphabet.size()),
                gap);
    }

    /**
     * Returns penalty score for gap
     *
     * @return penalty score for gap
     */
    public int getGapPenalty() {
        return gapPenalty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LinearGapAlignmentScoring that = (LinearGapAlignmentScoring) o;

        return gapPenalty == that.gapPenalty;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + gapPenalty;
        return result;
    }

    /* Nucleotide */

    /**
     * Returns standard Nucleotide BLAST scoring with {@code #gapPenalty=5}
     *
     * @return standard Nucleotide BLAST Scoring
     */
    public static LinearGapAlignmentScoring<NucleotideSequence> getNucleotideBLASTScoring() {
        return getNucleotideBLASTScoring(-5);
    }

    /**
     * Returns standard Nucleotide BLAST scoring
     *
     * @param gapPenalty penalty for gap value
     * @return standard Nucleotide BLAST scoring
     */
    public static LinearGapAlignmentScoring<NucleotideSequence> getNucleotideBLASTScoring(int gapPenalty) {
        return new LinearGapAlignmentScoring<>(NucleotideSequence.ALPHABET, 5, -4, gapPenalty);
    }

    /* Amino acid */

    /**
     * Returns standard amino acid BLAST scoring with {@code #gapPenalty=5}
     *
     * @param matrix BLAST substitution matrix
     * @return standard amino acid BLAST scoring
     */
    public static LinearGapAlignmentScoring<AminoAcidSequence> getAminoAcidBLASTScoring(BLASTMatrix matrix) {
        return getAminoAcidBLASTScoring(matrix, -5);
    }

    /**
     * Returns standard amino acid BLAST scoring
     *
     * @param matrix     BLAST substitution matrix
     * @param gapPenalty penalty for gap, must be < 0
     * @return standard amino acid BLAST scoring
     */
    public static LinearGapAlignmentScoring<AminoAcidSequence> getAminoAcidBLASTScoring(BLASTMatrix matrix, int gapPenalty) {
        return new LinearGapAlignmentScoring<>(AminoAcidSequence.ALPHABET,
                matrix.getMatrix(AminoAcidSequence.ALPHABET),
                gapPenalty);
    }
}