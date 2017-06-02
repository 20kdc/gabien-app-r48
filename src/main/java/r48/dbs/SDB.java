/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import gabien.GaBIEn;
import gabien.ui.IFunction;
import gabien.ui.ISupplier;
import r48.AppMain;
import r48.DictionaryUpdaterRunnable;
import r48.RubyIO;
import r48.map.StuffRenderer;
import r48.schema.*;
import r48.schema.arrays.OneIndexedArraySchemaElement;
import r48.schema.arrays.StandardArraySchemaElement;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.integers.IntBooleanSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.LowerBoundIntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.*;
import r48.schema.specialized.tbleditors.BitfieldTableCellEditor;
import r48.schema.specialized.tbleditors.DefaultTableCellEditor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;

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

    public void readFile(BufferedReader bufferedReader) throws IOException {
        new DBLoader(bufferedReader, new IDatabase() {
            AggregateSchemaElement workingObj;

            HashMap<Integer, String> commandBufferNames = new HashMap<Integer, String>();
            HashMap<Integer, SchemaElement> commandBufferSchemas = new HashMap<Integer, SchemaElement>();

            @Override
            public void newObj(int objId, String objName) {
                commandBufferNames.put(objId, objName);
                workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                commandBufferSchemas.put(objId, workingObj);
                //System.out.println("Array definition when inappropriate: " + objName);
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
                            return new OneIndexedArraySchemaElement(get(), 0);
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
                            return new SubwindowSchemaElement(get(), getFunctionToReturn(text2));
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
                            TypeChangerSchemaElement subag = new TypeChangerSchemaElement(strs.toArray(new String[0]), scms.toArray(new SchemaElement[0]));
                            return subag;
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
                        if (text.equals("table") || text.equals("tableF") || text.equals("tableTS") || text.equals("tableTSF")) {
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
                            if (text.equals("tableTSF")) {
                                // Flags which are marked with "." are hidden. Starts with 1, then 2, then 4...
                                LinkedList<String> flags = new LinkedList<String>();
                                while (point < args.length)
                                    flags.add(args[point++]);
                                return new SubwindowSchemaElement(new TilesetTableSchemaElement(iV, wV, hV, aW, aH, aI, new BitfieldTableCellEditor(flags.toArray(new String[0]))), iVT);
                            }
                            if (text.equals("tableTS"))
                                return new SubwindowSchemaElement(new TilesetTableSchemaElement(iV, wV, hV, aW, aH, aI, new DefaultTableCellEditor()), iVT);
                            if (text.equals("tableF")) {
                                // Flags which are marked with "." are hidden. Starts with 1, then 2, then 4...
                                LinkedList<String> flags = new LinkedList<String>();
                                while (point < args.length)
                                    flags.add(args[point++]);
                                return new SubwindowSchemaElement(new RubyTableSchemaElement(iV, wV, hV, aW, aH, aI, new BitfieldTableCellEditor(flags.toArray(new String[0]))), iVT);
                            }
                            return new SubwindowSchemaElement(new RubyTableSchemaElement(iV, wV, hV, aW, aH, aI, new DefaultTableCellEditor()), iVT);
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
                    for (int i = 0; i < syms.length; i++)
                        syms[i] = args[i + 1];
                    setSDBEntry(args[0], new SymEnumSchemaElement(syms));
                } else if (c == 'E') {
                    HashMap<Integer, String> options = new HashMap<Integer, String>();
                    for (int i = 2; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        options.put(k, args[i + 1]);
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, args[1]);
                    setSDBEntry(args[0], e);
                } else if (c == ']') {
                    workingObj.aggregate.add(new ArrayElementSchemaElement(Integer.parseInt(args[0]), args[1], handleChain(args, 2), null));
                } else if (c == 'i') {
                    try {
                        System.out.println(">>" + args[0]);
                        readFile(new BufferedReader(new InputStreamReader(GaBIEn.getFile(args[0]))));
                        System.out.println("<<" + args[0]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (c == 'D') {
                    final String[] split = args[1].split("@");
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], split[0], new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            for (int i = 1; i < split.length; i++)
                                rubyIO = rubyIO.getInstVarBySymbol("@" + split[i]);
                            return rubyIO;
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
                        InputStreamReader fr = new InputStreamReader(GaBIEn.getFile(args[i]));
                        AppMain.autoTiles[p] = new ATDB(args[i], new BufferedReader(fr));
                        fr.close();
                        // This is needed to make actual autotile *placement* work.
                        // In theory, it's independent of the AutoTiles setup,
                        //  so long as the AutoTiles setup's using the same sprite-sheets.
                        // In practice, it's only been tested with the default AutoTiles.txt setup.
                        if (!args[i + 1].equals(".")) {
                            fr = new InputStreamReader(GaBIEn.getFile(args[i + 1]));
                            AppMain.autoTiles[p].calculateInverseMap(new BufferedReader(fr));
                            fr.close();
                        }
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
                        StuffRenderer.versionId = args[1];
                    if (args[0].equals("defaultCB")) {
                        workingObj = new AggregateSchemaElement(new SchemaElement[] {});
                        commandBufferSchemas.put(-1, workingObj);
                    }
                    if (args[0].equals("toWinAGameThatHasNoEnd")) {
                        // Really special schema
                        workingObj.aggregate.add(new RMAnimSchemaElement(args[1], args[2], Integer.parseInt(args[3])));
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
                                    RubyIO res = rubyIO;
                                    String breakers = "$@]";
                                    String workingArg = arg;
                                    while (workingArg.length() > 0) {
                                        int plannedIdx = workingArg.length();
                                        for (char c : breakers.toCharArray()) {
                                            int idx = workingArg.indexOf(c, 1);
                                            if (idx >= 0)
                                                if (idx < plannedIdx)
                                                    plannedIdx = idx;
                                        }
                                        String subcom = workingArg.substring(1, plannedIdx);
                                        char f = workingArg.charAt(0);
                                        workingArg = workingArg.substring(plannedIdx);
                                        switch (f) {
                                            case '$':
                                                if (subcom.length() != 0)
                                                    throw new RuntimeException("unsure what to do here, $ doesn't accept additional");
                                                break;
                                            case '@':
                                                res = res.getInstVarBySymbol("@" + subcom);
                                                break;
                                            case ']':
                                                int atl = Integer.parseInt(subcom);
                                                if (atl < 0) {
                                                    res = null;
                                                    break;
                                                } else if (atl >= res.arrVal.length) {
                                                    res = null;
                                                    break;
                                                }
                                                res = res.arrVal[atl];
                                                break;
                                        }
                                        if (res == null)
                                            break; // Cannot go further.
                                    }
                                    parameters.add(res);
                                }
                                return RPGCommand.formatNameExtended(textF, rubyIO, parameters.toArray(new RubyIO[0]), null);
                            }
                        });
                    }
                } else if (c == ' ') {
                    // Comment
                } else {
                    for (int i = 0; i < args.length; i++)
                        System.err.print(args[i] + " ");
                    System.err.println("(The command " + c + " in the SDB is not supported.)");
                }
            }
        });
    }

    public CMDB getCMDB(String arg) {
        CMDB cm = cmdbs.get(arg);
        if (cm != null)
            return cm;
        try {
            InputStreamReader fr = new InputStreamReader(GaBIEn.getFile(arg));
            CMDB r = new CMDB(new BufferedReader(fr));
            cmdbs.put(arg, r);
            fr.close();
            return r;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

    public void updateDictionaries() {
        for (DictionaryUpdaterRunnable dur : dictionaryUpdaterRunnables)
            dur.actIfRequired();
    }
}
