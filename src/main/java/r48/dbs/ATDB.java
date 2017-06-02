/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Autotile Database, used to deal with those pesky AutoTile issues
 * <p/>
 * corners are:
 * 01
 * 23
 * <p/>
 * Created on 12/28/16.
 */
public class ATDB {
    public final String loadFile;
    // 50 is the biggest seen so far.
    public Autotile[] entries = new Autotile[50];
    public int[] inverseMap = new int[256];
    // objId is the first 3 digits, then bit ID is the 4th
    private boolean[] rulesEngineMustTrue = new boolean[10000];
    private boolean[] rulesEngineMustFalse = new boolean[10000];

    public int nameFromWord(String w) {
        if (w.equals("A"))
            return 0;
        if (w.equals("B"))
            return 2;

        if (w.equals("UL"))
            return 3;
        if (w.equals("U"))
            return 4;
        if (w.equals("UR"))
            return 5;


        if (w.equals("L"))
            return 6;
        if (w.equals("C"))
            return 7;
        if (w.equals("R"))
            return 8;

        if (w.equals("LL"))
            return 9;
        if (w.equals("D"))
            return 10;
        if (w.equals("LR"))
            return 11;
        return 0;
    }

    public ATDB(String name, BufferedReader br) throws IOException {
        loadFile = name;
        new DBLoader(br, new IDatabase() {
            Autotile current = null;
            // for 'x'-type one-line-space-delimited entries
            int autoIncrementingId = 0;

            @Override
            public void newObj(int objId, String objName) {
                current = new Autotile();
                current.name = objName;
                entries[objId] = current;
            }

            @Override
            public void execCmd(char cmd, String[] args) {
                if (cmd == 'x') {
                    current = new Autotile();
                    current.name = "X" + autoIncrementingId;
                    entries[autoIncrementingId++] = current;
                    int[] cornerMapping = new int[] {
                            0, 2, 3, 5, 9, 11
                    };
                    for (int i = 0; i < 4; i++)
                        current.corners[i] = cornerMapping[Integer.parseInt(args[i])];
                }
                if (cmd == 'd') {
                    current.corners[0] = nameFromWord(args[0]);
                    current.corners[1] = nameFromWord(args[1]);
                }
                if (cmd == 'D') {
                    current.corners[2] = nameFromWord(args[0]);
                    current.corners[3] = nameFromWord(args[1]);
                }
            }
        });
    }

    public void calculateInverseMap(BufferedReader br2) throws IOException {
        new DBLoader(br2, new IDatabase() {
            @Override
            public void newObj(int objId, String objName) {
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
            public void execCmd(char c, String[] args) {

            }
        });
        int issues = 0;
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
            inverseMap[i] = getMostSuitableAutotile(area);
            if (inverseMap[i] == -1) {
                String nano = "";
                for (int j = 0; j < area.length; j++) {
                    nano += area[j] ? "1" : "0";
                }
                System.out.println("ATR: " + nano);
                issues++;
                inverseMap[i] = 47;
            }
        }
        if (issues > 0) {
            System.out.println("     NNNWESSS");
            System.out.println("     W E  W E");
            System.out.println("There are " + issues + " situations in which placing " + loadFile + " ATs leads to an ambiguous result.");
            System.out.println("(In the default RXP dataset, 16 situations is normal, and the default value of AT47 will work.)");
        }
    }

