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
import r48.search.CommandTag;

import static r48.schema.op.BaseSchemaOps.*;

import gabien.GaBIEn;

/**
 * Created 19th December, 2025.
 */
public class RMToolsSchemaOps {
    /**
     * This contains the RMTools operators.
     */
    public static void defJavasideOperators(App app) {
        int catIdx = 0;
        int sortRTM = sortIdx(SORT_RMTOOLS, catIdx++);
        CommandTag rmTextManipTag = app.commandTags.get("sayCmd");
        if (rmTextManipTag != null) {
            new SchemaOp(app, BASE_SYSCORE, "rmtextmanip", sortRTM, app.opSites.SCHEMA_HEADER, app.opSites.ARRAY_SEL) {
                @Override
                public String shouldDisplay(SchemaOp.ExpandedCtx context) {
                    return app.t.s.op_rmTextManip;
                }
                @Override
                public String invoke(SchemaOp.ExpandedCtx parameters) {
                    return "TEST OPERATOR PLEASE IGNORE";
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

}
