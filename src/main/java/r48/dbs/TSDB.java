/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.dbs;

import java.io.IOException;

/**
 * Created on 04/06/17.
 */
public class TSDB {
    public int[] mapping;

    public TSDB(String arg) {
        DBLoader.readFile(arg, new IDatabase() {
            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == 't')
                    mapping = new int[Integer.parseInt(args[0])];
                if (c == 'i')
                    mapping[Integer.parseInt(args[0])] = Integer.parseInt(args[1]);
                if (c == 'r') {
                    int ofs1 = Integer.parseInt(args[0]);
                    int ofs2 = Integer.parseInt(args[1]);
                    int len = Integer.parseInt(args[2]);
                    for (int i = 0; i < len; i++)
                        mapping[ofs1 + i] = ofs2 + i;
                }
            }
        });
    }
}
