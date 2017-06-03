/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.imaging;

import gabien.IGrInDriver;

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
    IGrInDriver.IImage getImage(String name, boolean panorama);

    void flushCache();
}
