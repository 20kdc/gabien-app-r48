/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.datum.DatumSymbol;
import gabien.uslx.append.*;
import r48.App;
import r48.RubyIO;
import r48.io.data.RORIO;
import r48.minivm.MVMSlot;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.tr.IDynTrSystemNameRoutine;
import r48.tr.TrNames;
import r48.tr.TrPage.FF1;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Yet another class solely to hold a common syntax in an obvious place.
 * Created on 11/06/17.
 */
public class FormatSyntax extends App.Svc {
    public static final ParameterAccessor ACC_ARRAY = (root, idx) -> {
        if (root == null)
            return null;
        if (idx < 0)
            return null;
        if (root.getType() != '[')
            return null;
        if (idx >= root.getALen())
            return null;
        return root.getAElem(idx);
    };

    public FormatSyntax(App app) {
        super(app);
        // Note: The current NameDB initial state is a "bare minimum" maintenance mode.
        // Name routines are going to get farmed out to MiniVM as soon as possible.
        addNameDBFixed("Interp.lang-Common-add", (x) -> {
            RORIO rubyIO = (RORIO) x;
            String[] range = rubyIO.decString().split(" ");
            int v = 0;
            for (String s : range)
                v += Integer.valueOf(s);
            return Integer.toString(v);
        });
        addNameDBFixed("Interp.lang-Common-r2kTsConverter", (x) -> {
            RORIO rubyIO = (RORIO) x;
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

    private void addNameDBFixed(String name, IDynTrSystemNameRoutine routine) {
        app.vmCtx.ensureSlot(new DatumSymbol(TrNames.nameRoutine(name))).v = routine;
    }

    public @Nullable FF1 getNameDB(String name) {
        MVMSlot slot = app.vmCtx.getSlot(new DatumSymbol(TrNames.nameRoutine(name)));
        if (slot != null)
            return (FF1) slot.v;
        return null;
    }

    /**
     * Compiles a FormatSyntax for easier debugging.
     */
    public ICompiledFormatSyntax compile(String name, ParameterAccessor paramAcc) {
        // System.out.println("fs compile: " + name);
        LinkedList<CompiledChunk> r = new LinkedList<>();
        compileChunk(r, name, paramAcc);
        optimizeChunks(r);
        return (a, c) -> {
            StringBuilder sb = new StringBuilder();
            for (CompiledChunk chk : r)
                chk.r(sb, a, c);
            return sb.toString();
        };
    }

    /**
     * Compiles a part of a FormatSyntax.
     */
    private void compileChunk(LinkedList<CompiledChunk> r, String name, ParameterAccessor paramAcc) {
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
                    final ICompiledFormatSyntax valComp = compile(components.removeFirst(), paramAcc);
                    LinkedList<ICompiledFormatSyntax> componentsComp = new LinkedList<>();
                    for (String s : components)
                        componentsComp.add(compile(s, paramAcc));
                    ICompiledFormatSyntax def = (root, parameterSchemas) -> "";
                    if ((componentsComp.size() & 1) != 0)
                        def = componentsComp.removeLast();
                    final ICompiledFormatSyntax fDef = def;
                    r.add((sb, root, parameterSchemas) -> {
                        ICompiledFormatSyntax res = fDef;
                        String val = valComp.r(root, parameterSchemas);
                        for (int j = 0; j < componentsComp.size(); j += 2) {
                            if (val.equals(componentsComp.get(j).r(root, parameterSchemas))) {
                                res = componentsComp.get(j + 1);
                                break;
                            }
                        }
                        sb.append(res.r(root, parameterSchemas));
                    });
                } else if (data[i + 1] == ':') {
                    final char v = data[i];
                    // variable exists form.
                    i = explodeComponentsAndAdvance(components, data, i + 2, '}');
                    determineBooleanComponent(r, components, (root, parameterSchemas) -> {
                        return paramAcc.get(root, v - 'A') != null;
                    }, paramAcc);
                } else if (data[i + 1] == '=') {
                    char va = data[i];
                    // vt equality form.
                    StringBuilder eqTargetB = new StringBuilder();
                    i = explodeComponent(eqTargetB, data, i + 2, "=");
                    i = explodeComponentsAndAdvance(components, data, i + 1, '}');
                    final String eqTarget = eqTargetB.toString();
                    determineBooleanComponent(r, components, (root, parameterSchemas) -> {
                        boolean result = root != null;
                        if (result) {
                            SchemaElement as = getParameterDisplaySchemaFromArray(root, parameterSchemas, va - 'A');
                            String a = interpretParameter(paramAcc.get(root, va - 'A'), as, false);
                            result = a.equals(eqTarget);
                        }
                        return result;
                    }, paramAcc);
                } else if (data[i + 2] == ':') {
                    // vv equality form.
                    final char va = data[i];
                    final char vb = data[i + 1];
                    i = explodeComponentsAndAdvance(components, data, i + 3, '}');
                    determineBooleanComponent(r, components, (root, parameterSchemas) -> {
                        boolean result = root != null;
                        if (result) {
                            SchemaElement as = getParameterDisplaySchemaFromArray(root, parameterSchemas, va - 'A');
                            SchemaElement bs = getParameterDisplaySchemaFromArray(root, parameterSchemas, vb - 'A');
                            String a = interpretParameter(paramAcc.get(root, va - 'A'), as, false);
                            String b = interpretParameter(paramAcc.get(root, vb - 'A'), bs, false);
                            result = a.equals(b);
                        }
                        return result;
                    }, paramAcc);
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
                        // need to skip over the [ so we're at level 0
                        // this is still more comprehensible than the ad-hoc monolith for no reason being replaced here
                        // explodeComponent then returns the index of the ]
                        // but the for loop will do its own increment
                        i = explodeComponent(tx, data, i + 1, "]");
                        // ... then parse it.
                        ICompiledFormatSyntax out = compile(tx.toString(), paramAcc);
                        final boolean thisPrefixNext = prefixNext;
                        r.add((sb, root, parameterSchemas) -> {
                            if (root == null)
                                return;
                            RORIO p = new RubyIO().setString(out.r(root, parameterSchemas), true);
                            sb.append(interpretParameter(p, type, thisPrefixNext));
                        });
                    } else {
                        final boolean thisPrefixNext = prefixNext;
                        r.add((sb, root, parameterSchemas) -> {
                            if (root == null)
                                return;
                            RORIO p = paramAcc.get(root, ch - 'A');
                            sb.append(interpretParameter(p, type, thisPrefixNext));
                        });
                    }
                } else {
                    final String tp = type.substring(1);
                    r.add((sb, root, parameterSchemas) -> {
                        if (root == null)
                            return;
                        // Meta-interpretation syntax
                        FF1 n = getNameDB(tp);
                        if (n == null)
                            throw new RuntimeException("Expected NDB " + tp);
                        sb.append(n.r(root));
                    });
                }
                prefixNext = false;
            } else if (data[i] == '#') {
                final boolean thisPrefixNext = prefixNext;
                final char ltr = data[++i];
                final int pid = ltr - 'A';
                r.add((sb, root, parameterSchemas) -> {
                    RORIO v = paramAcc.get(root, pid);
                    if (v != null) {
                        sb.append(interpretParameter(v, getParameterDisplaySchemaFromArray(root, parameterSchemas, pid), thisPrefixNext));
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

    private void cChar(LinkedList<CompiledChunk> r, char c) {
        r.add(new StringChunk(Character.toString(c)));
    }

    private void optimizeChunks(LinkedList<CompiledChunk> ch) {
        Iterator<CompiledChunk> ci = ch.iterator();
        CompiledChunk prev = null;
        while (ci.hasNext()) {
            CompiledChunk chk = ci.next();
            if (prev != null && prev.tryCombine(chk)) {
                // remove by combination
                ci.remove();
            } else {
                prev = chk;
            }
        }
    }

    private void determineBooleanComponent(LinkedList<CompiledChunk> r, LinkedList<String> components, CompiledPredicate p, ParameterAccessor paramAcc) {
        LinkedList<CompiledChunk> cT = new LinkedList<CompiledChunk>();
        LinkedList<CompiledChunk> cF = new LinkedList<CompiledChunk>();
        for (int i = 0; i < components.size(); i++) {
            String elm = components.get(i);
            if ((i & 1) == 0) {
                // true
                compileChunk(cT, elm, paramAcc);
            } else {
                // false
                compileChunk(cF, elm, paramAcc);
            }
        }
        optimizeChunks(cT);
        optimizeChunks(cF);
        r.add((sb, root, parameterSchemas) -> {
            for (CompiledChunk c : (p.r(root, parameterSchemas) ? cT : cF))
                c.r(sb, root, parameterSchemas);
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
        if (rubyIO == null)
            return "";
        if (st != null) {
            FF1 handler = getNameDB("Interp." + st);
            if (handler != null) {
                return handler.r(rubyIO);
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
            FF1 handler = getNameDB("Class." + rubyIO.getSymbol());
            if (handler != null)
                return handler.r(rubyIO);
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

    private static ICompiledFormatSyntax wrapWithCMDisclaimer(String name, ICompiledFormatSyntax v) {
        return (root, parameterSchemas) -> {
            try {
                return v.r(root, parameterSchemas);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("While processing name " + name + ", an IndexOutOfBounds exception occurred. This suggests badly checked parameters.");
                e.printStackTrace();
                if (root != null)
                    return v.r(null, parameterSchemas);
                throw e;
            }
        };
    }
    public ICompiledFormatSyntax compileCM(String nameGet) {
        if (nameGet.startsWith("@@")) {
            return wrapWithCMDisclaimer(nameGet, compile(nameGet.substring(2), ACC_ARRAY));
        } else {
            LinkedList<CompiledChunk> r = new LinkedList<>();
            int pi = 0;
            for (char c : nameGet.toCharArray()) {
                if (c == '!') {
                    final int setPI = pi++;
                    r.add((sb, root, parameterSchemas) -> {
                        if (root != null) {
                            sb.append(" to ");
                            sb.append(interpretCMLocalParameter(root, setPI, true, parameterSchemas));
                        }
                    });
                } else if (c == '$') {
                    final int setPI = pi++;
                    r.add((sb, root, parameterSchemas) -> {
                        if (root != null) {
                            sb.append(" ");
                            sb.append(interpretCMLocalParameter(root, setPI, true, parameterSchemas));
                        }
                    });
                } else if (c == '#') {
                    final int setPI = pi++;
                    r.add((sb, root, parameterSchemas) -> {
                        if (root != null) {
                            String beginning = interpretCMLocalParameter(root, setPI, true, parameterSchemas);
                            String end = interpretCMLocalParameter(root, setPI + 1, true, parameterSchemas);
                            if (beginning.equals(end)) {
                                sb.append(" ");
                                sb.append(beginning);
                            } else {
                                sb.append("s ");
                                sb.append(beginning);
                                sb.append(" through ");
                                sb.append(end);
                            }
                        } else {
                            sb.append("#");
                        }
                    });
                } else {
                    r.add(new StringChunk(Character.toString(c)));
                }
            }
            optimizeChunks(r);
            return wrapWithCMDisclaimer(nameGet, (root, parameterSchemas) -> {
                StringBuilder sb = new StringBuilder();
                for (CompiledChunk chk : r)
                    chk.r(sb, root, parameterSchemas);
                return sb.toString();
            });
        }
    }

    private String interpretCMLocalParameter(RORIO root, int pi, boolean prefixEnums, IFunction<RORIO, SchemaElement>[] parameterSchemas) {
        RORIO parameter = root.getAElem(pi);
        if (parameterSchemas == null || pi < 0 || pi >= parameterSchemas.length)
            return app.fmt.interpretParameter(parameter, app.sdb.getSDBEntry("genericScriptParameter"), prefixEnums);
        return app.fmt.interpretParameter(parameter, parameterSchemas[pi].apply(root), prefixEnums);
    }

    /**
     * This is pretty much evil magic made so name routines get to survive,
     * while kicking the stuff they relied on past the compilation barrier.
     */
    public interface ParameterAccessor {
        /**
         * Gets the given parameter relative to the root.
         */
        @Nullable RORIO get(@Nullable RORIO root, int idx);
    }

    public interface ICompiledFormatSyntax {
        /**
         * Outputs a string given a root and parameter schemas.
         */
        String r(RORIO root, IFunction<RORIO, SchemaElement>[] parameterSchemas);
    }

    public interface CompiledChunk {
        void r(StringBuilder sb, RORIO root, IFunction<RORIO, SchemaElement>[] parameterSchemas);
        /**
         * Tries combining with a given "next" chunk. This is in-place.
         */
        default boolean tryCombine(CompiledChunk next) {
            return false;
        }
    }

    private interface CompiledPredicate {
        boolean r(RORIO root, IFunction<RORIO, SchemaElement>[] parameterSchemas);
    }

    private final class StringChunk implements CompiledChunk {
        public String str;
        public StringChunk(String s) {
            str = s;
        }
        @Override
        public void r(StringBuilder sb, RORIO root, IFunction<RORIO, SchemaElement>[] parameterSchemas) {
            sb.append(str);
        }
        @Override
        public boolean tryCombine(CompiledChunk next) {
            if (next instanceof StringChunk) {
                str += ((StringChunk) next).str;
                return true;
            }
            return false;
        }
    }
}
