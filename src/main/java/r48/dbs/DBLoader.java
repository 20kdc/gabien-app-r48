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
        while (br.ready()) {
            String l = br.readLine();
            if (l.length() > 0) {
                char cmd = l.charAt(0);
                String[] ll = l.substring(1).trim().split(" ");
                if (cmd >= '0')
                    if (cmd <= '9') {
                        String[] ll2 = l.split(":");
                        db.newObj(Integer.parseInt(ll2[0]), ll2[1].trim());
                        continue;
                    }
                db.execCmd(cmd, ll);
            }
        }
    }
}
