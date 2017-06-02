/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Handles the basic database syntax.
 * Created on 12/30/16.
 */
public class DBLoader {
    public DBLoader(BufferedReader br, IDatabase db) throws IOException {
        int ln = 1;
        try {
            while (br.ready()) {
                String l = br.readLine();
                if (l.length() > 0) {
                    char cmd = l.charAt(0);
                    String[] ll = l.substring(1).trim().split(" ");
                    if (cmd >= '0')
                        if (cmd <= '9') {
                            int a = l.indexOf(':');
                            if (a == -1)
                                throw new RuntimeException("Bad DB entry");
                            db.newObj(Integer.parseInt(l.substring(0, a)), l.substring(a + 1).trim());
                            continue;
                        }
                    db.execCmd(cmd, ll);
                }
                ln++;
            }
        } catch (RuntimeException re) {
            throw new RuntimeException("at line " + ln, re);
        }
    }
}
