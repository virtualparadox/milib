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

import com.milaboratory.core.sequence.NucleotideSequenceWithWildcards;
import com.milaboratory.util.GlobalObjectMappers;
import org.junit.Assert;
import org.junit.Test;

public class LinearGapAlignmentScoringTest {
    @Test
    public void test1() throws Exception {
        AlignmentScoring expected = AffineGapAlignmentScoring.getNucleotideBLASTScoring();
        String s = GlobalObjectMappers.PRETTY.writeValueAsString(expected);
        AlignmentScoring scoring = GlobalObjectMappers.ONE_LINE.readValue(s, AlignmentScoring.class);
        Assert.assertEquals(expected, scoring);
    }

    @Test
    public void test2() throws Exception {
        AlignmentScoring expected = new LinearGapAlignmentScoring<NucleotideSequenceWithWildcards>(NucleotideSequenceWithWildcards.ALPHABET, 15, -24, -4);
        String s = GlobalObjectMappers.PRETTY.writeValueAsString(expected);
        AlignmentScoring scoring = GlobalObjectMappers.ONE_LINE.readValue(s, AlignmentScoring.class);
        Assert.assertEquals(expected, scoring);
    }
}
