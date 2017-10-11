/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.tbleditors;

import gabien.ui.UIScrollLayout;
import r48.RubyIO;
import r48.ui.UIGrid;

/**
 * Created on 2/18/17.
 */

public interface ITableCellEditor {
    // Returns the on-selection-changed handler.
    public Runnable createEditor(final UIScrollLayout base, final RubyIO targV, final UIGrid uig, final Runnable changeOccurred);
}
