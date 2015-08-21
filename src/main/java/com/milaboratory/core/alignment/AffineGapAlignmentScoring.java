/*
 * Copyright 2015 MiLaboratory.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.milaboratory.core.alignment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.AminoAcidSequence;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.Sequence;

import static com.milaboratory.core.alignment.ScoringUtils.getSymmetricMatrix;

/**
 * AffineGapAlignmentScoring - scoring which uses different penalties for gap opening and gap extension
 *
 * @param <S> type of sequences to be aligned using this scoring
 */
public final class AffineGapAlignmentScoring<S extends Sequence<S>> extends AbstractAlignmentScoring<S>
        implements java.io.Serializable {
    /**
     * Penalty value for opening gap
     */
    private final int gapOpenPenalty;
    /**
     * Penalty for extending gap
     */
    private final int gapExtensionPenalty;

    /**
     * Creates new AffineGapScoring. Required for deserialization defaults.
     */
    @SuppressWarnings("unchecked")
    private AffineGapAlignmentScoring() {
        super((Alphabet) NucleotideSequence.ALPHABET);
        gapExtensionPenalty = -1;
        gapOpenPenalty = -10;
    }

    /**
     * Creates new AffineGapScoring
     *
     * @param alphabet            alphabet to be used
     * @param subsMatrix          substitution matrix to be used
     * @param gapOpenPenalty      penalty for opening gap
     * @param gapExtensionPenalty penalty for extending gap
     */
    @JsonCreator
    public AffineGapAlignmentScoring(
            @JsonProperty("alphabet") Alphabet<S> alphabet,
            @JsonProperty("subsMatrix") int[] subsMatrix,
            @JsonProperty("gapOpenPenalty") int gapOpenPenalty,
            @JsonProperty("gapExtensionPenalty") int gapExtensionPenalty) {
        super(alphabet, subsMatrix);
        if (gapOpenPenalty >= 0 || gapExtensionPenalty >= 0)
            throw new IllegalArgumentException();
        this.gapOpenPenalty = gapOpenPenalty;
        this.gapExtensionPenalty = gapExtensionPenalty;
    }

    /**
     * Creates scoring with uniform match and mismatch scores
     *
     * @param alphabet     alphabet
     * @param match        match score > 0
     * @param mismatch     mismatch score < 0
     * @param gapOpen      gap open score < 0
     * @param gapExtension gap extend score < 0
     * @return scoring with uniform match and mismatch scores
     */
    public AffineGapAlignmentScoring(Alphabet<S> alphabet, int match, int mismatch,
                                     int gapOpen, int gapExtension) {
        this(alphabet, getSymmetricMatrix(match, mismatch, alphabet), gapOpen, gapExtension);
    }

    /**
     * Returns score value for a gap with length {@code l}
     *
     * @param l lenth of gap
     * @return score value
     */
    public int getAffineGapPenalty(int l) {
        return l == 0 ? 0 : gapOpenPenalty + (l - 1) * gapExtensionPenalty;
    }

    /**
     * Returns penalty value for opening gap
     *
     * @return penalty value for opening gap
     */
    public int getGapOpenPenalty() {
        return gapOpenPenalty;
    }

    /**
     * Returns penalty value for extending gap
     *
     * @return penalty value for extending gap
     */
    public int getGapExtensionPenalty() {
        return gapExtensionPenalty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AffineGapAlignmentScoring that = (AffineGapAlignmentScoring) o;

        if (gapExtensionPenalty != that.gapExtensionPenalty) return false;
        return gapOpenPenalty == that.gapOpenPenalty;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + gapOpenPenalty;
        result = 31 * result + gapExtensionPenalty;
        return result;
    }

    /* Nucleotide */

    /**
     * Returns standard Nucleotide BLAST scoring ({@code #gapOpenPenalty=-10}, {@code #gapExtensionPenalty=-1})
     *
     * @return standard Nucleotide BLAST scoring
     */
    public static AffineGapAlignmentScoring<NucleotideSequence> getNucleotideBLASTScoring() {
        return getNucleotideBLASTScoring(-10, -1);
    }

    /**
     * Returns Nucleotide BLAST scoring
     *
     * @param gapOpenPenalty      penalty for opening gap to be used in system
     * @param gapExtensionPenalty penalty for extending gap to be used in system
     * @return Nucleotide BLAST scoring
     */
    public static AffineGapAlignmentScoring<NucleotideSequence> getNucleotideBLASTScoring(int gapOpenPenalty, int gapExtensionPenalty) {
        return new AffineGapAlignmentScoring<>(NucleotideSequence.ALPHABET, 5, -4, gapOpenPenalty, gapExtensionPenalty);
    }

    /* Amino acid */

    /**
     * Returns standard AminoAcid BLAST scoring ({@code #gapOpenPenalty=-10}, {@code #gapExtensionPenalty=-1})
     *
     * @param matrix BLAST substitution matrix to be used
     * @return standard AminoAcid BLAST scoring
     */
    public static AffineGapAlignmentScoring<AminoAcidSequence> getAminoAcidBLASTScoring(BLASTMatrix matrix) {
        return getAminoAcidBLASTScoring(matrix, -10, -1);
    }

    /**
     * Returns AminoAcid BLAST scoring
     *
     * @param matrix              BLAST substitution matrix to be used
     * @param gapOpenPenalty      penalty for opening gap to be used in system
     * @param gapExtensionPenalty penalty for extending gap to be used in system
     * @return AminoAcid BLAST scoring
     */
    public static AffineGapAlignmentScoring<AminoAcidSequence> getAminoAcidBLASTScoring(BLASTMatrix matrix, int gapOpenPenalty, int gapExtensionPenalty) {
        return new AffineGapAlignmentScoring<>(AminoAcidSequence.ALPHABET,
                matrix.getMatrix(),
                gapOpenPenalty, gapExtensionPenalty);
    }
}
