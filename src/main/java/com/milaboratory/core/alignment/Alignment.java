package com.milaboratory.core.alignment;

import com.milaboratory.core.Range;
import com.milaboratory.core.mutations.Mutation;
import com.milaboratory.core.mutations.Mutations;
import com.milaboratory.core.sequence.Alphabet;
import com.milaboratory.core.sequence.Sequence;
import com.milaboratory.util.BitArray;
import com.milaboratory.util.IntArrayList;

import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.core.mutations.Mutation.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Alignment<S extends Sequence<S>> {
    /**
     * Initial sequence. (upper sequence in alignment; sequence1)
     */
    final S sequence1;
    /**
     * Mutations
     */
    final Mutations<S> mutations;
    /**
     * Range in initial sequence (sequence1)
     */
    final Range sequence1Range;
    /**
     * Range in mutated sequence (sequence2)
     */
    final Range sequence2Range;
    /**
     * Alignment score
     */
    final float score;

    public Alignment(S sequence1, Mutations<S> mutations,
                     Range sequence1Range, Range sequence2Range,
                     float score) {

        if (!mutations.isEmpty()) {
            if (!mutations.isCompatibleWith(sequence1)
                    || !sequence1Range.contains(mutations.getMutatedRange())
                || sequence1Range.length() + mutations.getLengthDelta() != sequence2Range.length())
            throw new IllegalArgumentException("Not compatible arguments.");
        } else if (sequence1Range.length() != sequence2Range.length())
            throw new IllegalArgumentException("Not compatible arguments.");

        this.sequence1 = sequence1;
        this.mutations = mutations;
        this.sequence1Range = sequence1Range;
        this.sequence2Range = sequence2Range;
        this.score = score;
    }

    /**
     * Return initial sequence (upper sequence in alignment).
     *
     * @return initial sequence (upper sequence in alignment)
     */
    public S getSequence1() {
        return sequence1;
    }

    /**
     * Returns mutations in absolute (global) {@code sequence1} coordinates.
     *
     * @return mutations in absolute (global) {@code sequence1} coordinates
     */
    public Mutations<S> getAbsoluteMutations() {
        return mutations;
    }

    /**
     * Returns mutations in local coordinates, relative to {@code sequence1range}.
     *
     * @return mutations in local coordinates, relative to {@code sequence1range}
     */
    public Mutations<S> getRelativeMutations() {
        return mutations.move(-sequence1Range.getLower());
    }

    /**
     * Returns aligned range of sequence1.
     *
     * @return aligned range of sequence1
     */
    public Range getSequence1Range() {
        return sequence1Range;
    }

    /**
     * Returns aligned range of sequence2.
     *
     * @return aligned range of sequence2
     */
    public Range getSequence2Range() {
        return sequence2Range;
    }

    public float getScore() {
        return score;
    }

    /**
     * Returns number of matches divided by sum of number of matches and mismatches.
     *
     * @return number of matches divided by sum of number of matches and mismatches
     */
    public float similarity() {
        int match = 0, mismatch = 0;
        int pointer = sequence1Range.getFrom();
        int mutPointer = 0;
        int mut;
        while (mutPointer < mutations.size())
            if (mutPointer < mutations.size() && ((mut = mutations.getMutation(mutPointer)) >>> POSITION_OFFSET) <= pointer)
                switch (mut & MUTATION_TYPE_MASK) {
                    case RAW_MUTATION_TYPE_SUBSTITUTION:
                        pointer++;
                        ++mutPointer;
                        ++mismatch;
                        break;
                    case RAW_MUTATION_TYPE_DELETION:
                        pointer++;
                        ++mutPointer;
                        ++mismatch;
                        break;
                    case RAW_MUTATION_TYPE_INSERTION:
                        ++mutPointer;
                        ++mismatch;
                        break;
                }
            else {
                ++match;
                ++pointer;
            }

        // ??
        assert pointer <= sequence1Range.getUpper();

        return 1.0f * match / (match + mismatch);
    }

    /**
     * Returns alignment helper to simplify alignment output in conventional (BLAST) form.
     *
     * @return alignment helper
     */
    public AlignmentHelper getAlignmentHelper() {
        int pointer1 = getSequence1Range().getFrom();
        int pointer2 = getSequence2Range().getFrom();
        int mutPointer = 0;
        int mut;
        final Alphabet<S> alphabet = sequence1.getAlphabet();

        List<Boolean> matches = new ArrayList<>();
        IntArrayList pos1 = new IntArrayList(sequence1.size() + mutations.size()),
                pos2 = new IntArrayList(sequence1.size() + mutations.size());
        StringBuilder sb1 = new StringBuilder(),
                sb2 = new StringBuilder();

        while (pointer1 < sequence1.size() || mutPointer < mutations.size()) {
            if (mutPointer < mutations.size() && ((mut = mutations.getMutation(mutPointer)) >>> POSITION_OFFSET) <= pointer1)
                switch (mut & MUTATION_TYPE_MASK) {
                    case RAW_MUTATION_TYPE_SUBSTITUTION:
                        if (((mut >> FROM_OFFSET) & LETTER_MASK) != sequence1.codeAt(pointer1))
                            throw new IllegalArgumentException("Mutation = " + Mutation.toString(sequence1.getAlphabet(), mut) +
                                    " but seq[" + pointer1 + "]=" + sequence1.charFromCodeAt(pointer1));
                        pos1.add(pointer1);
                        pos2.add(pointer2++);
                        sb1.append(sequence1.charFromCodeAt(pointer1++));
                        sb2.append(alphabet.symbolFromCode((byte) (mut & LETTER_MASK)));
                        matches.add(false);
                        ++mutPointer;
                        break;
                    case RAW_MUTATION_TYPE_DELETION:
                        if (((mut >> FROM_OFFSET) & LETTER_MASK) != sequence1.codeAt(pointer1))
                            throw new IllegalArgumentException("Mutation = " + Mutation.toString(alphabet, mut) +
                                    " but seq[" + pointer1 + "]=" + sequence1.charFromCodeAt(pointer1));
                        pos1.add(pointer1);
                        pos2.add(-1 - pointer2);
                        sb1.append(sequence1.charFromCodeAt(pointer1++));
                        sb2.append("-");
                        matches.add(false);
                        ++mutPointer;
                        break;
                    case RAW_MUTATION_TYPE_INSERTION:
                        pos1.add(-1 - pointer1);
                        pos2.add(pointer2++);
                        sb1.append("-");
                        sb2.append(alphabet.symbolFromCode((byte) (mut & LETTER_MASK)));
                        matches.add(false);
                        ++mutPointer;
                        break;
                }
            else {
                pos1.add(pointer1);
                pos2.add(pointer2++);
                sb1.append(sequence1.charFromCodeAt(pointer1));
                sb2.append(sequence1.charFromCodeAt(pointer1++));
                matches.add(true);
            }
        }

        return new AlignmentHelper(sb1.toString(), sb2.toString(), pos1.toArray(), pos2.toArray(),
                new BitArray(matches));
    }
}
