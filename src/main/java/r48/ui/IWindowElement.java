/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui;

/**
 * Exists as a continuation of the previous IWindowElement.
 * As it turns out, the 'root disconnection' method is somewhat unreliable.
 * Written on February 18th, 2018
 */
public interface IWindowElement {
    // The window is absolutely, definitely being closed.
    // Will always be called when the window is being closed,
    //  and will only be called when the window is being closed.
    void windowClosing();
}
