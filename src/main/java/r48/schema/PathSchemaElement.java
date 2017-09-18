/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema;

import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UILabel;
import gabien.ui.UISplitterLayout;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.PathSyntax;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on 04/08/17.
 */
public class PathSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public String path, txPath;
    public SchemaElement core;
    private int fwOverride;
    private boolean enableFwOverride;

    public PathSchemaElement(String pat, String txPat, SchemaElement hide) {
        path = pat;
        txPath = txPat;
        core = hide;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, ISchemaHost launcher, SchemaPath pat) {
        RubyIO r = PathSyntax.parse(target, path);
        if (r == null)
            throw new RuntimeException("PathSchemaElement " + path + " was lied to, the target does not exist");
        UILabel label = new UILabel(txPath + " ", FontSizes.schemaFieldTextHeight);
        if (enableFwOverride) {
            label.setBounds(new Rect(0, 0, fwOverride, label.getBounds().height));
            enableFwOverride = false;
        }
        return new UISplitterLayout(label, core.buildHoldingEditor(r, launcher, pat), false, 0);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath pat, boolean setDefault) {
        RubyIO r = PathSyntax.parse(target, path);
        if (r == null)
            throw new RuntimeException("PathSchemaElement " + path + " was lied to, the target does not exist");
        core.modifyVal(r, pat, setDefault);
    }

    @Override
    public int getDefaultFieldWidth(RubyIO target) {
        return UILabel.getRecommendedSize(txPath + " ", FontSizes.schemaFieldTextHeight).width;
    }

    @Override
    public void setFieldWidthOverride(int w) {
        fwOverride = w;
        enableFwOverride = true;
    }
}
