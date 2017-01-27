/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import r48.AppMain;
import r48.schema.*;

import java.util.LinkedList;

/**
 * RPGCommand database entry.
 * Created on 12/30/16.
 */
public class RPGCommand {
    public String name;

    public String specialSchemaName;

    public LinkedList<ISchemaElement> paramType = new LinkedList<ISchemaElement>();
    public LinkedList<String> paramName = new LinkedList<String>();
    public int indentPre;
    public int indentPost;
    // Something that can also go before this command instead of a block leave
    public int blockLeaveReplacement = -1;
    public boolean needsBlockLeavePre;
    public boolean typeBlockLeave;

    // Pass null for parameters if this is for combobox display.
    public String formatName(String[] parameters) {
        try {
            String sn = "";
            int pi = 0;
            for (char c : name.toCharArray()) {
                if (c == '!') {
                    if (parameters != null)
                        sn += " to " + parameters[pi++];
                    continue;
                }
                if (c == '$') {
                    if (parameters != null)
                        sn += " " + parameters[pi++];
                    continue;
                }
                if (c == '#') {
                    if (parameters != null) {
                        String beginning = parameters[pi++];
                        String end = parameters[pi++];
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
            if (parameters != null)
                return formatName(null);
            throw e;
        }
    }

    public ISchemaElement getParameterSchema(int i) {
        if (paramType.size() <= i)
            return AppMain.schemas.getSDBEntry("genericScriptParameter");
        return paramType.get(i);
    }

    public String getParameterName(int i) {
        if (paramName.size() <= i)
            return "UNK.";
        return paramName.get(i);
    }
}
