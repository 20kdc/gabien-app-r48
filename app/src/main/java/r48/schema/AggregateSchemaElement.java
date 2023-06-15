/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.IPointer;
import gabien.ui.*;
import r48.App;
import r48.dbs.IProxySchemaElement;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.Collections;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Basically a UI element masquerading as a schema element.
 * Created on 12/29/16.
 */
public class AggregateSchemaElement extends SchemaElement implements IFieldSchemaElement {
    public final LinkedList<SchemaElement> aggregate = new LinkedList<SchemaElement>();
    public final SchemaElement impersonatorScroll;

    private int overrideFW = -1;
    private boolean overrideSet = false;

    public AggregateSchemaElement(App app, SchemaElement[] ag) {
        super(app);
        Collections.addAll(aggregate, ag);
        impersonatorScroll = this;
    }

    public AggregateSchemaElement(App app, SchemaElement[] ag, SchemaElement fake) {
        super(app);
        Collections.addAll(aggregate, ag);
        impersonatorScroll = fake;
    }

    @Override
    public UIElement buildHoldingEditor(IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        // Possibly question if this aggregate is useless???
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(launcher, impersonatorScroll, target);
        // Assist with the layout of "property grids".
        if (!overrideSet)
            overrideFW = getDefaultFieldWidth(target);
        for (SchemaElement ise : aggregate) {
            SchemaElement possibleField = extractField(ise, target);
            if (possibleField instanceof IFieldSchemaElement)
                ((IFieldSchemaElement) possibleField).setFieldWidthOverride(overrideFW);
            // still deal with ise because the proxies may have some function
            uiSVL.panelsAdd(ise.buildHoldingEditor(target, launcher, path));
        }
        overrideSet = false;
        uiSVL.forceToRecommended();
        return uiSVL;
    }

    public static SchemaElement extractField(SchemaElement ise, RORIO rio) {
        boolean continuing = true;
        while (continuing) {
            continuing = false;
            if (ise instanceof IProxySchemaElement) {
                ise = ((IProxySchemaElement) ise).getEntry();
                continuing = true;
            }
            if (rio != null) {
                if (ise instanceof DisambiguatorSchemaElement) {
                    ise = ((DisambiguatorSchemaElement) ise).getDisambiguation(rio);
                    continuing = true;
                }
            }
        }
        return ise;
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath i, boolean setDefault) {
        for (SchemaElement ise : aggregate)
            ise.modifyVal(target, i, setDefault);
    }

    // NOTE: In *general* elem should be the SchemaElement.
    // HOWEVER, if the object is regen-on-change w/ subwindows,
    //  this causes awful scroll loss, so instead nab the regenerator (it's not like the regenerator uses it for anything)
    // PREFERABLY avoid regeneration of schema objects that are reusable (RPGCommandSchemaElement was fixed this way)
    public static UIScrollLayout createScrollSavingSVL(final ISchemaHost host, final SchemaElement elem, final IRIO target) {
        final App app = host.getApp();
        final UIScrollLayout uiSVL = new UIScrollLayout(true, app.f.generalS) {
            @Override
            public void handleMousewheel(int x, int y, boolean north) {
                super.handleMousewheel(x, y, north);
                host.setEmbedDouble(elem, target, "N/scrollSavingSVL", scrollbar.scrollPoint);
            }

            @Override
            public IPointerReceiver handleNewPointer(IPointer state) {
                final IPointerReceiver ipr = super.handleNewPointer(state);
                if (ipr != null) {
                    return new IPointerReceiver() {
                        @Override
                        public void handlePointerBegin(IPointer state) {
                            ipr.handlePointerBegin(state);
                        }

                        @Override
                        public void handlePointerUpdate(IPointer state) {
                            ipr.handlePointerUpdate(state);
                        }

                        @Override
                        public void handlePointerEnd(IPointer state) {
                            ipr.handlePointerEnd(state);
                            host.setEmbedDouble(elem, target, "N/scrollSavingSVL", scrollbar.scrollPoint);
                        }
                    };
                }
                return null;
            }
        };
        uiSVL.scrollbar.scrollPoint = host.getEmbedDouble(elem, target, "N/scrollSavingSVL");
        return uiSVL;
    }

    // Only to be used if this button is known to cause changeOccurred.
    public static void hookButtonForPressPreserve(final ISchemaHost host, final SchemaElement elem, final IRIO target, final UITextButton utb, final String id) {
        final Runnable next = utb.onClick;
        utb.onClick = new Runnable() {
            @Override
            public void run() {
                if (next != null)
                    next.run();
                host.setEmbedDouble(elem, target, "B/" + id, 1d);
            }
        };
        if (host.getEmbedDouble(elem, target, "B/" + id) != 0d)
            utb.enableStateForClick();
        host.setEmbedDouble(elem, target, "B/" + id, 0d);
    }

    @Override
    public int getDefaultFieldWidth(IRIO target) {
        int maxFW = 1;
        for (SchemaElement ise : aggregate) {
            SchemaElement possibleField = extractField(ise, target);
            if (possibleField instanceof IFieldSchemaElement) {
                int dfw = ((IFieldSchemaElement) possibleField).getDefaultFieldWidth(target);
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

    @Override
    public @Nullable String windowTitleSuffix(SchemaPath path) {
        for (SchemaElement ise : aggregate) {
            String res = ise.windowTitleSuffix(path);
            if (res != null)
                return res;
        }
        return null;
    }
}
