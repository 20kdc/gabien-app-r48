/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import gabien.ui.IFunction;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.cmgb.IGroupBehavior;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created on 12/30/16.
 */
public class CMDB {
    public int digitCount = 3;
    public HashMap<Integer, RPGCommand> knownCommands = new HashMap<Integer, RPGCommand>();
    public LinkedList<Integer> knownCommandOrder = new LinkedList<Integer>();
    public int listLeaveCmd = -1; // -1 means "no list leave command actually exists".
    public int blockLeaveCmd = 0; // This is 10 on R2k, but that is controlled via Lblock.

    public int dUsers = 0;

    public CMDB(final String baseFile) {
        DBLoader.readFile(baseFile, new IDatabase() {
            RPGCommand rc;
            int workingCmdId = 0;
            RPGCommand.SpecialTag nextTag = null;
            HashMap<String, SchemaElement> localAliasing = new HashMap<String, SchemaElement>();
            HashMap<Integer, SchemaElement> currentPvH = new HashMap<Integer, SchemaElement>();
            HashMap<Integer, String> currentPvH2 = new HashMap<Integer, String>();
            String subContext = "CMDB@" + baseFile;
            // sorting order seems to work well enough:
            // X-/<name>
            // X./<desc>
            // X/

            @Override
            public void newObj(int objId, String objName) {
                rc = new RPGCommand();
                subContext = "CMDB@" + baseFile + "." + lenForm(objId);
                // Names use NDB syntax, thus, separate context
                rc.name = TXDB.get(subContext + "-", objName);
                if (knownCommands.containsKey(objId))
                    throw new RuntimeException("Redefined " + objId);
                knownCommands.put(objId, rc);
                knownCommandOrder.add(objId);
                workingCmdId = objId;
                nextTag = new RPGCommand.SpecialTag();
            }

            @Override
            public void execCmd(char c, String[] args) {
                if (c == 'p') {
                    final String fv = TXDB.getExUnderscore(subContext, args[0]);
                    rc.paramName.add(new IFunction<RubyIO, String>() {
                        @Override
                        public String apply(RubyIO rubyIO) {
                            return fv;
                        }
                    });
                    String s = args[1].trim();
                    final SchemaElement se = aliasingAwareSG(s);
                    rc.paramType.add(new IFunction<RubyIO, SchemaElement>() {
                        @Override
                        public SchemaElement apply(RubyIO rubyIO) {
                            return se;
                        }
                    });
                    useTag();
                } else if (c == 'P') {
                    final HashMap<Integer, SchemaElement> h = new HashMap<Integer, SchemaElement>();
                    currentPvH = h;
                    final HashMap<Integer, String> h2 = new HashMap<Integer, String>();
                    currentPvH2 = h2;
                    // Pv-syntax:
                    // P arrayDI defaultName defaultType
                    // v specificVal name type
                    final String defName = TXDB.getExUnderscore(subContext, args[1]);
                    final int arrayDI = Integer.parseInt(args[0]);
                    final SchemaElement defaultSE = aliasingAwareSG(args[2]);
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

                    rc.paramName.add(new IFunction<RubyIO, String>() {
                        @Override
                        public String apply(RubyIO rubyIO) {
                            if (rubyIO == null)
                                return defName;
                            if (rubyIO.arrVal == null)
                                return defName;
                            if (rubyIO.arrVal.length <= arrayDI)
                                return defName;
                            int p = (int) rubyIO.arrVal[arrayDI].fixnumVal;
                            String ise = h2.get(p);
                            if (ise != null)
                                return ise;
                            return defName;
                        }
                    });
                    useTag();
                } else if (c == 'v') {
                    // v specificVal name type
                    final int idx = Integer.parseInt(args[0]);
                    currentPvH.put(idx, aliasingAwareSG(args[2]));
                    currentPvH2.put(idx, TXDB.getExUnderscore(subContext, args[1]));
                } else if (c == 'd') {
                    String desc = "";
                    for (String s : args)
                        desc += " " + s;
                    rc.description = TXDB.get(subContext + ".", desc.trim());
                } else if (c == 'i') {
                    rc.indentPre = Integer.parseInt(args[0]);
                } else if (c == 'I') {
                    final int s = Integer.parseInt(args[0]);
                    rc.indentPost = new IFunction<RubyIO, Integer>() {
                        @Override
                        public Integer apply(RubyIO rubyIO) {
                            return s;
                        }
                    };
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
                } else if ((c == 'X') || (c == 'x')) {
                    rc.specialSchema = AppMain.schemas.getSDBEntry(args[0]);
                } else if (c == 'C') {
                    if (args[0].equals("digitCount"))
                        digitCount = Integer.parseInt(args[1]);
                    if (args[0].equals("commandIndentConditionalIB")) {
                        final int target = Integer.parseInt(args[1]);
                        rc.indentPost = new IFunction<RubyIO, Integer>() {
                            @Override
                            public Integer apply(RubyIO rubyIO) {
                                if (rubyIO.arrVal.length <= target)
                                    return 0;
                                if (rubyIO.arrVal[target].fixnumVal == 0)
                                    return 0;
                                return 1;
                            }
                        };
                    }
                    if (args[0].equals("commandIndentConditionalOF")) {
                        final int[] iargs = new int[args.length - 1];
                        for (int i = 0; i < iargs.length; i++)
                            iargs[i] = Integer.parseInt(args[i + 1]);
                        rc.indentPost = new IFunction<RubyIO, Integer>() {
                            @Override
                            public Integer apply(RubyIO rubyIO) {
                                for (int i = 0; i < iargs.length; i += 2) {
                                    if (rubyIO.arrVal.length <= iargs[i])
                                        continue;
                                    if (rubyIO.arrVal[iargs[i]].type == 'i')
                                        if (rubyIO.arrVal[iargs[i]].fixnumVal == iargs[i + 1])
                                            return 1;
                                }
                                return 0;
                            }
                        };
                    }
                    if (args[0].equals("spritesheet")) {
                        // C spritesheet 0 CharSet/
                        nextTag.hasSpritesheet = true;
                        nextTag.spritesheetTargstr = Integer.parseInt(args[1]);
                        nextTag.spritesheetId = args[2];
                    }
                    if (args[0].equals("r2kTonePicker")) {
                        // C r2kTonePicker 0 1 2 3
                        nextTag.hasTonepicker = true;
                        nextTag.tpBase = 100;
                        nextTag.tpA = Integer.parseInt(args[1]);
                        nextTag.tpB = Integer.parseInt(args[2]);
                        nextTag.tpC = Integer.parseInt(args[3]);
                        nextTag.tpD = Integer.parseInt(args[4]);
                    }
                    if (args[0].equals("groupBehavior")) {
                        // For commands with just one parameter that is a string.
                        if (args[1].equals("messagebox")) {
                            final int code = Integer.parseInt(args[2]);
                            rc.groupBehavior = new IGroupBehavior() {
                                @Override
                                public int getGroupLength(RubyIO[] array, int index) {
                                    int l = 1;
                                    for (int i = index + 1; i < array.length; i++) {
                                        if (array[i].getInstVarBySymbol("@code").fixnumVal == code) {
                                            l++;
                                        } else {
                                            break;
                                        }
                                    }
                                    return l;
                                }

                                @Override
                                public int getAdditionCode() {
                                    return code;
                                }

                                @Override
                                public boolean correctElement(LinkedList<RubyIO> array, int commandIndex, RubyIO command) {
                                    return false;
                                }
                            };
                        } else if (args[1].equals("r2k_choice")) {
                            rc.groupBehavior = new IGroupBehavior() {
                                @Override
                                public int getGroupLength(RubyIO[] array, int index) {
                                    return 0;
                                }

                                @Override
                                public int getAdditionCode() {
                                    return 0;
                                }

                                @Override
                                public boolean correctElement(LinkedList<RubyIO> array, int commandIndex, RubyIO command) {
                                    RubyIO res = command.getInstVarBySymbol("@parameters").arrVal[1];
                                    long id = command.getInstVarBySymbol("@indent").fixnumVal;
                                    long nIdx = 0;
                                    if (res.fixnumVal == 4) {
                                        // Always 4.
                                        nIdx = 4;
                                    } else {
                                        for (int i = commandIndex - 1; i >= 0; i--) {
                                            RubyIO cmd = array.get(i);
                                            if (cmd.getInstVarBySymbol("@indent").fixnumVal == id) {
                                                long code = cmd.getInstVarBySymbol("@code").fixnumVal;
                                                if (code == 10140) {
                                                    // Show Choices (term.)
                                                    break;
                                                } else if (code == 20140) {
                                                    // Choice...
                                                    nIdx++;
                                                }
                                            }
                                        }
                                    }
                                    if (nIdx != res.fixnumVal) {
                                        res.fixnumVal = nIdx;
                                        return true;
                                    }
                                    return false;
                                }
                            };
                        } else {
                            throw new RuntimeException("Unknown group behavior " + args[1]);
                        }
                    }
                } else if (c == '#') {
                    DBLoader.readFile(args[0], this);
                } else if (c != ' ') {
                    // Aha! Defining comments as a != ought to shut up the warnings!
                    throw new RuntimeException("Unknown command '" + c + "'.");
                }
            }

            private void useTag() {
                rc.paramSpecialTags.add(nextTag);
                nextTag = new RPGCommand.SpecialTag();
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
        if (dUsers > 0)
            System.err.println(dUsers + " commands use D-syntax. This is deprecated in favour of Pv-syntax, which changes the parameter name accordingly and is easier to read.");
    }

    public String buildCodename(RubyIO target, boolean indent) {
        String ext = "";
        int cid = (int) target.getInstVarBySymbol("@code").fixnumVal;
        if (knownCommands.containsKey(cid)) {
            RPGCommand cmd = knownCommands.get(cid);
            RubyIO params = target.getInstVarBySymbol("@parameters");
            ext = cmd.formatName(params, params.arrVal);
        }
        String spc = lenForm(cid) + " ";
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

    private String lenForm(int cid) {
        String spc = Integer.toString(cid);
        while (spc.length() < digitCount)
            spc = "0" + spc;
        return spc;
    }
}
