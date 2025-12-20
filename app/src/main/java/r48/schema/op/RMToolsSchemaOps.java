/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.schema.op;

import r48.App;
import r48.dbs.RPGCommand;
import r48.io.data.IRIO;
import r48.schema.specialized.textboxes.R2kTextRules;
import r48.schema.specialized.textboxes.TextRules;
import r48.search.CommandTag;

import static r48.schema.op.BaseSchemaOps.*;

import java.util.LinkedList;

import gabien.GaBIEn;

/**
 * Created 19th December, 2025.
 */
public class RMToolsSchemaOps {
    public static final long RTM_MODE_LEFT = 0;
    public static final long RTM_MODE_CENTRE = 1;
    public static final long RTM_MODE_RIGHT = 2;

    /**
     * This contains the RMTools operators.
     */
    public static void defJavasideOperators(App app) {
        int catIdx = 0;
        int sortRTM = sortIdx(SORT_RMTOOLS, catIdx++);
        CommandTag rmTextManipTag = app.commandTags.get("sayCmd");
        if (rmTextManipTag != null) {
            final TextRules textRules = new R2kTextRules();
            new SchemaOp(app, BASE_SYSCORE, "rmtextmanip", sortRTM, app.opSites.SCHEMA_HEADER, app.opSites.ARRAY_SEL) {
                @Override
                public String shouldDisplay(SchemaOp.ExpandedCtx context) {
                    if (context.commandList == null)
                        return null;
                    return app.t.s.op_rmTextManip;
                }
                static final boolean DEBUG = false;
                @Override
                public String invoke(SchemaOp.ExpandedCtx parameters) {
                    if (DEBUG)
                        System.out.println("textmanip invoked");
                    if (parameters.commandList == null) {
                        if (DEBUG)
                            System.out.println("textmanip error no CL");
                        return null;
                    }
                    long mode = getParamLong(parameters, "@mode", 0);
                    boolean ignoreFirst = getParamLong(parameters, "@ignore_first", 0) != 0;
                    int fieldWidth = (int) getParamLong(parameters, "@wrap_to_limit", 0);
                    boolean wrap = fieldWidth > 0;
                    // This has to be written in a really specific way, because we insert new commands.
                    int idx = parameters.commandList.startIndex;
                    int endIndex = parameters.commandList.endIndex;
                    IRIO cmdArray = parameters.path.targetElement;
                    boolean didModify = false;
                    if (DEBUG)
                        System.out.println("textmanip " + idx + " " + endIndex);
                    while (idx < endIndex) {
                        IRIO cmd = cmdArray.getAElem(idx);
                        RPGCommand rc = parameters.commandList.cmdb.entryOf(cmd);
                        if (rc == null) {
                            if (DEBUG)
                                System.out.println("textmanip root CMD unknown!");
                            // next command
                            idx++;
                            continue;
                        }
                        if (!rc.tags.contains(rmTextManipTag)) {
                            if (DEBUG)
                                System.out.println("textmanip root CMD not textmanip (" + rc + ")");
                            // not interested in this command
                            idx++;
                            continue;
                        }
                        // ugh
                        int additionCode = rc.additionCode == -1 ? rc.commandId : rc.additionCode;
                        // get the original group length
                        int groupLen = parameters.commandList.cmdb.getGroupLengthCore(cmdArray, idx);
                        if (groupLen < 1)
                            groupLen = 1;
                        if (DEBUG)
                            System.out.println("textmanip root CMD is textmanip, GL " + groupLen);
                        // These two have to be kept in sync.
                        // Ugly, I know.
                        LinkedList<IRIO> commandTextIRIOs = new LinkedList<>();
                        LinkedList<IRIO> commandCmdIRIOs = new LinkedList<>();
                        boolean ignoreFirstFlag = ignoreFirst;
                        for (int subIdx = 0; subIdx < groupLen; subIdx++) {
                            IRIO subCmd = cmdArray.getAElem(idx + subIdx);
                            RPGCommand subRC = parameters.commandList.cmdb.entryOf(subCmd);
                            if (subRC == null)
                                continue;
                            if (subRC.textArg == -1)
                                continue;
                            if (!ignoreFirstFlag) {
                                commandTextIRIOs.add(subCmd.getIVar("@parameters").getAElem(subRC.textArg));
                                commandCmdIRIOs.add(subCmd);
                            }
                            ignoreFirstFlag = false;
                        }
                        IRIO[] commandTextIRIOsArray = commandTextIRIOs.toArray(new IRIO[0]);
                        idx += groupLen;
                        // This catches if we were told to ignore the first line but there's no other lines.
                        // Otherwise, we're liable to accidentally generate a blank group.
                        if (commandTextIRIOsArray.length == 0)
                            continue;
                        // alright, we're now ready to begin the rewriting process
                        String[] text = new String[commandTextIRIOsArray.length];
                        for (int textIdx = 0; textIdx < text.length; textIdx++)
                            text[textIdx] = commandTextIRIOsArray[textIdx].decString();
                        // do the rewrite
                        text = align(wrap, mode, text, textRules, fieldWidth);
                        // adjustments begin here
                        didModify = true;
                        // adjust
                        while (text.length < commandTextIRIOs.size()) {
                            cmdArray.rmAElemByIRIO(commandCmdIRIOs.removeLast());
                            commandTextIRIOs.removeLast();
                            idx--;
                            endIndex--;
                        }
                        while (text.length > commandTextIRIOs.size()) {
                            IRIO newCmd = cmdArray.addAElem(idx);
                            parameters.commandList.eventCommandArraySchema.initCommand(additionCode, newCmd, idx);
                            commandTextIRIOs.add(newCmd.getIVar("@parameters").getAElem(parameters.commandList.cmdb.knownCommands.get((int) additionCode).textArg));
                            commandCmdIRIOs.add(newCmd);
                            idx++;
                            endIndex++;
                        }
                        // alright, commandTextIRIOs should now be the same length as text
                        for (int textIdx = 0; textIdx < text.length; textIdx++)
                            commandTextIRIOs.get(textIdx).setString(text[textIdx]);
                    }
                    if (didModify)
                        parameters.path.changeOccurred(false);
                    return null;
                }
            };
        }
        new SchemaOp(app, BASE_SYSCORE, "rmcopytext", sortRTM, app.opSites.SCHEMA_HEADER, app.opSites.ARRAY_SEL) {
            @Override
            public String shouldDisplay(SchemaOp.ExpandedCtx context) {
                if (context.commandList != null) {
                    IRIO target = context.path.targetElement;
                    for (int i = context.commandList.startIndex; i < context.commandList.endIndex; i++) {
                        IRIO commandTarg = target.getAElem(i);
                        int code = (int) commandTarg.getIVar("@code").getFX();
                        RPGCommand rc = context.commandList.cmdb.knownCommands.get(code);
                        if (rc != null)
                            if (rc.textArg != -1)
                                return app.t.s.bCopyTextToClipboard;
                    }
                }
                return null;
            }
            @Override
            public String invoke(SchemaOp.ExpandedCtx parameters) {
                StringBuilder total = new StringBuilder();
                if (parameters.commandList != null) {
                    IRIO target = parameters.path.targetElement;
                    for (int i = parameters.commandList.startIndex; i < parameters.commandList.endIndex; i++) {
                        IRIO commandTarg = target.getAElem(i);
                        int code = (int) commandTarg.getIVar("@code").getFX();
                        RPGCommand rc = parameters.commandList.cmdb.knownCommands.get(code);
                        if (rc != null)
                            if (rc.textArg != -1) {
                                // VERY UGLY AND BAD: We use commandSiteAllowed as a shorthand for 'paragraph break'.
                                // This is terrible, but it really does make the output nicer.
                                // It's this or some other purpose-specific thing (paragraphBreakBefore/paragraphBreakAfter comes to mind).
                                if (total.length() != 0 && rc.commandSiteAllowed)
                                    total.append('\n');
                                total.append(commandTarg.getIVar("@parameters").getAElem(rc.textArg).decString());
                                total.append('\n');
                            }
                    }
                }
                GaBIEn.clipboard.copyText(total.toString());
                return null;
            }
        };
    }

