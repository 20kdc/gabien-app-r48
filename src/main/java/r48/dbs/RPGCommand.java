/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import gabien.ui.IFunction;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;

import java.util.LinkedList;

/**
 * RPGCommand database entry.
 * Created on 12/30/16.
 */
public class RPGCommand {
    public String name;

    public SchemaElement specialSchema;

    public LinkedList<IFunction<RubyIO, SchemaElement>> paramType = new LinkedList<IFunction<RubyIO, SchemaElement>>();
    public LinkedList<IFunction<RubyIO, String>> paramName = new LinkedList<IFunction<RubyIO, String>>();
    public int indentPre;
    public int indentPost;
    // Something that can also go before this command instead of a block leave
    public int blockLeaveReplacement = -1;
    public boolean needsBlockLeavePre;
    public boolean typeBlockLeave;

    public String description = null;

    // Pass null for parameters if this is for combobox display.
    public String formatName(RubyIO root, RubyIO[] parameters) {
        try {
            if (name.startsWith("@@"))
                return formatNameExtended(name.substring(2), root, parameters, paramType.toArray(new IFunction[0]));
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
        return interpretParameter(parameter, getParameterSchema(root, pi), prefixEnums);
    }

    // The new format allows for more precise setups,
    // but isn't as neat
    public static String formatNameExtended(String name, RubyIO root, RubyIO[] parameters, IFunction<RubyIO, SchemaElement>[] parameterSchemas) {
        String r = "";
        char[] data = name.toCharArray();
        int disables = 0;
        boolean prefixNext = false;
        for (int i = 0; i < data.length; i++) {
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
                    wantedVal = interpretParameter(parameters[pidB], getParameterSchemaFromArray(root, parameterSchemas, pidB), true);
                    prefixComparisonVar = true;
                }
                if (parameters.length <= pidA) {
                    disables++;
                } else {
                    if (!interpretParameter(parameters[pidA], getParameterSchemaFromArray(root, parameterSchemas, pidA), prefixComparisonVar).equals(wantedVal))
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
                if (disables == 0)
                    prefixNext = true;
            } else if (data[i] == '[') {
                if (disables > 0)
                    continue;
                // commence reinterpretation.
                String type = "";
                while (data[++i] != ']')
                    type += data[i];
                int ss = type.indexOf('@');
                if (parameters != null) {
                    if (ss != 0) {
                        RubyIO p = parameters[data[++i] - 'A'];
                        IFunction<RubyIO, String> handler = AppMain.schemas.nameDB.get("Interp." + type);
                        if (handler != null) {
                            r += handler.apply(p);
                        } else {
                            SchemaElement ise = AppMain.schemas.getSDBEntry(type);
                            r += interpretParameter(p, ise, prefixNext);
                        }
                    } else {
                        // Meta-interpretation syntax
                        String tp = type.substring(1);
                        IFunction<RubyIO, String> n = AppMain.schemas.nameDB.get(tp);
                        if (n == null)
                            throw new RuntimeException("Expected NDB " + tp);
                        r += n.apply(root);
                    }
                }
                prefixNext = false;
            } else if (data[i] == '#') {
                if (parameters != null)
                    if (disables == 0) {
                        int pid = data[++i] - 'A';
                        if ((pid >= 0) && (pid < parameters.length)) {
                            r += interpretParameter(parameters[pid], getParameterSchemaFromArray(root, parameterSchemas, pid), prefixNext);
                        } else {
                            r += data[i];
                        }
                    }
                prefixNext = false;
            } else {
                if (disables == 0)
                    r += data[i];
            }
        }
        return r;
    }

    public static SchemaElement getParameterSchemaFromArray(RubyIO root, IFunction<RubyIO, SchemaElement>[] ise, int i) {
        if (ise == null)
            return AppMain.schemas.getSDBEntry("genericScriptParameter");
        if (ise.length <= i)
            return AppMain.schemas.getSDBEntry("genericScriptParameter");
        return ise[i].apply(root);
    }

    public SchemaElement getParameterSchema(RubyIO root, int i) {
        if (paramType.size() <= i)
            return AppMain.schemas.getSDBEntry("genericScriptParameter");
        return paramType.get(i).apply(root);
    }

    public String getParameterName(RubyIO root, int i) {
        if (paramName.size() <= i)
            return "UNK.";
        return paramName.get(i).apply(root);
    }

    public static String interpretParameter(RubyIO rubyIO, SchemaElement ise, boolean prefixEnums) {
        // Basically, Class. overrides go first, then everything else comes after.
        if (rubyIO.type == 'o') {
            IFunction<RubyIO, String> handler = AppMain.schemas.nameDB.get("Class." + rubyIO.symVal);
            if (handler != null)
                return handler.apply(rubyIO);
        }
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
