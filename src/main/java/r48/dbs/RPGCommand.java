/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import gabien.ui.IFunction;
import gabien.ui.UIElement;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.TonePickerSchemaElement;
import r48.schema.specialized.cmgb.IGroupBehavior;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * RPGCommand database entry.
 * Created on 12/30/16.
 */
public class RPGCommand {
    public String name;

    public SchemaElement specialSchema;

    public LinkedList<IFunction<RubyIO, SchemaElement>> paramType = new LinkedList<IFunction<RubyIO, SchemaElement>>();
    // As the entire paramType cannot be replaced (paramType is relied upon for parameter names), this instead allows supplementing it.
    public LinkedList<SpecialTag> paramSpecialTags = new LinkedList<SpecialTag>();
    public LinkedList<IFunction<RubyIO, String>> paramName = new LinkedList<IFunction<RubyIO, String>>();
    public int indentPre;
    // This is conditional solely because of Show Inn (R2k).
    public IFunction<RubyIO, Integer> indentPost = new IFunction<RubyIO, Integer>() {
        @Override
        public Integer apply(RubyIO rubyIO) {
            return 0;
        }
    };
    // Something that can also go before this command instead of a block leave
    public int blockLeaveReplacement = -1;
    public boolean needsBlockLeavePre;
    public boolean typeBlockLeave;

    public String description;

    // Extrenely special behavior for certain commands.
    // Only usable with the proper "shielding".
    // (Probably refers to making sure that this will NEVER crash)
    public IGroupBehavior groupBehavior;
    public int category;

    // Pass null for parameters if this is for combobox display.
    public String formatName(RubyIO root, RubyIO[] parameters) {
        try {
            if (name.startsWith("@@"))
                return FormatSyntax.formatNameExtended(name.substring(2), root, parameters, paramType.toArray(new IFunction[0]));
            boolean prefixes = true;
            if (name.startsWith("@P")) {
                prefixes = false;
                name = name.substring(2);
            }
            String sn = "";
            int pi = 0;
            for (char c : name.toCharArray()) {
                if (c == '!') {
                    if (parameters != null) {
                        sn += " to " + interpretLocalParameter(root, pi, parameters[pi], prefixes);
                        pi++;
                    }
                    continue;
                }
                if (c == '$') {
                    if (parameters != null) {
                        sn += " " + interpretLocalParameter(root, pi, parameters[pi], prefixes);
                        pi++;
                    }
                    continue;
                }
                if (c == '#') {
                    if (parameters != null) {
                        String beginning = interpretLocalParameter(root, pi, parameters[pi], true);
                        pi++;
                        String end = interpretLocalParameter(root, pi, parameters[pi], true);
                        pi++;
                        if (beginning.equals(end)) {
                            sn += " " + beginning;
                        } else {
                            sn += "s " + beginning + " through " + end;
                        }
                        continue;
                    }
                    // Notably, the '#' is kept if parameters are missing.
                }
                sn += c;
            }
            return sn;
        } catch (IndexOutOfBoundsException e) {
            System.err.println("While processing name " + name + ", an IndexOutOfBounds exception occurred. This suggests badly checked parameters.");
            e.printStackTrace();
            if (parameters != null)
                return formatName(root, null);
            throw e;
        }
    }

    private String interpretLocalParameter(RubyIO root, int pi, RubyIO parameter, boolean prefixEnums) {
        return FormatSyntax.interpretParameter(parameter, getParameterSchema(root, pi), prefixEnums);
    }

    public SchemaElement getParameterSchema(RubyIO root, int i) {
        if (paramType.size() <= i)
            return AppMain.schemas.getSDBEntry("genericScriptParameter");
        return paramType.get(i).apply(root);
    }

    public String getParameterName(RubyIO root, int i) {
        if (paramName.size() <= i)
            return TXDB.get("UNK.");
        return paramName.get(i).apply(root);
    }

    public static class SpecialTag {
        public boolean hasTonepicker;
        public int tpA, tpB, tpC, tpD;
        public boolean hasSpritesheet;
        public String spritesheetId;
        public int spritesheetTargstr;
        public int tpBase;

        public void applyTo(int idx, LinkedList<UIElement> elementList, RubyIO targetParamArray, ISchemaHost launcher, SchemaPath path) {
            if (hasSpritesheet) {
                SchemaElement scse = AppMain.schemas.helpers.makeSpriteSelector("]" + idx, "]" + spritesheetTargstr, spritesheetId);
                elementList.add(scse.buildHoldingEditor(targetParamArray, launcher, path));
            }
            if (hasTonepicker) {
                SchemaElement scse = new TonePickerSchemaElement("]" + tpA, "]" + tpB, "]" + tpC, "]" + tpD, tpBase);
                elementList.add(scse.buildHoldingEditor(targetParamArray, launcher, path));
            }
        }
    }
}
