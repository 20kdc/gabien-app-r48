/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.toolsets;

import gabien.ui.UIElement;
import r48.ui.utilitybelt.ImageEditorController;

public class ImageEditToolset implements IToolset {
    public ImageEditorController imgEdit;
    @Override
    public UIElement[] generateTabs() {
        return new UIElement[] {
                (imgEdit = new ImageEditorController()).rootView
        };
    }
}
