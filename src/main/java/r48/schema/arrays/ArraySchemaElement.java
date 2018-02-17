/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.arrays;

import gabien.ui.*;
import r48.AppMain;
import r48.ArrayUtils;
import r48.RubyIO;
import r48.dbs.IProxySchemaElement;
import r48.dbs.TXDB;
import r48.schema.AggregateSchemaElement;
import r48.schema.EnumSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

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
    private IntegerSchemaElement myUniqueStateInstance = new IntegerSchemaElement(0);

    public ArraySchemaElement(int fixedSize, int al1, int ido, IArrayInterface uiHelp) {
        sizeFixed = fixedSize;
        atLeast = al1;
        indexDisplayOffset = ido;
        uiHelper = uiHelp;
    }

    public ArraySchemaElement(int fixedSize, int al1, int ido, IArrayInterface uiHelp, SchemaElement enumer) {
        this(fixedSize, al1, ido, uiHelp);
        possibleEnumElement = enumer;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path2) {
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this, false) : path2;
        final UIScrollLayout uiSVL = AggregateSchemaElement.createScrollSavingSVL(path, launcher, this, target);

        uiSVL.panelsClear();

        final SchemaPath keyStoragePath = path.findLast();

        uiHelper.provideInterfaceFrom(uiSVL, new IFunction<String, IArrayInterface.IProperty>() {
            @Override
            public IArrayInterface.IProperty apply(String s) {
                final SchemaPath.EmbedDataKey myKey = new SchemaPath.EmbedDataKey(myUniqueStateInstance, target, uiHelper.getClass(), s);

                return new IArrayInterface.IProperty() {
                    @Override
                    public void accept(Double v) {
                        keyStoragePath.getEmbedMap(launcher).put(myKey, v);
                    }

                    @Override
                    public Double get() {
                        return keyStoragePath.getEmbedSP(launcher, myKey);
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

    private IArrayInterface.ArrayPosition[] getPositions(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        int nextAdvance;
        LinkedList<IArrayInterface.ArrayPosition> positions = new LinkedList<IArrayInterface.ArrayPosition>();
        for (int i = 0; i < target.arrVal.length; i += nextAdvance) {
            nextAdvance = 1;
            int pLevel = elementPermissionsLevel(i, target);
            if (pLevel < 1)
                continue;
            SchemaPath ind = path.arrayHashIndex(new RubyIO().setFX(i), "[" + (i + indexDisplayOffset) + "]");

            SchemaElement subelem = getElementSchema(i);
            nextAdvance = getGroupLength(target.arrVal, i);
            boolean hasNIdxSchema = false;
            if (nextAdvance == 0) {
                nextAdvance = 1;
            } else {
                subelem = getElementContextualSchema(target.arrVal, i, nextAdvance);
                hasNIdxSchema = true;
            }

            ISupplier<Runnable> deleter = getRemovalCallback(pLevel, target, launcher, i, nextAdvance, path, ind);
            Runnable addition = getAdditionCallback(target, launcher, i, path, ind);
            Runnable clipAddition = getClipAdditionCallback(target, i, path);

            UIElement uie;
            if (hasNIdxSchema) {
                uie = subelem.buildHoldingEditor(target, launcher, path);
            } else {
                uie = subelem.buildHoldingEditor(target.arrVal[i], launcher, ind);
            }
            RubyIO[] copyHelpElems = new RubyIO[nextAdvance];
            System.arraycopy(target.arrVal, i, copyHelpElems, 0, copyHelpElems.length);
            String dispData = (i + indexDisplayOffset) + " ";
            if (possibleEnumElement != null) {
                SchemaElement se = possibleEnumElement;
                while (se instanceof IProxySchemaElement)
                    se = ((IProxySchemaElement) se).getEntry();
                dispData = ((EnumSchemaElement) se).viewValue(new RubyIO().setFX(i + indexDisplayOffset), true) + " ";
            }
            IArrayInterface.ArrayPosition position = new IArrayInterface.ArrayPosition(dispData, copyHelpElems, uie, deleter, addition, clipAddition);
            positions.add(position);
        }
        // The 4 for-loop is to deal with 1-indexing and such
        for (int i = 0; i < 4; i++) {
            int idx = target.arrVal.length + i;
            if (elementPermissionsLevel(idx, target) != 0) {
                SchemaPath ind = path.arrayHashIndex(new RubyIO().setFX(idx), "[" + (idx + indexDisplayOffset) + "]");
                IArrayInterface.ArrayPosition position = new IArrayInterface.ArrayPosition((idx + indexDisplayOffset) + " ", null, null, null, getAdditionCallback(target, launcher, idx, path, ind), getClipAdditionCallback(target, idx, path));
                positions.add(position);
                break;
            }
        }
        return positions.toArray(new IArrayInterface.ArrayPosition[0]);
    }

    private ISupplier<Runnable> getRemovalCallback(final int pLevel, final RubyIO target, final ISchemaHost launcher, final int mi, final int thisNextAdvance, final SchemaPath path, final SchemaPath ind) {
        if (pLevel < 2)
            return null;
        if (sizeFixed != 0)
            return null;
        return new ISupplier<Runnable>() {
            @Override
            public Runnable get() {
                for (int j = 0; j < thisNextAdvance; j++)
                    ArrayUtils.removeRioElement(target, mi);
                return new Runnable() {
                    @Override
                    public void run() {
                        path.changeOccurred(false);
                    }
                };
            }
        };
    }

    private Runnable getAdditionCallback(final RubyIO target, final ISchemaHost launcher, final int i, final SchemaPath path, final SchemaPath ind) {
        if (sizeFixed != 0)
            return null;
        return new Runnable() {
            @Override
            public void run() {
                RubyIO rio = new RubyIO();
                SchemaElement subelem = getElementSchema(i);
                subelem.modifyVal(rio, ind, true);

                ArrayUtils.insertRioElement(target, rio, i);
                // whack the UI
                path.changeOccurred(false);
                // Perform *magic*.
                // What this means is that the subclass is given everything it needs to, theoretically,
                //  construct a contrived sequence of schema paths that lead to the 'user' switching into the target element,
                //  selecting something in there, and popping up a command selection dialog.
                elementOnCreateMagic(target, i, launcher, ind, path);
            }
        };
    }

    private Runnable getClipAdditionCallback(final RubyIO target, final int i, final SchemaPath path) {
        if (sizeFixed != 0)
            return null;
        return new Runnable() {
            @Override
            public void run() {
                if (AppMain.theClipboard != null) {
                    // could have changed
                    if (AppMain.theClipboard.type == '[') {
                        RubyIO[] finalInsertionRv = AppMain.theClipboard.arrVal;
                        for (int j = finalInsertionRv.length - 1; j >= 0; j--)
                            ArrayUtils.insertRioElement(target, new RubyIO().setDeepClone(finalInsertionRv[j]), i);
                        // whack the UI
                        path.changeOccurred(false);
                    } else {
                        AppMain.launchDialog(TXDB.get("Can't copy in - copying in a range into an array requires that range be an array.") + "\n" + TXDB.get("Copying from the array interface will give you these."));
                    }
                } else {
                    AppMain.launchDialog(TXDB.get("Can't copy in - the clipboard is empty."));
                }
            }
        };
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path2, boolean setDefault) {
        final SchemaPath path = monitorsSubelements() ? path2.tagSEMonitor(target, this, false) : path2;
        setDefault = IntegerSchemaElement.ensureType(target, '[', setDefault);
        if (target.arrVal == null) {
            setDefault = true;
        } else if (target.arrVal.length < atLeast) {
            setDefault = true;
        }
        if (setDefault) {
            if (sizeFixed < atLeast) {
                target.arrVal = new RubyIO[atLeast];
            } else {
                target.arrVal = new RubyIO[sizeFixed];
            }
            for (int i = 0; i < target.arrVal.length; i++)
                target.arrVal[i] = new RubyIO();
        }
        boolean modified = setDefault;
        if (sizeFixed != 0) {
            if (target.arrVal.length != sizeFixed) {
                int lenCut = Math.min(sizeFixed, target.arrVal.length);
                RubyIO[] newArr = new RubyIO[sizeFixed];
                for (int j = 0; j < newArr.length; j++)
                    newArr[j] = new RubyIO();
                System.arraycopy(target.arrVal, 0, newArr, 0, lenCut);
                target.arrVal = newArr;
                modified = true;
            }
        }
        while (true) {
            for (int j = 0; j < target.arrVal.length; j++) {
                RubyIO rio = target.arrVal[j];
                // Fun fact: There's a reason for this not-quite-linear timeline.
                // It's because otherwise, when the subelement tries to notify the array of the modification,
                //  it will lead to an infinite loop!
                // So it has to be able to see it's own object for the loop to terminate.
                // (Later: This got changed around a bit in a restructuring. Point is, target.arrVal[j] == rio)
                getElementSchema(j).modifyVal(rio, path.arrayHashIndex(new RubyIO().setFX(j), "[" + j + "]"), setDefault);
            }
            int groupStep;
            for (int j = 0; j < target.arrVal.length; j += groupStep) {
                groupStep = getGroupLength(target.arrVal, j);
                if (groupStep == 0) {
                    groupStep = 1;
                    continue;
                }
                getElementContextualSchema(target.arrVal, j, groupStep).modifyVal(target, path, setDefault);
            }
            boolean aca = autoCorrectArray(target, path);
            modified = modified || aca;
            if (!aca)
                break;
            setDefault = false;
        }
        if (modified)
            path.changeOccurred(true);
    }

    // Used to do the correct tagging so that updates to children will affect the parent
    public boolean monitorsSubelements() {
        return false;
    }

    // Used by certain rather complicated array types that want to give the user a *specific* UI on the creation of an element.
    // The element gets created & acknowledged, then this kickstarts the UI for editing details.
    // If the user exits this UI via the "back" method, further contrived sequences can be created to delete the element.
    // As for closing the window... hm.
    protected void elementOnCreateMagic(RubyIO target, int i, ISchemaHost launcher, SchemaPath ind, SchemaPath path) {
    }

    // Allows performing automatic correction of structural issues,
    //  after any data issues have been cleaned up.
    // path is already in the array entry.
    // Note that this is solely for auto-correction.
    // If setDefault is required, just override modifyVal
    //  and don't bother to call super if setDefault is true,
    //  instead doing whatever you need yourself.
    // Also note that if a modification is performed, another check is done so that things like indent processing can run.
    protected abstract boolean autoCorrectArray(RubyIO array, SchemaPath path);

    // Allows using a custom schema for specific elements in specific contexts in subclasses.
    // Note that this is meant to be used by things messing with getGroupLength, and will not be used otherwise.
    // Also note that for modifyVal purposes this acts *in addition* to getElementSchema,
    //  so that getGroupLength can safely assume that getElementSchema is being followed.
    protected SchemaElement getElementContextualSchema(RubyIO[] arr, int start, int length) {
        throw new RuntimeException("Group length was used, but no contextual schema was defined for it.");
    }

    protected abstract SchemaElement getElementSchema(int j);

    // Used to replace groups of elements with a single editor, where this makes sense.
    // If this is non-zero for a given element, then the element schema is assumed to apply to the array.
    // Use with care.
    protected int getGroupLength(RubyIO[] array, int j) {
        return 0;
    }

    // 0: Do not even show this element.
    // 1: Show & allow editing of this element, but disallow deletion.
    // 2: All permissions.
    // (Used to prevent a user shooting themselves in the foot - should not be considered a serious mechanism.)
    protected int elementPermissionsLevel(int i, RubyIO target) {
        boolean canDelete = (sizeFixed == 0) && (!(target.arrVal.length <= atLeast));
        return canDelete ? 2 : 1;
    }
}