    private int getMostSuitableAutotile(boolean[] area) {
        int bestAT = 47;
        int bestATReq = 0;
        boolean multiplePossible = true;
        for (int i = 0; i < 48; i++) {
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
                    multiplePossible = false;
                } else if (reqCount == bestATReq) {
                    multiplePossible = true;
                }
            }
        }
        if (multiplePossible) {
            // It seems 47 is actually the correct response in some cases.
            /*
            String s = "";
            for (int k = 0; k < 8; k++)
                s += area[k] ? "1" : "0";
            System.out.println("Situation code " + s);*/
            return -1;
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
    // this is basically guesswork

    private void setMasksForCornerOLD(boolean[] mustTrue, boolean[] mustFalse, int corner, int ci) {
        switch (corner) {
            case 0:
                switch (ci) {
                    case 0:
                        mustFalse[1] = true;
                        mustFalse[3] = true;
                        break;
                    case 1:
                        mustFalse[1] = true;
                        mustFalse[4] = true;
                        break;
                    case 2:
                        mustFalse[3] = true;
                        mustFalse[6] = true;
                        break;
                    case 3:
                        mustFalse[4] = true;
                        mustFalse[6] = true;
                        break;
                }
                break;
            case 2:
                switch (ci) {
                    case 0:
                        mustTrue[1] = true;
                        mustFalse[0] = true;
                        mustTrue[3] = true;
                        break;
                    case 1:
                        mustTrue[1] = true;
                        mustFalse[2] = true;
                        mustTrue[4] = true;
                        break;
                    case 2:
                        mustTrue[3] = true;
                        mustFalse[5] = true;
                        mustTrue[6] = true;
                        break;
                    case 3:
                        mustTrue[4] = true;
                        mustFalse[7] = true;
                        mustTrue[6] = true;
                        break;
                }
                break;
            case 7:
                switch (ci) {
                    case 0:
                        mustTrue[1] = true;
                        mustTrue[0] = true;
                        mustTrue[3] = true;
                        break;
                    case 1:
                        mustTrue[1] = true;
                        mustTrue[2] = true;
                        mustTrue[4] = true;
                        break;
                    case 2:
                        mustTrue[3] = true;
                        mustTrue[5] = true;
                        mustTrue[6] = true;
                        break;
                    case 3:
                        mustTrue[4] = true;
                        mustTrue[7] = true;
                        mustTrue[6] = true;
                        break;
                }
                break;

            // 012
            // 3 4
            // 567
            //Cr Cut.
            // 0.2
            // 345
            // 678
            // 9**
            // 01
            // 23
            case 3:
                if (ci == 0) {
                    mustFalse[1] = true;
                    mustFalse[3] = true;
                } else if (ci == 3) {
                    // using the UL tile on 3 means this is a full UL
                    mustTrue[4] = true;
                    mustTrue[7] = true;
                    mustTrue[6] = true;
                }
                break;
            case 4:
                if ((ci == 0) || (ci == 1)) {
                    mustFalse[1] = true;

                    mustTrue[3] |= ci == 0;
                    mustTrue[4] |= ci == 1;
                }
                break;
            case 5:
                if (ci == 1) {
                    mustFalse[1] = true;
                    mustFalse[4] = true;
                } else if (ci == 2) {
                    mustTrue[3] = true;
                    mustTrue[5] = true;
                    mustTrue[6] = true;
                }
                break;
            case 6:
                if ((ci == 0) || (ci == 2)) {
                    mustFalse[3] = true;

                    mustTrue[1] |= ci == 0;
                    mustTrue[6] |= ci == 2;
                }
                break;
            // 012
            // 3 4
            // 567
            //Cr Cut.
            // 0.2
            // 345
            // 678
            // 9**
            // 01
            // 23
            case 8:
                if ((ci == 1) || (ci == 3)) {
                    mustFalse[4] = true;

                    mustTrue[1] |= ci == 1;
                    mustTrue[6] |= ci == 3;
                }
                break;
            case 9:
                if (ci == 2) {
                    mustFalse[3] = true;
                    mustFalse[6] = true;
                } else if (ci == 1) {
                    mustTrue[1] = true;
                    mustTrue[2] = true;
                    mustTrue[4] = true;
                }
                break;
            case 10:
                if ((ci == 2) || (ci == 3)) {
                    mustFalse[6] = true;

                    mustTrue[3] |= ci == 2;
                    mustTrue[4] |= ci == 3;
                }
                break;
            case 11:
                if (ci == 3) {
                    mustFalse[4] = true;
                    mustFalse[6] = true;
                } else if (ci == 0) {
                    mustTrue[3] = true;
                    mustTrue[0] = true;
                    mustTrue[1] = true;
                }
                break;
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
