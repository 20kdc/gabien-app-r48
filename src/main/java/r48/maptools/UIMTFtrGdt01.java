/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.maptools;

import gabien.IGrDriver;
import gabien.ui.UILabel;
import r48.map.IMapToolContext;
import r48.map.IMapViewCallbacks;
import r48.ui.utilitybelt.FillAlgorithm;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created on December 30, 2018.
 */
public class UIMTFtrGdt01 extends UIMTBase implements IMapViewCallbacks {
    public ConnectorOutline workspace;
    public int wsX, wsY;
    public int intileX, intileY;

    public UIMTFtrGdt01(IMapToolContext o) {
        super(o);
        changeInner(new UILabel("EXPERIMENTAL FUNCTIONALITY\nThis is experimental. Try at your own risk.", 8), true);
    }

    @Override
    public short shouldDrawAt(boolean mouseAllowed, int cx, int cy, int tx, int ty, short there, int layer, int currentLayer) {
        return there;
    }

    @Override
    public int wantOverlay(boolean minimap) {
        return 1;
    }

    @Override
    public void performOverlay(int tx, int ty, IGrDriver igd, int px, int py, int ol, boolean minimap) {
        // It's pretty simple, so you'll want to put full pips to ????
        if (workspace != null) {
            if ((tx != wsX) || (ty != wsY))
                return;
            for (int i = -1; i < 4; i++) {
                for (int j = -1; j < 4; j++) {
                    Runnable optval = optValidity(i, j);
                    boolean gbi = (i == intileX) && (j == intileY);
                    int gb = gbi ? 0 : 255;
                    if ((optval != null) || gbi)
                        igd.clearRect(255, gb, gb, px + (i * 8) - 1, py + (j * 8) - 1, 2, 2);
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    igd.clearRect(255, 255, 255, px + (i * 8) - 1, py + (j * 8) - 1, 2, 2);
                }
            }
        }
    }

    private Runnable optValidity(final int i, final int j) {
        if (i < 0)
            return null;
        if (j < 0)
            return null;
        if (i > 2)
            return null;
        if (j > 2)
            return null;
        final ConnectorLine cl = new ConnectorLine(intileX, intileY, i, j);
        if (workspace.set.contains(cl))
            return null;

        workspace.set.add(cl);
        final LinkedList<Integer> ok = workspace.getValidIds();
        workspace.set.remove(cl);

        if (ok.size() > 0)
            return new Runnable() {
                @Override
                public void run() {
                    workspace.set.add(cl);
                    intileX = i;
                    intileY = j;
                    boolean needToDoThisNow = true;
                    for (int i = -1; i < 4; i++) {
                        for (int j = -1; j < 4; j++) {
                            if (optValidity(i, j) != null) {
                                needToDoThisNow = false;
                                break;
                            }
                        }
                        if (!needToDoThisNow)
                            break;
                    }
                    if (needToDoThisNow) {
                        int finx = ok.getFirst();
                        if (finx >= 16) {
                            finx -= 16;
                            finx *= 50;
                            finx += 2000;
                        } else {
                            finx *= 50;
                        }
                        mapToolContext.getMapView().mapTable.setTiletype(wsX, wsY, mapToolContext.getMapView().currentLayer, (short) finx);
                        mapToolContext.getMapView().passModificationNotification();
                        workspace = null;
                    }
                }
            };
        return null;
    }

    @Override
    public void performGlobalOverlay(IGrDriver igd, int px, int py, int l, boolean minimap, int eTileSize) {

    }

    @Override
    public void confirmAt(int x, int y, int pixx, int pixy, int layer) {
        int ps2 = mapToolContext.getMapView().tileSize / 2;
        int cpcX = (x * 2 * ps2) + pixx;
        int cpcY = (y * 2 * ps2) + pixy;
        cpcX += ps2 / 2;
        cpcY += ps2 / 2;
        cpcX = sensibleCellDiv(cpcX, ps2);
        cpcY = sensibleCellDiv(cpcY, ps2);

        if (workspace == null) {
            wsX = x;
            wsY = y;
        }
        int litx = cpcX - (wsX * 2);
        int lity = cpcY - (wsY * 2);
        if (workspace == null) {
            workspace = new ConnectorOutline(new ConnectorType[] {ConnectorType.Empty, ConnectorType.Empty, ConnectorType.Empty, ConnectorType.Empty});
            intileX = litx;
            intileY = lity;
        } else {
            Runnable rr = optValidity(litx, lity);
            if (rr != null)
                rr.run();
        }
    }

    @Override
    public boolean shouldIgnoreDrag() {
        return true;
    }

    public enum ConnectorType {
        TLCorner,
        TRCorner,
        BLCorner,
        BRCorner,
        Full,
        Empty
    }


