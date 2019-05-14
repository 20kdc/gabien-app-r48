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
import r48.io.data.IRIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;

import java.util.LinkedList;

/**
 * Yet another class solely to hold a common syntax in an obvious place.
 * Created on 11/06/17.
 */
public class FormatSyntax {
    // The new format allows for more precise setups,
    // but isn't as neat.
    public static String formatNameExtended(String name, IRIO root, IRIO[] parameters, IFunction<IRIO, SchemaElement>[] parameterSchemas) {
        StringBuilder r = new StringBuilder();
        char[] data = name.toCharArray();
        boolean prefixNext = false;
        // C: A fully parsable formatNameExtended string.
        // A: A component array of the form C or C|A.
        // V: A single letter from 'A' through 'Z', representing a parameter.
        // T: An unparsed string limited by context (no escapes, nor [{}]# etc.)
        // R: An instance of T : Name routine.
        // I: An instance of R where Interp. is prepended.
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '{') {
                // Parse condition
                // Precedence:
                // {@A} ('arbitrary enumeration form')
                // {V:A} ('variable exists form')
                // {V=T=A} ('vt equality form')
                // {VV:A} ('vv equality form ')
                i++;
                LinkedList<String> components = new LinkedList<String>();
                if (data[i] == '@') {
                    // arbitrary enumeration form.
                    i = explodeComponentsAndAdvance(components, data, i + 1, '}');
                    // If components is even, then that's because there's one start and one default to balance it out,
                    //  at the start and end respectively.
                    // Of course, since the first component is removed instantly,
                    //  the check is for odd.
                    String val = formatNameExtended(components.removeFirst(), root, parameters, parameterSchemas);
                    String def = "";
                    if ((components.size() & 1) != 0)
                        def = components.removeLast();
                    for (int j = 0; j < components.size(); j += 2) {
                        if (val.equals(formatNameExtended(components.get(j), root, parameters, parameterSchemas))) {
                            def = components.get(j + 1);
                            break;
                        }
                    }
                    def = formatNameExtended(def, root, parameters, parameterSchemas);
                    r.append(def);
                } else if (data[i + 1] == ':') {
                    char v = data[i];
                    // variable exists form.
                    i = explodeComponentsAndAdvance(components, data, i + 2, '}');
                    boolean result = parameters != null;
                    if (result)
                        result = (v - 'A') < parameters.length;
                    determineBooleanComponent(r, components, result, root, parameters, parameterSchemas);
                } else if (data[i + 1] == '=') {
                    char va = data[i];
                    // vt equality form.
                    StringBuilder eqTarget = new StringBuilder();
                    i = explodeComponent(eqTarget, data, i + 2, "=");
                    i = explodeComponentsAndAdvance(components, data, i + 1, '}');
                    boolean result = parameters != null;
                    if (result) {
                        SchemaElement as = getParameterDisplaySchemaFromArray(parameters[va - 'A'], parameterSchemas, va - 'A');
                        String a = interpretParameter(parameters[va - 'A'], as, false);
                        result = a.equals(eqTarget.toString());
                    }
                    determineBooleanComponent(r, components, result, root, parameters, parameterSchemas);
                } else if (data[i + 2] == ':') {
                    // vv equality form.
                    char va = data[i];
                    char vb = data[i + 1];
                    boolean result = parameters != null;
                    if (result) {
                        SchemaElement as = getParameterDisplaySchemaFromArray(parameters[va - 'A'], parameterSchemas, va - 'A');
                        SchemaElement bs = getParameterDisplaySchemaFromArray(parameters[vb - 'A'], parameterSchemas, vb - 'A');
                        String a = interpretParameter(parameters[va - 'A'], as, false);
                        String b = interpretParameter(parameters[vb - 'A'], bs, false);
                        result = a.equals(b);
                    }
                    i = explodeComponentsAndAdvance(components, data, i + 3, '}');
                    determineBooleanComponent(r, components, result, root, parameters, parameterSchemas);
                } else {
                    throw new RuntimeException("Unknown conditional type!");
                }
            } else if (data[i] == '@') {
                prefixNext = true;
            } else if (data[i] == '[') {
                // Parse precedence order:
                // [@R]
                // [I][C]
                // [I]V
                // commence reinterpretation.
                StringBuilder type = new StringBuilder();
                int indexOfAt = -1;
                while (data[++i] != ']') {
                    if (indexOfAt == -1)
                        if (data[i] == '@')
                            indexOfAt = type.length();
                    type.append(data[i]);
                }
                if (parameters != null) {
                    if (indexOfAt != 0) {
                        char ch = data[++i];
                        IRIO p;
                        if (ch == '[') {
                            // At this point, it's gone recursive.
                            // Need to safely skip over this lot...
                            StringBuilder tx = new StringBuilder();
                            int escapeCount = 1;
                            while (escapeCount > 0) {
                                char ch2 = data[++i];
                                if (ch2 == '#') {
                                    tx.append(ch2);
                                    tx.append(data[++i]);
                                    continue;
                                }
                                if (ch2 == '[')
                                    escapeCount++;
                                if (ch2 == ']') {
                                    escapeCount--;
                                    if (escapeCount == 0)
                                        break;
                                }
                                if (ch2 == '{')
                                    escapeCount++;
                                if (ch2 == '}') {
                                    escapeCount--;
                                    if (escapeCount == 0)
                                        return "Mismatched []{} error.";
                                }
                                tx.append(ch2);
                            }
                            // ... then parse it.
                            String out = formatNameExtended(tx.toString(), root, parameters, parameterSchemas);
                            p = new RubyIO().setString(out, true);
                        } else {
                            p = parameters[ch - 'A'];
                        }
                        r.append(interpretParameter(p, type.toString(), prefixNext));
                    } else {
                        // Meta-interpretation syntax
                        String tp = type.substring(1);
                        IFunction<IRIO, String> n = TXDB.nameDB.get(tp);
                        if (n == null)
                            throw new RuntimeException("Expected NDB " + tp);
                        r.append(n.apply(root));
                    }
                }
                prefixNext = false;
            } else if (data[i] == '#') {
                if (parameters != null) {
                    int pid = data[++i] - 'A';
                    if ((pid >= 0) && (pid < parameters.length)) {
                        r.append(interpretParameter(parameters[pid], getParameterDisplaySchemaFromArray(root, parameterSchemas, pid), prefixNext));
                    } else {
                        r.append(data[i]);
                    }
                }
                prefixNext = false;
            } else {
                r.append(data[i]);
            }
        }
        return r.toString();
    }

    private static void determineBooleanComponent(StringBuilder r, LinkedList<String> components, boolean result, IRIO root, IRIO[] parameters, IFunction<IRIO, SchemaElement>[] parameterSchemas) {
        for (int i = (result ? 0 : 1); i < components.size(); i += 2)
            r.append(formatNameExtended(components.get(i), root, parameters, parameterSchemas));
    }

    private static int explodeComponent(StringBuilder eqTarget, char[] data, int i, String s) {
        int level = 0;
        boolean escape = false;
        while (i < data.length) {
            char c = data[i++];
            if (level == 0)
                if (s.indexOf(c) != -1)
                    return i - 1;
            eqTarget.append(c);
            if (!escape) {
                if (c == '#') {
                    escape = true;
                } else if (c == '{') {
                    level++;
                } else if (c == '[') {
                    level++;
                } else if (c == ']') {
                    level--;
                    if (level < 0)
                        throw new RuntimeException("Parse error - too many levels out, hit ]");
                } else if (c == '}') {
                    level--;
                    if (level < 0)
                        throw new RuntimeException("Parse error - too many levels out, hit }");
                }
            } else {
                escape = false;
            }
        }
        throw new RuntimeException("Hit end-of-data without reaching end character.");
    }

    private static int explodeComponentsAndAdvance(LinkedList<String> components, char[] data, int i, char ender) {
        while (i < data.length) {
            StringBuilder c = new StringBuilder();
            i = explodeComponent(c, data, i, "|" + ender);
            components.add(c.toString());
            if (data[i] == ender)
                return i;
            i++;
        }
        throw new RuntimeException("Hit end-of-data without reaching end character.");
    }

    public static String interpretParameter(IRIO rubyIO, String st, boolean prefixEnums) {
        if (st != null) {
            IFunction<IRIO, String> handler = TXDB.nameDB.get("Interp." + st);
            if (handler != null) {
                return handler.apply(rubyIO);
            } else {
                SchemaElement ise = AppMain.schemas.getSDBEntry(st);
                return interpretParameter(rubyIO, ise, prefixEnums);
            }
        } else {
            return interpretParameter(rubyIO, (SchemaElement) null, prefixEnums);
        }
    }

    public static String interpretParameter(IRIO rubyIO, SchemaElement ise, boolean prefixEnums) {
        // Basically, Class. overrides go first, then everything else comes after.
        if (rubyIO.getType() == 'o') {
            IFunction<IRIO, String> handler = TXDB.nameDB.get("Class." + rubyIO.getSymbol());
            if (handler != null)
                return handler.apply(rubyIO);
        }
        String r = null;
        if (ise != null) {
            ise = AggregateSchemaElement.extractField(ise, rubyIO);
            if (ise instanceof EnumSchemaElement)
                r = ((EnumSchemaElement) ise).viewValue(rubyIO, prefixEnums);
        }
        if (r == null)
            r = rubyIO.toString();
        return r;
    }

    // NOTE: This can return null.
    public static SchemaElement getParameterDisplaySchemaFromArray(IRIO root, IFunction<IRIO, SchemaElement>[] ise, int i) {
        if (ise == null)
            return null;
        if (ise.length <= i)
            return null;
        return ise[i].apply(root);
    }

    public static String formatExtended(String s, IRIO... pieces) {
        return formatNameExtended(s, new RubyIO().setNull(), pieces, null);
    }
}
