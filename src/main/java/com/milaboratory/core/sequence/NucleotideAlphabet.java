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
package com.milaboratory.core.sequence;

import gnu.trove.map.hash.TCharObjectHashMap;

import java.util.Collection;
import java.util.Collections;

/**
 * Nucleotide alphabet.
 * <p/>
 * <table> <tr><td>0</td><td>A</td></tr> <tr><td>1</td><td>G</td></tr> <tr><td>2</td><td>C</td></tr>
 * <tr><td>3</td><td>T</td></tr> </table>
 *
 * @author Bolotin Dmitriy (bolotin.dmitriy@gmail.com)
 * @author Shugay Mikhail (mikhail.shugay@gmail.com)
 */
public final class NucleotideAlphabet extends Alphabet<NucleotideSequence> implements WithWildcards {
    public static final byte A = 0x00;
    public static final byte G = 0x01;
    public static final byte C = 0x02;
    public static final byte T = 0x03;
    private static char[] chars = {'A', 'G', 'C', 'T'};
    private static byte[] bytes = {'A', 'G', 'C', 'T'};
    final static NucleotideAlphabet INSTANCE = new NucleotideAlphabet();
    private static TCharObjectHashMap<WildcardSymbol> wildcardsMap =
            new WildcardMapBuilder()
                    /* Exact match letters */
                    .addAlphabet(INSTANCE)
                    /* Two-letter wildcard */
                    .addWildcard('R', A, G)
                    .addWildcard('Y', C, T)
                    .addWildcard('S', G, C)
                    .addWildcard('W', A, T)
                    .addWildcard('K', G, T)
                    .addWildcard('M', A, C)
                    /* Three-letter wildcard */
                    .addWildcard('B', C, G, T)
                    .addWildcard('D', A, G, T)
                    .addWildcard('H', A, C, T)
                    .addWildcard('V', A, C, G)
                    /* N */
                    .addWildcard('N', A, T, G, C)
                    .get();

    private NucleotideAlphabet() {
        super("nucleotide");
    }

    public static byte getComplement(byte code) {
        return (code ^= 3);
    }

    public byte codeFromSymbol(char symbol) {
        switch (symbol) {
            case 'a':
            case 'A':
                return (byte) 0x00;
            case 't':
            case 'T':
                return (byte) 0x03;
            case 'g':
            case 'G':
                return (byte) 0x01;
            case 'c':
            case 'C':
                return (byte) 0x02;
        }
        return -1;
    }

    public static byte codeFromSymbolByte(byte symbol) {
        switch (symbol) {
            case (byte) 'a':
            case (byte) 'A':
                return (byte) 0x00;
            case (byte) 't':
            case (byte) 'T':
                return (byte) 0x03;
            case (byte) 'g':
            case (byte) 'G':
                return (byte) 0x01;
            case (byte) 'c':
            case (byte) 'C':
                return (byte) 0x02;
        }
        return -1;
    }

    public static byte symbolByteFromCode(byte code) {
        return bytes[code];
    }

    public char symbolFromCode(byte code) {
        if (code < 0 || code >= chars.length)
            throw new RuntimeException("Wrong code.");
        return chars[code];
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public Collection<WildcardSymbol> getAllWildcards() {
        return Collections.unmodifiableCollection(wildcardsMap.valueCollection());
    }

    @Override
    public WildcardSymbol getWildcardFor(char symbol) {
        return wildcardsMap.get(symbol);
    }

    @Override
    public NucleotideSequenceBuilder getBuilder() {
        return new NucleotideSequenceBuilder();
    }
}
