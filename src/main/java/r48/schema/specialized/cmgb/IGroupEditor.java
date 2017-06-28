/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.cmgb;

import gabien.ui.UIPanel;
import r48.RubyIO;
import r48.schema.util.SchemaPath;

/**
 * Created on 6/28/17.
 */
public interface IGroupEditor {
    UIPanel getEditor(RubyIO target, SchemaPath arrayPath);
    int getLength();
}
