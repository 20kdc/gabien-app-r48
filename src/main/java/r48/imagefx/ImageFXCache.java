/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.imagefx;

import gabien.IGrInDriver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.WeakHashMap;

/**
 * Created on 30/07/17.
 */
public class ImageFXCache {
    public WeakHashMap<IGrInDriver.IImage, HashMap<String, IGrInDriver.IImage>> effectsMap = new WeakHashMap<IGrInDriver.IImage, HashMap<String, IGrInDriver.IImage>>();
    public IGrInDriver.IImage process(IGrInDriver.IImage input, LinkedList<IImageEffect> effectsChain) {
        if (effectsChain.size() == 0)
            return input;
        String key = "";
        for (IImageEffect effect :effectsChain)
            key += effect.uniqueToString() + ":";
        HashMap<String, IGrInDriver.IImage> imageEffectMap = effectsMap.get(input);
        if (imageEffectMap == null) {
            imageEffectMap = new HashMap<String, IGrInDriver.IImage>();
            effectsMap.put(input, imageEffectMap);
        }
        IGrInDriver.IImage result = imageEffectMap.get(key);
        if (result == null) {
            result = input;
            for (IImageEffect eff : effectsChain)
                result = eff.process(result);
            imageEffectMap.put(key, result);
        }
        return result;
    }
}
