/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp.schema.util;

import gabien.ui.UIElement;
import gabienapp.StuffRenderer;
import gabienapp.UITest;

/**
 * Used to make the Schema interface slightly saner to use
 * Created on 12/29/16.
 */
public interface ISchemaHost {
    void switchObject(SchemaPath nextObject);
    void launchOther(UIElement uiTest);

    // The StuffRenderer applicable to this window.
    StuffRenderer getContextRenderer();
}
