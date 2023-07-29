/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests;

import java.io.IOException;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Test;

import gabien.TestKickstart;
import r48.App;
import r48.imageio.ImageIOFormat;
import r48.imageio.ImageIOImage;
import r48.imageio.PNG8IImageIOFormat;
import r48.imageio.XYZImageIOFormat;

/**
 * Created 29th July, 2023
 */
public class ImageIOTest {
    @Test
    public void testXYZ() throws IOException {
        App app = new TestKickstart().kickstart("RAM/", "UTF-8", "r2k");
        testFormat(new XYZImageIOFormat(app), true);
    }
    @Test
    public void testPNG8I() throws IOException {
        App app = new TestKickstart().kickstart("RAM/", "UTF-8", "r2k");
        testFormat(new PNG8IImageIOFormat(app), false);
    }

    private void testFormat(ImageIOFormat imageIOFormat, boolean expectedToLoseTransparency) throws IOException {
        LinkedList<Integer> pal = new LinkedList<>();
        pal.add(0x00000000);
        pal.add(0xFFFF0000);
        pal.add(0xFF00FF00);
        pal.add(0xFF0000FF);
        ImageIOImage iioi = new ImageIOImage(2, 2, new int[] {0, 3, 2, 1}, pal);
        assertExpectedTestImageContents(iioi, false);
        byte[] data = imageIOFormat.saveFile(iioi);
        iioi = imageIOFormat.loadFile(data, null);
        assertExpectedTestImageContents(iioi, expectedToLoseTransparency);
    }

    private void assertExpectedTestImageContents(ImageIOImage iioi, boolean expectedToLoseTransparency) {
        Assert.assertEquals(0, iioi.getRaw(0, 0));
        Assert.assertEquals(3, iioi.getRaw(1, 0));
        Assert.assertEquals(2, iioi.getRaw(0, 1));
        Assert.assertEquals(1, iioi.getRaw(1, 1));
        if (expectedToLoseTransparency) {
            Assert.assertEquals(0xFF000000, (int) iioi.palette.get(0));
        } else {
            Assert.assertEquals(0x00000000, (int) iioi.palette.get(0));
        }
        Assert.assertEquals(0xFFFF0000, (int) iioi.palette.get(1));
        Assert.assertEquals(0xFF00FF00, (int) iioi.palette.get(2));
        Assert.assertEquals(0xFF0000FF, (int) iioi.palette.get(3));
    }
}
