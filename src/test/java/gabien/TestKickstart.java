/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import gabien.ui.IConsumer;
import r48.AppMain;
import r48.dbs.ObjectDB;
import r48.io.IObjectBackend;

import java.io.*;
import java.util.HashMap;

/**
 * Ties into gabien-javase to create a test workbench.
 * Unusable outside of it.
 * Created on November 19, 2018.
 */
public class TestKickstart {
    public static void kickstart(final String s2, final String encoding, final String schema) {
        kickstartRFS();
        // In case unset.
        IObjectBackend.Factory.encoding = encoding;
        AppMain.initializeCore(s2, schema);
    }

    public static void kickstartRFS() {
        final HashMap<String, byte[]> mockFS = new HashMap<String, byte[]>();
        GaBIEn.internal = new GaBIEnImpl(false) {
            @Override
            public InputStream getFile(String FDialog) {
                byte[] data = mockFS.get(FDialog);
                if (data == null) {
                    if (FDialog.startsWith("./"))
                        FDialog = FDialog.substring(2);
                    return getResource(FDialog);
                }
                return new ByteArrayInputStream(data);
            }

            @Override
            public OutputStream getOutFile(final String FDialog) {
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        mockFS.put(FDialog, toByteArray());
                    }
                };
            }
        };
        // Cleanup any possible contamination of application state between tests.
        AppMain.shutdown();
    }

    public static void resetODB() {
        AppMain.objectDB = new ObjectDB(IObjectBackend.Factory.create(AppMain.odbBackend, AppMain.rootPath, AppMain.dataPath, AppMain.dataExt), new IConsumer<String>() {
            @Override
            public void accept(String s) {
            }
        });
    }
}
