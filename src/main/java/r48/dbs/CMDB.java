/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import r48.AppMain;
import r48.RubyIO;
import r48.schema.EnumSchemaElement;
import r48.schema.ISchemaElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created on 12/30/16.
 */
public class CMDB {
    public HashMap<Integer, RPGCommand> knownCommands = new HashMap<Integer, RPGCommand>();
    public LinkedList<Integer> knownCommandOrder = new LinkedList<Integer>();

    public CMDB(BufferedReader br) throws IOException {
        new DBLoader(br, new IDatabase() {
            RPGCommand rc;
            HashMap<String, ISchemaElement> localAliasing = new HashMap<String, ISchemaElement>();
            @Override
            public void newObj(int objId, String objName) {
                rc = new RPGCommand();
                rc.name = objName;
                knownCommands.put(objId, rc);
                knownCommandOrder.add(objId);
            }

            @Override
            public void execCmd(char c, String[] args) {
                if (c == 'p') {
                    rc.paramName.add(args[0].trim());
                    String s = args[1].trim();
                    ISchemaElement se = localAliasing.get(s);
                    if (se == null)
                        se = AppMain.schemas.getSDBEntry(s);
                    rc.paramType.add(se);
                }
                if (c == 'i')
                    rc.indentPre = Integer.parseInt(args[0]);
                if (c == 'I')
                    rc.indentPost = Integer.parseInt(args[0]);
                if (c == 'K') {
                    rc.needsBlockLeavePre = true;
                    rc.blockLeaveReplacement = Integer.parseInt(args[0]);
                }
                if (c == 'l')
                    rc.needsBlockLeavePre = true;
                if (c == 'L')
                    rc.typeBlockLeave = true;
                if (c == '>')
                    localAliasing.put(args[0], AppMain.schemas.getSDBEntry(args[1]));
                if (c == 'X')
                    rc.specialSchemaName = args[0];
            }
        });
    }

    public String buildCodename(RubyIO target, boolean indent) {
        String ext = "";
        int cid = (int) target.getInstVarBySymbol("@code").fixnumVal;
        if (knownCommands.containsKey(cid)) {
            RPGCommand cmd = knownCommands.get(cid);
            RubyIO params = target.getInstVarBySymbol("@parameters");
            ext = cmd.formatName(params, params.arrVal);
        }
        String spc = cid + " ";
        while (spc.length() < 4)
            spc = "0" + spc;
        RubyIO indentValue = target.getInstVarBySymbol("@indent");
        if ((indentValue != null) && indent) {
            int len = (int) target.getInstVarBySymbol("@indent").fixnumVal;
            if (len < 0) {
                spc += "(INDTERR" + len + ")";
            } else {
                for (int i = 0; i < len; i++)
                    spc += "_";
            }
        }
        return spc + ext;
    }
}
