/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.dbs;

import gabien.ui.UIPanel;
import r48.RubyIO;
import r48.schema.SchemaElement;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * Used in RPGCommand arrays. Tagged onto potential first elements of a group.
 * IGroupEditor is the part used in ArraySchemaElement (in order to keep the implementation simple)
 * Created on 6/28/17.
 */
public interface IGroupBehavior {
    interface IGroupEditor {
        UIPanel getEditor(RubyIO array, SchemaPath arrayPath);
        int getLength();
    }
    // If this returns null, then stuff acts "as per default".
    IGroupEditor getGroupEditor(RubyIO[] array, int index);
    // Note that this returning true indicates if modifications occurred.
    // This can add/remove elements on the array.
    boolean correctElement(LinkedList<RubyIO> array, int commandIndex, RubyIO command);
}