    /**
     * Wrapping and alignment computer.
     * May return the input string array or a new one.
     */
    public static String[] align(boolean wrap, long mode, String[] lines, TextRules textRules, int fieldWidth) {
        if (wrap) {
            // Step 1: merge
            StringBuilder initialMerge = new StringBuilder();
            boolean isFirst = true;
            for (String s : lines) {
                if (!isFirst)
                    initialMerge.append(' ');
                initialMerge.append(s.trim());
                isFirst = false;
            }
            String remainder = initialMerge.toString();
            // Step 2: split
            LinkedList<String> res = new LinkedList<>();
            while (textRules.countCells(remainder) > fieldWidth) {
                String candidate = null;
                int candidateLength = 0;
                for (int pass = 0; pass < 2; pass++) {
                    for (int cutPoint = 0; cutPoint < remainder.length(); cutPoint++) {
                        // if on the first pass, try word breaking
                        if (pass == 0)
                            if (remainder.charAt(cutPoint) != ' ')
                                continue;
                        // possible cut point
                        String pc = remainder.substring(0, cutPoint);
                        if (textRules.countCells(pc) <= fieldWidth) {
                            // acceptable
                            candidate = pc;
                            candidateLength = cutPoint;
                        } else {
                            // no further possible candidates possible
                            break;
                        }
                    }
                    // don't need to do char split if we have a candidate
                    if (candidateLength != 0)
                        break;
                }
                // whoops!
                if (candidateLength == 0)
                    break;
                res.add(candidate);
                remainder = remainder.substring(candidateLength);
            }
            if (res.size() == 0 || remainder.length() != 0)
                res.add(remainder);
            lines = res.toArray(new String[] {});
        }
        for (int line = 0; line < lines.length; line++) {
            String s = lines[line];
            s = s.trim();
            if (mode == RTM_MODE_LEFT) {
                // do nothing
            } else if (mode == RTM_MODE_CENTRE) {
                int startWidth = textRules.countCells(s);
                int expectWidth = ((fieldWidth - startWidth) / 2) + startWidth;
                for (int i = 0; i < fieldWidth; i++) {
                    if (textRules.countCells(s) >= expectWidth)
                        break;
                    s = " " + s;
                }
            } else if (mode == RTM_MODE_RIGHT) {
                s = s.trim();
                for (int i = 0; i < fieldWidth; i++) {
                    if (textRules.countCells(s) >= fieldWidth)
                        break;
                    s = " " + s;
                }
            }
            lines[line] = s;
        }
        return lines;
    }
}
