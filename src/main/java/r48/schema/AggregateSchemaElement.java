/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import gabien.ui.UITextButton;
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
public class AggregateSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public final LinkedList<SchemaElement> aggregate = new LinkedList<SchemaElement>();
    public final SchemaElement impersonatorScroll;

    private int overrideFW = -1;
    private boolean overrideSet = false;

    public AggregateSchemaElement(SchemaElement[] ag) {
        Collections.addAll(aggregate, ag);
        impersonatorScroll = this;
    }

    public AggregateSchemaElement(SchemaElement[] ag, SchemaElement fake) {
        Collections.addAll(aggregate, ag);
        impersonatorScroll = fake;
    }

    @Override
    public UIElement buildHoldingEditor(RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        // Possibly question if this aggregate is useless???
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(path, launcher, impersonatorScroll, target);
        // Assist with the layout of "property grids".
        if (!overrideSet)
            overrideFW = getDefaultFieldWidth(target);
        for (SchemaElement ise : aggregate) {
            IFieldSchemaElement possibleField = extractField(ise, target);
            if (possibleField != null)
                possibleField.setFieldWidthOverride(overrideFW);
            // still deal with ise because the proxies may have some function
            uiSVL.panels.add(ise.buildHoldingEditor(target, launcher, path));
        }
        overrideSet = false;
        int h = 0;
        for (UIElement uie : uiSVL.panels)
            h += uie.getBounds().height;
        uiSVL.setBounds(new Rect(0, 0, 128, h));
        return uiSVL;
    }

    private IFieldSchemaElement extractField(SchemaElement ise, RubyIO rio) {
        boolean continuing = true;
        while (continuing) {
            continuing = false;
            if (ise instanceof IProxySchemaElement) {
                ise = ((IProxySchemaElement) ise).getEntry();
                continuing = true;
            }
            if (ise instanceof DisambiguatorSchemaElement) {
                ise = ((DisambiguatorSchemaElement) ise).getDisambiguation(rio);
                continuing = true;
            }
        }
        if (ise instanceof IFieldSchemaElement)
            return (IFieldSchemaElement) ise;
        return null;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath i, boolean setDefault) {
        for (SchemaElement ise : aggregate)
            ise.modifyVal(target, i, setDefault);
    }

    // NOTE: In *general* elem should be the SchemaElement.
    // HOWEVER, if the object is regen-on-change w/ subwindows,
    //  this causes awful scroll loss, so instead nab the regenerator (it's not like the regenerator uses it for anything)
    // PREFERABLY avoid regeneration of schema objects that are reusable (RPGCommandSchemaElement was fixed this way)
    public static UIScrollLayout createScrollSavingSVL(final SchemaPath path, final ISchemaHost host, final SchemaElement elem, final RubyIO target) {
        final SchemaPath.EmbedDataKey myKey = new SchemaPath.EmbedDataKey(elem, target, AggregateSchemaElement.class, "N/scrollSavingSVL");
        final SchemaPath keyStoragePath = path.findLast();
        final UIScrollLayout uiSVL = new UIScrollLayout(true, FontSizes.generalScrollersize) {
            @Override
            public void updateAndRender(int ox, int oy, double DeltaTime, boolean select, IGrInDriver igd) {
                super.updateAndRender(ox, oy, DeltaTime, select, igd);
                keyStoragePath.getEmbedMap(host).put(myKey, scrollbar.scrollPoint);
            }
        };
        uiSVL.scrollbar.scrollPoint = keyStoragePath.getEmbedSP(host, myKey);
        return uiSVL;
    }

    // Only to be used if this button is known to cause changeOccurred.
    // The Runnable is an "undo" for if it's uncertain - needs to be triggered on updateAndRender.
    public static Runnable hookButtonForPressPreserve(final SchemaPath path, final ISchemaHost host, final SchemaElement elem, final RubyIO target, final UITextButton utb, final String id) {
        final SchemaPath.EmbedDataKey myKey = new SchemaPath.EmbedDataKey(elem, target, AggregateSchemaElement.class, "B/" + id);
        final SchemaPath keyStoragePath = path.findLast();
        final Runnable saver = new Runnable() {
            @Override
            public void run() {
                keyStoragePath.getEmbedMap(host).put(myKey, 0d);
            }
        };
        final Runnable next = utb.OnClick;
        utb.OnClick = new Runnable() {
            @Override
            public void run() {
                if (next != null)
                    next.run();
                keyStoragePath.getEmbedMap(host).put(myKey, 1d);
            }
        };
        if (keyStoragePath.getEmbedSP(host, myKey) != 0d) {
            utb.state = true;
            utb.PressedTime = 0.5d;
        }
        return saver;
    }

    @Override
    public int getDefaultFieldWidth(RubyIO target) {
        int maxFW = 1;
        for (SchemaElement ise : aggregate) {
            IFieldSchemaElement possibleField = extractField(ise, target);
            if (possibleField != null) {
                int dfw = possibleField.getDefaultFieldWidth(target);
                if (maxFW < dfw)
                    maxFW = dfw;
            }
        }
        return maxFW;
    }

    @Override
    public void setFieldWidthOverride(int w) {
        overrideFW = w;
        overrideSet = true;
    }
}
