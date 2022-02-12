package r48.maptools.deep;

import r48.ui.utilitybelt.FillAlgorithm;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created on December 30, 2018.
 */
public class TOutline {
    public static TOutline[] mainItems;

    public final HashSet<Line> set = new HashSet<Line>();
    public final Type[] basis;

    public TOutline() {
        basis = null;
    }

    public enum Type {
        Inward,
        Outward,
        Full,
        Empty
    }

    public TOutline(Type[] base) {
        basis = base;
        // just handles what is necessary
        if (base[0] == Type.Inward) {
            set.add(new Line(0, 0, 1, 0));
            set.add(new Line(1, 0, 0, 1));
            set.add(new Line(0, 1, 0, 0));
        } else if (base[0] == Type.Outward) {
            set.add(new Line(0, 1, 1, 0));
        } else if (base[0] == Type.Full) {
            set.add(new Line(0, 1, 0, 0));
            set.add(new Line(0, 0, 1, 0));
        } else if (base[0] == Type.Empty) {
            set.add(new Line(0, 0, 0, 1));
            set.add(new Line(1, 0, 0, 0));
        }
        if (base[1] == Type.Inward) {
            set.add(new Line(1, 0, 2, 0));
            set.add(new Line(2, 0, 2, 1));
            set.add(new Line(2, 1, 1, 0));
        } else if (base[1] == Type.Outward) {
            set.add(new Line(1, 0, 2, 1));
        } else if (base[1] == Type.Full) {
            set.add(new Line(1, 0, 2, 0));
            set.add(new Line(2, 0, 2, 1));
        } else if (base[1] == Type.Empty) {
            set.add(new Line(2, 0, 1, 0));
            set.add(new Line(2, 1, 2, 0));
        }
        if (base[2] == Type.Inward) {
            set.add(new Line(0, 1, 1, 2));
            set.add(new Line(1, 2, 0, 2));
            set.add(new Line(0, 2, 0, 1));
        } else if (base[2] == Type.Outward) {
            set.add(new Line(1, 2, 0, 1));
        } else if (base[2] == Type.Full) {
            set.add(new Line(1, 2, 0, 2));
            set.add(new Line(0, 2, 0, 1));
        } else if (base[2] == Type.Empty) {
            set.add(new Line(0, 2, 1, 2));
            set.add(new Line(0, 1, 0, 2));
        }
        if (base[3] == Type.Inward) {
            set.add(new Line(1, 2, 2, 1));
            set.add(new Line(2, 1, 2, 2));
            set.add(new Line(2, 2, 1, 2));
        } else if (base[3] == Type.Outward) {
            set.add(new Line(2, 1, 1, 2));
        } else if (base[3] == Type.Full) {
            set.add(new Line(2, 1, 2, 2));
            set.add(new Line(2, 2, 1, 2));
        } else if (base[3] == Type.Empty) {
            set.add(new Line(2, 2, 2, 1));
            set.add(new Line(1, 2, 2, 2));
        }
    }

    public static Type[] getConnectorTypes(int item) {
        Type fillType = item < 16 ? Type.Empty : Type.Full;
        Type plantType = item < 16 ? Type.Inward : Type.Outward;
        Type[] res = new Type[] {
                fillType,
                fillType,
                fillType,
                fillType,
        };
        if ((item & 1) != 0)
            res[0] = plantType;
        if ((item & 2) != 0)
            res[1] = plantType;
        if ((item & 4) != 0)
            res[2] = plantType;
        if ((item & 8) != 0)
            res[3] = plantType;
        return res;
    }

    static {
        mainItems = new TOutline[32];
        for (int k = 0; k < mainItems.length; k++)
            mainItems[k] = new TOutline(getConnectorTypes(k));
    }

    public LinkedList<Integer> getValidIds() {
        LinkedList<Integer> lli = new LinkedList<Integer>();
        for (int i = 0; i < mainItems.length; i++)
            if (mainItems[i].set.containsAll(set))
                lli.add(i);
        return lli;
    }

    public int getUsedId() {
        int usedId = -1;
        int points = Integer.MIN_VALUE;
        for (int i = 0; i < mainItems.length; i++) {
            if (mainItems[i].set.containsAll(set)) {
                int mP = 0;
                for (Type l : mainItems[i].basis) {
                    if (l == Type.Full || l == Type.Empty)
                        mP++;
                }
                if (mP > points) {
                    usedId = i;
                    points = mP;
                }
            }
        }
        return usedId;
    }

    public short getUsedIdReal() {
        int i = getUsedId();
        if (i < 16) {
            return (short) (i * 50);
        } else {
            return (short) (2000 + ((i - 16) * 50));
        }
    }

    public static class Line {
        public final FillAlgorithm.Point a, b;

        public Line(int x1, int y1, int x2, int y2) {
            a = new FillAlgorithm.Point(x1, y1);
            b = new FillAlgorithm.Point(x2, y2);
        }

        public Line(FillAlgorithm.Point a, FillAlgorithm.Point b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Line))
                return false;
            if (((Line) o).a.equals(a))
                if (((Line) o).b.equals(b))
                    return true;
            return false;
        }

        @Override
        public int hashCode() {
            return a.hashCode() ^ b.hashCode();
        }

        public Line transformFor(int i, int i1) {
            return new Line(a.offset((i * 2), (i1 * 2)), b.offset((i * 2), (i1 * 2)));
        }
    }

    public static boolean isLineValid(Line cl) {
        if (!isPointValid(cl.a))
            return false;
        if (!isPointValid(cl.b))
            return false;
        return true;
    }

    private static boolean isPointValid(FillAlgorithm.Point b) {
        if (b.x < 0)
            return false;
        if (b.y < 0)
            return false;
        if (b.x > 2)
            return false;
        if (b.y > 2)
            return false;
        return !((b.x == 1) && (b.y == 1));
    }
}
