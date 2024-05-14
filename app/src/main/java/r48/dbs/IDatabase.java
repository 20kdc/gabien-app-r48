/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import java.io.IOException;
import java.util.List;

import gabien.datum.DatumDecToLambdaVisitor;
import gabien.datum.DatumSrcLoc;
import r48.minivm.MVMU;

/**
 * Called upon by DBLoader to actually implement the database.
 * Created on 12/30/16.
 */
public interface IDatabase extends DatumDecToLambdaVisitor.Handler {
    @SuppressWarnings("unchecked")
    @Override
    default void handle(Object value, DatumSrcLoc srcLoc) {
        try {
            receiveLine((List<Object>) value, srcLoc);
        } catch (Exception e) {
            throw new RuntimeException("@ " + srcLoc, e);
        }
    }

    default void receiveLine(List<Object> args, DatumSrcLoc sl) throws IOException {
        String ctl = MVMU.coerceToString(args.get(0));
        if (ctl.equals("obj")) {
            if (args.size() != 3)
                throw new RuntimeException("args must be 3 long for obj");
            newObj(Integer.parseInt(MVMU.coerceToString(args.get(1))), MVMU.coerceToString(args.get(2)));
        } else {
            if (ctl.length() != 1)
                throw new RuntimeException("nope");
            String[] args2 = new String[args.size() - 1];
            for (int i = 0; i < args2.length; i++)
                args2[i] = MVMU.coerceToString(args.get(i + 1));
            execCmd(ctl.charAt(0), args2);
        }
    }

    void newObj(int objId, String objName) throws IOException;

    default void updateSrcLoc(DatumSrcLoc sl) {
        
    }

    void execCmd(char c, String[] args) throws IOException;

    default void comment(String string) {
    }
}
