package com.milaboratory.core.alignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.Sequence;

import java.util.Arrays;

/**
 * AbstractAlignmentScoring - abstract scoring system class used for alignment procedure.
 *
 * @param <S> type of sequences to be aligned using scoring system
 */
public class AbstractAlignmentScoring<S extends Sequence<S>> implements AlignmentScoring<S> {
    /**
     * Link to alphabet
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected final Alphabet<S> alphabet;

    /**
     * Substitution matrix
     */
    @JsonSerialize(using = ScoringMatrixIO.Serializer.class)
    @JsonDeserialize(using = ScoringMatrixIO.Deserializer.class)
    protected final int[] subsMatrix;

    /**
     * Flag indicating whether substitution matrix has the same value on main diagonal or not
     */
    final transient boolean uniformMatch;

    /**
     * Abstract class constructor. <p>Initializes substitution matrix to {@code null} and uniformMatch to {@code
     * true}</p>
     *
     * @param alphabet alphabet to be used by scoring system
     */
    protected AbstractAlignmentScoring(Alphabet<S> alphabet) {
        this.alphabet = alphabet;
        this.subsMatrix = null;
        this.uniformMatch = true;
    }

    /**
     * Abstract class constructor. <p>Initializes uniformMatch to {@code true}</p>
     *
     * @param alphabet   alphabet to be used by scoring system
     * @param subsMatrix substitution matrix
     */
    public AbstractAlignmentScoring(Alphabet<S> alphabet, int[] subsMatrix) {
        int size = alphabet.size();

        //For deserialization see ScoringMatrixIO.Deserializer
        if (subsMatrix.length == 2)
            subsMatrix = ScoringUtils.getSymmetricMatrix(subsMatrix[0], subsMatrix[1], size);
        else
            //Normal arguments check
            if (subsMatrix.length != size * size)
                throw new IllegalArgumentException();

        this.alphabet = alphabet;
        this.subsMatrix = subsMatrix;

        // Setting uniformity of match score flag
        int val = getScore((byte) 0, (byte) 0);
        boolean e = true;
        for (byte i = (byte) (size - 1); i > 0; --i)
            if (getScore(i, i) != val) {
                e = false;
                break;
            }
        this.uniformMatch = e;
    }

    /**
     * Returns score value for specified alphabet letter codes
     *
     * @param from code of letter which is to be replaced
     * @param to   code of letter which is replacing
     * @return score value
     */
    public int getScore(byte from, byte to) {
        return subsMatrix[from * alphabet.size() + to];
    }

    /**
     * Returns alphabet
     *
     * @return alphabet
     */
    public Alphabet<S> getAlphabet() {
        return alphabet;
    }

    /**
     * Returns @code{true} if @code{getScore(i, i)} returns the same score for all possible values of @code{i}.
     *
     * @return @code{true} if @code{getScore(i, i)} returns the same score for all possible values of @code{i}
     */
    public boolean uniformMatchScore() {
        return uniformMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractAlignmentScoring that = (AbstractAlignmentScoring) o;

        if (getAlphabet() != ((AbstractAlignmentScoring) o).getAlphabet())
            return false;

        return Arrays.equals(subsMatrix, that.subsMatrix);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(subsMatrix) + 31 * getAlphabet().hashCode();
    }
}