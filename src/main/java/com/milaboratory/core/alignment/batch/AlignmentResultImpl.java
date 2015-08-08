package com.milaboratory.core.alignment.batch;

import com.milaboratory.core.sequence.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlignmentResultImpl<H extends AlignmentHit<?, ?>> implements AlignmentResult<H> {
    final List<? extends H> hits;

    public AlignmentResultImpl() {
        this.hits = Collections.emptyList();
    }

    public AlignmentResultImpl(List<? extends H> hits) {
        this.hits = hits;
    }

    @Override
    public List<? extends H> getHits() {
        return hits;
    }

    @Override
    public boolean isEmpty() {
        return hits.isEmpty();
    }

    @Override
    public String toString() {
        return isEmpty() ? "Empty result." : (hits.size() + " hits.");
    }
}
