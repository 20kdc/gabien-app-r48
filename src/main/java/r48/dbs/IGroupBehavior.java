/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.dbs;

import r48.RubyIO;
import r48.schema.util.SchemaPath;

import java.util.LinkedList;

/**
 * Created on 14/06/17.
 */
public interface IGroupBehavior {
    // Note that this returning true indicates if modifications occurred.
    boolean correctElement(LinkedList<RubyIO> array, int commandIndex, RubyIO command);
}
