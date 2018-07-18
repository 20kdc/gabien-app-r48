/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.arrays;

import gabien.ui.*;
import r48.RubyIO;

/**
 * Simplifies the code involved in an array UI by abstracting away the complicated permissions logic.
 * Note that selection & copy logic is the responsibility of the array interface (and thus optional!)
 * 25th October 2017.
 */
public interface IArrayInterface {

    /*
     * Create the interface.
     * Note that svl is dedicated to this interface - the only reason it's built like this is so SchemaPath won't be unnecessarily 'leaked' for scroll stuff.
     * state is tied to a unique SchemaElement held by the ArraySchemaElement for the purposes of holding extra state.
     * Also note that the positions are invalidated when any exec function is called.
     */
    void provideInterfaceFrom(UIScrollLayout svl, IFunction<String, IProperty> state, ISupplier<ArrayPosition[]> positions);

    interface IProperty extends ISupplier<Double>, IConsumer<Double> {

    }

    /**
     * A place in the array.
     * <p>
     * There are two "types" of ArrayPosition:
     * Those with core, and those without.
     * <p>
     * Those with core always have elements, and may have execDelete.
     * Those without don't have either.
     * <p>
     * Both of these types have text, and may have execInsert and execInsertCopiedArray (which are "together", can't have one without the other)
     * <p>
     * elements exists for copying & pasting arrays.
     * <p>
     * It's up to the interface to work out how to implement everything it wants to do with these primitives.
     */
    class ArrayPosition {
        public final String text;
        // These are only allowed for two purposes:
        // 1. Comparison (for group-deletion algorithm)
        // 2. Copying (for clipboard)
        public final RubyIO[] elements;
        public final Runnable execInsert, execInsertCopiedArray;
        // The way this works is that you run a get to perform the delete,
        //  then run the Runnable to perform the update
        // This is weird but very convenient
        public final ISupplier<Runnable> execDelete;
        public final UIElement core;
        public int coreIndent;

        public ArrayPosition(String txt, RubyIO[] elem, UIElement cor, int subelemId, ISupplier<Runnable> exeDelete, Runnable exeInsert, Runnable exeInsertCopiedArray) {
            text = txt;
            elements = elem;
            core = cor;
            coreIndent = subelemId;
            execDelete = exeDelete;
            execInsert = exeInsert;
            execInsertCopiedArray = exeInsertCopiedArray;
        }
    }
}
