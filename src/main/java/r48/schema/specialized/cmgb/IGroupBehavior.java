/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.cmgb;

import r48.RubyIO;

import java.util.LinkedList;

/**
 * Used in RPGCommand arrays. Tagged onto potential first elements of a group.
 * Created on 6/28/17.
 */
public interface IGroupBehavior {
    // The user-visible result of this is the commands one after the other, with a tag to not show indentation on the later commands.
    // If 0, then this group behavior is just for correction purposes.
    int getGroupLength(RubyIO[] arr, int ind);

    // Gets the correct code to be appendable to the end, if the group has a non-zero length.
    int getAdditionCode();

    // Note that this returning true indicates if modifications occurred.
    // This can add/remove elements on the array.
    boolean correctElement(LinkedList<RubyIO> array, int commandIndex, RubyIO command);
}
