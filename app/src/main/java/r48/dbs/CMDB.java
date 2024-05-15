/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.datum.DatumSrcLoc;
import r48.App;
import r48.dbs.RPGCommand.PDyn;
import r48.dbs.RPGCommand.SpritesheetSpecialTag;
import r48.dbs.RPGCommand.TonepickerSpecialTag;
import r48.dbs.RPGCommand.XPMoveCommandSetGraphicSpecialTag;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.cmgb.IGroupBehavior;
import r48.schema.util.SchemaPath;
import r48.search.CommandTag;
import r48.tr.TrNames;
import r48.tr.TrPage.FF0;
import r48.ui.dialog.UIEnumChoice;

import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.*;

/**
 * Created on 12/30/16.
 */
public class CMDB extends App.Svc {
    /**
     * DB ID for use in internationalization
     */
    public final String dbId;

    public int digitCount = 3;
    public FF0[] categories = new FF0[] {() -> T.s.cmdb_defCatName};
    public HashMap<Integer, RPGCommand> knownCommands = new HashMap<Integer, RPGCommand>();
    public LinkedList<Integer> knownCommandOrder = new LinkedList<Integer>();
    public @Nullable DMKey listLeaveCmd = null; // null means "no list leave command actually exists".
    public int blockLeaveCmd = 0; // This is 10 on R2k, but that is controlled via Lblock.

    public CMDB(final CMDBDB cdb, final String id) {
        super(cdb.app);
        dbId = id;
    }

