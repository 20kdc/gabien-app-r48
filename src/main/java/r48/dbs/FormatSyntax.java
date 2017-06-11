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

/**
 * Yet another class solely to hold a common syntax in an obvious place.
 * Created on 11/06/17.
 */
public class FormatSyntax {
    // The new format allows for more precise setups,
    // but isn't as neat.
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
                    wantedVal = interpretParameter(parameters[pidB], getParameterDisplaySchemaFromArray(root, parameterSchemas, pidB), true);
                }
                if (parameters.length <= pidA) {
                    disables++;
                } else {
                    if (!parameters[pidA].toString().equals(wantedVal))
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
                            r += interpretParameter(parameters[pid], getParameterDisplaySchemaFromArray(root, parameterSchemas, pid), prefixNext);
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

    public static String interpretParameter(RubyIO rubyIO, SchemaElement ise, boolean prefixEnums) {
        // Basically, Class. overrides go first, then everything else comes after.
        if (rubyIO.type == 'o') {
            IFunction<RubyIO, String> handler = AppMain.schemas.nameDB.get("Class." + rubyIO.symVal);
            if (handler != null)
                return handler.apply(rubyIO);
        }
        String r = null;
        if (ise != null) {
            while (ise instanceof ProxySchemaElement)
                ise = ((ProxySchemaElement) ise).getEntry();
            if (ise instanceof EnumSchemaElement)
                r = ((EnumSchemaElement) ise).viewValue((int) rubyIO.fixnumVal, prefixEnums);
        }
        if (r == null)
            r = rubyIO.toString();
        return r;
    }

    // NOTE: This can return null.
    public static SchemaElement getParameterDisplaySchemaFromArray(RubyIO root, IFunction<RubyIO, SchemaElement>[] ise, int i) {
        if (ise == null)
            return null;
        if (ise.length <= i)
            return null;
        return ise[i].apply(root);
    }

    public static String formatExtended(String s, RubyIO[] pieces) {
        RubyIO synthRoot = new RubyIO();
        synthRoot.type = '[';
        synthRoot.arrVal = pieces;
        return formatNameExtended(s, synthRoot, pieces, null);
    }
}
