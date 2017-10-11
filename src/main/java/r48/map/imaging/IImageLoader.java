/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map.imaging;

import gabien.IImage;

/**
 * Replaces *all* instances of using GaBIEn image loading, due to XYZ support that may be required in future.
 * Or other things.
 * It is assumed the image loader has a cache.
 * Note that everything going here comes from the Map group.
 * <p/>
 * -- Ok, the update on how this is meant to go:
 * In general, do not use BCK.
 * At all. Just bury it. Don't use it, don't touch it.
 * ...UNLESS, of course, you're versionId == "Ika" or a similar case where colour-keying is used FOR EVERYTHING.
 * Then you use GabienImageLoader with the correct BCK settings for your case.
 * ...UNLESS, of course, you can't do that because differing BCKs for different tasks.
 * In which case, pass metadata in the image names.
 * (...At some point I am really going to have to cede all rendering control to the backend, aren't I?)
 * <p/>
 * Created on 29/05/17.
 */
public interface IImageLoader {
    // Similar to getImage in the old system.
    // "panorama" indicates if *index-based* transparency should be disabled (R2k panoramas)
    IImage getImage(String name, boolean panorama);

    void flushCache();
}
