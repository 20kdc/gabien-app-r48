/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.uslx.append.*;
import r48.App;
import r48.RubyIO;
import r48.io.data.RORIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Yet another class solely to hold a common syntax in an obvious place.
 * Created on 11/06/17.
 */
public class FormatSyntax extends App.Svc {
    public HashMap<String, IFunction<RORIO, String>> nameDB = new HashMap<>();

    public FormatSyntax(App app) {
        super(app);
        // Note: The current NameDB initial state is a "bare minimum" maintenance mode.
        // Name routines are going to get farmed out to MiniVM as soon as possible.
        nameDB.put("Interp.lang-Common-add", (rubyIO) -> {
            String[] range = rubyIO.decString().split(" ");
            int v = 0;
            for (String s : range)
                v += Integer.valueOf(s);
            return Integer.toString(v);
        });
        nameDB.put("Interp.lang-Common-r2kTsConverter", (rubyIO) -> {
            double d = Double.parseDouble(rubyIO.decString());
            // WARNING: THIS IS MADNESS, and could be off by a few seconds.
            // In practice I tested it and it somehow wasn't off at all.
            // Command used given here:
            // [gamemanj@archways ~]$ date --date="12/30/1899 12:00 am" +%s
            // -2209161600
            // since we want ms, 3 more 0s have been added
            long v = -2209161600000L;
            long dayLen = 24L * 60L * 60L * 1000L;
            // Ok, so, firstly, fractional part is considered completely separately and absolutely.
            double fractional = Math.abs(d);
            fractional -= Math.floor(fractional);
            // Now get rid of fractional in the "right way" (round towards 0)
            if (d < 0) {
                d += fractional;
            } else {
                d -= fractional;
            }
            v += ((long) d) * dayLen;
            v += (long) (fractional * dayLen);

            // NOTE: This converts to local time zone.
            return new Date(v).toString();
        });
    }

    /**
     * This is sort of fake for now. Roll with it, please...?
     */
    public ICompiledFormatSyntax compile(String name) {
        LinkedList<ICompiledFormatSyntaxChunk> r = new LinkedList<>();
        compileChunk(r, name);
        optimizeChunks(r);
        return (a, b, c) -> {
            StringBuilder sb = new StringBuilder();
            for (ICompiledFormatSyntaxChunk chk : r)
                chk.r(sb, a, b, c);
            return sb.toString();
        };
    }

