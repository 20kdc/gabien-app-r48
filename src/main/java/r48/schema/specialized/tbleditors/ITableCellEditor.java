/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.tbleditors;

import r48.RubyIO;
import r48.ui.UIGrid;
import r48.ui.UIScrollVertLayout;

/**
 * Created on 2/18/17.
 */

public interface ITableCellEditor {
    // Returns the on-selection-changed handler.
    public Runnable createEditor(final UIScrollVertLayout base, final RubyIO targV, final UIGrid uig, final Runnable changeOccurred);
}
