/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package r48.tests;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import r48.io.r2k.struct.MoveCommand;

/**
 * Created on August 12th 2022
 */
public class MCMetaIntegerTest {
    @Test
    public void test() {
        int[] testVectors = new int[] {
                -1,
                0,
                1,
                127,
                128,
                256,
                512,
                0x00001000,
                0x00010000,
                0x01000000,
                0xFFDACABE,
                0x80000000,
                0x7FFFFFFF,
                0x80000001,
                0x28912595,
                0x10000000,
                0xF0F0F0F0,
                0x10101010
        };
        for (int i : testVectors) {
            List<Integer> li = new LinkedList<Integer>();
            MoveCommand.addMetaInteger(li, i);
            System.out.println(i + " - encoded as " + li.size() + " ints");
            int check = MoveCommand.popMetaInteger(li.iterator());
            Assert.assertEquals(check, i);
        }
    }

}
