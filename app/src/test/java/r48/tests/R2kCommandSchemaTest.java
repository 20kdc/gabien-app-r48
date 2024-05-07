/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.tests;

import gabien.TestKickstart;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import r48.App;
import r48.dbs.CMDB;
import r48.io.IObjectBackend;
import r48.io.data.DMKey;
import r48.io.data.IDM3Context;
import r48.io.data.IRIO;
import r48.io.data.obj.DM2Context;
import r48.io.r2k.obj.Event;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

/**
 * Created on April 18, 2019.
 */
public class R2kCommandSchemaTest {

    private IRIO rpgEvInst;
    private SchemaElement rpgEv;
    private SchemaPath rpgEvP;

    @Test
    public void testEventCommands() {
        App app = beginCommandProcedure();
        runMainCommandProcedure(app, "RPG::EventCommand", "event", rpgEvInst.getIVar("@pages").getAElem(1).getIVar("@list"));
    }

    @Test
    public void testMoveCommands() {
        App app = beginCommandProcedure();
        runMainCommandProcedure(app, "RPG::MoveCommand", "move", rpgEvInst.getIVar("@pages").getAElem(1).getIVar("@move_route").getIVar("@list"));
    }

    @Test
    public void testMoveCommandsEmbeddedInEventCommand() {
        App app = beginCommandProcedure();
        IRIO lst = rpgEvInst.getIVar("@pages").getAElem(1).getIVar("@list");
        IRIO res = addCommandInto(app, "RPG::EventCommand", lst);
        res.getIVar("@code").setFX(11330);
        rpgEv.modifyVal(rpgEvInst, rpgEvP, false);
        runMainCommandProcedure(app, "RPG::MoveCommand", "move", res.getIVar("@move_commands"));
    }

    private IRIO addCommandInto(App app, String listType, IRIO iVar) {
        IRIO res = iVar.addAElem(0);
        SchemaPath.setDefaultValue(res, app.sdb.getSDBEntry(listType), DMKey.of(0));
        rpgEv.modifyVal(rpgEvInst, rpgEvP, false);
        return res;
    }

    private App beginCommandProcedure() {
        App app = new TestKickstart().kickstart("RAM/", "UTF-8", "r2k");
        rpgEvInst = new Event(new DM2Context(IDM3Context.Null.INSTANCE, StandardCharsets.UTF_8));
        rpgEv = app.sdb.getSDBEntry("RPG::Event");
        rpgEvP = new SchemaPath(rpgEv, new IObjectBackend.MockLoadedObject(rpgEvInst));
        rpgEv.modifyVal(rpgEvInst, rpgEvP, true);
        return app;
    }

    private void runMainCommandProcedure(App app, String cmdt, String cmd, IRIO iVar) {
        IRIO res = addCommandInto(app, cmdt, iVar);
        CMDB cmdb = app.sdb.getCMDB(cmd);
        for (int i : cmdb.knownCommandOrder) {
            res.getIVar("@code").setFX(i);
            rpgEv.modifyVal(rpgEvInst, rpgEvP, true);
        }
    }
}
