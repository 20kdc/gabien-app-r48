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
import r48.dbs.CMDB;
import r48.io.IObjectBackend;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
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
        beginCommandProcedure();
        runMainCommandProcedure("RPG::EventCommand", "R2K/Commands.txt", rpgEvInst.getIVar("@pages").getAElem(1).getIVar("@list"));
    }

    @Test
    public void testMoveCommands() {
        beginCommandProcedure();
        runMainCommandProcedure("RPG::MoveCommand", "R2K/CommandsMove.txt", rpgEvInst.getIVar("@pages").getAElem(1).getIVar("@move_route").getIVar("@list"));
    }

    @Test
    public void testMoveCommandsEmbeddedInEventCommand() {
        beginCommandProcedure();
        IRIO lst = rpgEvInst.getIVar("@pages").getAElem(1).getIVar("@list");
        IRIO res = addCommandInto("RPG::EventCommand", lst);
        res.getIVar("@code").setFX(11330);
        rpgEv.modifyVal(rpgEvInst, rpgEvP, false);
        runMainCommandProcedure("RPG::MoveCommand", "R2K/CommandsMove.txt", res.getIVar("@move_commands"));
    }

    private IRIO addCommandInto(String listType, IRIO iVar) {
        IRIO res = iVar.addAElem(0);
        SchemaPath.setDefaultValue(res, AppMain.schemas.getSDBEntry(listType), new IRIOFixnum(0));
        rpgEv.modifyVal(rpgEvInst, rpgEvP, false);
        return res;
    }

    private void beginCommandProcedure() {
        TestKickstart.kickstart("RAM/", "UTF-8", "R2K/");
        rpgEvInst = new Event();
        rpgEv = AppMain.schemas.getSDBEntry("RPG::Event");
        rpgEvP = new SchemaPath(rpgEv, new IObjectBackend.MockLoadedObject(rpgEvInst));
        rpgEv.modifyVal(rpgEvInst, rpgEvP, true);
    }

    private void runMainCommandProcedure(String cmdt, String cmd, IRIO iVar) {
        IRIO res = addCommandInto(cmdt, iVar);
        CMDB cmdb = AppMain.schemas.getCMDB(cmd);
        for (int i : cmdb.knownCommandOrder) {
            res.getIVar("@code").setFX(i);
            rpgEv.modifyVal(rpgEvInst, rpgEvP, true);
        }
    }
}