    /**
     * Still pretty fake.
     */
    public void compileChunk(LinkedList<ICompiledFormatSyntaxChunk> r, String name) {
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
                    final ICompiledFormatSyntax valComp = compile(components.removeFirst());
                    LinkedList<ICompiledFormatSyntax> componentsComp = new LinkedList<>();
                    for (String s : components)
                        componentsComp.add(compile(s));
                    ICompiledFormatSyntax def = (root, parameters, parameterSchemas) -> "";
                    if ((componentsComp.size() & 1) != 0)
                        def = componentsComp.removeLast();
                    final ICompiledFormatSyntax fDef = def;
                    r.add((sb, root, parameters, parameterSchemas) -> {
                        ICompiledFormatSyntax res = fDef;
                        String val = valComp.r(root, parameters, parameterSchemas);
                        for (int j = 0; j < componentsComp.size(); j += 2) {
                            if (val.equals(componentsComp.get(j).r(root, parameters, parameterSchemas))) {
                                res = componentsComp.get(j + 1);
                                break;
                            }
                        }
                        sb.append(res.r(root, parameters, parameterSchemas));
                    });
                } else if (data[i + 1] == ':') {
                    final char v = data[i];
                    // variable exists form.
                    i = explodeComponentsAndAdvance(components, data, i + 2, '}');
                    determineBooleanComponent(r, components, (root, parameters, parameterSchemas) -> {
                        boolean result = parameters != null;
                        if (result)
                            result = (v - 'A') < parameters.length;
                        return result;
                    });
                } else if (data[i + 1] == '=') {
                    char va = data[i];
                    // vt equality form.
                    StringBuilder eqTargetB = new StringBuilder();
                    i = explodeComponent(eqTargetB, data, i + 2, "=");
                    i = explodeComponentsAndAdvance(components, data, i + 1, '}');
                    final String eqTarget = eqTargetB.toString();
                    determineBooleanComponent(r, components, (root, parameters, parameterSchemas) -> {
                        boolean result = parameters != null;
                        if (result) {
                            SchemaElement as = getParameterDisplaySchemaFromArray(root, parameterSchemas, va - 'A');
                            String a = interpretParameter(parameters[va - 'A'], as, false);
                            result = a.equals(eqTarget);
                        }
                        return result;
                    });
                } else if (data[i + 2] == ':') {
                    // vv equality form.
                    final char va = data[i];
                    final char vb = data[i + 1];
                    i = explodeComponentsAndAdvance(components, data, i + 3, '}');
                    determineBooleanComponent(r, components, (root, parameters, parameterSchemas) -> {
                        boolean result = parameters != null;
                        if (result) {
                            SchemaElement as = getParameterDisplaySchemaFromArray(root, parameterSchemas, va - 'A');
                            SchemaElement bs = getParameterDisplaySchemaFromArray(root, parameterSchemas, vb - 'A');
                            String a = interpretParameter(parameters[va - 'A'], as, false);
                            String b = interpretParameter(parameters[vb - 'A'], bs, false);
                            result = a.equals(b);
                        }
                        return result;
                    });
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
                StringBuilder typeB = new StringBuilder();
                int indexOfAt = -1;
                while (data[++i] != ']') {
                    if (indexOfAt == -1)
                        if (data[i] == '@')
                            indexOfAt = typeB.length();
                    typeB.append(data[i]);
                }
                final String type = typeB.toString();
                if (indexOfAt != 0) {
                    char ch = data[++i];
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
                                    throw new RuntimeException("Mismatched []{} error.");
                            }
                            tx.append(ch2);
                        }
                        // ... then parse it.
                        ICompiledFormatSyntax out = compile(tx.toString());
                        final boolean thisPrefixNext = prefixNext;
                        r.add((sb, root, parameters, parameterSchemas) -> {
                            if (parameters == null)
                                return;
                            RORIO p = new RubyIO().setString(out.r(root, parameters, parameterSchemas), true);
                            sb.append(interpretParameter(p, type, thisPrefixNext));
                        });
                    } else {
                        final boolean thisPrefixNext = prefixNext;
                        r.add((sb, root, parameters, parameterSchemas) -> {
                            if (parameters == null)
                                return;
                            RORIO p = parameters[ch - 'A'];
                            sb.append(interpretParameter(p, type, thisPrefixNext));
                        });
                    }
                } else {
                    final String tp = type.substring(1);
                    r.add((sb, root, parameters, parameterSchemas) -> {
                        if (parameters == null)
                            return;
                        // Meta-interpretation syntax
                        IFunction<RORIO, String> n = nameDB.get(tp);
                        if (n == null)
                            throw new RuntimeException("Expected NDB " + tp);
                        sb.append(n.apply(root));
                    });
                }
                prefixNext = false;
            } else if (data[i] == '#') {
                final boolean thisPrefixNext = prefixNext;
                final char ltr = data[++i];
                final int pid = ltr - 'A';
                r.add((sb, root, parameters, parameterSchemas) -> {
                    if (parameters != null && (pid >= 0) && (pid < parameters.length)) {
                        sb.append(interpretParameter(parameters[pid], getParameterDisplaySchemaFromArray(root, parameterSchemas, pid), thisPrefixNext));
                    } else {
                        sb.append(ltr);
                    }
                });
                prefixNext = false;
            } else {
                cChar(r, data[i]);
            }
        }
    }

    private void cChar(LinkedList<ICompiledFormatSyntaxChunk> r, char c) {
        r.add(new StringChunk(Character.toString(c)));
    }

    private void optimizeChunks(LinkedList<ICompiledFormatSyntaxChunk> ch) {
        
    }

    private void determineBooleanComponent(LinkedList<ICompiledFormatSyntaxChunk> r, LinkedList<String> components, ICompiledFormatSyntaxPredicate p) {
        LinkedList<ICompiledFormatSyntaxChunk> cT = new LinkedList<ICompiledFormatSyntaxChunk>();
        LinkedList<ICompiledFormatSyntaxChunk> cF = new LinkedList<ICompiledFormatSyntaxChunk>();
        for (int i = 0; i < components.size(); i++) {
            String elm = components.get(i);
            if ((i & 1) == 0) {
                // true
                compileChunk(cT, elm);
            } else {
                // false
                compileChunk(cF, elm);
            }
        }
        optimizeChunks(cT);
        optimizeChunks(cF);
        r.add((sb, root, parameters, parameterSchemas) -> {
            for (ICompiledFormatSyntaxChunk c : (p.r(root, parameters, parameterSchemas) ? cT : cF))
                c.r(sb, root, parameters, parameterSchemas);
        });
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

    public String interpretParameter(RORIO rubyIO, String st, boolean prefixEnums) {
        if (st != null) {
            IFunction<RORIO, String> handler = nameDB.get("Interp." + st);
            if (handler != null) {
                return handler.apply(rubyIO);
            } else {
                SchemaElement ise = app.sdb.getSDBEntry(st);
                return interpretParameter(rubyIO, ise, prefixEnums);
            }
        } else {
            return interpretParameter(rubyIO, (SchemaElement) null, prefixEnums);
        }
    }

    /**
     * This is replacing a lot of really silly FormatSyntax use.
     */
    public String interpretParameter(RORIO rubyIO) {
        return interpretParameter(rubyIO, (SchemaElement) null, false);
    }

    public String interpretParameter(RORIO rubyIO, SchemaElement ise, boolean prefixEnums) {
        // Basically, Class. overrides go first, then everything else comes after.
        if (rubyIO.getType() == 'o') {
            IFunction<RORIO, String> handler = nameDB.get("Class." + rubyIO.getSymbol());
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
    private static SchemaElement getParameterDisplaySchemaFromArray(RORIO root, IFunction<RORIO, SchemaElement>[] ise, int i) {
        if (ise == null)
            return null;
        if (ise.length <= i)
            return null;
        return ise[i].apply(root);
    }

    public interface ICompiledFormatSyntax {
        String r(RORIO root, RORIO[] parameters, IFunction<RORIO, SchemaElement>[] parameterSchemas);
    }

    public interface ICompiledFormatSyntaxChunk {
        void r(StringBuilder sb, RORIO root, RORIO[] parameters, IFunction<RORIO, SchemaElement>[] parameterSchemas);
        default @Nullable ICompiledFormatSyntaxChunk tryCombine(ICompiledFormatSyntaxChunk previous) {
            return null;
        }
    }

    private interface ICompiledFormatSyntaxPredicate {
        boolean r(RORIO root, RORIO[] parameters, IFunction<RORIO, SchemaElement>[] parameterSchemas);
    }

    private final class StringChunk implements ICompiledFormatSyntaxChunk {
        public final String str;
        public StringChunk(String s) {
            str = s;
        }
        @Override
        public void r(StringBuilder sb, RORIO root, RORIO[] parameters, IFunction<RORIO, SchemaElement>[] parameterSchemas) {
            sb.append(str);
        }
        @Override
        public @Nullable ICompiledFormatSyntaxChunk tryCombine(ICompiledFormatSyntaxChunk previous) {
            if (previous instanceof StringChunk)
                return new StringChunk(((StringChunk) previous).str + str);
            return null;
        }
    }
}
