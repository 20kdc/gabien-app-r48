/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import r48.AppMain;
import r48.RubyIO;
import r48.schema.EnumSchemaElement;
import r48.schema.ISchemaElement;

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
    public String formatName(RubyIO[] parameters) {
        try {
            if (name.startsWith("@@"))
                return formatNameExtended(parameters);
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
                        sn += " to " + interpretLocalParameter(pi, parameters[pi], prefixes);
                        pi++;
                    }
                    continue;
                }
                if (c == '$') {
                    if (parameters != null) {
                        sn += " " + interpretLocalParameter(pi, parameters[pi], prefixes);
                        pi++;
                    }
                    continue;
                }
                if (c == '#') {
                    if (parameters != null) {
                        String beginning = interpretLocalParameter(pi, parameters[pi], true);
                        pi++;
                        String end = interpretLocalParameter(pi, parameters[pi], true);
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
            if (parameters != null)
                return formatName(null);
            throw e;
        }
    }

    private String interpretLocalParameter(int pi, RubyIO parameter, boolean prefixEnums) {
        return interpretParameter(parameter, getParameterSchema(pi), prefixEnums);
    }

    // The new format allows for more precise setups,
    // but isn't as neat
    private String formatNameExtended(RubyIO[] parameters) {
        String r = "";
        char[] data = name.toCharArray();
        int disables = 0;
        boolean prefixNext = false;
        for (int i = 2; i < data.length; i++) {
            if (data[i] == '{') {
                if ((parameters == null) || (disables > 0)) {
                    disables++;
                    continue;
                }
                // Parse condition
                i++;
                // Firstly a pid
                int pidA = data[i] - 'A';
                i++;
                if (data[i] == ':') {
                    // The parameter exists, that's enough
                    if (parameters.length <= pidA)
                        disables++;
                    continue;
                }
                // The parameter must be equal to something else
                char pb = data[i++];
                String wantedVal = "";
                // If comparing parameter to parameter, both parameters can be force-prefixed.
                boolean prefixComparisonVar = false;
                if (pb == '=') {
                    // It's a string
                    pb = data[i++];
                    while (pb != '=') {
                        wantedVal += pb;
                        pb = data[i++];
                    }
                    i--; // it'll i++ later
                } else {
                    if (data[i] != ':')
                        return "BADLY FORMATTED PS!";
                    int pidB = pb - 'A';
                    if (parameters.length <= pidB) {
                        disables++;
                        continue;
                    }
                    wantedVal = interpretLocalParameter(pidB, parameters[pidB], true);
                    prefixComparisonVar = true;
                }
                if (parameters.length <= pidA) {
                    disables++;
                } else {
                    if (!interpretLocalParameter(pidA, parameters[pidA], prefixComparisonVar).equals(wantedVal))
                        disables++;
                }
            } else if (data[i] == '|') {
                if (disables == 1) {
                    disables = 0;
                } else if (disables == 0) {
                    disables = 1;
                }
            } else if (data[i] == '}') {
                if (disables > 0)
                    disables--;
            } else if (data[i] == '@') {
                if (disables > 0)
                    prefixNext = true;
            } else if (data[i] == '[') {
                if (disables > 0)
                    continue;
                // commence reinterpretation.
                String type = "";
                while (data[++i] != ']')
                    type += data[i];
                ISchemaElement ise = AppMain.schemas.getSDBEntry(type);
                i++;
                if (parameters != null)
                    r += interpretParameter(parameters[data[i] - 'A'], ise, prefixNext);
                prefixNext = false;
            } else if (data[i] == '#') {
                if (parameters != null)
                    if (disables == 0) {
                        int pid = data[++i] - 'A';
                        r += interpretLocalParameter(pid, parameters[pid], prefixNext);
                        prefixNext = false;
                    }
            } else {
                if (disables == 0)
                    r += data[i];
            }
        }
        return r;
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

    public static String interpretParameter(RubyIO rubyIO, ISchemaElement ise, boolean prefixEnums) {
        while (ise instanceof ProxySchemaElement)
            ise = ((ProxySchemaElement) ise).getEntry();
        String r = null;
        if (ise instanceof EnumSchemaElement)
            r = ((EnumSchemaElement) ise).viewValue((int) rubyIO.fixnumVal, prefixEnums);
        if (r == null)
            r = rubyIO.toString();
        return r;
    }
}
