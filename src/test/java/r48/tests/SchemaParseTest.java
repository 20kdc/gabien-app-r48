/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests;

import gabien.TestKickstart;
import org.junit.Test;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;
import r48.dbs.SDB;

import java.io.IOException;
import java.util.HashSet;

/**
 * Ensures that all schemas parse correctly.
 * Created on November 19, 2018.
 */
public class SchemaParseTest {
    @Test
    public void testSchemasParse() {
        TestKickstart.kickstart();
        final HashSet<String> schemas = new HashSet<String>();
        DBLoader.readFile("Gamepaks.txt", new IDatabase() {
            @Override
            public void newObj(int objId, final String objName) throws IOException {
                schemas.add(objName);
            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {

            }
        });
        for (String st : schemas) {
            System.err.println("-- Testing Gamepak " + st + " --");
            SDB sdb = new SDB();
            sdb.readFile(st + "/Schema.txt");
        }
    }
}
