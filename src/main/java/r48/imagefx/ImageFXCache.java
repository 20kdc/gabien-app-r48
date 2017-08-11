/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.imagefx;

import gabien.IGrInDriver;
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
