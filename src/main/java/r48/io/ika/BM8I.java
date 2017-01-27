/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.ika;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handler for a very specific bitmap format.
 * Created on 1/27/17.
 */
public class BM8I {

    public int width, height;
    public int[] data;

    /* Header of Ikachan's Map1.pbm,
       StartOffset: Length: 0x36 */
    // This is the exact same data as any other relatively space-efficient winbmp
    //  with the same parameters and colourmap.
    // Please don't sue me.
    private static byte normalPbmHeader[] = {
            (byte) 0x42, (byte) 0x4D, (byte) 0x36, (byte) 0x4F, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x36, (byte) 0x04,
            (byte) 0x00, (byte) 0x00, (byte) 0x28, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            // 0x12
            (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x78, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 4, 4
            // 0x1A
            (byte) 0x01, (byte) 0x00, (byte) 0x08, (byte) 0x00, // 2, 2
            // 0x1E
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 4, 4
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 4, 4
            // 0x2E
            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 4, 4
            // 0x36
    };

    public void saveBitmap(OutputStream os) throws IOException {
        os.write(normalPbmHeader, 0, 18);
        os.write(width & 0xFF);
        os.write((width & 0xFF00) >> 8);
        os.write((width & 0xFF0000) >> 16);
        os.write((width & 0xFF000000) >> 24);
        os.write(height & 0xFF);
        os.write((height & 0xFF00) >> 8);
        os.write((height & 0xFF0000) >> 16);
        os.write((height & 0xFF000000) >> 24);
        os.write(normalPbmHeader, 26, normalPbmHeader.length - 26);
        // dump palette
        for (int i = 0; i < 256; i++) {
            os.write(0);
            os.write(0);
            os.write(0);
            os.write(0);
        }
        for (int x = 0; x < width * height; x++) {
            int rx = x % width;
            int ry = x / width;
            ry = height - (1 + ry);
            os.write(data[rx + (ry * width)]);
        }
    }

    public void loadBitmap(InputStream ids) throws IOException {
        if (ids.read() != ((int) 'B'))
            throw new IOException("Not a WINBMP");
        if (ids.read() != ((int) 'M'))
            throw new IOException("Not a WINBMP");
        ids.skip(8);//10
        int ofs = ids.read();
        ofs |= ids.read() << 8;
        ofs |= ids.read() << 16;
        ofs |= ids.read() << 24;
        //14
        ids.skip(4);//18
        ofs -= 18;
        width = ids.read();
        width |= ids.read() << 8;
        width |= ids.read() << 16;
        width |= ids.read() << 24;

        height = ids.read();
        height |= ids.read() << 8;
        height |= ids.read() << 16;
        height |= ids.read() << 24;
        ofs -= 8;
        System.out.println("Loading Pixel's BMP header ok," + width + "x" + height + " - if it's not the right bit depth prepare for spectacular fireworks");
        data = new int[width * height];
        ids.skip(ofs);
        for (int x = 0; x < width * height; x++) {
            int rx = x % width;
            int ry = x / width;
            ry = height - (1 + ry);
            data[rx + (ry * width)] = ids.read();
        }
    }
}
