/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.arrays;

import gabien.uslx.append.*;
import gabien.ui.UIElement;
import gabien.ui.UIScrollLayout;
import r48.App;
import r48.io.IntUtils;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOFixnum;
import r48.io.data.RORIO;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.arrays.IArrayInterface.Host;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

import java.util.HashMap;
import java.util.LinkedList;

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
    private IntegerSchemaElement myUniqueStateInstance;

    public ArraySchemaElement(App app, int fixedSize, int al1, int ido, IArrayInterface uiHelp) {
        super(app);
        myUniqueStateInstance = new IntegerSchemaElement(app, 0);
        sizeFixed = fixedSize;
        atLeast = al1;
        indexDisplayOffset = ido;
        uiHelper = uiHelp;
    }

    public ArraySchemaElement(App app, int fixedSize, int al1, int ido, IArrayInterface uiHelp, SchemaElement enumer) {
        this(app, fixedSize, al1, ido, uiHelp);
        possibleEnumElement = enumer;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost launcher, final SchemaPath path2) {
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this, false) : path2;
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(launcher, this, target);
        final App app = launcher.getApp();

        uiHelper.provideInterfaceFrom(new Host() {
            @Override
            public void panelsClear() {
                uiSVL.panelsClear();
            }
            
            @Override
            public void panelsAdd(UIElement element) {
                uiSVL.panelsAdd(element);
            }

            @Override
            public App getApp() {
                return app;
            }
        }, launcher.getValidity(), new IFunction<String, IArrayInterface.IProperty>() {
            @Override
            public IArrayInterface.IProperty apply(final String s) {
                return new IArrayInterface.IProperty() {
                    @Override
                    public void accept(Double v) {
                        launcher.setEmbedDouble(myUniqueStateInstance, target, s, v);
                    }

                    @Override
                    public Double get() {
                        return launcher.getEmbedDouble(myUniqueStateInstance, target, s);
                    }
                };
            }
        }, new ISupplier<IArrayInterface.ArrayPosition[]>() {
            @Override
            public IArrayInterface.ArrayPosition[] get() {
                return getPositions(target, launcher, path);
            }
        });

        return uiSVL;
    }

    private IArrayInterface.ArrayPosition[] getPositions(final IRIO target, final ISchemaHost launcher, final SchemaPath path) {
        int nextAdvance;
        LinkedList<IArrayInterface.ArrayPosition> positions = new LinkedList<IArrayInterface.ArrayPosition>();
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
            nextAdvance = getGroupLength(target, i);
            boolean hasNIdxSchema = false;
            if (nextAdvance == 0) {
                nextAdvance = 1;
            } else {
                ElementContextual ec = getElementContextualSchema(target, i, nextAdvance, indentAnchors);
                subelem = ec.element;
                subelemId = ec.indent;
                hasNIdxSchema = true;
            }

            ISupplier<Runnable> deleter = getRemovalCallback(pLevel, target, launcher, i, nextAdvance, path, ind);
            Runnable addition = getAdditionCallback(target, launcher, i, path, ind);
            Runnable clipAddition = getClipAdditionCallback(target, i, path);

            UIElement uie;
            if (hasNIdxSchema) {
                uie = subelem.buildHoldingEditor(target, launcher, path);
            } else {
                uie = subelem.buildHoldingEditor(target.getAElem(i), launcher, ind);
            }

            String dispData = (i + indexDisplayOffset) + " ";
            if (possibleEnumElement != null) {
                SchemaElement se = AggregateSchemaElement.extractField(possibleEnumElement, null);
                dispData = ((EnumSchemaElement) se).viewValue(new IRIOFixnum(i + indexDisplayOffset), EnumSchemaElement.Prefix.Prefix) + " ";
            }

            IRIO[] copyHelpElems = new IRIO[nextAdvance];
            for (int j = 0; j < copyHelpElems.length; j++)
                copyHelpElems[j] = target.getAElem(i + j);

            IArrayInterface.ArrayPosition position = new IArrayInterface.ArrayPosition(dispData, copyHelpElems, uie, subelemId, deleter, addition, clipAddition);
            positions.add(position);
        }
        // The 4 for-loop is to deal with 1-indexing and such
        for (int i = 0; i < 4; i++) {
            int idx = target.getALen() + i;
            if (elementPermissionsLevel(idx, target) != 0) {
                SchemaPath ind = path.arrayHashIndex(DMKey.of(idx), "[" + (idx + indexDisplayOffset) + "]");
                IArrayInterface.ArrayPosition position = new IArrayInterface.ArrayPosition((idx + indexDisplayOffset) + " ", null, null, 0, null, getAdditionCallback(target, launcher, idx, path, ind), getClipAdditionCallback(target, idx, path));
                positions.add(position);
                break;
            }
        }
        return positions.toArray(new IArrayInterface.ArrayPosition[0]);
    }

    private ISupplier<Runnable> getRemovalCallback(final int pLevel, final IRIO target, final ISchemaHost launcher, final int mi, final int thisNextAdvance, final SchemaPath path, final SchemaPath ind) {
        if (pLevel < 2)
            return null;
        if (sizeFixed != -1)
            return null;
        return new ISupplier<Runnable>() {
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

    private Runnable getClipAdditionCallback(final IRIO target, final int i, final SchemaPath path) {
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
                            app.ui.launchDialog(T.s.array_dCFCompat, e);
                        }
                        // whack the UI
                        path.changeOccurred(false);
                    } else {
                        app.ui.launchDialog(T.s.array_dCFNotArray);
                    }
                } else {
                    app.ui.launchDialog(T.s.array_dCFEmpty);
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
                groupStep = getGroupLength(target, j);
                if (groupStep == 0) {
                    groupStep = 1;
                    continue;
                }
                getElementContextualSchema(target, j, groupStep, indentAnchors).element.modifyVal(target, path, setDefault);
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
        HashMap<Integer, Integer> indentAnchors = new HashMap<Integer, Integer>();
        int alen = target.getALen();
        for (int j = 0; j < alen; j++) {
            IRIO rio = target.getAElem(j);
            getElementSchema(j).visit(rio, path.arrayHashIndex(DMKey.of(j), "[" + j + "]"), v, detailedPaths);
        }
        int groupStep;
        for (int j = 0; j < alen; j += groupStep) {
            groupStep = getGroupLength(target, j);
            if (groupStep == 0) {
                groupStep = 1;
                continue;
            }
            getElementContextualSchema(target, j, groupStep, indentAnchors).element.visit(target, path, v, detailedPaths);
        }
    }

    // Used to do the correct tagging so that updates to children will affect the parent
    public boolean monitorsSubelements() {
        return false;
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

    // Allows using a custom schema for specific elements in specific contexts in subclasses.
    // Note that this is meant to be used by things messing with getGroupLength, and will not be used otherwise.
    // Also note that for modifyVal purposes this acts *in addition* to getElementSchema,
    //  so that getGroupLength can safely assume that getElementSchema is being followed.
    protected ElementContextual getElementContextualSchema(IRIO arr, int start, int length, final HashMap<Integer, Integer> indentAnchors) {
        throw new RuntimeException("Group length was used, but no contextual schema was defined for it.");
    }

    protected abstract SchemaElement getElementSchema(int j);

    // Used to replace groups of elements with a single editor, where this makes sense.
    // If this is non-zero for a given element, then the element schema is assumed to apply to the array.
    // Use with care.
    protected int getGroupLength(IRIO array, int j) {
        return 0;
    }

    // 0: Do not even show this element.
    // 1: Show & allow editing of this element, but disallow deletion.
    // 2: All permissions.
    // (Used to prevent a user shooting themselves in the foot - should not be considered a serious mechanism.)
    protected int elementPermissionsLevel(int i, IRIO target) {
        boolean canDelete = (sizeFixed == -1) && (!(target.getALen() <= atLeast));
        return canDelete ? 2 : 1;
    }

    public static class ElementContextual {
        public int indent;
        public SchemaElement element;

        public ElementContextual(int id, SchemaElement elem) {
            indent = id;
            element = elem;
        }
    }
}
