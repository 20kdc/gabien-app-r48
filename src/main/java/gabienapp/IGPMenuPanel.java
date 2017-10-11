/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import gabien.ui.ISupplier;

public interface IGPMenuPanel {
    String[] getButtonText();
    ISupplier<IGPMenuPanel>[] getButtonActs();
}
