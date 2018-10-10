/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos2;

import java.util.LinkedList;

/**
 * Created on October 10, 2018.
 */
public class GP2File {
    public LinkedList<GP2CellType> allCellTypes = new LinkedList<GP2CellType>();
    public LinkedList<GP2Cell> allObjects = new LinkedList<GP2Cell>();
    // 0 to setLength - 1
    public int setLength = 0;
    // flashes/etc.
    // public LinkedList<GP2Action> allActions = new LinkedList<GP2Action>();
}
