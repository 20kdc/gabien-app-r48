/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.imagefx;

import gabien.IImage;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.WeakHashMap;

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
