/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema;

import gabien.ui.*;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import gabien.uslx.append.IGetSet;
import gabien.wsi.IPointer;
import r48.R48;
import r48.dbs.IProxySchemaElement;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.util.EmbedDataKey;
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
    public final EmbedDataKey<Double> scrollPointKey;

    private int overrideFW = -1;
    private boolean overrideSet = false;

    public AggregateSchemaElement(R48 app, SchemaElement[] ag) {
        super(app);
        Collections.addAll(aggregate, ag);
        scrollPointKey = new EmbedDataKey<>();
    }

    public AggregateSchemaElement(R48 app, SchemaElement[] ag, EmbedDataKey<Double> fake) {
        super(app);
        Collections.addAll(aggregate, ag);
        scrollPointKey = fake;
    }

    @Override
    public UIElement buildHoldingEditorImpl(IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        // Assist with the layout of "property grids".
        if (!overrideSet)
            overrideFW = getDefaultFieldWidth(target);
        LinkedList<UIElement> uiSVLList = new LinkedList<>();
        for (SchemaElement ise : aggregate) {
            SchemaElement possibleField = extractField(ise, target);
            if (possibleField instanceof IFieldSchemaElement)
                ((IFieldSchemaElement) possibleField).setFieldWidthOverride(overrideFW);
            // still deal with ise because the proxies may have some function
            uiSVLList.add(ise.buildHoldingEditor(target, launcher, path));
        }
        overrideSet = false;
        // Possibly question if this aggregate is useless???
        return AggregateSchemaElement.createScrollSavingSVL(launcher, scrollPointKey, target, uiSVLList);
    }

    public static SchemaElement extractField(SchemaElementIOP ise, @Nullable RORIO rio) {
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
        return SchemaElement.cast(ise);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath i, boolean setDefault) {
        for (SchemaElement ise : aggregate)
            ise.modifyVal(target, i, setDefault);
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
        for (SchemaElement ise : aggregate)
            ise.visit(target, path, v, detailedPaths);
    }

    // NOTE: In *general* elem should be the SchemaElement.
    // HOWEVER, if the object is regen-on-change w/ subwindows,
    //  this causes awful scroll loss, so instead nab the regenerator (it's not like the regenerator uses it for anything)
    // PREFERABLY avoid regeneration of schema objects that are reusable (RPGCommandSchemaElement was fixed this way)
    public static UIScrollLayout createScrollSavingSVL(final ISchemaHost host, final EmbedDataKey<Double> key, final IRIO target) {
        final IGetSet<Double> savedPoint = host.embedSlot(target, key, 0.0d);
        final R48 app = host.getApp();
        final UIScrollLayout uiSVL = new UIScrollLayout(true, app.f.generalS) {
            @Override
            public void handleMousewheel(int x, int y, boolean north) {
                super.handleMousewheel(x, y, north);
                savedPoint.accept(scrollbar.scrollPoint);
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
                            savedPoint.accept(scrollbar.scrollPoint);
                        }
                    };
                }
                return null;
            }
        };
        uiSVL.scrollbar.scrollPoint = savedPoint.get();
        return uiSVL;
    }
    public static UIScrollLayout createScrollSavingSVL(final ISchemaHost host, final EmbedDataKey<Double> key, final IRIO target, UIElement... contents) {
        UIScrollLayout usl = createScrollSavingSVL(host, key, target);
        usl.panelsSet(contents);
        usl.forceToRecommended();
        return usl;
    }
    public static UIScrollLayout createScrollSavingSVL(final ISchemaHost host, final EmbedDataKey<Double> key, final IRIO target, Iterable<UIElement> contents) {
        UIScrollLayout usl = createScrollSavingSVL(host, key, target);
        usl.panelsSet(contents);
        usl.forceToRecommended();
        return usl;
    }

    // Only to be used if this button is known to cause changeOccurred.
    public static void hookButtonForPressPreserve(final ISchemaHost host, final IRIO target, final UITextButton utb, final EmbedDataKey<Boolean> id) {
        IGetSet<Boolean> wasPressed = host.embedSlot(target, id, false);
        final Runnable next = utb.onClick;
        utb.onClick = () -> {
            wasPressed.accept(true);
            if (next != null)
                next.run();
        };
        if (wasPressed.get())
            utb.enableStateForClick();
        wasPressed.accept(false);
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
