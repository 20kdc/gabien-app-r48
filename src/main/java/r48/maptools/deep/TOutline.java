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

    public TOutline(Type[] base) {
        basis = base;
        // just handles what is necessary
        if (base[0] == Type.TLCorner) {
            set.add(new Line(0, 0, 1, 0));
            set.add(new Line(1, 0, 0, 1));
            set.add(new Line(0, 1, 0, 0));
        }
        if (base[1] == Type.TRCorner) {
            set.add(new Line(1, 0, 2, 0));
            set.add(new Line(2, 0, 2, 1));
            set.add(new Line(2, 1, 1, 0));
        }
        if (base[2] == Type.BLCorner) {
            set.add(new Line(0, 1, 1, 2));
            set.add(new Line(1, 2, 0, 2));
            set.add(new Line(0, 2, 0, 1));
        }
        if (base[3] == Type.BRCorner) {
            set.add(new Line(1, 2, 2, 1));
            set.add(new Line(2, 1, 2, 2));
            set.add(new Line(2, 2, 1, 2));
        }
        // inv
        if (base[0] == Type.BRCorner)
            set.add(new Line(0, 1, 1, 0));
        if (base[1] == Type.BLCorner)
            set.add(new Line(1, 0, 2, 1));
        if (base[2] == Type.TRCorner)
            set.add(new Line(1, 2, 0, 1));
        if (base[3] == Type.TLCorner)
            set.add(new Line(2, 1, 1, 2));
        // full-edge (ignoring invalid interior)
        if (base[0] == Type.Full) {
            set.add(new Line(0, 1, 0, 0));
            set.add(new Line(0, 0, 1, 0));
        }
        if (base[1] == Type.Full) {
            set.add(new Line(1, 0, 2, 0));
            set.add(new Line(2, 0, 2, 1));
        }
        if (base[2] == Type.Full) {
            set.add(new Line(1, 2, 0, 2));
            set.add(new Line(0, 2, 0, 1));
        }
        if (base[3] == Type.Full) {
            set.add(new Line(2, 1, 2, 2));
            set.add(new Line(2, 2, 1, 2));
        }
        // And for empty
        if (base[0] == Type.Empty) {
            set.add(new Line(0, 0, 0, 1));
            set.add(new Line(1, 0, 0, 0));
        }
        if (base[1] == Type.Empty) {
            set.add(new Line(2, 0, 1, 0));
            set.add(new Line(2, 1, 2, 0));
        }
        if (base[2] == Type.Empty) {
            set.add(new Line(0, 2, 1, 2));
            set.add(new Line(0, 1, 0, 2));
        }
        if (base[3] == Type.Empty) {
            set.add(new Line(2, 2, 2, 1));
            set.add(new Line(1, 2, 2, 2));
        }
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

    public enum Type {
        TLCorner,
        TRCorner,
        BLCorner,
        BRCorner,
        Full,
        Empty
    }


    public static Type[] getConnectorTypes(int item) {
        Type[] res = new Type[] {
                item >= 16 ? Type.Full : Type.Empty,
                item >= 16 ? Type.Full : Type.Empty,
                item >= 16 ? Type.Full : Type.Empty,
                item >= 16 ? Type.Full : Type.Empty
        };
        if (item < 16) {
            if ((item & 1) != 0)
                res[0] = Type.TLCorner;
            if ((item & 2) != 0)
                res[1] = Type.TRCorner;
            if ((item & 4) != 0)
                res[2] = Type.BLCorner;
            if ((item & 8) != 0)
                res[3] = Type.BRCorner;
        } else {
            if ((item & 1) != 0)
                res[0] = Type.BRCorner;
            if ((item & 2) != 0)
                res[1] = Type.BLCorner;
            if ((item & 4) != 0)
                res[2] = Type.TRCorner;
            if ((item & 8) != 0)
                res[3] = Type.TLCorner;
        }
        return res;
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
