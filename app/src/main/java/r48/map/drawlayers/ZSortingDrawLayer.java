/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.map.drawlayers;

import r48.map.MapViewDrawContext;

import java.util.*;

/**
 * Z-sorts the contents after creation.
 * Created on 28th November 2019.
 */
public class ZSortingDrawLayer implements IMapViewDrawLayer {
    protected final LinkedList<IZSortedObject> zSorting = new LinkedList<IZSortedObject>();
    public final HashSet<SignalMapViewLayer> signals = new HashSet<SignalMapViewLayer>();

    public ZSortingDrawLayer() {
    }

    protected void completeSetup() {
        Collections.sort(zSorting, new Comparator<IZSortedObject>() {
            @Override
            public int compare(IZSortedObject t0, IZSortedObject t1) {
                long zA = t0.getZ();
                long zB = t1.getZ();
                if (zA < zB)
                    return -1;
                if (zA > zB)
                    return 1;
                return 0;
            }
        });
    }

    public interface IZSortedObject extends IMapViewDrawLayer {
        long getZ();
        SignalMapViewLayer getControlSignal();
    }

    @Override
    public String getName() {
        return "Z-Sorting Draw Layer";
    }

    @Override
    public void draw(MapViewDrawContext mvdc) {
        for (IZSortedObject obj : zSorting) {
            SignalMapViewLayer controlSignal = obj.getControlSignal();
            if ((controlSignal == null) || controlSignal.active)
                obj.draw(mvdc);
        }
        for (SignalMapViewLayer signal : signals)
            signal.active = false;
    }

    public static class SignalMapViewLayer implements IMapViewDrawLayer {
        private final String signalName;
        public boolean active;

        public SignalMapViewLayer(String sn) {
            signalName = sn;
        }

        @Override
        public String getName() {
            return signalName;
        }

        @Override
        public void draw(MapViewDrawContext mvdc) {
            active = true;
        }
    }
}
