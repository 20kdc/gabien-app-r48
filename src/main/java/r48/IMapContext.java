/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48;

/**
 * Acts as a getter/setter for the current map for dictionaries & r2ksystemdefaults game bootstrap code.
 * Created on 08/06/17.
 */
public interface IMapContext {
    // Returns null if none loaded.
    String getCurrentMap();
    void loadMap(String s);
}
