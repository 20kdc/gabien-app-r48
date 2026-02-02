/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.arrays;

import gabien.ui.UIElement;
import gabien.ui.elements.UILabel;
import gabien.ui.layouts.UIScrollLayout;
import r48.R48;
import r48.io.IntUtils;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.RORIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.ArrayElementSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.arrays.IArrayInterface.ArrayPosition;
import r48.schema.arrays.IArrayInterface.Host;
import r48.schema.op.BaseSchemaOps;
import r48.schema.util.EmbedDataDir;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;
import r48.ui.AppUI;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Notably, abstracting away sizeFixed and atLeastOne would just be an overcomplication.
 * Created on 12/28/16. Abstractified 16 Feb 2017.
 */
public abstract class ArraySchemaElement extends SchemaElement {
    public int sizeFixed, indexDisplayOffset;
    public int atLeast;

    public IArrayInterface uiHelper;
    // Usually null, but can point to something that resolves to an EnumSchemaElement
    public SchemaElement possibleEnumElement;

    // Used for pager state
    public final EmbedDataKey<Double> scrollPointKey = new EmbedDataKey<>();

    public ArraySchemaElement(R48 app, int fixedSize, int al1, int ido, IArrayInterface uiHelp) {
        super(app);
        sizeFixed = fixedSize;
        atLeast = al1;
        indexDisplayOffset = ido;
        uiHelper = uiHelp;
    }

