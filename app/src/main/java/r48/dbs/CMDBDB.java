/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.dbs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import r48.R48;

/**
 * CMDB can't be pushed into AppCore so push it outwards instead
 */
public class CMDBDB extends R48.Svc {
    public CMDBDB(R48 app) {
        super(app);
    }

    protected HashMap<String, CMDB> cmdbs = new HashMap<>();
    private LinkedList<CMDB> cmdbEntries = new LinkedList<>();


    public void newCMDB(String a0) {
        if (cmdbs.containsKey(a0))
            throw new RuntimeException("Attempted to overwrite CMDB: " + a0);
        CMDB cm = new CMDB(this, a0);
        cmdbs.put(a0, cm);
        cmdbEntries.add(cm);
    }

    public CMDB getCMDB(String arg) {
        CMDB cm = cmdbs.get(arg);
        if (cm == null)
            throw new RuntimeException("Expected CMDB to exist (and it didn't): " + arg);
        return cm;
    }

    public void loadCMDB(String arg, String fn) {
        getCMDB(arg).load(fn);
    }

    /**
     * Added for liblcf#245, not really something app should use otherwise
     */
    public HashSet<String> getAllCMDBIDs() {
        return new HashSet<>(cmdbs.keySet());
    }

    /**
     * Gets all CMDBs in a consistent order.
     */
    public CMDB[] getAllCMDBs() {
        return cmdbEntries.toArray(new CMDB[0]);
    }

    public void confirmAllExpectationsMet() {
        for (CMDB cmdb : cmdbs.values())
            cmdb.check();
    }
}
