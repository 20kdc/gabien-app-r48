/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import gabien.ui.IFunction;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.SchemaElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created on 12/30/16.
 */
public class CMDB {
    public int digitCount = 3;
    public HashMap<Integer, RPGCommand> knownCommands = new HashMap<Integer, RPGCommand>();
    public LinkedList<Integer> knownCommandOrder = new LinkedList<Integer>();
    public int listLeaveCmd = 0;
    public int blockLeaveCmd = 0; // This is 10 on R2k, but that is controlled via Lblock

    public CMDB(String baseFile) {
        DBLoader.readFile(baseFile, new IDatabase() {
            RPGCommand rc;
            int workingCmdId = 0;
            HashMap<String, SchemaElement> localAliasing = new HashMap<String, SchemaElement>();

            @Override
            public void newObj(int objId, String objName) {
                rc = new RPGCommand();
                rc.name = objName;
                knownCommands.put(objId, rc);
                knownCommandOrder.add(objId);
                workingCmdId = objId;
            }

            @Override
            public void execCmd(char c, String[] args) {
                if (c == 'p') {
                    rc.paramName.add(args[0].trim());
                    String s = args[1].trim();
                    final SchemaElement se = aliasingAwareSG(s);
                    rc.paramType.add(new IFunction<RubyIO, SchemaElement>() {
                        @Override
                        public SchemaElement apply(RubyIO rubyIO) {
                            return se;
                        }
                    });
                } else if (c == 'D') {
                    rc.paramName.add(args[0].trim());
                    final int arrayDI = Integer.parseInt(args[1]);
                    final SchemaElement defaultSE = aliasingAwareSG(args[2]);
                    final HashMap<Integer, SchemaElement> h = new HashMap<Integer, SchemaElement>();
                    for (int i = 3; i < args.length; i += 2) {
                        int ind = Integer.parseInt(args[i]);
                        SchemaElement se = aliasingAwareSG(args[i + 1]);
                        h.put(ind, se);
                    }
                    rc.paramType.add(new IFunction<RubyIO, SchemaElement>() {
                        @Override
                        public SchemaElement apply(RubyIO rubyIO) {
                            if (rubyIO == null)
                                return defaultSE;
                            if (rubyIO.arrVal == null)
                                return defaultSE;
                            if (rubyIO.arrVal.length <= arrayDI)
                                return defaultSE;
                            int p = (int) rubyIO.arrVal[arrayDI].fixnumVal;
                            SchemaElement ise = h.get(p);
                            if (ise != null)
                                return ise;
                            return defaultSE;
                        }
                    });
                } else if (c == 'd') {
                    String desc = "";
                    for (String s : args)
                        desc += " " + s;
                    rc.description = desc.trim();
                } else if (c == 'i') {
                    rc.indentPre = Integer.parseInt(args[0]);
                } else if (c == 'I') {
                    rc.indentPost = Integer.parseInt(args[0]);
                } else if (c == 'K') {
                    rc.needsBlockLeavePre = true;
                    rc.blockLeaveReplacement = Integer.parseInt(args[0]);
                } else if (c == 'l') {
                    rc.needsBlockLeavePre = true;
                } else if (c == 'L') {
                    rc.typeBlockLeave = true;
                    if (args[0].equals("block")) {
                        // block context only
                        blockLeaveCmd = workingCmdId;
                    } else {
                        // default context: all
                        listLeaveCmd = workingCmdId;
                        blockLeaveCmd = workingCmdId;
                    }
                } else if (c == '>') {
                    localAliasing.put(args[0], AppMain.schemas.getSDBEntry(args[1]));
                } else if (c == 'X') {
                    rc.specialSchemaName = args[0];
                } else if (c == 'C') {
                    if (args[0].equals("digitCount"))
                        digitCount = Integer.parseInt(args[1]);
                } else if (c == '#') {
                    DBLoader.readFile(args[0], this);
                } else if (c != ' ') {
                    // Aha! Defining comments as a != ought to shut up the warnings!
                    throw new RuntimeException("Unknown command '" + c + "'.");
                }
            }

            private SchemaElement aliasingAwareSG(String s) {
                SchemaElement se = localAliasing.get(s);
                if (se == null)
                    se = AppMain.schemas.getSDBEntry(s);
                return se;
            }
        });
        // see if I need to be informed that the schema doesn't support the latest and greatest features
        int fails1 = 0;
        for (RPGCommand rc : knownCommands.values())
            if (rc.description == null) {
                System.err.print(rc.name + " ");
                fails1++;
            }
        if (fails1 > 0)
            System.err.println(fails1 + " commands do not have descriptions.");
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
        while (spc.length() < (digitCount + 1))
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
