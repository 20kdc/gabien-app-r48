/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.dbs;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created on 04/06/17.
 */
public class TSDB {

    public LinkedList<TSPicture> pictures = new LinkedList<TSPicture>();
    public int[] mapping;
    public int xorDoubleclick = 0;
    public boolean disableHex = false;

    public TSDB(String arg) {
        DBLoader.readFile(arg, new IDatabase() {
            @Override
            public void newObj(int objId, String objName) throws IOException {

            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == 'p')
                    pictures.add(new TSPicture(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8])));
                if (c == 'x')
                    xorDoubleclick = Integer.parseInt(args[0]);
                if (c == 'z')
                    disableHex = true;
                if (c == 't')
                    mapping = new int[Integer.parseInt(args[0])];
                if (c == 'i')
                    mapping[Integer.parseInt(args[0])] = Integer.parseInt(args[1]);
                if (c == '>')
                    DBLoader.readFile(args[0], this);
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

    public class TSPicture {
        public int flag, layertabIX, layertabIY, layertabAX, layertabAY, x, y, w, h;

        public TSPicture(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            flag = i;
            layertabIX = i1;
            layertabIY = i2;
            layertabAX = i3;
            layertabAY = i4;
            x = i5;
            y = i6;
            w = i7;
            h = i8;
        }
    }
}
