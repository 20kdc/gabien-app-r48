/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.tbleditors;

import java.util.LinkedList;

import gabien.ui.UIElement;

/**
 * Created on 2/18/17.
 */

public interface ITableCellEditor {
    // Appends the editing UI to a UIScrollLayout (assumed vertical).
    // Returns the handler for when the cell changes.
    public Runnable createEditor(LinkedList<UIElement> base, int[] planes, Runnable changeOccurred);
}
