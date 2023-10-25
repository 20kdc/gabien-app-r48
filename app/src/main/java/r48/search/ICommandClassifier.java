/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import org.eclipse.jdt.annotation.Nullable;

import gabien.ui.UIScrollLayout;
import r48.dbs.RPGCommand;

/**
 * Generic classifier of which CommandTag is a kind.
 * Created 18th August, 2023.
 */
public interface ICommandClassifier {
    /**
     * Returns a localized name for this classifier.
     */
    String getName();

    /**
     * Creates an instance of this classifier.
     */
    Instance instance();

    /**
     * Instance of a classifier. Immutable Instances need not be unique.
     */
    interface Instance {
        /**
         * Installs an editor for this classifier instance, if possible.
         * Only one editor should be present at a given time for this instance.
         */
        void setupEditor(UIScrollLayout usl, Runnable onEdit);

        /**
         * Checks if the given RPGCommand matches this classifier instance.
         */
        boolean matches(@Nullable RPGCommand target);
    }

    /**
     * Immutable ICommandClassifiers should extend this so they can be used by appropriate logic.
     * (In particular Universal String Locator logic.)
     */
    interface Immutable extends ICommandClassifier, Instance {
        @Override
        default void setupEditor(UIScrollLayout usl, Runnable onEdit) {
        }

        @Override
        default Instance instance() {
            return this;
        }
    }
}
