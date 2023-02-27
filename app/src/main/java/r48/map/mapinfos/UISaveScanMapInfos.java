/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.mapinfos;

import org.eclipse.jdt.annotation.NonNull;

import gabien.ui.*;
import gabien.uslx.append.*;
import r48.App;
import r48.FontSizes;
import r48.IMapContext;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.ui.UIAppendButton;

/**
 * A 'flat' explorer showing just map information.
 * Created sometime in December 2017 (whoops!)
 */
public class UISaveScanMapInfos extends App.Prx {
    public final UIScrollLayout mainLayout = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public final IFunction<Integer, String> objectMapping, gumMapping;
    public final IMapContext context;
    public final int first, last;
    private final String toStringRes;

    public UISaveScanMapInfos(IFunction<Integer, String> map, IFunction<Integer, String> gummap, int f, int l, @NonNull IMapContext ctx, String saves) {
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
                    mainLayout.panelsAdd(new UITextButton(app.fmt.formatExtended(TXDB.get("#A : #B"), new RubyIO().setString(gum, true), rio.getObject()), FontSizes.mapInfosTextHeight, new Runnable() {
                        @Override
                        public void run() {
                            context.loadMap(gum);
                        }
                    }));
                } else {
                    mainLayout.panelsAdd(new UIAppendButton(TXDB.get("New..."), new UILabel(app.fmt.formatExtended(TXDB.get("#A (Unavailable)"), new RubyIO().setString(gum, true)), FontSizes.mapInfosTextHeight), new Runnable() {
                        @Override
                        public void run() {
                            context.loadMap(gum);
                            reload();
                        }
                    }, FontSizes.mapInfosTextHeight));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                UILabel warning = new UILabel(TXDB.get("Internal error in R48 or with file."), FontSizes.mapInfosTextHeight);
                mainLayout.panelsAdd(new UIAppendButton(TXDB.get("Attempt Load Anyway"), warning, new Runnable() {
                    @Override
                    public void run() {
                        context.loadMap(gumMapping.apply(fi));
                    }
                }, FontSizes.mapInfosTextHeight));
            }
        }
    }
}
