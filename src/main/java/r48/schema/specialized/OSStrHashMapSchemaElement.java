/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.UITextBox;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.io.IMIUtils;
import r48.io.PathUtils;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Because we're coming full circle.
 * Written on December 1st, 2017.
 */
public class OSStrHashMapSchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, ISchemaHost launcher, final SchemaPath path) {
        tryInitOSSHESEDB();
        if (AppMain.osSHESEDB == null)
            AppMain.launchDialog(TXDB.get("This is basically useless without a locmaps.txt file. Please prepare one by going into RXP mode, System Tools, and pressing 'Retrieve all object strings', then return here."));
        final UITextBox utb = new UITextBox("", FontSizes.schemaFieldTextHeight);
        utb.onEdit = new Runnable() {
            @Override
            public void run() {
                try {
                    target.setFX(hashString(utb.text.getBytes("UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                path.changeOccurred(false);
            }
        };
        return utb;
    }

    public static void tryInitOSSHESEDB() {
        InputStream inp = null;
        try {
            inp = GaBIEn.getInFile(PathUtils.autoDetectWindows(AppMain.rootPath + "locmaps.txt"));
            if (inp == null)
                return;
            HashMap<Integer, String> bigMap = new HashMap<Integer, String>();
            while (true) {
                int start = inp.read();
                if (start == ';')
                    break;
                if (start == -1)
                    break;
                if (start <= 32)
                    continue;
                byte[] data = IMIUtils.readIMIStringBody(inp);
                String dataStr = new String(data, "UTF-8");
                bigMap.put(hashString(data), dataStr);
            }
            inp.close();
            AppMain.osSHESEDB = bigMap;
            // Insert strings here that won't be covered by the locmap
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (inp != null)
                    inp.close();
            } catch (Exception e2) {

            }
        }
    }

    // ZLIB-CRC32. Great. Let's see if I can golf this.
    private static int hashString(byte[] dataStr) {
        int v = 0xFFFFFFFF;
        for (byte b : dataStr) {
            int zc = (b & 0xFF) ^ (v & 0xFF);
            // Calculate table value at runtime to avoid being complained at by people for using 'their' table
            for (int bit = 0; bit < 8; bit++)
                zc = ((zc >> 1) & 0x7FFFFFFF) ^ (0xedb88320 * (zc & 1));
            v = ((v >> 8) & 0xFFFFFF) ^ zc;
        }
        return ~v;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        if (target.type != 'l') {
            if (IntegerSchemaElement.ensureType(target, 'i', setDefault)) {
                target.fixnumVal = 0;
                path.changeOccurred(true);
            }
        } else if (setDefault) {
            IntegerSchemaElement.ensureType(target, 'i', setDefault);
            target.fixnumVal = 0;
            path.changeOccurred(true);
        }
    }

    public static String decode(IRIO v) {
        if (AppMain.osSHESEDB == null)
            tryInitOSSHESEDB();
        int type = v.getType();
        if (type == 'i')
            return mainDecode((int) v.getFX());
        if (type == 'l') {
            int p = 0;
            // crc32: 468dce18
            // byte order:
            //        18CE8D46
            byte[] buf = v.getBuffer();
            for (int i = buf.length - 1; i > 0; i--) {
                p <<= 8;
                p |= buf[i] & 0xFF;
            }
            return mainDecode(p);
        }
        return v.toString();
    }

    private static String mainDecode(int fixnumVal) {
        if (AppMain.osSHESEDB == null)
            return TXDB.get("[NO DB AVAILABLE]");
        String r = AppMain.osSHESEDB.get(fixnumVal);
        if (r == null)
            return TXDB.get("[UNKNOWN STRING. I just don't know what went wrong...]");
        return r;
    }
}
