package r48.maptools.deep;

import gabien.ui.UIElement;
import r48.ui.utilitybelt.FillAlgorithm;

import java.util.*;

/**
 * Created on December 30, 2018.
 */
public class MOutline {
    private LinkedList<TOutline.Line> set = new LinkedList<TOutline.Line>();
    private HashMap<TOutline.Line, Boolean> testedValidity = new HashMap<TOutline.Line, Boolean>();
    private Set<FillAlgorithm.Point> involvedTiles;

    public boolean containsLine(TOutline.Line tl) {
        return set.contains(tl);
    }

    public void append(TOutline.Line tl) {
        set.add(tl);
        testedValidity.clear();
        involvedTiles = null;
    }

    public TOutline.Line removeLast() {
        if (set.size() > 0) {
            TOutline.Line l = set.removeLast();
            testedValidity.clear();
            involvedTiles = null;
            return l;
        }
        return null;
    }


    public boolean validWith(TOutline.Line tl) {
        if (testedValidity.containsKey(tl))
            return testedValidity.get(tl);
        MOutline mo = new MOutline();
        mo.set.addAll(set);
        mo.set.add(tl);
        boolean v = mo.valid();
        testedValidity.put(tl, v);
        return v;
    }

    public Set<FillAlgorithm.Point> getAllInvolvedTiles() {
        if (involvedTiles == null) {
            involvedTiles = new HashSet<FillAlgorithm.Point>();
            for (TOutline.Line cl : set)
                addValidityForLine(involvedTiles, cl);
            involvedTiles = Collections.unmodifiableSet(involvedTiles);
        }
        return involvedTiles;
    }

    public static boolean addValidityForLine(Set<FillAlgorithm.Point> correlation, TOutline.Line cl) {
        int tileX = UIElement.sensibleCellDiv(cl.a.x, 2);
        int tileY = UIElement.sensibleCellDiv(cl.a.y, 2);
        boolean wouldBeValid = false;
        // Need to check the tiles 'behind' as well
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (TOutline.isLineValid(cl.transformFor(i - tileX, j - tileY))) {
                    if (correlation != null)
                        correlation.add(new FillAlgorithm.Point(tileX - i, tileY - j));
                    wouldBeValid = true;
                }
            }
        }
        return wouldBeValid;
    }

    public TOutline getOutlineForTile(int x, int y) {
        TOutline res = new TOutline();
        for (TOutline.Line cl : set) {
            TOutline.Line rl = cl.transformFor(-x, -y);
            if (TOutline.isLineValid(rl))
                res.set.add(rl);
        }
        if (res.set.size() > 0)
            return res;
        return null;
    }

    // Uncached validity test.
    private boolean valid() {
        for (FillAlgorithm.Point p : getAllInvolvedTiles()) {
            TOutline to = getOutlineForTile(p.x, p.y);
            if (to == null) {
                System.out.println("getAllInvolvedTiles and getOutlineForTile conflict (should never happen)");
            } else {
                if (to.getValidIds().size() == 0)
                    return false;
            }
        }
        return true;
    }
}
