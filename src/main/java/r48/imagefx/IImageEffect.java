/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.imagefx;

import gabien.IGrInDriver;

/**
 * Created on 30/07/17.
 */
public interface IImageEffect {
    // Must be unique between all other configurations of image effect. Do not use ':'.
    String uniqueToString();

    IGrInDriver.IImage process(IGrInDriver.IImage input);
}