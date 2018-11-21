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
import r48.dbs.SDB;
import r48.io.IObjectBackend;

/**
 * Created on November 19, 2018.
 */
public class TestKickstart {
    public static void kickstart() {
        GaBIEn.internal = new GaBIEnImpl(false);
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
    }
}
