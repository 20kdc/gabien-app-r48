/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import java.util.LinkedList;

import gabien.datum.DatumSrcLoc;
import r48.app.AppCore;

/**
 * Autotile Database, used to deal with those pesky AutoTile issues
 * <p/>
 * corners are:
 * 01
 * 23
 * <p/>
 * Created on 12/28/16.
 */
public class ATDB extends AppCore.Csv {
    public final String loadFile;
    // 50 is the biggest seen so far.
    public Autotile[] entries = new Autotile[50];
    public int[] inverseMap = new int[256];
    // objId is the first 3 digits, then bit ID is the 4th
    private boolean[] rulesEngineMustTrue = new boolean[10000];
    private boolean[] rulesEngineMustFalse = new boolean[10000];

    public String[] wordMap = {
            "A",
            "_",
            "B",

            "UL",
            "U",
            "UR",

            "L",
            "C",
            "R",

            "LL",
            "D",
            "LR"
    };

    public int nameFromWord(String w) {
        for (int i = 0; i < wordMap.length; i++)
            if (wordMap[i].equals(w))
                return i;
        return 0;
    }

    public ATDB(AppCore app, String file) {
        super(app);
        loadFile = file;
        DBLoader.readFile(app, file, new IDatabase() {
            Autotile current = null;
            // for 'x'-type one-line-space-delimited entries
            int autoIncrementingId = 0;

            @Override
            public void newObj(int objId, String objName, DatumSrcLoc sl) {
                current = new Autotile();
                current.name = objName;
                entries[objId] = current;
            }

            @Override
            public void execCmd(String cmd, String[] args, Object[] argsObj, DatumSrcLoc sl) {
                // Import a new word-map.
                if (cmd.equals("w")) {
                    wordMap = args;
                } else if (cmd.equals("x")) {
                    // "x" format for wall ATs (used to help import Ancurio's table)
                    current = new Autotile();
                    current.name = "X" + autoIncrementingId;
                    entries[autoIncrementingId++] = current;
                    int[] cornerMapping = new int[] {
                            0, 2, 3, 5, 9, 11
                    };
                    for (int i = 0; i < 4; i++)
                        current.corners[i] = cornerMapping[Integer.parseInt(args[i])];
                } else if (cmd.equals("d")) {
                    // "Standard" format for RXP
                    current.corners[0] = nameFromWord(args[0]);
                    current.corners[1] = nameFromWord(args[1]);
                } else if (cmd.equals("D")) {
                    current.corners[2] = nameFromWord(args[0]);
                    current.corners[3] = nameFromWord(args[1]);
                } else {
                    throw new RuntimeException("Unknown cmd: " + cmd);
                }
            }
        });
    }

    public void calculateInverseMap(String file) {
        if (file.equals("$WallATs$")) {
            // This is wrong, but it works okayish.
            // I haven't been able to get full information on this.
            for (int i = 0; i < 256; i++) {
                // Tried and failed to get this consistent with the image Ozzy gave me for:
                //  ###
                // ###
                // #
                // ##
                // I'm not sure there is any consistent logic in the actual output, so I'm using my own logic
                // boolean ul = (i & 1) != 0;
                boolean um = (i & 2) != 0;
                // boolean ur = (i & 4) != 0;
                boolean ml = (i & 8) != 0;
                boolean mr = (i & 16) != 0;
                // boolean ll = (i & 32) != 0;
                boolean lm = (i & 64) != 0;
                // boolean lr = (i & 128) != 0;
                int p = 0;
                p |= (!ml) ? 1 : 0;
                p |= (!um) ? 2 : 0;
                p |= (!mr) ? 4 : 0;
                p |= (!lm) ? 8 : 0;
                inverseMap[i] = p;
            }
            return;
        }
        calculateInverseMapRulesEngine(file);
    }

