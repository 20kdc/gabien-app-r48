/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.tests;

import gabien.TestKickstart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import r48.app.InterlaunchGlobals;
import r48.dbs.DatumLoader;
import r48.ui.help.HelpFile;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created 14th May, 2024.
 */
@RunWith(Parameterized.class)
public class HelpCheckTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        final LinkedList<Object[]> tests = new LinkedList<Object[]>();
        new TestKickstart().kickstartRFS();
        try {
            DatumLoader.readEssential("helpFileIndex", null, (value, srcLoc) -> {
                tests.add(new Object[] {value});
            });
        } catch (Exception e) {
            System.err.println("Exception during LocalTestExecutive parameterization. Unread tests will not be executed.");
            System.err.println("If you do not have the LTE files, then this is normal, do not panic.");
            e.printStackTrace();
        }
        return tests;
    }

    private final String name;
    public HelpCheckTest(String nam) {
        name = nam;
    }

    @Test
    public void test() {
        InterlaunchGlobals ilg = new TestKickstart().kickstartILG();
        HelpFile.load(ilg, name);
    }
}
