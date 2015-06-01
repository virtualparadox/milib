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
package com.milaboratory.core.alignment.batchaligner;

import com.milaboratory.core.alignment.AlignmentScoring;
import com.milaboratory.core.sequence.Sequence;

public class BatchAlignerParameters<S extends Sequence<S>> {
    int maxHits;
    float relativeMinScore, absoluteMinScore;
    boolean global;
    AlignmentScoring<S> scoring;

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public AlignmentScoring<S> getScoring() {
        return scoring;
    }

    public void setScoring(AlignmentScoring<S> scoring) {
        this.scoring = scoring;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }

    public float getRelativeMinScore() {
        return relativeMinScore;
    }

    public void setRelativeMinScore(float relativeMinScore) {
        this.relativeMinScore = relativeMinScore;
    }

    public float getAbsoluteMinScore() {
        return absoluteMinScore;
    }

    public void setAbsoluteMinScore(float absoluteMinScore) {
        this.absoluteMinScore = absoluteMinScore;
    }
}
