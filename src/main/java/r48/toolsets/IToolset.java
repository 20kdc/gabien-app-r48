/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.toolsets;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;

/**
 * Hopefully will allow cleaning up the initial tab creation code.
 * Created on 2/12/17.
 */
public interface IToolset {
    String[] tabNames();

    // NOTE: This allows skipping out on actually generating tabs at the end, if you dare.
    UIElement[] generateTabs(ISupplier<IConsumer<UIElement>> windowMaker);
}
