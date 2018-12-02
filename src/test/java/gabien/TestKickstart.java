/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabien;

import gabien.ui.IConsumer;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.ObjectDB;
import r48.dbs.SDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.schema.specialized.IMagicalBinder;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Ties into gabien-javase to create a test workbench.
 * Unusable outside of it.
 * Created on November 19, 2018.
 */
public class TestKickstart {
    public static void kickstart() {
        final HashMap<String, byte[]> mockFS = new HashMap<String, byte[]>();
        GaBIEn.internal = new GaBIEnImpl(false) {
            @Override
            public InputStream getFile(String FDialog) {
                byte[] data = mockFS.get(FDialog);
                if (data == null)
                    return null;
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
        IObjectBackend.Factory.encoding = "UTF-8";
        // Reset schemas and objectDB
        AppMain.objectDB = new ObjectDB(new IObjectBackend() {

            @Override
            public ILoadedObject loadObject(String filename) {
                return null;
            }

            @Override
            public ILoadedObject newObject(String filename) {
                return null;
            }

            @Override
            public String userspaceBindersPrefix() {
                return null;
            }

        }, new IConsumer<String>() {
            @Override
            public void accept(String s) {
            }
        });
        AppMain.schemas = new SDB();
        AppMain.magicalBindingCache = new WeakHashMap<IRIO, HashMap<IMagicalBinder, WeakReference<RubyIO>>>();
    }
}
