/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.map;

import gabien.IOsbDriver;

/**
 * Responsible for making UIMapView update less.
 * Created on 04/06/17.
 */
public class MapViewUpdateScheduler {
    public boolean forceNextUpdate = true;
    public String lastConfig = "";
    public boolean needsUpdate(String config) {
        if (forceNextUpdate || (!lastConfig.equals(config))) {
            lastConfig = config;
            forceNextUpdate = false;
            return true;
        }
        return false;
    }
}
