/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.ui.utilitybelt;

import gabien.ui.IFunction;

import java.util.HashSet;

/**
 * Created on October 09, 2018.
 */
public class FillAlgorithm {
    public final HashSet<Point> executedPointSet = new HashSet<Point>();
    public HashSet<Point> availablePointSet = new HashSet<Point>();
    public final IFunction<Point, Point> inBounds;
    public final IFunction<Point, Boolean> matchesFill;

    public FillAlgorithm(IFunction<Point, Point> bounds, IFunction<Point, Boolean> points) {
        inBounds = bounds;
        matchesFill = points;
    }

    public void pass() {
        HashSet<Point> aps = new HashSet<Point>(availablePointSet);
        availablePointSet.clear();
        for (Point p : aps) {
            p = inBounds.apply(p);
            if (p == null)
                continue;
            if (executedPointSet.contains(p))
                continue;
            if (matchesFill.apply(p)) {
                executedPointSet.add(p);
                availablePointSet.add(p.offset(0, -1));
                availablePointSet.add(p.offset(0, 1));
                availablePointSet.add(p.offset(-1, 0));
                availablePointSet.add(p.offset(1, 0));
            }
        }
    }

    public static final class Point {
        public final int x, y;

        public Point(int x1, int y1) {
            x = x1;
            y = y1;
        }

        @Override
        public int hashCode() {
            return x ^ y;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Point) {
                if (((Point) o).x != x)
                    return false;
                if (((Point) o).y != y)
                    return false;
                return true;
            }
            return false;
        }

        public Point offset(int i, int i1) {
            return new Point(x + i, y + i1);
        }
    }
}
