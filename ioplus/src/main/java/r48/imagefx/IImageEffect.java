/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.imagefx;

import gabien.render.IImage;

/**
 * REGISTRY:
 * 'H' + shift = HueShift
 * 'M' + spriteSize + '.' + spritesW + '.' + spritesH = MirrorSubsprites
 * 'O' + opacity = Opacity
 * 'T' + R + ',' + G + ',' + B + ',' + S
 * Created on 30/07/17.
 */
public interface IImageEffect {
    // Must be unique between all other configurations of image effect. Do not use ':'.
    String uniqueToString();

    IImage process(IImage input);
}
