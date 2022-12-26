/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import r48.io.R48ObjectBackend;
import r48.io.r2k.struct.MoveCommand;

/**
 * Created on August 12th 2022
 */
public class MCMetaIntegerTest {
    public static final int[] intTestVectors = new int[] {
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

    @Test
    public void testR2KMoveCommandMetaInteger() {
        for (int i : intTestVectors) {
            List<Integer> li = new LinkedList<Integer>();
            MoveCommand.addMetaInteger(li, i);
            System.out.println(i + " - encoded as " + li.size() + " ints");
            int check = MoveCommand.popMetaInteger(li.iterator());
            Assert.assertEquals(check, i);
        }
    }

    @Test
    public void testR48() throws Exception {
        for (int i : intTestVectors) {
            testR48(i);
        }
    }
    private void testR48(long l) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        R48ObjectBackend.save32STM(new DataOutputStream(baos), l);
        System.out.println(l + " encoded in " + baos.size() + " bytes");
        long check = R48ObjectBackend.load32(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        Assert.assertEquals(check, l);
    }
}