    public static ConnectorType[] getConnectorTypes(int item) {
        ConnectorType[] res = new ConnectorType[] {
                item >= 16 ? ConnectorType.Full : ConnectorType.Empty,
                item >= 16 ? ConnectorType.Full : ConnectorType.Empty,
                item >= 16 ? ConnectorType.Full : ConnectorType.Empty,
                item >= 16 ? ConnectorType.Full : ConnectorType.Empty
        };
        if (item < 16) {
            if ((item & 1) != 0)
                res[0] = ConnectorType.TLCorner;
            if ((item & 2) != 0)
                res[1] = ConnectorType.TRCorner;
            if ((item & 4) != 0)
                res[2] = ConnectorType.BLCorner;
            if ((item & 8) != 0)
                res[3] = ConnectorType.BRCorner;
        } else {
            if ((item & 1) != 0)
                res[0] = ConnectorType.BRCorner;
            if ((item & 2) != 0)
                res[1] = ConnectorType.BLCorner;
            if ((item & 4) != 0)
                res[2] = ConnectorType.TRCorner;
            if ((item & 8) != 0)
                res[3] = ConnectorType.TLCorner;
        }
        return res;
    }

    public static class ConnectorOutline {
        public static ConnectorOutline[] mainItems;

        public HashSet<ConnectorLine> set = new HashSet<ConnectorLine>();

        public ConnectorOutline(ConnectorType[] base) {
            // just handles what is necessary
            if (base[0] == ConnectorType.TLCorner) {
                set.add(new ConnectorLine(0, 0, 1, 0));
                set.add(new ConnectorLine(1, 0, 0, 1));
                set.add(new ConnectorLine(0, 1, 0, 0));
            }
            if (base[1] == ConnectorType.TRCorner) {
                set.add(new ConnectorLine(1, 0, 2, 0));
                set.add(new ConnectorLine(2, 0, 2, 1));
                set.add(new ConnectorLine(2, 1, 1, 0));
            }
            if (base[2] == ConnectorType.BLCorner) {
                set.add(new ConnectorLine(0, 1, 1, 2));
                set.add(new ConnectorLine(1, 2, 0, 2));
                set.add(new ConnectorLine(0, 2, 0, 1));
            }
            if (base[3] == ConnectorType.BRCorner) {
                set.add(new ConnectorLine(1, 2, 2, 1));
                set.add(new ConnectorLine(2, 1, 2, 2));
                set.add(new ConnectorLine(2, 2, 1, 2));
            }
            // inv
            if (base[0] == ConnectorType.BRCorner)
                set.add(new ConnectorLine(0, 1, 1, 0));
            if (base[1] == ConnectorType.BLCorner)
                set.add(new ConnectorLine(1, 0, 2, 1));
            if (base[2] == ConnectorType.TRCorner)
                set.add(new ConnectorLine(1, 2, 0, 1));
            if (base[3] == ConnectorType.TLCorner)
                set.add(new ConnectorLine(2, 1, 1, 2));
            // full-edge (ignoring invalid interior)
            if (base[0] == ConnectorType.Full) {
                set.add(new ConnectorLine(0, 1, 0, 0));
                set.add(new ConnectorLine(0, 0, 1, 0));
            }
            if (base[1] == ConnectorType.Full) {
                set.add(new ConnectorLine(1, 0, 2, 0));
                set.add(new ConnectorLine(2, 0, 2, 1));
            }
            if (base[2] == ConnectorType.Full) {
                set.add(new ConnectorLine(1, 2, 0, 2));
                set.add(new ConnectorLine(0, 2, 0, 1));
            }
            if (base[3] == ConnectorType.Full) {
                set.add(new ConnectorLine(2, 1, 2, 2));
                set.add(new ConnectorLine(2, 2, 1, 2));
            }
        }

        public LinkedList<Integer> getValidIds() {
            LinkedList<Integer> lli = new LinkedList<Integer>();
            for (int i = 0; i < mainItems.length; i++)
                if (mainItems[i].set.containsAll(set))
                    lli.add(i);
            return lli;
        }
    }

    static {
        ConnectorOutline.mainItems = new ConnectorOutline[32];
        for (int k = 0; k < ConnectorOutline.mainItems.length; k++)
            ConnectorOutline.mainItems[k] = new ConnectorOutline(getConnectorTypes(k));
    }

    public static class ConnectorLine {
        public final FillAlgorithm.Point a, b;

        public ConnectorLine(int x1, int y1, int x2, int y2) {
            a = new FillAlgorithm.Point(x1, y1);
            b = new FillAlgorithm.Point(x2, y2);
        }

        public ConnectorLine(FillAlgorithm.Point a, FillAlgorithm.Point b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ConnectorLine))
                return false;
            if (((ConnectorLine) o).a.equals(a))
                if (((ConnectorLine) o).b.equals(b))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode();
        }
    }
}
