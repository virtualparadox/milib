/*
 * MiTCR <http://milaboratory.com>
 *
 * Copyright (c) 2010-2013:
 *     Bolotin Dmitriy     <bolotin.dmitriy@gmail.com>
 *     Chudakov Dmitriy    <chudakovdm@mail.ru>
 *
 * MiTCR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.milaboratory.core.sequence.nucleotide;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class NucleotideSequencesTest {
    @Test
    public void test1() {
        NucleotideSequence sequence = new NucleotideSequence("ATTAGACATAGACA");
        Assert.assertEquals(sequence.toString(), "ATTAGACATAGACA");
        NucleotideSequence subSequence = sequence.getRange(0, sequence.size());
        Assert.assertEquals(subSequence.toString(), "ATTAGACATAGACA");
        Assert.assertEquals(subSequence.hashCode(), sequence.hashCode());
        Assert.assertEquals(subSequence, sequence);

        NucleotideSequence sequence1 = new NucleotideSequence("AGACATAGACA");
        NucleotideSequence subSequence1 = sequence.getRange(3, sequence.size());

        Assert.assertEquals(subSequence1.hashCode(), sequence1.hashCode());
        Assert.assertEquals(subSequence1, sequence1);
        Assert.assertEquals(NucleotideSequence.EMPTY, NucleotideSequence.EMPTY.getReverseComplement());
    }

    @Test
    public void testConcatenate() throws Exception {
        NucleotideSequence s1 = new NucleotideSequence("ATTAGACA"),
                s2 = new NucleotideSequence("GACATATA");

        Assert.assertEquals(new NucleotideSequence("ATTAGACAGACATATA"), s1.concatenate(s2));
    }

    @Test
    public void testRC1() {
        NucleotideSequence ns = new NucleotideSequence("atagagaattagataaggcagatacgatcgacgtgtactactagcta");
        NucleotideSequence rc = ns.getReverseComplement();
        NucleotideSequence rcrc = rc.getReverseComplement();
        assertEquals(rcrc, ns);
        assertEquals(rcrc.hashCode(), ns.hashCode());
        assertThat(rc, not(ns));
        assertThat(rc.hashCode(), not(ns.hashCode()));
    }
}
