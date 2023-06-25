/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.datum.DatumSymbol;
import gabien.datum.DatumWriter;
import r48.App;
import r48.io.data.RORIO;
import r48.minivm.MVMSlot;
import r48.minivm.MVMU;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.tr.TrNames;
import r48.tr.TrPage.FF1;

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
    }

    private @Nullable FF1 getNameDB(String name) {
        MVMSlot slot = app.vmCtx.getSlot(new DatumSymbol(TrNames.nameRoutine(name)));
        if (slot != null)
            return (FF1) slot.v;
        return null;
    }

    /**
     * Compiles a FormatSyntax for easier debugging.
     */
    public ICompiledFormatSyntax compile(String name, ParameterAccessor paramAcc) {
        System.out.println("fs compile: " + name);
        LinkedList<CompiledChunk> r = new LinkedList<>();
        compileChunk(r, name, paramAcc);
        optimizeChunks(r);
        System.out.print("decompile: ");
        LinkedList<Object> llo = new LinkedList<>();
        decompileList(r, llo);
        System.out.println(DatumWriter.objectToString(llo));
        return (a) -> {
            StringBuilder sb = new StringBuilder();
            for (CompiledChunk chk : r)
                chk.r(sb, a);
            return sb.toString();
        };
    }
    private void decompileList(LinkedList<CompiledChunk> ccr, LinkedList<Object> llo) {
        for (CompiledChunk cc : ccr)
            cc.decompile(llo);
    }

    /**
     * Compiles a part of a FormatSyntax.
     */
    private void compileChunk(LinkedList<CompiledChunk> r, String name, ParameterAccessor paramAcc) {
        char[] data = name.toCharArray();
        EnumSchemaElement.Prefix prefixNext = EnumSchemaElement.Prefix.NoPrefix;
        // C: A fully parsable formatNameExtended string.
        // A: A component array of the form C or C|A.
        // V: A single letter from 'A' through 'Z', representing a parameter.
        // T: An unparsed string limited by context (no escapes, nor [{}]# etc.)
        // R: An instance of T : Name routine.
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
                    ICompiledFormatSyntax def = (root) -> "";
                    if ((componentsComp.size() & 1) != 0)
                        def = componentsComp.removeLast();
                    final ICompiledFormatSyntax fDef = def;
                    r.add(new CompiledChunk() {
                        @Override
                        public void r(StringBuilder sb, RORIO root) {
                            ICompiledFormatSyntax res = fDef;
                            String val = valComp.r(root);
                            for (int j = 0; j < componentsComp.size(); j += 2) {
                                if (val.equals(componentsComp.get(j).r(root))) {
                                    res = componentsComp.get(j + 1);
                                    break;
                                }
                            }
                            sb.append(res.r(root));
                        }
                        @Override
                        public void decompile(LinkedList<Object> llo) {
                            llo.add(new DatumSymbol("IDk1"));
                        }
                    });
                } else if (data[i + 1] == ':') {
                    final char v = data[i];
                    // variable exists form.
                    i = explodeComponentsAndAdvance(components, data, i + 2, '}');
                    determineBooleanComponent(r, components, (root) -> {
                        return paramAcc.get(root, v - 'A') != null;
                    }, paramAcc);
                } else if (data[i + 1] == '=') {
                    char va = data[i];
                    // vt equality form.
                    StringBuilder eqTargetB = new StringBuilder();
                    i = explodeComponent(eqTargetB, data, i + 2, "=");
                    i = explodeComponentsAndAdvance(components, data, i + 1, '}');
                    final String eqTarget = eqTargetB.toString();
                    determineBooleanComponent(r, components, (root) -> {
                        boolean result = root != null;
                        if (result)
                            result = paramAcc.get(root, va - 'A').toString().equals(eqTarget);
                        return result;
                    }, paramAcc);
                } else {
                    throw new RuntimeException("Unknown conditional type!");
                }
            } else if (data[i] == '@') {
                prefixNext = EnumSchemaElement.Prefix.Prefix;
            } else if (data[i] == '[') {
                // Parse precedence order:
                // [@R]
                // [R][C]
                // [R]V
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
                    final EnumSchemaElement.Prefix thisPrefixNext = prefixNext;
                    r.add(new CompiledChunk() {
                        @Override
                        public void r(StringBuilder sb, RORIO root) {
                            if (root == null)
                                return;
                            RORIO p = paramAcc.get(root, ch - 'A');
                            sb.append(interpretParameter(p, type, thisPrefixNext));
                        }
                        @Override
                        public void decompile(LinkedList<Object> llo) {
                            int idx = (ch - 'A');
                            if (thisPrefixNext == EnumSchemaElement.Prefix.Prefix) {
                                llo.add(MVMU.l(
                                    new DatumSymbol("@"), new DatumSymbol("]" + idx),
                                    new DatumSymbol(type), true
                                ));
                            } else {
                                llo.add(MVMU.l(
                                    new DatumSymbol("@"), new DatumSymbol("]" + idx),
                                    new DatumSymbol(type)
                                ));
                            }
                        }
                    });
                } else {
                    final String tp = type.substring(1);
                    final FF1 n = getNameDB(tp);
                    if (n == null)
                        throw new RuntimeException("Expected NDB " + tp);
                    r.add(new CompiledChunk() {
                        @Override
                        public void r(StringBuilder sb, RORIO root) {
                            if (root == null)
                                return;
                            // Meta-interpretation syntax
                            sb.append(n.r(root));
                        }
                        @Override
                        public void decompile(LinkedList<Object> llo) {
                            llo.add(new DatumSymbol("Idk4"));
                        }
                    });
                }
                prefixNext = EnumSchemaElement.Prefix.NoPrefix;
            } else if (data[i] == '#') {
                final EnumSchemaElement.Prefix thisPrefixNext = prefixNext;
                final char ltr = data[++i];
                final int pid = ltr - 'A';
                r.add(new CompiledChunk() {
                    @Override
                    public void r(StringBuilder sb, RORIO root) {
                        RORIO v = paramAcc.get(root, pid);
                        if (v != null) {
                            sb.append(interpretParameter(v, (SchemaElement) null, thisPrefixNext));
                        } else {
                            sb.append(ltr);
                        }
                    }
                    @Override
                    public void decompile(LinkedList<Object> llo) {
                        llo.add(MVMU.l(new DatumSymbol("@"), new DatumSymbol("]" + pid)));
                    }
                });
                prefixNext = EnumSchemaElement.Prefix.NoPrefix;
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
        r.add(new CompiledChunk() {
            @Override
            public void r(StringBuilder sb, RORIO root) {
                for (CompiledChunk c : (p.r(root) ? cT : cF))
                    c.r(sb, root);
            }
            @Override
            public void decompile(LinkedList<Object> llo) {
                LinkedList<Object> dclT = new LinkedList<>();
                LinkedList<Object> dclF = new LinkedList<>();
                decompileList(cT, dclT);
                decompileList(cF, dclF);
                llo.add(MVMU.l(
                    new DatumSymbol("IDKX"),
                    dclT,
                    dclF
                ));
            }
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

    public String interpretParameter(RORIO rubyIO, String st, EnumSchemaElement.Prefix prefixEnums) {
        if (rubyIO == null)
            return "";
        if (st != null) {
            FF1 handler = getNameDB(st);
            if (handler != null) {
                return handler.r(rubyIO);
            } else if (app.sdb.hasSDBEntry(st)) {
                SchemaElement ise = app.sdb.getSDBEntry(st);
                return interpretParameter(rubyIO, ise, prefixEnums);
            }
        }
        return interpretParameter(rubyIO, (SchemaElement) null, prefixEnums);
    }

    /**
     * This is replacing a lot of really silly FormatSyntax use.
     */
    public String interpretParameter(RORIO rubyIO) {
        return interpretParameter(rubyIO, (SchemaElement) null, EnumSchemaElement.Prefix.NoPrefix);
    }

    public String interpretParameter(RORIO rubyIO, SchemaElement ise, EnumSchemaElement.Prefix prefixEnums) {
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

    private static ICompiledFormatSyntax wrapWithCMDisclaimer(String name, ICompiledFormatSyntax v) {
        return (root) -> {
            try {
                return v.r(root);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("While processing name " + name + ", an IndexOutOfBounds exception occurred. This suggests badly checked parameters.");
                e.printStackTrace();
                if (root != null)
                    return v.r(null);
                throw e;
            }
        };
    }
    public ICompiledFormatSyntax compileCMNew(String nameGet) {
        return wrapWithCMDisclaimer(nameGet, compile(nameGet, ACC_ARRAY));
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
        String r(RORIO root);
    }

    public interface CompiledChunk {
        void r(StringBuilder sb, RORIO root);
        /**
         * Decompile
         */
        void decompile(LinkedList<Object> llo);
        /**
         * Tries combining with a given "next" chunk. This is in-place.
         */
        default boolean tryCombine(CompiledChunk next) {
            return false;
        }
    }

    private interface CompiledPredicate {
        boolean r(RORIO root);
    }

    private final class StringChunk implements CompiledChunk {
        public String str;
        public StringChunk(String s) {
            str = s;
        }
        @Override
        public void r(StringBuilder sb, RORIO root) {
            sb.append(str);
        }
        @Override
        public void decompile(LinkedList<Object> llo) {
            llo.add(str);
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