    /**
     * Actual load code was moved here to help pull stuff out of the old system
     */
    public void load(String readFile) {
        final SDB sdb = app.sdb;
        DBLoader.readFile(app, readFile, new IDatabase() {
            RPGCommand rc;
            int workingCmdId = 0;
            LinkedList<RPGCommand.SpecialTag> nextTag = null;
            HashMap<String, SchemaElement> localAliasing = new HashMap<String, SchemaElement>();

            String currentPvHLocPrefix;
            HashMap<Integer, RPGCommand.Param> currentPvH;

            String baseFile = readFile;
            // sorting order seems to work well enough:
            // X-/<name>
            // X./<desc>
            // X/

            DatumSrcLoc srcLoc = DatumSrcLoc.NONE;

            private @Nullable FF0 dTrExUnderscoreNull(String subkey, String defVal) {
                // Hidden parameters, introduced to deal with the "text as first parameter" thing brought about by R2k
                if (defVal.equals("_"))
                    return null;
                return app.dTr(srcLoc, subkey, defVal);
            }

            @Override
            public void newObj(int objId, String objName, DatumSrcLoc sl) {
                srcLoc = sl;
                rc = new RPGCommand(app, objId, srcLoc, dbId, objName);
                rc.category = categories.length - 1;
                if (knownCommands.containsKey(objId))
                    throw new RuntimeException("Redefined " + objId);
                knownCommands.put(objId, rc);
                knownCommandOrder.add(objId);
                workingCmdId = objId;
                nextTag = new LinkedList<RPGCommand.SpecialTag>();
            }

            int gbStatePosition;
            String[] gbStateArgs;

            IGroupBehavior getGroupBehavior() {
                final String arg = gbStateArgs[gbStatePosition++];
                // For commands with just one parameter that is a string.
                if (arg.equals("messagebox")) {
                    final int code = Integer.parseInt(gbStateArgs[gbStatePosition++]);
                    return new IGroupBehavior() {
                        @Override
                        public int getGroupLength(IRIO array, int index) {
                            int l = 1;
                            int alen = array.getALen();
                            for (int i = index + 1; i < alen; i++) {
                                if (array.getAElem(i).getIVar("@code").getFX() == code) {
                                    l++;
                                } else {
                                    break;
                                }
                            }
                            return l;
                        }

                        @Override
                        public boolean handlesAddition() {
                            return true;
                        }

                        @Override
                        public int getAdditionCode() {
                            return code;
                        }

                        @Override
                        public boolean correctElement(IRIO array, int commandIndex, IRIO command) {
                            return false;
                        }

                        @Override
                        public boolean majorCorrectElement(IRIO arr, int i, IRIO commandTarg, SchemaElement baseElement) {
                            return false;
                        }
                    };
                } else if (arg.equals("r2k_choice")) {
                    return new IGroupBehavior() {
                        @Override
                        public int getGroupLength(IRIO array, int index) {
                            return 0;
                        }

                        @Override
                        public boolean handlesAddition() {
                            return false;
                        }

                        @Override
                        public int getAdditionCode() {
                            return 0;
                        }

                        @Override
                        public boolean correctElement(IRIO array, int commandIndex, IRIO command) {
                            IRIO res = command.getIVar("@parameters").getAElem(1);
                            long id = command.getIVar("@indent").getFX();
                            long nIdx = 0;
                            if (res.getFX() == 4) {
                                // Always 4.
                                nIdx = 4;
                            } else {
                                for (int i = commandIndex - 1; i >= 0; i--) {
                                    IRIO cmd = array.getAElem(i);
                                    if (cmd.getIVar("@indent").getFX() == id) {
                                        long code = cmd.getIVar("@code").getFX();
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
                            if (nIdx != res.getFX()) {
                                res.setFX(nIdx);
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public boolean majorCorrectElement(IRIO arr, int i, IRIO commandTarg, SchemaElement baseElement) {
                            return false;
                        }
                    };
                } else if (arg.equals("form")) {
                    final int[] subIds = new int[(gbStateArgs.length - (gbStatePosition + 1)) / 2];
                    for (int i = 0; i < subIds.length; i++) {
                        subIds[i] = Integer.parseInt(gbStateArgs[gbStatePosition++]);
                        // translated names aren't actually used, which is good since I have no good idea how to translate them
                        gbStatePosition++;
                    }
                    final int lastId = Integer.parseInt(gbStateArgs[gbStatePosition++]);
                    return new IGroupBehavior() {
                        @Override
                        public int getGroupLength(IRIO arr, int ind) {
                            return 0;
                        }

                        @Override
                        public boolean handlesAddition() {
                            return false;
                        }

                        @Override
                        public int getAdditionCode() {
                            return 0;
                        }

                        @Override
                        public boolean correctElement(IRIO array, int commandIndex, IRIO command) {
                            return false;
                        }

                        @Override
                        public boolean majorCorrectElement(IRIO arr, int i, IRIO commandTarg, SchemaElement baseElement) {
                            // Form correction
                            IRIO rio = commandTarg.getIVar("@indent");
                            long topIndent = 0;
                            if (rio != null)
                                topIndent = rio.getFX();

                            int indexOfLastValid = i;

                            for (int j = i + 1; j < arr.getALen(); j++) {
                                IRIO riox = arr.getAElem(j);
                                IRIO rioy = riox.getIVar("@indent");
                                long subIndent = 0;
                                if (rioy != null)
                                    subIndent = rioy.getFX();
                                if (subIndent > topIndent) {
                                    indexOfLastValid = j;
                                    continue;
                                }
                                if (subIndent == topIndent) {
                                    long tid = riox.getIVar("@code").getFX();
                                    if (tid == lastId)
                                        return false;
                                    // If TID does not match a valid follower, BREAK NOW.
                                    // Otherwise, push valid
                                    for (int subId : subIds) {
                                        if (tid == subId) {
                                            indexOfLastValid = j;
                                            break;
                                        }
                                    }
                                    if (indexOfLastValid != j)
                                        break;
                                } else if (subIndent < topIndent) {
                                    // Abandon hope.
                                    break;
                                }
                            }
                            // Didn't find 'top', insert at best-guess
                            IRIO cap = arr.addAElem(indexOfLastValid + 1);
                            SchemaPath.setDefaultValue(cap, baseElement, null);
                            cap.getIVar("@code").setFX(lastId);
                            return true;
                        }
                    };
                } else if (arg.equals("expectHead") || arg.equals("expectTail")) {
                    final int[] ikeys = new int[gbStateArgs.length - gbStatePosition];
                    for (int i = 0; i < ikeys.length; i++)
                        ikeys[i] = Integer.parseInt(gbStateArgs[gbStatePosition++]);
                    final boolean tail = arg.equals("expectTail");
                    return new IGroupBehavior() {
                        @Override
                        public int getGroupLength(IRIO arr, int ind) {
                            return 0;
                        }

                        @Override
                        public boolean handlesAddition() {
                            return false;
                        }

                        @Override
                        public int getAdditionCode() {
                            return 0;
                        }

                        @Override
                        public boolean correctElement(IRIO array, int commandIndex, IRIO command) {
                            return false;
                        }

                        @Override
                        public boolean majorCorrectElement(IRIO arr, int i, IRIO commandTarg, SchemaElement baseElement) {
                            IRIO rio = commandTarg.getIVar("@indent");
                            long topIndent = 0;
                            if (rio != null)
                                topIndent = rio.getFX();
                            int oi = i;
                            if (tail) {
                                i++;
                            } else {
                                i--;
                            }
                            while ((i >= 0) && (i < arr.getALen())) {
                                IRIO riox = arr.getAElem(i);
                                IRIO rioy = riox.getIVar("@indent");
                                long subIndent = 0;
                                if (rioy != null)
                                    subIndent = rioy.getFX();
                                if (subIndent <= topIndent) {
                                    // Check...
                                    long perm = riox.getIVar("@code").getFX();
                                    // Permitted?
                                    for (int ip : ikeys)
                                        if (perm == ip)
                                            return false;
                                    // Not permitted, exit out immediately to let arr.remove(oi) take place
                                    break;
                                }
                                if (tail) {
                                    i++;
                                } else {
                                    i--;
                                }
                            }
                            // Ran out!
                            arr.rmAElem(oi);
                            return true;
                        }
                    };
                } else if (arg.equals("condition")) {
                    String s = gbStateArgs[gbStatePosition++];
                    final PathSyntax idx;
                    final boolean inv;
                    if (inv = s.startsWith("!")) {
                        idx = PathSyntax.compile(app.vmCtx, s.substring(1));
                    } else {
                        idx = PathSyntax.compile(app.vmCtx, s);
                    }
                    final DMKey v = ValueSyntax.decode(gbStateArgs[gbStatePosition++]);
                    final IGroupBehavior igb = getGroupBehavior();
                    return new IGroupBehavior() {
                        private boolean checkCondition(IRIO command) {
                            RORIO p = idx.getRO(command);
                            if (p == null)
                                return false;
                            return inv ^ IRIO.rubyEquals(p, v);
                        }

                        @Override
                        public int getGroupLength(IRIO arr, int ind) {
                            if (!checkCondition(arr.getAElem(ind)))
                                return 0;
                            return igb.getGroupLength(arr, ind);
                        }

                        @Override
                        public boolean handlesAddition() {
                            return igb.handlesAddition();
                        }

                        @Override
                        public int getAdditionCode() {
                            return igb.getAdditionCode();
                        }

                        @Override
                        public boolean correctElement(IRIO array, int commandIndex, IRIO command) {
                            if (!checkCondition(command))
                                return false;
                            return igb.correctElement(array, commandIndex, command);
                        }

                        @Override
                        public boolean majorCorrectElement(IRIO arr, int i, IRIO commandTarg, SchemaElement baseElement) {
                            if (!checkCondition(commandTarg))
                                return false;
                            return igb.majorCorrectElement(arr, i, commandTarg, baseElement);
                        }
                    };
                } else {
                    throw new RuntimeException("Unknown group behavior " + arg);
                }
            }

            @Override
            public void execCmd(String c, String[] args, Object[] argsObj, DatumSrcLoc sl) {
                srcLoc = sl;
                gbStatePosition = -1;
                gbStateArgs = null;
                if (c.equals("p")) {
                    int paramIdx = rc.params.size();
                    final FF0 fv = dTrExUnderscoreNull(TrNames.cmdbParam(dbId, rc.commandId, paramIdx), args[0]);
                    String s = args[1].trim();
                    final SchemaElement se = aliasingAwareSG(s);
                    rc.params.add(new RPGCommand.PStatic(fv, se));
                    useTag();
                } else if (c.equals("P")) {
                    int paramIdx = rc.params.size();
                    // Pv-syntax:
                    // P arrayDI defaultName defaultType
                    // v specificVal name type
                    final FF0 defName = dTrExUnderscoreNull(TrNames.cmdbParam(dbId, rc.commandId, paramIdx), args[1]);
                    currentPvHLocPrefix = TrNames.cmdbParam(dbId, rc.commandId, paramIdx) + ".";
                    final int arrayDI = Integer.parseInt(args[0]);
                    final SchemaElement defaultSE = aliasingAwareSG(args[2]);
                    // prepare the schema stuff which now has special requirements
                    final PDyn sd = new RPGCommand.PDyn(new RPGCommand.PStatic(defName, defaultSE), arrayDI);
                    currentPvH = sd.contents;
                    // and actually add
                    rc.params.add(sd);
                    useTag();
                } else if (c.equals("v")) {
                    // v specificVal name type
                    final int idx = Integer.parseInt(args[0]);
                    FF0 name = dTrExUnderscoreNull(currentPvHLocPrefix + idx, args[1]);
                    currentPvH.put(idx, new RPGCommand.PStatic(name, aliasingAwareSG(args[2])));
                } else if (c.equals("d")) {
                    String desc = "";
                    for (String s : args)
                        desc += " " + s;
                    rc.description = app.dTr(srcLoc, TrNames.cmdbDesc(dbId, rc.commandId), desc.trim());
                } else if (c.equals("i")) {
                    rc.indentPre = Integer.parseInt(args[0]);
                } else if (c.equals("I")) {
                    final int s = Integer.parseInt(args[0]);
                    rc.indentPost = (irio) -> s;
                } else if (c.equals("K")) {
                    rc.needsBlockLeavePre = true;
                    rc.blockLeaveReplacement = Integer.parseInt(args[0]);
                } else if (c.equals("l")) {
                    rc.needsBlockLeavePre = true;
                } else if (c.equals("L")) {
                    if (args.length > 0) {
                        if (args[0].equals("block")) {
                            // block context only
                            blockLeaveCmd = workingCmdId;
                            rc.typeBlockLeave = true;
                        } else if (args[0].equals("list")) {
                            listLeaveCmd = DMKey.of(workingCmdId);
                            rc.typeListLeave = true;
                        } else if (args[0].equals("strict")) {
                            rc.typeStrictLeave = true;
                        }
                        // "none" is neither
                    } else {
                        // default context: all
                        listLeaveCmd = DMKey.of(workingCmdId);
                        blockLeaveCmd = workingCmdId;
                        rc.typeBlockLeave = true;
                        rc.typeListLeave = true;
                    }
                } else if (c.equals(">")) {
                    localAliasing.put(args[0], sdb.getSDBEntry(args[1]));
                } else if (c.equals("X") || c.equals("x")) {
                    rc.specialSchema = sdb.getSDBEntry(args[0]);
                    rc.specialSchemaEssential = c.equals("X");
                } else if (c.equals("C")) {
                    if (args[0].equals("category")) {
                        rc.category = Integer.parseInt(args[1]);
                    } else if (args[0].equals("categories")) {
                        categories = new FF0[args.length - 1];
                        // No longer using EscapedStringSyntax, so sanity has been restored (yay!)
                        for (int i = 1; i < args.length; i++)
                            categories[i - 1] = app.dTr(srcLoc, TrNames.cmdbCat(dbId, i), args[i]);
                    } else if (args[0].equals("digitCount")) {
                        digitCount = Integer.parseInt(args[1]);
                    } else if (args[0].equals("commandIndentConditionalIB")) {
                        final int target = Integer.parseInt(args[1]);
                        rc.indentPost = (irio) -> {
                            if (irio.getALen() <= target)
                                return 0;
                            if (irio.getAElem(target).getFX() == 0)
                                return 0;
                            return 1;
                        };
                    } else if (args[0].equals("commandIndentConditionalOF")) {
                        final DMKey[] iargs = new DMKey[(args.length - 1) / 2];
                        final int[] ikeys = new int[iargs.length];
                        for (int i = 0; i < iargs.length; i++) {
                            ikeys[i] = Integer.parseInt(args[(i * 2) + 1]);
                            iargs[i] = ValueSyntax.decode(args[(i * 2) + 2]);
                        }
                        rc.indentPost = (irio) -> {
                            for (int i = 0; i < iargs.length; i++) {
                                if (irio.getALen() <= ikeys[i])
                                    continue;
                                if (IRIO.rubyEquals(irio.getAElem(ikeys[i]), iargs[i]))
                                    return 1;
                            }
                            return 0;
                        };
                    } else if (args[0].equals("spritesheet")) {
                        // C spritesheet 0 CharSet/
                        nextTag.add(new SpritesheetSpecialTag(Integer.parseInt(args[1]), args[2]));
                    } else if (args[0].equals("r2kTonePicker")) {
                        // C r2kTonePicker 0 1 2 3
                        nextTag.add(new TonepickerSpecialTag(100, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])));
                    } else if (args[0].equals("r2kFETonePicker")) {
                        // C r2kFETonePicker 0 1 2 3
                        nextTag.add(new TonepickerSpecialTag(31, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])));
                    } else if (args[0].equals("xpMoveCommandSetGraphic")) {
                        // C xpMoveCommandSetGraphic
                        nextTag.add(new XPMoveCommandSetGraphicSpecialTag());
                    } else if (args[0].equals("groupBehavior")) {
                        gbStatePosition = 1;
                        gbStateArgs = args;
                        rc.groupBehaviors.add(getGroupBehavior());
                        if (gbStatePosition != args.length)
                            throw new RuntimeException("Group behavior must consume all args");
                    } else if (args[0].equals("template")) {
                        rc.template = new int[args.length - 1];
                        for (int i = 1; i < args.length; i++)
                            rc.template[i - 1] = Integer.parseInt(args[i]);
                    } else if (args[0].equals("tag")) {
                        for (int i = 1; i < args.length; i++) {
                            CommandTag tag = app.commandTags.get(args[i]);
                            if (tag == null)
                                throw new RuntimeException("unknown command tag: " + args[i]);
                            rc.tags.add(tag);
                        }
                    } else if (args[0].equals("commandSiteAllowed")) {
                        rc.commandSiteAllowed = Boolean.parseBoolean(args[1]);
                    } else if (args[0].equals("textArg")) {
                        rc.textArg = Integer.parseInt(args[1]);
                    } else {
                        throw new RuntimeException("Unknown C-command: " + args[0]);
                    }
                } else if (c.equals("#")) {
                    String oldFile = baseFile;
                    baseFile = args[0];
                    DBLoader.readFile(app, args[0], this);
                    baseFile = oldFile;
                } else {
                    throw new RuntimeException("Unknown command '" + c + "'.");
                }
            }

            private void useTag() {
                rc.paramSpecialTags.add(nextTag);
                nextTag = new LinkedList<RPGCommand.SpecialTag>();
            }

            private SchemaElement aliasingAwareSG(String s) {
                SchemaElement se = localAliasing.get(s);
                if (se == null)
                    se = sdb.getSDBEntry(s);
                return se;
            }
        });
    }

    public void check() {
        // see if I need to be informed that the schema doesn't support the latest and greatest features
        int fails1 = 0;
        for (RPGCommand rc : knownCommands.values()) {
            rc.finish();
            if (rc.description == null) {
                System.err.print(rc.name.toString() + " ");
                fails1++;
            }
        }
        if (fails1 > 0)
            System.err.println(fails1 + " commands do not have descriptions.");
    }

    public String buildCodename(IRIO target, boolean indent, boolean full) {
        String ext = "";
        int cid = (int) target.getIVar("@code").getFX();
        if (knownCommands.containsKey(cid)) {
            RPGCommand cmd = knownCommands.get(cid);
            ext = cmd.formatName(target.getIVar("@parameters"));
        }
        String spc = full ? lenForm(cid) + " " : "";
        IRIO indentValue = target.getIVar("@indent");
        if ((indentValue != null) && indent) {
            int len = (int) target.getIVar("@indent").getFX();
            if (len < 0)
                spc += "(INDTERR" + len + ") ";
        }
        return spc + ext;
    }

    public String buildGroupCodename(IRIO rubyIO, int start, boolean full) {
        String tx = buildCodename(rubyIO.getAElem(start), true, full);
        int groupLen = getGroupLengthCore(rubyIO, start);
        for (int i = 1; i < groupLen; i++)
            tx += "\n" + buildCodename(rubyIO.getAElem(start + i), true, full);
        return tx;
    }

    private String lenForm(int cid) {
        String spc = Integer.toString(cid);
        while (spc.length() < digitCount)
            spc = "0" + spc;
        return spc;
    }

    /**
     * Can return 0, implying ungrouped.
     */
    public int getGroupLengthCore(IRIO arr, int j) {
        IRIO commandTarg = arr.getAElem(j);
        RPGCommand rc = entryOf(commandTarg);
        int max = 0;
        if (rc != null)
            for (IGroupBehavior groupBehavior : rc.groupBehaviors)
                max = Math.max(max, groupBehavior.getGroupLength(arr, j));
        return max;
    }

    /**
     * Can return null if the entry isn't there.
     */
    public RPGCommand entryOf(IRIO command) {
        int code = (int) command.getIVar("@code").getFX();
        return knownCommands.get(code);
    }

    /**
     * Builds enum data for all the commands, yay!
     */
    public UIEnumChoice.Category[] buildEnum() {
        UIEnumChoice.Category[] ecats = new UIEnumChoice.Category[categories.length];
        for (int i = 0; i < ecats.length; i++) {
            LinkedList<UIEnumChoice.Option> llo = new LinkedList<UIEnumChoice.Option>();
            for (Integer key : knownCommandOrder) {
                RPGCommand rc = knownCommands.get(key);
                String text = key + ";" + rc.formatName(null);
                if (rc.category == i)
                    llo.add(new UIEnumChoice.Option(text, DMKey.of((long) (int) key)));
            }
            ecats[i] = new UIEnumChoice.Category(categories[i].r(), llo);
        }
        return ecats;
    }
}
