package com.milaboratory.core.alignment;

/**
 * BandedSemiLocalResult - class which is result of BandedSemiLocal alignment.
 * <p/>
 * <p>BandedSemiLocal alignment - alignment where part of second sequence is aligned either to left part of to right
 * part of first sequence. </p>
 * <p/>
 * <p>"Banded alignment" means that sequences to be aligned are very similar and number of mutations is very low.</p>
 */
public final class BandedSemiLocalResult {
    /**
     * Positions at which alignment terminates.
     * <p/>
     * If BandedSemiLocalLeft alignment was performed, second sequence is aligned to the left part of first sequence. If
     * BandedSemiLocalRight alignment was performed, second sequence is aligned to the right part of first sequence.
     */
    public final int sequence1Stop, sequence2Stop;
    /**
     * Score
     */
    public final int score;

    /**
     * Creates new BandedSemiLocalResult
     *
     * @param sequence1Stop position at which alignment of first sequence terminates (inclusive)
     * @param sequence2Stop position at which alignment of second sequence terminates (inclusive)
     */
    public BandedSemiLocalResult(int sequence1Stop, int sequence2Stop, int score) {
        this.sequence1Stop = sequence1Stop;
        this.sequence2Stop = sequence2Stop;
        this.score = score;
    }
}
