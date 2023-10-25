/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.UIScrollLayout;

/**
 * Generic classifier of which CommandTag is a kind.
 * Created 25th October, 2023.
 */
public interface IClassifierish<T extends IClassifierish.BaseInstance> {
    /**
     * Returns a localized name for this classifier.
     */
    @NonNull String getName();

    /**
     * Creates an instance of this classifier.
     */
    @NonNull T instance();

    /**
     * Instance of a classifier. Immutable Instances need not be unique.
     */
    interface BaseInstance {
        /**
         * Installs an editor for this classifier instance, if possible.
         * Only one editor should be present at a given time for this instance.
         */
        void setupEditor(@NonNull UIScrollLayout usl, @NonNull Runnable onEdit);
    }
}
