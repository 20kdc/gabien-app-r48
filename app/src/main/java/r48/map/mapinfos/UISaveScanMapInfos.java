/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.mapinfos;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UIScrollLayout;
import r48.App;
import r48.IMapContext;
import r48.io.IObjectBackend;
import r48.ui.UIAppendButton;

/**
 * A 'flat' explorer showing just map information.
 * Created sometime in December 2017 (whoops!)
 */
public class UISaveScanMapInfos extends App.Prx {
    public final UIScrollLayout mainLayout = new UIScrollLayout(true, app.f.generalS);
    public final Function<Integer, String> objectMapping, gumMapping;
    public final IMapContext context;
    public final int first, last;
    private final String toStringRes;

    public UISaveScanMapInfos(Function<Integer, String> map, Function<Integer, String> gummap, int f, int l, @NonNull IMapContext ctx, String saves) {
        super(ctx.getApp());
        objectMapping = map;
        gumMapping = gummap;
        context = ctx;
        first = f;
        last = l;
        toStringRes = saves;
        reload();
        proxySetElement(mainLayout, true);
    }

    @Override
    public String toString() {
        return toStringRes;
    }

    public void reload() {
        mainLayout.panelsClear();
        for (int i = first; i <= last; i++) {
            final int fi = i;
            try {
                IObjectBackend.ILoadedObject rio = app.odb.getObject(objectMapping.apply(i), null);
                final String gum = gumMapping.apply(i);
                if (rio != null) {
                    String obj = app.format(rio.getObject());
                    mainLayout.panelsAdd(new UITextButton(T.m.dSaveColon.r(gum, obj), app.f.mapInfosTH, new Runnable() {
                        @Override
                        public void run() {
                            context.loadMap(gum);
                        }
                    }));
                } else {
                    mainLayout.panelsAdd(new UIAppendButton(T.m.bNew, new UILabel(T.m.unavailable.r(gum), app.f.mapInfosTH), new Runnable() {
                        @Override
                        public void run() {
                            context.loadMap(gum);
                            reload();
                        }
                    }, app.f.mapInfosTH));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                UILabel warning = new UILabel(T.m.internalErr, app.f.mapInfosTH);
                mainLayout.panelsAdd(new UIAppendButton(T.m.bAttemptLoadAnyway, warning, new Runnable() {
                    @Override
                    public void run() {
                        context.loadMap(gumMapping.apply(fi));
                    }
                }, app.f.mapInfosTH));
            }
        }
    }
}
