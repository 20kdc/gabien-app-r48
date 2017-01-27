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
import r48.UIMapInfos;
import r48.map.StuffRenderer;
import r48.schema.specialized.*;
import r48.RubyIO;
import r48.schema.*;
import r48.schema.displays.EPGDisplaySchemaElement;

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
    private HashMap<String, ISchemaElement> schemaDatabase = new HashMap<String, ISchemaElement>();
    protected HashMap<String, ISchemaElement> schemaTrueDatabase = new HashMap<String, ISchemaElement>();
    private LinkedList<DictionaryUpdaterRunnable> dictionaryUpdaterRunnables = new LinkedList<DictionaryUpdaterRunnable>();
    private LinkedList<String> remainingExpected = new LinkedList<String>();
    private HashMap<String, CMDB> cmdbs = new HashMap<String, CMDB>();

    public SDB() {
        schemaDatabase.put("nil", new OpaqueSchemaElement());
        schemaDatabase.put("int", new IntegerSchemaElement(0));
        schemaDatabase.put("index", new AMAISchemaElement());
        schemaDatabase.put("string", new StringSchemaElement(""));
        schemaDatabase.put("boolean", new BooleanSchemaElement(false));
        schemaDatabase.put("booleanDefTrue", new BooleanSchemaElement(true));
        schemaDatabase.put("int_boolean", new IntBooleanSchemaElement(false));
        schemaDatabase.put("int_booleanDefTrue", new IntBooleanSchemaElement(true));

        schemaDatabase.put("zlibBlobEditor", new ZLibBlobSchemaElement());

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
        schemaDatabase.put("OPAQUE", new OpaqueSchemaElement());
        new DBLoader(bufferedReader, new IDatabase() {
            AggregateSchemaElement workingObj;
            @Override
            public void newObj(int objId, String objName) {

            }

            public ISchemaElement handleChain(final String[] args, final int start) {
                return new ISupplier<ISchemaElement>() {
                    // This function is recursive but needs state to be kept around after exit.
                    // Kind of a pain, *unless* you have a surrounding instance.
                    public int point = start;
                    @Override
                    public ISchemaElement get() {
                        final String text = args[point++];
                        if (text.equals("roint=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new ROIntegerSchemaElement(n);
                        }
                        if (text.equals("int=")) {
                            int n = Integer.parseInt(args[point++]);
                            return new IntegerSchemaElement(n);
                        }
                        if (text.equals("string="))
                            return new StringSchemaElement(args[point++]);

                        // CS means "control indent if allowed"
                        // MS means "never control indent"
                        if (text.equals("RPGCS")) {
                            final CMDB database = getCMDB(args[point++]);
                            ISchemaElement a = get();
                            return new SubwindowSchemaElement(new RPGCommandSchemaElement(a, get(), database, allowControlOfEventCommandIndent), new IFunction<RubyIO, String>() {
                                @Override
                                public String apply(RubyIO rubyIO) {
                                    return database.buildCodename(rubyIO);
                                }
                            });
                        }
                        if (text.equals("RPGMS")) {
                            final CMDB database = getCMDB(args[point++]);
                            ISchemaElement a = get();
                            return new SubwindowSchemaElement(new RPGCommandSchemaElement(a, get(), database, false), new IFunction<RubyIO, String>() {
                                @Override
                                public String apply(RubyIO rubyIO) {
                                    return database.buildCodename(rubyIO);
                                }
                            });
                        }

                        if (text.equals("array")) {
                            int n = Integer.parseInt(args[point++]);
                            return new ArraySchemaElement(get(), n, false);
                        }
                        if (text.equals("table") || text.equals("tableTS")) {
                            String iV = args[point++];
                            String wV = args[point++];
                            if (wV.equals("."))
                                wV = null;
                            String hV = args[point++];
                            if (hV.equals("."))
                                hV = null;
                            int aW = Integer.parseInt(args[point++]);
                            int aH = Integer.parseInt(args[point++]);
                            int aI = Integer.parseInt(args[point++]);
                            if (text.equals("tableTS"))
                                return new SubwindowSchemaElement(new TilesetTableSchemaElement(iV, wV, hV, aW, aH, aI), getFunctionToReturn(iV));
                            return new SubwindowSchemaElement(new RubyTableSchemaElement(iV, wV, hV, aW, aH, aI), getFunctionToReturn(iV));
                        }
                        if (text.equals("CTNative"))
                            return new CTNativeSchemaElement(args[point++]);
                        if (text.equals("arrayCS")) {
                            String a = args[point++];
                            ISchemaElement ise = get();
                            return new EventCommandArraySchemaElement(ise, getCMDB(a));
                        }
                        if (text.equals("arrayAL1"))
                            return new ArraySchemaElement(get(), 0, true);
                        if (text.equals("arrayDAM")) {
                            int disambiguatorIndex = Integer.parseInt(args[point++]);
                            ISchemaElement disambiguatorType = get();
                            ISchemaElement backup = get();
                            HashMap<Integer, ISchemaElement> disambiguations = new HashMap<Integer, ISchemaElement>();
                            while (point < args.length) {
                                int ind = Integer.parseInt(args[point++]);
                                disambiguations.put(ind, get());
                            }
                            return new ArrayDisambiguatorSchemaElement(disambiguatorIndex, disambiguatorType, backup, disambiguations);
                        }
                        if (text.equals("hash")) {
                            ISchemaElement k = get();
                            return new HashSchemaElement(k, get());
                        }
                        if (text.equals("subwindow"))
                            return new SubwindowSchemaElement(get());
                        if (text.equals("{")) {
                            // Aggregate
                            AggregateSchemaElement subag = new AggregateSchemaElement(new ISchemaElement[] {});
                            ISchemaElement ise = get();
                            while (ise != null) {
                                subag.aggregate.add(ise);
                                ise = get();
                            }
                            return subag;
                        }

                        if (text.equals("typeChanger{")) {
                            // Type Changer
                            LinkedList<String> strs = new LinkedList<String>();
                            LinkedList<ISchemaElement> scms = new LinkedList<ISchemaElement>();
                            while (true) {
                                ISchemaElement a = get();
                                if (a == null)
                                    break;
                                String b = args[point++];
                                strs.add(b);
                                scms.add(a);
                            }
                            TypeChangerSchemaElement subag = new TypeChangerSchemaElement(strs.toArray(new String[0]), scms.toArray(new ISchemaElement[0]));
                            return subag;
                        }

                        if (text.equals("}"))
                            return null;
                        // -- If all else fails, it's an ID to be looked up. --
                        return getSDBEntry(text);
                    }
                }.get();
            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == 'a')
                    if (!schemaDatabase.containsKey(args[0]))
                        throw new RuntimeException("Bad Schema Database: 'a' used to expect item " + args[0] + " that didn't exist.");
                if (c == ':') {
                    workingObj = new AggregateSchemaElement(new ISchemaElement[]{});
                    setSDBEntry(args[0], new ObjectClassSchemaElement(args[0], workingObj, 'o'));
                }
                if (c == '.') {
                    workingObj = new AggregateSchemaElement(new ISchemaElement[]{});
                    setSDBEntry(args[0], workingObj);
                }
                if (c == '@')
                    workingObj.aggregate.add(new IVarSchemaElement("@" + args[0], handleChain(args, 1)));
                if (c == '+')
                    workingObj.aggregate.add(handleChain(args, 0));
                if (c == '>')
                    setSDBEntry(args[0], handleChain(args, 1));
                if (c == 'e') {
                    HashMap<String, Integer> options = new HashMap<String, Integer>();
                    for (int i = 1; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        options.put(k + ":" + args[i + 1], k);
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, "Integer");
                    setSDBEntry(args[0], e);
                }
                if (c == 'E') {
                    HashMap<String, Integer> options = new HashMap<String, Integer>();
                    for (int i = 2; i < args.length; i += 2) {
                        int k = Integer.parseInt(args[i]);
                        options.put(k + ":" + args[i + 1], k);
                    }
                    EnumSchemaElement e = new EnumSchemaElement(options, args[1]);
                    setSDBEntry(args[0], e);
                }
                if (c == ']')
                    workingObj.aggregate.add(new ArrayElementSchemaElement(Integer.parseInt(args[0]), args[1], handleChain(args, 2)));
                if (c == 'i') {
                    try {
                        readFile(new BufferedReader(new InputStreamReader(GaBIEn.getFile(args[0]))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (c == 'D')
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], args[1], null, args[2].equals("1"), args[3]));
                if (c == 'd') {
                    final String a2 = args[2];
                    dictionaryUpdaterRunnables.add(new DictionaryUpdaterRunnable(args[0], args[1], new IFunction<RubyIO, RubyIO>() {
                        @Override
                        public RubyIO apply(RubyIO rubyIO) {
                            return rubyIO.getInstVarBySymbol(a2);
                        }
                    }, false, null));
                }
                if (c == 'A') {
                    // This is needed so the engine actually understands which autotiles map to what
                    InputStreamReader fr = new InputStreamReader(GaBIEn.getFile(args[0]));
                    AppMain.autoTiles = new ATDB(new BufferedReader(fr));
                    fr.close();
                    // This is needed to make actual autotile *placement* work.
                    // In theory, it's independent of the AutoTiles setup,
                    //  so long as the AutoTiles setup's using the same sprite-sheets.
                    // In practice, it's only been tested with the default AutoTiles.txt setup.
                    if (args.length > 1) {
                        fr = new InputStreamReader(GaBIEn.getFile(args[1]));
                        AppMain.autoTiles.calculateInverseMap(new BufferedReader(fr));
                        fr.close();
                    }
                }
                if (c == 'C') {
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
                    if (args[0].equals("mapinfosInert"))
                        UIMapInfos.mapSequenceInert = true;
                    if (args[0].equals("versionId"))
                        StuffRenderer.versionId = args[1];
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
    public void setSDBEntry(final String text, ISchemaElement ise) {
        remainingExpected.remove(text);
        // If a placeholder exists, keep using that
        if (!schemaDatabase.containsKey(text))
            schemaDatabase.put(text, ise);
        schemaTrueDatabase.put(text, ise);
    }
    public ISchemaElement getSDBEntry(final String text) {
        if (schemaDatabase.containsKey(text))
            return schemaDatabase.get(text);
        // Notably, the proxy is put in the database so the expectation is only added once.
        remainingExpected.add(text);
        ISchemaElement ise = new ProxySchemaElement(text);
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
