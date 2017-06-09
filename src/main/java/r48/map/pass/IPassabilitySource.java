/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.pass;

/**
 * Created on 09/06/17.
 */
public interface IPassabilitySource {
    // 0x01: down 0x02 right 0x04 left 0x08 up
    // -1 means don't even bother.
    int getPassability(int x, int y);
}
