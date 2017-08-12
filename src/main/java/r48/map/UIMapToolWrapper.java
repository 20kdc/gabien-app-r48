/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.map;

import gabien.IGrInDriver;
import gabien.ui.IWindowElement;
import gabien.ui.Rect;
import gabien.ui.UIElement;

/**
 * Keeps the application informed about what happens to UI Tool Windows,
 * without getting in their way.
 * Created on 12/30/16.
 */
public class UIMapToolWrapper extends UIElement implements IWindowElement {
    public UIElement pattern;
    public boolean selfClose = false;
    public boolean hasClosed = false;

    public UIMapToolWrapper(UIElement uie) {
        pattern = uie;
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        pattern.updateAndRender(ox, oy, deltaTime, selected, igd);
    }

    @Override
    public void handleClick(int x, int y, int button) {
        pattern.handleClick(x, y, button);
    }

    @Override
    public void handleDrag(int x, int y) {
        pattern.handleDrag(x, y);
    }

    @Override
    public void handleRelease(int x, int y) {
        pattern.handleRelease(x, y);
    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        pattern.handleMousewheel(x, y, north);
    }

    @Override
    public Rect getBounds() {
        return pattern.getBounds();
    }

    @Override
    public void setBounds(Rect r) {
        pattern.setBounds(r);
    }

    @Override
    public boolean wantsSelfClose() {
        if (pattern instanceof IWindowElement)
            return selfClose || ((IWindowElement) pattern).wantsSelfClose();
        return selfClose;
    }

    @Override
    public void windowClosed() {
        if (pattern instanceof IWindowElement)
            ((IWindowElement) pattern).windowClosed();
        hasClosed = true;
    }

    @Override
    public String toString() {
        return pattern.toString();
    }
}
