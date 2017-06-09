/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map.drawlayers;

/**
 * Created on 09/06/17.
 */
public interface IPassabilitySource {
    // 0x01: up 0x02 right 0x04 down 0x08 left
    int getPassability(int x, int y);
}
