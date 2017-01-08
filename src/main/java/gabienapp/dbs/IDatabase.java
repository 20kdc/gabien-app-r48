/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.dbs;

import java.io.IOException;

/**
 * Called upon by DBLoader to actually implement the database.
 * Created on 12/30/16.
 */
public interface IDatabase {
    void newObj(int objId, String objName) throws IOException;
    void execCmd(char c, String[] args) throws IOException;
}
