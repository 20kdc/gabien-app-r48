/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.io.r2k.chunks;

import r48.RubyIO;

import java.io.IOException;
import java.io.InputStream;

/**
 * My goodness, the format is madness.
 * But I must continue.
 * R2kObject is a subclass of this for Lcf Chunked objects.
 * Created on 31/05/17.
 */
public interface IR2kInterpretable {
    void importData(InputStream bais) throws IOException;
    RubyIO asRIO();
}
