/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos2;

import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.Rect;

/**
 * Created on October 10, 2018.
 */
public interface IGP2Renderer {
    IImage getBackground(int frame);

    int[] getIndicators(int frame);

    Rect getCellSelectionIndicator(GP2Cell selectedCell, int frame);

    void drawCells(int frame, int opx, int opy, IGrDriver igd);
}
