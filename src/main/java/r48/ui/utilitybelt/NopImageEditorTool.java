package r48.ui.utilitybelt;

import gabien.ui.Rect;
import gabien.ui.UIElement;

public class NopImageEditorTool implements IImageEditorTool {
    @Override
    public UIElement createToolPalette(UIImageEditView uiev) {
        return RootImageEditorTool.createToolPalette(uiev, NopImageEditorTool.class);
    }

    @Override
    public Rect getSelection() {
        return null;
    }

    @Override
    public String getLocalizedText(boolean dedicatedDragControl) {
        return "";
    }

    @Override
    public IImageEditorTool getCamModeLT() {
        return null;
    }

    @Override
    public void accept(UIImageEditView uiImageEditView) {

    }
}
