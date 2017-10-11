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
                if (data[i] == '@') {
                    // Special case.
                    String tx = "";
                    LinkedList<String> components = new LinkedList<String>();
                    int escapeCount = 0;
                    while (true) {
                        char ch2 = data[++i];
                        if (ch2 == '#') {
                            tx += ch2;
                            tx += data[++i];
                            continue;
                        }
                        if (ch2 == '{')
                            escapeCount++;
                        if (ch2 == '}') {
                            if (escapeCount == 0)
                                break;
                            escapeCount--;
                        }
                        if (ch2 == '[')
                            escapeCount++;
                        if (ch2 == ']') {
                            if (escapeCount == 0)
                                return "Mismatched []{} error.";
                            escapeCount--;
                        }
                        if (ch2 == '|')
                            if (escapeCount == 0) {
                                components.add(tx);
                                tx = "";
                                continue;
                            }
                        tx += ch2;
                    }
                    components.add(tx);
                    tx = components.getFirst();
                    // If the component count is even, there's a default (1 + even KVP + 1)
                    // If it's odd, there isn't.
                    tx = formatNameExtended(tx, root, parameters, parameterSchemas);
                    String val = "";
                    if ((components.size() % 2) == 0)
                        val = components.getLast();
                    for (int j = 1; j < components.size() - 1; j += 2) {
                        String a = formatNameExtended(components.get(j), root, parameters, parameterSchemas);
                        String b = components.get(j + 1);
                        if (tx.equals(a))
                            val = b;
                    }
                    r += formatNameExtended(val, root, parameters, parameterSchemas);
                    continue;
                }
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
                    wantedVal = parameters[pidB].toString();
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
                        char ch = data[++i];
                        RubyIO p;
                        if (ch == '[') {
                            // At this point, it's gone recursive.
                            // Need to safely skip over this lot...
                            String tx = "";
                            int escapeCount = 1;
                            while (escapeCount > 0) {
                                char ch2 = data[++i];
                                if (ch2 == '#') {
                                    tx += ch2;
                                    tx += data[++i];
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
                                tx += ch2;
                            }
                            // ... then parse it.
                            tx = formatNameExtended(tx, root, parameters, parameterSchemas);
                            p = new RubyIO().encString(tx);
                        } else {
                            p = parameters[ch - 'A'];
                        }
                        IFunction<RubyIO, String> handler = TXDB.nameDB.get("Interp." + type);
                        if (handler != null) {
                            r += handler.apply(p);
                        } else {
                            SchemaElement ise = AppMain.schemas.getSDBEntry(type);
                            r += interpretParameter(p, ise, prefixNext);
                        }
                    } else {
                        // Meta-interpretation syntax
                        String tp = type.substring(1);
                        IFunction<RubyIO, String> n = TXDB.nameDB.get(tp);
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
            IFunction<RubyIO, String> handler = TXDB.nameDB.get("Class." + rubyIO.symVal);
            if (handler != null)
                return handler.apply(rubyIO);
        }
        String r = null;
        if (ise != null) {
            while (ise instanceof IProxySchemaElement)
                ise = ((IProxySchemaElement) ise).getEntry();
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

    public static String formatExtended(String s, RubyIO... pieces) {
        RubyIO synthRoot = new RubyIO();
        synthRoot.type = '[';
        synthRoot.arrVal = pieces;
        return formatNameExtended(s, synthRoot, pieces, null);
    }
}