    public ArraySchemaElement(R48 app, int fixedSize, int al1, int ido, IArrayInterface uiHelp, SchemaElement enumer) {
        this(app, fixedSize, al1, ido, uiHelp);
        possibleEnumElement = enumer;
    }

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost launcher, final SchemaPath path2) {
        if (target.getType() != '[')
            return objectHasBecomeInvalidScreen(path2);
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this, false) : path2;
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(launcher, scrollPointKey, target);
        final R48 app = launcher.getApp();

        uiHelper.provideInterfaceFrom(new Host() {
            LinkedList<UIElement> uie = new LinkedList<>();

            @Override
            public void panelsClear() {
                uie.clear();
                uiSVL.panelsSet();
            }
            
            @Override
            public void panelsAdd(UIElement element) {
                uie.add(element);
            }

            @Override
            public void panelsFinished() {
                uiSVL.panelsSet(uie);
            }

            @Override
            public R48 getApp() {
                return app;
            }

            @Override
            public AppUI getAppUI() {
                return launcher.getAppUI();
            }
        }, launcher.getValidity(), launcher.embedContext(target), new IArrayInterface.Array() {
            
            @Override
            public int resolveTrueSelection(ArrayPosition[] positions, int selection, boolean isEnd) {
                if (selection < 0)
                    return -1;
                if (selection >= positions.length)
                    return target.getALen();
                ArrayPosition ap = positions[selection];
                return isEnd ? ap.trueEnd : ap.trueStart;
            }
            
            @Override
            public SchemaPath getTrueSchemaPath() {
                return path;
            }
            
            @Override
            public ISchemaHost getTrueSchemaHost() {
                return launcher;
            }
            
            @Override
            public IRIO getTrueIRIO() {
                return target;
            }
            
            @Override
            public ArrayPosition[] getPositions() {
                return ArraySchemaElement.this.getPositions(target, launcher, path);
            }
        });

        return uiSVL;
    }

    private IArrayInterface.ArrayPosition[] getPositions(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        AppUI U = launcher.getAppUI();
        int nextAdvance;
        LinkedList<IArrayInterface.ArrayPosition> positions = new LinkedList<>();
        int alen = target.getALen();
        HashMap<Integer, Integer> indentAnchors = new HashMap<Integer, Integer>();
        for (int i = 0; i < alen; i += nextAdvance) {
            nextAdvance = 1;
            int pLevel = elementPermissionsLevel(i, target);
            if (pLevel < 1)
                continue;
            SchemaPath ind = path.arrayHashIndex(DMKey.of(i), "[" + (i + indexDisplayOffset) + "]");

            SchemaElement subelem = getElementSchema(i);
            int subelemId = 0;
            GroupInfo ec = getGroupInfo(target, i, indentAnchors);
            boolean hasNIdxSchema = false;
            if (ec == null) {
                nextAdvance = 1;
            } else {
                nextAdvance = ec.groupLength;
                subelem = ec.element;
                subelemId = ec.indent;
                hasNIdxSchema = true;
            }

            Supplier<Runnable> deleter = getRemovalCallback(pLevel, target, launcher, i, nextAdvance, path, ind);
            Runnable addition = getAdditionCallback(target, launcher, i, path, ind);
            Runnable clipAddition = getClipAdditionCallback(launcher.getAppUI(), target, i, path);

            UIElement uie;
            if (hasNIdxSchema) {
                uie = subelem.buildHoldingEditor(target, launcher, path);
            } else {
                uie = subelem.buildHoldingEditor(target.getAElem(i), launcher, ind);
            }

            String dispData = (i + indexDisplayOffset) + " ";
            if (possibleEnumElement != null) {
                SchemaElement se = AggregateSchemaElement.extractField(possibleEnumElement, null);
                dispData = ((EnumSchemaElement) se).viewValue(DMKey.of(i + indexDisplayOffset), EnumSchemaElement.Prefix.Prefix) + " ";
            }

            IRIO[] copyHelpElems = new IRIO[nextAdvance];
            for (int j = 0; j < copyHelpElems.length; j++)
                copyHelpElems[j] = target.getAElem(i + j);

            IArrayInterface.ArrayPosition position = new IArrayInterface.ArrayPosition(i, i + nextAdvance, dispData, copyHelpElems, uie, subelemId, deleter, addition, clipAddition);
            positions.add(position);
        }
        // Append position; quite ugly, really
        int appendIdx = getAppendIdx(target);
        if (elementPermissionsLevel(appendIdx, target) != 0) {
            SchemaPath ind = path.arrayHashIndex(DMKey.of(appendIdx), "[" + (appendIdx + indexDisplayOffset) + "]");
            IArrayInterface.ArrayPosition position = new IArrayInterface.ArrayPosition(appendIdx, appendIdx, (appendIdx + indexDisplayOffset) + " ", null, null, 0, null, getAdditionCallback(target, launcher, appendIdx, path, ind), getClipAdditionCallback(U, target, appendIdx, path));
            positions.add(position);
        }
        return positions.toArray(new IArrayInterface.ArrayPosition[0]);
    }

    private Supplier<Runnable> getRemovalCallback(final int pLevel, final IRIO target, final ISchemaHost launcher, final int mi, final int thisNextAdvance, final SchemaPath path, final SchemaPath ind) {
        if (pLevel < 2)
            return null;
        if (sizeFixed != -1)
            return null;
        return new Supplier<Runnable>() {
            @Override
            public Runnable get() {
                for (int j = 0; j < thisNextAdvance; j++)
                    target.rmAElem(mi);
                return new Runnable() {
                    @Override
                    public void run() {
                        path.changeOccurred(false);
                    }
                };
            }
        };
    }

    private Runnable getAdditionCallback(final IRIO target, final ISchemaHost launcher, final int i, final SchemaPath path, final SchemaPath ind) {
        if (sizeFixed != -1)
            return null;
        return new Runnable() {
            @Override
            public void run() {
                // This is specifically to deal with issues in at-least-1 cases and the like
                while (target.getALen() < i)
                    target.addAElem(target.getALen());
                SchemaElement subelem = getElementSchema(i);
                IRIO rio = target.addAElem(i);
                subelem.modifyVal(rio, ind, true);

                // whack the UI
                path.changeOccurred(false);
                // We don't actually know the command appeared where we told it to appear.
                // Rescan.
                int alen = target.getALen();
                for (int j = 0; j < alen; j++) {
                    if (target.getAElem(j) == rio) {
                        // Perform *magic*.
                        // What this means is that the subclass is given everything it needs to, theoretically,
                        //  construct a contrived sequence of schema paths that lead to the 'user' switching into the target element,
                        //  selecting something in there, and popping up a command selection dialog.
                        elementOnCreateMagic(target, j, launcher, ind, path);
                        return;
                    }
                }
            }
        };
    }

    private Runnable getClipAdditionCallback(final AppUI U, final IRIO target, final int i, final SchemaPath path) {
        if (sizeFixed != -1)
            return null;
        return new Runnable() {
            @Override
            public void run() {
                RORIO ro = app.theClipboard;
                if (ro != null) {
                    // could have changed
                    if (ro.getType() == '[') {
                        int roLen = ro.getALen();
                        try {
                            // Insert in reverse
                            for (int j = roLen - 1; j >= 0; j--)
                                target.addAElem(i).setDeepClone(ro.getAElem(j));
                        } catch (Exception e) {
                            U.launchDialog(T.s.array_dCFCompat, e);
                        }
                        // whack the UI
                        path.changeOccurred(false);
                    } else {
                        U.launchDialog(T.s.array_dCFNotArray);
                    }
                } else {
                    U.launchDialog(T.s.array_dCFEmpty);
                }
            }
        };
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path2, boolean setDefault) {
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this, false) : path2;
        setDefault = checkType(target, '[', null, setDefault);
        if (setDefault) {
            target.setArray();
            if (target.getALen() < atLeast)
                IntUtils.resizeArrayTo(target, atLeast);
        }
        boolean debugMod = false;
        boolean modified = setDefault;
        if (debugMod && modified)
            System.out.println("MOD: setDefault");
        if (sizeFixed != -1) {
            if (target.getALen() != sizeFixed) {
                if (debugMod)
                    System.out.println("MOD: size = " + sizeFixed + ", was " + target.getALen());
                IntUtils.resizeArrayTo(target, sizeFixed);
                modified = true;
            }
        } else if (target.getALen() < atLeast) {
            IntUtils.resizeArrayTo(target, atLeast);
            if (debugMod)
                System.out.println("MOD: size AL " + atLeast);
            modified = true;
        }
        HashMap<Integer, Integer> indentAnchors = new HashMap<Integer, Integer>();
        while (true) {
            int alen = target.getALen();
            for (int j = 0; j < alen; j++) {
                IRIO rio = target.getAElem(j);
                // Fun fact: There's a reason for this not-quite-linear timeline.
                // It's because otherwise, when the subelement tries to notify the array of the modification,
                //  it will lead to an infinite loop!
                // So it has to be able to see it's own object for the loop to terminate.
                // (Later: This got changed around a bit in a restructuring. Point is, target.arrVal[j] == rio)
                getElementSchema(j).modifyVal(rio, path.arrayHashIndex(DMKey.of(j), "[" + j + "]"), setDefault);
            }
            int groupStep;
            for (int j = 0; j < alen; j += groupStep) {
                GroupInfo ec = getGroupInfo(target, j, indentAnchors);
                if (ec == null) {
                    groupStep = 1;
                    continue;
                }
                ec.element.modifyVal(target, path, setDefault);
                groupStep = ec.groupLength;
            }
            boolean aca = autoCorrectArray(target, path);
            if (debugMod && aca)
                System.out.println("MOD: ACA");
            modified = modified || aca;
            if (!aca)
                break;
            setDefault = false;
        }
        if (modified)
            path.changeOccurred(true);
    }

    @Override
    public void visitChildren(IRIO target, SchemaPath path2, Visitor v, boolean detailedPaths) {
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this, false) : path2;
        HashMap<Integer, Integer> indentAnchors = new HashMap<>();
        int alen = target.getALen();
        for (int j = 0; j < alen; j++) {
            IRIO rio = target.getAElem(j);
            getElementSchema(j).visit(rio, path.arrayHashIndex(DMKey.of(j), "[" + j + "]"), v, detailedPaths);
        }
        int groupStep;
        for (int j = 0; j < alen; j += groupStep) {
            GroupInfo ec = getGroupInfo(target, j, indentAnchors);
            if (ec == null) {
                groupStep = 1;
                continue;
            }
            ec.element.visit(target, path, v, detailedPaths);
            groupStep = ec.groupLength;
        }
    }

    // Used to do the correct tagging so that updates to children will affect the parent
    public boolean monitorsSubelements() {
        return false;
    }

    /**
     * Finds the instance tracker in target.
     */
    public static int findActualStart(IRIO target, IRIO tracker) {
        if (target.getType() != '[')
            return -1;
        int alen = target.getALen();
        for (int i = 0; i < alen; i++)
            if (target.getAElem(i) == tracker)
                return i;
        return -1;
    }

    /**
     * This provides the 'interior' for getElementContextualWindowSchema.
     * That function provides the 'tracking' on array changes.
     * The main thing this function does is account for if the object is no longer part of a group.
     */
    protected final SchemaElement getElementContextualWindowSchemaUntracked(IRIO arr, final int start, final EmbedDataDir embedDataDir) {
        GroupInfo gi = getGroupInfo(arr, start, new HashMap<>());
        if (gi == null) {
            SchemaElement elm = getElementSchema(start);
            return new ArrayElementSchemaElement(app, start, () -> "", elm, null, false);
        }
        return gi.elementContextualUntracked.apply(embedDataDir);
    }

    /**
     * Returns a contextualized schema for the given array element IRIO.
     * This schema is applied to the array, and tracks the element by its IRIO.
     * Public because this is used by stuff like Find Translatables to get "access" into editing a command, among other things.
     * Note that this expects the command IRIO (this acts as the "array index").
     * The IRIO used for this element is expected to be the list.
     */
    public SchemaElement getGroupTrackedWindowSchema(final IRIO tracker) {
        return new TrackingSE(app, this, tracker);
    }

    // Used by certain rather complicated array types that want to give the user a *specific* UI on the creation of an element.
    // The element gets created & acknowledged, then this kickstarts the UI for editing details.
    // If the user exits this UI via the "back" method, further contrived sequences can be created to delete the element.
    // As for closing the window... hm.
    protected void elementOnCreateMagic(IRIO target, int i, ISchemaHost launcher, SchemaPath ind, SchemaPath path) {
    }

    // Allows performing automatic correction of structural issues,
    //  after any data issues have been cleaned up.
    // path is already in the array entry.
    // Note that this is solely for auto-correction.
    // If setDefault is required, just override modifyVal
    //  and don't bother to call super if setDefault is true,
    //  instead doing whatever you need yourself.
    // Also note that if a modification is performed, another check is done so that things like indent processing can run.
    protected abstract boolean autoCorrectArray(IRIO array, SchemaPath path);

    /**
     * Update 19th Dec. 2025: This is the new home of group control.
     * This function returns both the length of a group and its element schema, along with indent.
     * Note that for modifyVal purposes this acts *in addition* to getElementSchema.
     */
    protected GroupInfo getGroupInfo(IRIO arr, int start, final HashMap<Integer, Integer> indentAnchors) {
        return null;
    }

    protected abstract SchemaElement getElementSchema(int j);

    /**
     * 0: Do not even show this element.
     * 1: Show & allow editing of this element, but disallow deletion.
     * 2: All permissions.
     * (Used to prevent a user shooting themselves in the foot - should not be considered a serious mechanism.)
     * If you have start-of-array nulls, you may need to edit getAppendIdx.
     */
    protected int elementPermissionsLevel(int i, IRIO target) {
        boolean canDelete = (sizeFixed == -1) && (!(target.getALen() <= atLeast));
        return canDelete ? 2 : 1;
    }

    /**
     * Gets the append index; where the append button is placed.
     * This is expected to be at the end of the array.
     */
    protected int getAppendIdx(IRIO target) {
        return target.getALen();
    }

    public static final class GroupInfo {
        public int indent;
        /**
         * The 'outer element'.
         * This schema element is attached to the array, is presented to the user from the array interface, and participates in modifyVal.
         * It may or may not attempt to track array index changes.
         */
        public SchemaElement element;
        /**
         * The 'contextual element'; the 'core' of the group.
         * This schema element is attached to the array at an index set when the GroupInfo is returned.
         * The EmbedDataDir passed in is assumed to be created in getGroupTrackedWindowSchema (or somewhere equivalent).
         * This is used by getElementContextualWindowSchemaUntracked.
         */
        public Function<EmbedDataDir, SchemaElement> elementContextualUntracked;
        /**
         * Group length. This must be at least 1.
         */
        public int groupLength;

        public GroupInfo(int id, SchemaElement elem, Function<EmbedDataDir, SchemaElement> ecu, int gl) {
            indent = id;
            element = elem;
            elementContextualUntracked = ecu;
            groupLength = gl;
            if (gl < 1)
                throw new IllegalArgumentException("groupLength < 1");
        }
    }

    /**
     * Finds the tracked group in an array, and routes through to getElementContextualWindowSchemaUntracked. 
     */
    public static final class TrackingSE extends SchemaElement {
        public final ArraySchemaElement parentArraySE; 
        public final IRIO tracker;
        public final EmbedDataDir ecwsKey = new EmbedDataDir();

        public TrackingSE(R48 app, ArraySchemaElement ase, IRIO t) {
            super(app);
            parentArraySE = ase;
            tracker = t;
        }

        @Override
        public boolean declaresSelfEditorOf(RORIO target, RORIO check) {
            return check == tracker;
        }

        @Override
        public UIElement buildHoldingEditorImpl(IRIO target, ISchemaHost launcher, SchemaPath path) {
            int actualStart = findActualStart(target, tracker);
            if (actualStart == -1)
                return new UILabel(T.s.cmdOutOfList, app.f.schemaFieldTH);
            GroupInfo ec = parentArraySE.getGroupInfo(target, actualStart, new HashMap<>());
            int length = ec == null ? 1 : ec.groupLength;
            int actualEnd = actualStart + length;
            launcher.addOperatorContext(target, BaseSchemaOps.CTXPARAM_ARRAYSTART, DMKey.of(actualStart));
            launcher.addOperatorContext(target, BaseSchemaOps.CTXPARAM_ARRAYEND, DMKey.of(actualEnd));
            return parentArraySE.getElementContextualWindowSchemaUntracked(target, actualStart, ecwsKey).buildHoldingEditor(target, launcher, path);
        }

        @Override
        public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
            int actualStart = findActualStart(target, tracker);
            if (actualStart == -1)
                return;
            parentArraySE.getElementContextualWindowSchemaUntracked(target, actualStart, ecwsKey).modifyVal(target, path, setDefault);
        }

        @Override
        public void visitChildren(IRIO target, SchemaPath path, Visitor v, boolean detailedPaths) {
            int actualStart = findActualStart(target, tracker);
            if (actualStart == -1)
                return;
            parentArraySE.getElementContextualWindowSchemaUntracked(target, actualStart, ecwsKey).visit(target, path, v, detailedPaths);
        }
    }
}
