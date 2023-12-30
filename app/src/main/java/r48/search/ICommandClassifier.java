/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIElement;
import r48.App;
import r48.dbs.RPGCommand;
import r48.io.data.RORIO;

/**
 * Generic classifier of which CommandTag is a kind.
 * Created 18th August, 2023.
 */
public interface ICommandClassifier extends IClassifierish<ICommandClassifier.Instance> {
    /**
     * Instance of a classifier. Immutable Instances need not be unique.
     */
    interface Instance extends IClassifierish.BaseInstance {
        /**
         * Checks if the given RPGCommand/RORIO matches this classifier instance.
         */
        boolean matches(@Nullable RPGCommand dbEntry, @Nullable RORIO cmd);
    }

    /**
     * Immutable ICommandClassifiers should extend this so they can be used by appropriate logic.
     * (In particular Universal String Locator logic.)
     */
    interface Immutable extends ICommandClassifier, Instance {
        @Override
        default void setupEditor(LinkedList<UIElement> usl, Runnable onEdit) {
        }

        @Override
        default Instance instance(App app) {
            return this;
        }
    }
}