    public void calculateInverseMapRulesEngine(String file) {
        final LinkedList<Integer> avoidThese = new LinkedList<Integer>();
        DBLoader.readFile(app, file, new IDatabase() {
            @Override
            public void newObj(int objId, String objName, DatumSrcLoc sl) {
                boolean[] mustTrue = new boolean[8];
                boolean[] mustFalse = new boolean[8];
                String trueThings = objName.split("T")[1];
                String falseThings = "";
                String[] falseA = trueThings.split("F");
                trueThings = falseA[0];
                if (falseA.length != 1)
                    falseThings = falseA[1];
                for (char c : trueThings.toCharArray())
                    mustTrue[c - '0'] = true;
                for (char c : falseThings.toCharArray())
                    mustFalse[c - '0'] = true;
                int base = objId * 10;
                for (int i = 0; i < 8; i++) {
                    rulesEngineMustTrue[base + i] = mustTrue[i];
                    rulesEngineMustFalse[base + i] = mustFalse[i];
                }
            }

            @Override
            public void execCmd(String c, String[] args, Object[] argsObj, DatumSrcLoc sl) {
                if (c.equals("C")) {
                    if (args[0].equals("disable"))
                        for (int i = 1; i < args.length; i++)
                            avoidThese.add(Integer.parseInt(args[i]));
                } else {
                    throw new RuntimeException("unknown command: " + c);
                }
            }
        });
        LinkedList<String> issues = new LinkedList<String>();
        for (int i = 0; i < 256; i++) {
            boolean[] area = new boolean[8];
            area[0] = (i & 1) != 0;
            area[1] = (i & 2) != 0;
            area[2] = (i & 4) != 0;
            area[3] = (i & 8) != 0;
            area[4] = (i & 16) != 0;
            area[5] = (i & 32) != 0;
            area[6] = (i & 64) != 0;
            area[7] = (i & 128) != 0;
            inverseMap[i] = getMostSuitableAutotile(area, avoidThese, issues);
        }
        if (issues.size() > 0) {
            for (String s : issues)
                System.out.println("ATR: " + s);
            System.out.println("     NNNWESSS");
            System.out.println("     W E  W E");
            System.out.println("There are " + issues.size() + " situations in which placing " + loadFile + " ATs leads to an ambiguous result.");
            System.out.println("(In the default RXP dataset, 16 situations is normal, and the default value of AT47 will work.)");
        }
    }

    private int getMostSuitableAutotile(boolean[] area, LinkedList<Integer> avoidThese, LinkedList<String> issues) {
        int bestAT = 0;
        int bestATReq = 0;
        boolean multiplePossible = true;
        StringBuilder issueResolution = new StringBuilder(" NONE");
        for (int i = 0; i < entries.length; i++) {
            if (avoidThese.indexOf(i) != -1)
                continue;
            if (entries[i] != null) {
                int reqCount = 0;
                boolean fail = false;
                for (int j = 0; j < 4; j++) {
                    boolean[] mustTrue = new boolean[8];
                    boolean[] mustFalse = new boolean[8];
                    setMasksForCorner(mustTrue, mustFalse, entries[i].corners[j], j);
                    for (int k = 0; k < 8; k++) {
                        if (mustTrue[k]) {
                            reqCount++;
                            if (!area[k]) {
                                fail = true;
                                break;
                            }
                        }
                        if (mustFalse[k]) {
                            reqCount++;
                            if (area[k]) {
                                fail = true;
                                break;
                            }
                        }
                    }
                    if (fail)
                        break;
                }
                if (fail)
                    continue;
                if (reqCount > bestATReq) {
                    bestAT = i;
                    bestATReq = reqCount;
                    issueResolution = new StringBuilder(" " + Integer.toString(i));
                    multiplePossible = false;
                } else if (reqCount == bestATReq) {
                    issueResolution.append(' ');
                    issueResolution.append(Integer.toString(i));
                    multiplePossible = true;
                }
            }
        }
        if (multiplePossible) {
            // Alert the user to the ambiguous result but still return first as best.
            if (issues != null) {
                String nano = "";
                for (int j = 0; j < area.length; j++)
                    nano += area[j] ? "1" : "0";
                issues.add(nano + issueResolution.toString());
            }
            return bestAT;
        }
        return bestAT;
    }

    private void setMasksForCorner(boolean[] mustTrue, boolean[] mustFalse, int corner, int ci) {
        int base = ((corner * 10) + ci) * 10;
        for (int i = 0; i < 8; i++) {
            mustTrue[i] |= rulesEngineMustTrue[base + i];
            mustFalse[i] |= rulesEngineMustFalse[base + i];
        }
    }

    public class Autotile {
        // AB
        // CD
        // 012
        // 345
        // 678
        // 901
        public int[] corners = new int[4];
        public String name;
    }
}
