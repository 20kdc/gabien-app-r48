/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.schema;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.IProxySchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Basically a UI element masquerading as a schema element.
 * Created on 12/29/16.
 */
public class AggregateSchemaElement extends SchemaElement {
    public LinkedList<SchemaElement> aggregate = new LinkedList<SchemaElement>();

    public AggregateSchemaElement(SchemaElement[] ag) {
        Collections.addAll(aggregate, ag);
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(path, launcher, this, target);
        // Assist with the layout of "property grids".
        int maxFW = 1;
        for (SchemaElement ise : aggregate) {
            SchemaElement useIse = ise;
            while (useIse instanceof IProxySchemaElement)
                useIse = ((IProxySchemaElement) useIse).getEntry();
            if (useIse instanceof IFieldSchemaElement) {
                int dfw = ((IFieldSchemaElement) useIse).getDefaultFieldWidth();
                if (maxFW < dfw)
                    maxFW = dfw;
            }
        }
        for (SchemaElement ise : aggregate) {
            SchemaElement useIse = ise;
            while (useIse instanceof IProxySchemaElement)
                useIse = ((IProxySchemaElement) useIse).getEntry();
            if (useIse instanceof IFieldSchemaElement)
                ((IFieldSchemaElement) useIse).setFieldWidthOverride(maxFW);
            // still deal with ise because the proxies may have some function
            uiSVL.panels.add(ise.buildHoldingEditor(target, launcher, path));
        }
        int h = 0;
        for (UIElement uie : uiSVL.panels)
            h += uie.getBounds().height;
        uiSVL.setBounds(new Rect(0, 0, 128, h));
        return uiSVL;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath i, boolean setDefault) {
        for (SchemaElement ise : aggregate)
            ise.modifyVal(target, i, setDefault);
    }

    public static UIScrollLayout createScrollSavingSVL(final SchemaPath path, final ISchemaHost host, final SchemaElement elem, final RubyIO target) {
        final SchemaPath.EmbedDataKey myKey = new SchemaPath.EmbedDataKey(elem, target);
        final SchemaPath keyStoragePath = path.findLast();
        final UIScrollLayout uiSVL = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public void updateAndRender(int ox, int oy, double DeltaTime, boolean select, IGrInDriver igd) {
                super.updateAndRender(ox, oy, DeltaTime, select, igd);
                path.findLast().getEmbedMap(host).put(myKey, scrollbar.scrollPoint);
            }
        };
        uiSVL.scrollbar.scrollPoint = keyStoragePath.getEmbedSP(host, myKey);
        return uiSVL;
    }
}
