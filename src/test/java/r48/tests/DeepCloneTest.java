/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.tests;

import gabien.TestKickstart;
import org.junit.Test;
import r48.AppMain;
import r48.RubyIO;
import r48.io.IMIUtils;
import r48.schema.util.SchemaPath;

import java.io.IOException;

/**
 * Created on November 21, 2018.
 */
public class DeepCloneTest {
    @Test
    public void testDeepCloneRPGMap() {
        TestKickstart.kickstart();
        AppMain.schemas.readFile("RXP/Schema.txt");
        AppMain.schemas.updateDictionaries(null);
        RubyIO newObj = new RubyIO().setNull();
        SchemaPath.setDefaultValue(newObj, AppMain.schemas.getSDBEntry("RPG::Map"), null);
        RubyIO newObj2 = new RubyIO().setDeepClone(newObj);
        try {
            byte[] dat = IMIUtils.createIMIData(newObj, newObj2, "");
            if (dat != null)
                System.out.write(dat);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Possibly compare objects in IMI?
    }
}
