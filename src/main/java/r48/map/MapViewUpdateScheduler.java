/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.map;

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
