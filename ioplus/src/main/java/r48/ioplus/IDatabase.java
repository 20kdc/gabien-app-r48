/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.ioplus;

import java.io.IOException;
import java.util.List;

import datum.DatumTreeUtils;
import datum.DatumSrcLoc;
import r48.minivm.MVMU;

/**
 * Called upon by DBLoader to actually implement the database.
 * Created on 12/30/16.
 */
public interface IDatabase extends DatumTreeUtils.VisitorLambda {
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
        Object[] arga = new Object[args.size() - 1];
        for (int i = 0; i < arga.length; i++)
            arga[i] = args.get(i + 1);
        receiveCmd(ctl, arga, sl);
    }

    default void receiveCmd(String c, Object[] args, DatumSrcLoc sl) throws IOException {
        if (c.equals("obj")) {
            if (args.length != 2)
                throw new RuntimeException("args must be 3 long for obj");
            newObj(MVMU.cInt(args[0]), MVMU.coerceToString(args[1]), sl);
        } else {
            String[] args2 = new String[args.length];
            for (int i = 0; i < args2.length; i++)
                args2[i] = MVMU.coerceToString(args[i]);
            execCmd(c, args2, args, sl);
        }
    }

    void newObj(int objId, String objName, DatumSrcLoc sl) throws IOException;

    void execCmd(String c, String[] args, Object[] argsObj, DatumSrcLoc sl) throws IOException;
}
