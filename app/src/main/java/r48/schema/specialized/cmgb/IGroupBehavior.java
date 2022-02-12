/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.cmgb;

import r48.io.data.IRIO;
import r48.schema.SchemaElement;

/**
 * Used in RPGCommand arrays. Tagged onto potential first elements of a group.
 * Created on 6/28/17.
 */
public interface IGroupBehavior {
    // The user-visible result of this is the commands one after the other, with a tag to not show indentation on the later commands.
    // If 0, then this group behavior is just for correction purposes.
    int getGroupLength(IRIO arr, int ind);

    // Gets the correct code to be appendable to the end, if the group has a non-zero length.
    int getAdditionCode();

    // Note that this returning true indicates if modifications occurred.
    // This can add/remove elements on the array.
    boolean correctElement(IRIO array, int commandIndex, IRIO command);

    // This second pass is used by certain group-behaviors that *really, really* need accurate indent information to not cause damage.
    // Specifically consider this for behaviors which add/remove commands.
    boolean majorCorrectElement(IRIO arr, int i, IRIO commandTarg, SchemaElement baseElement);
}
