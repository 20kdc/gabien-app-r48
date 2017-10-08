/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.toolsets;

import gabien.ui.IConsumer;
import gabien.ui.ISupplier;
import gabien.ui.UIElement;
import r48.ui.utilitybelt.ImageEditorController;

public class ImageEditToolset implements IToolset {
    @Override
    public String[] tabNames() {
        return new String[] {
                "Image Editor"
        };
    }

    @Override
    public UIElement[] generateTabs(ISupplier<IConsumer<UIElement>> windowMaker) {
        return new UIElement[] {
                new ImageEditorController().rootView
        };
    }
}
