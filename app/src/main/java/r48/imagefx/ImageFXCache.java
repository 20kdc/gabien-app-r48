/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.imagefx;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.WeakHashMap;

import gabien.render.IImage;

/**
 * Created on 30/07/17.
 */
public class ImageFXCache {
    public WeakHashMap<IImage, HashMap<String, IImage>> effectsMap = new WeakHashMap<IImage, HashMap<String, IImage>>();

    public IImage process(IImage input, LinkedList<IImageEffect> effectsChain) {
        if (effectsChain.size() == 0)
            return input;
        String key = "";
        for (IImageEffect effect : effectsChain)
            key += effect.uniqueToString() + ":";
        HashMap<String, IImage> imageEffectMap = effectsMap.get(input);
        if (imageEffectMap == null) {
            imageEffectMap = new HashMap<String, IImage>();
            effectsMap.put(input, imageEffectMap);
        }
        IImage result = imageEffectMap.get(key);
        if (result == null) {
            result = input;
            for (IImageEffect eff : effectsChain)
                result = eff.process(result);
            imageEffectMap.put(key, result);
        }
        return result;
    }

    public IImage process(IImage input, IImageEffect... effectsChain) {
        if (effectsChain.length == 0)
            return input;
        String key = "";
        for (IImageEffect effect : effectsChain)
            key += effect.uniqueToString() + ":";
        HashMap<String, IImage> imageEffectMap = effectsMap.get(input);
        if (imageEffectMap == null) {
            imageEffectMap = new HashMap<String, IImage>();
            effectsMap.put(input, imageEffectMap);
        }
        IImage result = imageEffectMap.get(key);
        if (result == null) {
            result = input;
            for (IImageEffect eff : effectsChain)
                result = eff.process(result);
            imageEffectMap.put(key, result);
        }
        return result;
    }
}
