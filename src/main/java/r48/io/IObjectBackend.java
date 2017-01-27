/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io;

import r48.RubyIO;

import java.io.IOException;

/**
 * Allows for the creation of non-standard backends which don't use the normal Ruby marshal format.
 * Presumably for "flat binary file" formats, some emulation is involved.
 * In any case, this makes the whole thing more flexible.
 * Created on 1/27/17.
 */
public interface IObjectBackend {
    RubyIO loadObjectFromFile(String filename);
    void saveObjectToFile(String filename, RubyIO object) throws IOException;
}
