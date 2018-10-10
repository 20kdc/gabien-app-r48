/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized.genpos2;

import java.util.LinkedList;

/**
 * Created on October 10, 2018.
 */
public class GP2Timeline {
    private LinkedList<TimePoint> points = new LinkedList<TimePoint>();

    public GP2Timeline(TimePoint n) {
        points.add(n);
    }

    public void addPoint(TimePoint point) {
        for (TimePoint n : points) {
            if (n.frame == point.frame) {
                points.remove(n);
                break;
            }
        }
        points.add(point);
    }

    public void removePoint(int frame) {
        if (points.size() > 1) {
            for (TimePoint n : points) {
                if (n.frame == frame) {
                    points.remove(n);
                    break;
                }
            }
        }
    }

    public TimePoint getFirstPoint() {
        return getNextPoint(Integer.MIN_VALUE);
    }

    public TimePoint getLastPoint() {
        return getPrevPoint(Integer.MAX_VALUE);
    }

    public TimePoint getPrevPoint(int frame) {
        TimePoint val = null;
        int highestFrame = Integer.MIN_VALUE;
        for (TimePoint n : points) {
            if ((n.frame >= highestFrame) && (n.frame <= frame)) {
                val = n;
                highestFrame = n.frame;
            }
        }
        return val;
    }

    public TimePoint getNextPoint(int frame) {
        TimePoint val = null;
        int lowestFrame = Integer.MAX_VALUE;
        for (TimePoint n : points) {
            if ((n.frame <= lowestFrame) && (n.frame >= frame)) {
                val = n;
                lowestFrame = n.frame;
            }
        }
        return val;
    }

    public Object getValueAt(int frame) {
        // WARNING! Hilariously Unsafe Operations occurring...
        TimePoint a = getPrevPoint(frame);
        TimePoint b = getNextPoint(frame);

        // If there are no points behind-equal, then they're all in front
        if (a == null)
            a = b;
        // If there are no points ahead-equal, then they're all behind
        if (b == null)
            b = a;

        if (a.frame == b.frame)
            return a.value;
        int length = b.frame - a.frame;
        if (a.value instanceof Double) {
            double dst = (((Double) b.value) - ((Double) a.value)) * (frame - a.frame);
            return (Double) (((Double) a.value) + (dst / length));
        } else if (a.value instanceof Integer) {
            int dst = (((Integer) b.value) - ((Integer) a.value)) * (frame - a.frame);
            return (Integer) (((Integer) a.value) + (dst / length));
        }
        return a.value;
    }

    public void attemptOptimize(int a, int b) {
        LinkedList<TimePoint> removedPoints = new LinkedList<TimePoint>();
        for (TimePoint n : points)
            if ((n.frame > a) && (n.frame < b))
                removedPoints.add(n);
        if (removedPoints.size() == points.size())
            return;
        for (int i = a; i <= b; i++) {
            Object oldValue = getValueAt(i);
            points.removeAll(removedPoints);
            Object newValue = getValueAt(i);
            points.addAll(removedPoints);
            if (!newValue.equals(oldValue))
                return;
        }
        points.removeAll(removedPoints);
    }

    public void optimize() {
        int a = getFirstPoint().frame;
        // The +1 is to allow optimizing out the last frame.
        int b = getLastPoint().frame + 1;
        for (int i = a; i < b - 1; i++)
            for (int j = i + 2; j <= b; j++)
                attemptOptimize(i, j);
    }

    public static class TimePoint<O> {
        public final int frame;
        public final O value;

        public TimePoint(int frame, O value) {
            this.frame = frame;
            this.value = value;
        }
    }
}
