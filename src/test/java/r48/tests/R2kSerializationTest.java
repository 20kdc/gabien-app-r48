/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests;

import gabien.TestKickstart;
import gabien.ui.IConsumer;
import org.junit.Assert;
import org.junit.Test;
import r48.AppMain;
import r48.dbs.ObjectDB;
import r48.io.IObjectBackend;
import r48.io.R2kObjectBackend;

import java.io.IOException;

/**
 * Created on December 02, 2018.
 */
public class R2kSerializationTest {
    @Test
    public void testFullIOStack() throws IOException {
        TestKickstart.kickstart();
        newIOBackend();
        AppMain.schemas.readFile("R2K/Schema.txt");
        AppMain.schemas.startupSanitizeDictionaries();
        AppMain.schemas.updateDictionaries(null);
        AppMain.schemas.confirmAllExpectationsMet();
        // Save it, but skip past most of ObjectDB since it will not be queried in future & it uses UI on failure.
        for (String s : AppMain.schemas.listFileDefs())
            AppMain.objectDB.getObject(s).save();
        // Kills off the old ObjectDB
        newIOBackend();
        for (String s : AppMain.schemas.listFileDefs()) {
            IObjectBackend.ILoadedObject i = AppMain.objectDB.getObject(s, null);
            Assert.assertNotNull(i);
        }
    }

    private void newIOBackend() {
        AppMain.objectDB = new ObjectDB(new R2kObjectBackend("/"), new IConsumer<String>() {
            @Override
            public void accept(String s) {

            }
        });
        IObjectBackend.Factory.encoding = "UTF-8";
    }

}
