/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import gabien.ui.IFunction;
import gabien.ui.ISupplier;
import r48.AppMain;
import r48.DictionaryUpdaterRunnable;
import r48.RubyIO;
import r48.schema.*;
import r48.schema.arrays.ArbIndexedArraySchemaElement;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.integers.IntBooleanSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.LowerBoundIntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.*;
import r48.schema.specialized.tbleditors.BitfieldTableCellEditor;
import r48.schema.specialized.tbleditors.DefaultTableCellEditor;
import r48.schema.specialized.tbleditors.ITableCellEditor;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * The ultimate database, more or less, since this houses the data definitions needed to do things like edit Events.
 * Not required for reading maps.
 * Created on 12/30/16.
 */
public class SDB {
    // The very unsafe option which will turn on all sorts of automatic script helper functions.
    // Some of which currently WILL destroy scripts. 315, map29
    // Ok, so I've checked in various ways and done a full restore from virgin copy.
    // I've used UITest to avoid triggering any potentially destructive Schema code.
    // The answer is the same:
    // Entries 9 and 10 of entity 315, in the Map029 file, contain a duplicate Leave Block.
    // I have no idea how this was managed.

    public static boolean allowControlOfEventCommandIndent = false;
    private HashMap<String, SchemaElement> schemaDatabase = new HashMap<String, SchemaElement>();
    protected HashMap<String, SchemaElement> schemaTrueDatabase = new HashMap<String, SchemaElement>();
    private LinkedList<DictionaryUpdaterRunnable> dictionaryUpdaterRunnables = new LinkedList<DictionaryUpdaterRunnable>();
    private LinkedList<Runnable> mergeRunnables = new LinkedList<Runnable>();
    private LinkedList<String> remainingExpected = new LinkedList<String>();
    private HashMap<String, CMDB> cmdbs = new HashMap<String, CMDB>();
    public HashMap<String, IFunction<RubyIO, String>> nameDB = new HashMap<String, IFunction<RubyIO, String>>();

    public SDB() {
        schemaDatabase.put("nil", new OpaqueSchemaElement());
        schemaDatabase.put("int", new IntegerSchemaElement(0));
        schemaDatabase.put("int+0", new LowerBoundIntegerSchemaElement(0, 0));
        schemaDatabase.put("int+1", new LowerBoundIntegerSchemaElement(1, 1));
        schemaDatabase.put("index", new AMAISchemaElement());
        schemaDatabase.put("float", new FloatSchemaElement("0"));
        schemaDatabase.put("string", new StringSchemaElement("", '\"'));
        schemaDatabase.put("boolean", new BooleanSchemaElement(false));
        schemaDatabase.put("booleanDefTrue", new BooleanSchemaElement(true));
        schemaDatabase.put("int_boolean", new IntBooleanSchemaElement(false));
        schemaDatabase.put("int_booleanDefTrue", new IntBooleanSchemaElement(true));
        schemaDatabase.put("OPAQUE", new OpaqueSchemaElement());

        schemaDatabase.put("zlibBlobEditor", new ZLibBlobSchemaElement());
        schemaDatabase.put("stringBlobEditor", new StringBlobSchemaElement());

        schemaDatabase.put("internal_EPGD", new EPGDisplaySchemaElement());

        schemaTrueDatabase.putAll(schemaDatabase);
    }

    private IFunction<RubyIO, String> getFunctionToReturn(final String s) {
        return new IFunction<RubyIO, String>() {
            @Override
            public String apply(RubyIO rubyIO) {
                return s;
            }
        };
    }

