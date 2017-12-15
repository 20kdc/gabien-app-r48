/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.mapinfos;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;

/**
 * A 'flat' explorer showing just map information.
 */
public class UISaveScanMapInfos extends UIPanel {
    public final UIScrollLayout mainLayout = new UIScrollLayout(true, FontSizes.generalScrollersize);
    public final IFunction<Integer, String> objectMapping;
    public final int first, last;

    public UISaveScanMapInfos(IFunction<Integer, String> map, int f, int l) {
        objectMapping = map;
        first = f;
        last = l;
        reload();
    }

    @Override
    public void setBounds(Rect r) {
        super.setBounds(r);
        mainLayout.setBounds(new Rect(0, 0, r.width, r.height));
    }

    public void reload() {
        for (int i = first; i <= last; i++) {
            RubyIO rio = AppMain.objectDB.getObject(objectMapping.apply(i), null);
            if (rio == null)
                mainLayout.panels.add(new UITextButton(0, FormatSyntax.formatExtended(TXDB.get("#A : #B"), new RubyIO().setFX(i), rio), new Runnable() {
                    @Override
                    public void run() {

                    }
                }));
        }
    }
}
