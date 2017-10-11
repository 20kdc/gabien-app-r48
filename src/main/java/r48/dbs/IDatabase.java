/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import java.io.IOException;

/**
 * Called upon by DBLoader to actually implement the database.
 * Created on 12/30/16.
 */
public interface IDatabase {
    void newObj(int objId, String objName) throws IOException;

    void execCmd(char c, String[] args) throws IOException;
}