    public void readFile(String s) {
        DBLoader.readFile(s, new IDatabase() {
            AggregateSchemaElement workingObj;

            HashMap<Integer, String> commandBufferNames = new HashMap<Integer, String>();
            HashMap<Integer, SchemaElement> commandBufferSchemas = new HashMap<Integer, SchemaElement>();

            @Override
            public void newObj(int objId, String objName) {
                commandBufferNames.put(objId, objName);
                workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                commandBufferSchemas.put(objId, workingObj);
                //MapSystem.out.println("Array definition when inappropriate: " + objName);
            }

            public SchemaElement handleChain(final String[] args, final int start) {
                return new ISupplier<SchemaElement>() {
                    // This function is recursive but needs state to be kept around after exit.
                    // Kind of a pain, *unless* you have a surrounding instance.
                    public int point = start;

                    @Override
                    public SchemaElement get() {
                        final String text = args[point++];
                        if (text.equals("roint=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new ROIntegerSchemaElement(n);
                        }
                        if (text.equals("int=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new IntegerSchemaElement(n);
                        }
                        if (text.equals("int+0=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new LowerBoundIntegerSchemaElement(0, n);
                        }
                        if (text.equals("int+1=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new LowerBoundIntegerSchemaElement(1, n);
                        }
                        if (text.equals("float="))
                            return new FloatSchemaElement(args[point++]);
                        if (text.equals("string="))
                            return new StringSchemaElement(args[point++], '\"');
                        if (text.equals("hwnd")) {
                            String a = args[point++];
                            if (a.equals("."))
                                a = null;
                            return new HWNDSchemaElement(a, args[point++]);
                        }
                        if (text.equals("optIV")) {
                            String a = args[point++];
                            return new IVarSchemaElement(a, get(), true);
                        }

                        if (text.equals("array")) {
                            int n = Integer.parseInt(args[point++]);
                            return new StandardArraySchemaElement(get(), n, false);
                        }
                        if (text.equals("arrayAL1"))
                            return new StandardArraySchemaElement(get(), 0, true);
                        if (text.equals("arrayIx1"))
                            return new ArbIndexedArraySchemaElement(get(), 1, 0);
                        if (text.equals("arrayIxN")) {
                            int ofx = Integer.parseInt(args[point++]);
                            return new ArbIndexedArraySchemaElement(get(), ofx, 0);
                        }
                        if (text.equals("arrayDAM")) {
                            int disambiguatorIndex = Integer.parseInt(args[point++]);
                            SchemaElement disambiguatorType = get();
                            SchemaElement backup = get();
                            HashMap<Integer, SchemaElement> disambiguations = new HashMap<Integer, SchemaElement>();
                            while (point < args.length) {
                                int ind = Integer.parseInt(args[point++]);
                                disambiguations.put(ind, get());
                            }
                            return new ArrayDisambiguatorSchemaElement(disambiguatorIndex, disambiguatorType, backup, disambiguations);
                        }
                        if (text.equals("flushCommandBuffer")) {
                            // time to flush it!
                            String disambiguationIVar = args[point++];
                            setSDBEntry(args[point++], new EnumSchemaElement(commandBufferNames, "Code.Int."));
                            HashMap<Integer, SchemaElement> baseSE = commandBufferSchemas;
                            commandBufferNames = new HashMap<Integer, String>();
                            commandBufferSchemas = new HashMap<Integer, SchemaElement>();
                            return new GenericDisambiguationSchemaElement(disambiguationIVar, baseSE);
                        }
                        if (text.equals("hash")) {
                            SchemaElement k = get();
                            return new HashSchemaElement(k, get());
                        }
                        if (text.equals("subwindow"))
                            return new SubwindowSchemaElement(get());
                        // subwindow[ This Is A Test ]
                        if (text.equals("subwindow[")) {
                            String text2 = args[point++];
                            while (!args[point].equals("]"))
                                text2 += " " + args[point++];
                            point++;
                            if (text2.startsWith("@")) {
                                final String textFinal = text2.substring(1);
                                return new SubwindowSchemaElement(get(), new IFunction<RubyIO, String>() {
                                    @Override
                                    public String apply(RubyIO rubyIO) {
                                        return nameDB.get("Interp." + textFinal).apply(rubyIO);
                                    }
                                });
                            } else {
                                return new SubwindowSchemaElement(get(), getFunctionToReturn(text2));
                            }
                        }

                        if (text.equals("{")) {
                            // Aggregate
                            AggregateSchemaElement subag = new AggregateSchemaElement(new SchemaElement[] {});
                            SchemaElement ise = get();
                            while (ise != null) {
                                subag.aggregate.add(ise);
                                ise = get();
                            }
                            return subag;
                        }

                        if (text.equals("typeChanger{")) {
                            // Type Changer
                            LinkedList<String> strs = new LinkedList<String>();
                            LinkedList<SchemaElement> scms = new LinkedList<SchemaElement>();
                            while (true) {
                                SchemaElement a = get();
                                if (a == null)
                                    break;
                                String b = args[point++];
                                strs.add(b);
                                scms.add(a);
                            }
                            return new TypeChangerSchemaElement(strs.toArray(new String[0]), scms.toArray(new SchemaElement[0]));
                        }

                        // -- These two must be in this order.
                        if (text.startsWith("]?")) {
                            // yay for... well, semi-consistency!
                            String a = text.substring(2);
                            String b = args[point++];
                            String o = args[point++];
                            return new ArrayElementSchemaElement(Integer.parseInt(a), b, get(), o);
                        }
                        if (text.startsWith("]")) {
                            // yay for consistency!
                            String a = text.substring(1);
                            String b = args[point++];
                            return new ArrayElementSchemaElement(Integer.parseInt(a), b, get(), null);
                        }
                        // --

                        if (text.equals("}"))
                            return null;

                        // Specialized stuff starts here.
                        // This includes anything of type 'u'.

                        // CS means "control indent if allowed"
                        // MS means "never control indent"
                        if (text.equals("RPGCS")) {
                            final CMDB database = getCMDB(args[point++]);
                            SchemaElement a = get();
                            return new SubwindowSchemaElement(new RPGCommandSchemaElement(a, get(), database, allowControlOfEventCommandIndent), new IFunction<RubyIO, String>() {
                                @Override
                                public String apply(RubyIO rubyIO) {
                                    return database.buildCodename(rubyIO, true);
                                }
                            });
                        }
                        if (text.equals("RPGMS")) {
                            final CMDB database = getCMDB(args[point++]);
                            SchemaElement a = get();
                            return new SubwindowSchemaElement(new RPGCommandSchemaElement(a, get(), database, false), new IFunction<RubyIO, String>() {
                                @Override
                                public String apply(RubyIO rubyIO) {
                                    return database.buildCodename(rubyIO, true);
                                }
                            });
                        }
                        if (text.startsWith("table")) {
                            String eText = text;
                            boolean hasFlags = eText.endsWith("F");
                            if (hasFlags)
                                eText = eText.substring(0, eText.length() - 1);
                            boolean hasDefault = eText.endsWith("D");
                            if (hasDefault)
                                eText = eText.substring(0, eText.length() - 1);

                            TSDB tilesetAllocations = null;
                            if (eText.equals("tableSTA"))
                                tilesetAllocations = new TSDB(args[point++]);
                            String iV = args[point++];
                            if (iV.equals("."))
                                iV = null;
                            String wV = args[point++];
                            if (wV.equals("."))
                                wV = null;
                            String hV = args[point++];
                            if (hV.equals("."))
                                hV = null;

                            IFunction<RubyIO, String> iVT = getFunctionToReturn(iV == null ? "Open Table..." : iV);

                            int aW = Integer.parseInt(args[point++]);
                            int aH = Integer.parseInt(args[point++]);
                            int aI = Integer.parseInt(args[point++]);
                            int[] defVals = new int[aI];
                            ITableCellEditor tcf = new DefaultTableCellEditor();
                            if (hasDefault)
                                for (int i = 0; i < defVals.length; i++)
                                    defVals[i] = Integer.parseInt(args[point++]);
                            // Flags which are marked with "." are hidden. Starts with 1, then 2, then 4...
                            if (hasFlags) {
                                LinkedList<String> flags = new LinkedList<String>();
                                while (point < args.length)
                                    flags.add(args[point++]);
                                tcf = new BitfieldTableCellEditor(flags.toArray(new String[0]));
                            }
                            if (eText.equals("tableSTA"))
                                return new SubwindowSchemaElement(new TilesetAllocTableSchemaElement(tilesetAllocations, iV, wV, hV, aW, aH, aI, tcf, defVals), iVT);
                            if (eText.equals("tableTS"))
                                return new SubwindowSchemaElement(new TilesetTableSchemaElement(iV, wV, hV, aW, aH, aI, tcf, defVals), iVT);
                            if (eText.equals("table"))
                                return new SubwindowSchemaElement(new RubyTableSchemaElement(iV, wV, hV, aW, aH, aI, tcf, defVals), iVT);
                            throw new RuntimeException("Unknown table type " + text);
                        }
                        if (text.equals("CTNative"))
                            return new CTNativeSchemaElement(args[point++]);
                        if (text.equals("arrayCS")) {
                            String a = args[point++];
                            SchemaElement ise = get();
                            return new EventCommandArraySchemaElement(ise, getCMDB(a));
                        }
                        // -- If all else fails, it's an ID to be looked up. --
                        return getSDBEntry(text);
                    }
                }.get();
            }

            @Override
            public void execCmd(char c, final String[] args) throws IOException {
                if (c == 'a') {
                    if (!schemaDatabase.containsKey(args[0]))
                        throw new RuntimeException("Bad Schema Database: 'a' used to expect item " + args[0] + " that didn't exist.");
                } else if (c == ':') {
                    workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                    setSDBEntry(args[0], new ObjectClassSchemaElement(args[0], workingObj, 'o'));
                } else if (c == '.') {
                    workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                    setSDBEntry(args[0], workingObj);
                } else if (c == '@') {
                    workingObj.aggregate.add(new IVarSchemaElement("@" + args[0], handleChain(args, 1), false));
                } else if (c == '+') {
                    workingObj.aggregate.add(handleChain(args, 0));
                } else if (c == '>') {
                    setSDBEntry(args[0], handleChain(args, 1));
                } else if (c == 'e') {
                    HashMap<Integer, String> options = new HashMap<Integer, String>();
                    for (int i = 1; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        options.put(k, args[i + 1]);
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, "Integer");
                    setSDBEntry(args[0], e);
                } else if (c == 's') {
                    // Symbols
                    String[] syms = new String[args.length - 1];
                    System.arraycopy(args, 1, syms, 0, syms.length);
                    setSDBEntry(args[0], new SymEnumSchemaElement(syms));
                } else if (c == 'E') {
                    HashMap<Integer, String> options = new HashMap<Integer, String>();
                    for (int i = 2; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        options.put(k, args[i + 1]);
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, args[1]);
                    setSDBEntry(args[0], e);
                } else if (c == 'M') {
                    mergeRunnables.add(new Runnable() {
                        @Override
                        public void run() {
                            // Proxies are bad for this.
                            EnumSchemaElement mergeA = (EnumSchemaElement) schemaTrueDatabase.get(args[0]);
                            EnumSchemaElement mergeB = (EnumSchemaElement) schemaTrueDatabase.get(args[1]);
                            HashMap<Integer, String> finalMap = new HashMap<Integer, String>();
                            finalMap.putAll(mergeA.options);
                            finalMap.putAll(mergeB.options);
                            SchemaElement ise = new EnumSchemaElement(finalMap, mergeB.buttonText);
                            AppMain.schemas.setSDBEntry(args[2], ise);
                        }
                    });
                } else if (c == ']') {
                    workingObj.aggregate.add(new ArrayElementSchemaElement(Integer.parseInt(args[0]), args[1], handleChain(args, 2), null));
                } else if (c == 'i') {
                    readFile(args[0]);
                } else if (c == 'D') {
                    String root = PathSyntax.breakToken(args[1]);
                    final String remainder = args[1].substring(root.length());
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], root, new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            return PathSyntax.parse(rubyIO, remainder);
                        }
                    }, args[2].equals("1"), args[3]));
                } else if (c == 'd') {
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], args[1], new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            for (int i = 2; i < args.length; i++)
                                rubyIO = rubyIO.getInstVarBySymbol(args[i]);
                            return rubyIO;
                        }
                    }, false, null));
                } else if (c == 'A') {
                    // This is needed so the engine actually understands which autotiles map to what
                    int p = 0;
                    AppMain.autoTiles = new ATDB[args.length / 2];
                    for (int i = 0; i < args.length; i += 2) {
                        AppMain.autoTiles[p] = new ATDB(args[i]);
                        // This is needed to make actual autotile *placement* work.
                        // In theory, it's independent of the AutoTiles setup,
                        //  so long as the AutoTiles setup's using the same sprite-sheets.
                        // In practice, it's only been tested with the default AutoTiles.txt setup.
                        if (!args[i + 1].equals("."))
                            AppMain.autoTiles[p].calculateInverseMap(args[i + 1]);
                        p++;
                    }
                } else if (c == 'C') {
                    if (args[0].equals("allowIndentControl"))
                        allowControlOfEventCommandIndent = true;
                    if (args[0].equals("defineIndent")) {
                        if (allowControlOfEventCommandIndent) {
                            schemaDatabase.put("indent", new ROIntegerSchemaElement(0));
                        } else {
                            schemaDatabase.put("indent", new IntegerSchemaElement(0));
                        }
                        schemaTrueDatabase.put("indent", schemaDatabase.get("indent"));
                    }
                    if (args[0].equals("objectDB"))
                        AppMain.odbBackend = args[1];
                    if (args[0].equals("dataPath"))
                        AppMain.dataPath = args[1];
                    if (args[0].equals("dataExt"))
                        AppMain.dataExt = args[1];
                    if (args[0].equals("versionId"))
                        AppMain.sysBackend = args[1];
                    if (args[0].equals("defaultCB")) {
                        workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                        commandBufferSchemas.put(-1, workingObj);
                    }
                    if (args[0].equals("toWinAGameThatHasNoEnd")) {
                        // Really special schema
                        workingObj.aggregate.add(new RMAnimSchemaElement(args[1], args[2], Integer.parseInt(args[3])));
                    }
                    if (args[0].equals("burnYourselfOutDoYourselfIn")) {
                        // Really special schema
                        workingObj.aggregate.add(new R2kSystemDefaultsInstallerSchemaElement());
                    }
                    if (args[0].equals("name")) {
                        final LinkedList<String> arguments = new LinkedList<String>();
                        String text = "";
                        boolean nextState = false;
                        for (int i = 2; i < args.length; i++) {
                            if (nextState) {
                                if (text.length() > 0)
                                    text += " ";
                                text += args[i];
                            } else {
                                if (!args[i].equals("|")) {
                                    arguments.add(args[i]);
                                } else {
                                    nextState = true;
                                }
                            }
                        }
                        final String textF = text;

                        nameDB.put(args[1], new IFunction<RubyIO, String>() {
                            @Override
                            public String apply(RubyIO rubyIO) {
                                LinkedList<RubyIO> parameters = new LinkedList<RubyIO>();
                                for (String arg : arguments) {
                                    RubyIO res = PathSyntax.parse(rubyIO, arg);
                                    if (res == null)
                                        break;
                                    parameters.add(res);
                                }
                                return RPGCommand.formatNameExtended(textF, rubyIO, parameters.toArray(new RubyIO[0]), null);
                            }
                        });
                    }
                } else if (c != ' ') {
                    for (String arg : args)
                        System.err.print(arg + " ");
                    System.err.println("(The command " + c + " in the SDB is not supported.)");
                }
            }
        });
    }

    public CMDB getCMDB(String arg) {
        CMDB cm = cmdbs.get(arg);
        if (cm != null)
            return cm;
        CMDB r = new CMDB(arg);
        cmdbs.put(arg, r);
        return r;
    }

    public void confirmAllExpectationsMet() {
        if (remainingExpected.size() > 0)
            throw new RuntimeException("Remaining expectation " + remainingExpected.getFirst());
    }

    public boolean hasSDBEntry(String text) {
        return schemaDatabase.containsKey(text);
    }

    public void setSDBEntry(final String text, SchemaElement ise) {
        remainingExpected.remove(text);
        // If a placeholder exists, keep using that
        if (!schemaDatabase.containsKey(text))
            schemaDatabase.put(text, ise);
        schemaTrueDatabase.put(text, ise);
    }

    public SchemaElement getSDBEntry(final String text) {
        if (schemaDatabase.containsKey(text))
            return schemaDatabase.get(text);
        // Notably, the proxy is put in the database so the expectation is only added once.
        remainingExpected.add(text);
        SchemaElement ise = new ProxySchemaElement(text);
        schemaDatabase.put(text, ise);
        return ise;
    }

    public LinkedList<String> listFileDefs() {
        LinkedList<String> fd = new LinkedList<String>();
        for (String s : schemaDatabase.keySet())
            if (s.startsWith("File."))
                fd.add(s.substring(5));
        return fd;
    }

    public void startupSanitizeDictionaries() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.sanitize();
        for (Runnable merge : mergeRunnables)
            merge.run();
    }

    public void updateDictionaries(RubyIO map) {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.actIfRequired(map);
        for (Runnable merge : mergeRunnables)
            merge.run();
    }

    public void kickAllDictionariesForMapChange() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.run();
    }
}
