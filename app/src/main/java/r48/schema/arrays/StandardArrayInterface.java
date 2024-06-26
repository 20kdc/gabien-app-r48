/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.arrays;

import gabien.GaBIEnUI;
import gabien.ui.*;
import gabien.ui.elements.UIBorderedElement;
import gabien.ui.elements.UIEmpty;
import gabien.ui.elements.UILabel;
import gabien.ui.elements.UITextButton;
import gabien.ui.layouts.UISplitterLayout;
import gabien.ui.theming.IIcon;
import gabien.ui.theming.Theme;
import gabien.uslx.append.*;
import r48.App;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.util.EmbedDataKey;
import r48.schema.util.EmbedDataSlot;
import r48.schema.util.IEmbedDataContext;
import r48.tr.pages.TrRoot;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;
import r48.ui.UIMenuButton;
import r48.ui.spacing.UIIndentThingy;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Please work.
 * 25th October 2017.
 */
public class StandardArrayInterface implements IArrayInterface {
    public boolean hasIndexLabels = true;
    public EmbedDataKey<WeakHashMap<IRIO, Object>> indentTreeClosedKey = new EmbedDataKey<>();

    public StandardArrayInterface withoutIndexLabels() {
        hasIndexLabels = false;
        return this;
    }
    
    @Override
    public void provideInterfaceFrom(final Host uiSVL, final Supplier<Boolean> valid, final IEmbedDataContext prop, final Supplier<ArrayPosition[]> getPositions) {
        final EmbedDataSlot<WeakHashMap<IRIO, Object>> indentTreeClosedSlot = prop.embedSlot(indentTreeClosedKey, null);
        if (indentTreeClosedSlot.value == null)
            indentTreeClosedSlot.value = new WeakHashMap<>();
        final ArrayPosition[] positions = getPositions.get();
        final App app = uiSVL.getApp();
        final TrRoot T = app.t;
        // this object is needed as a pin to hold things together.
        // It used to be kind of redundant, but now with the selection stuff...
        final Runnable runCompleteRelayout = new Runnable() {
            // Only check selectedStart.
            int selectedStart = -1;
            int selectedEnd = -1;

            // So IPCRESS did end up with one disadvantage: the exact parent tracking means that now this has to be explicitly unbound.
            LinkedList<Runnable> releasers = new LinkedList<Runnable>();

            // Because of name ambiguity. Previous versions also whacked uiSVL (that's no longer necessary)
            public void containerRCL() {
                run();
            }

            @Override
            public void run() {
                uiSVL.panelsClear();
                for (Runnable r : releasers)
                    r.run();
                releasers.clear();
                // Work out how big each array index field has to be.
                Theme theme = GaBIEnUI.sysThemeRoot.getTheme();
                final Size maxSizePre = UIBorderedElement.getRecommendedTextSize(theme, "", app.f.schemaFieldTH);
                final AtomicInteger maxWidth = new AtomicInteger(maxSizePre.width);
                int selectButtonUnit = maxSizePre.height;
                int indentUnit = app.f.scaleGuess(8);
                if (positions.length > 0) {
                    if (selectedStart == -1) {
                        UITextButton button = genAdditionButton(true, positions[0].execInsert, positions[0].execInsertCopiedArray);
                        if (button != null)
                            uiSVL.panelsAdd(button);
                    }
                }
                // for tree open/close logic: hide stuff until this indent arrives
                // if -1, disabled
                int hiddenUntilThisIndent = -1;
                WeakHashMap<IRIO, Object> indentTreeClosed = indentTreeClosedSlot.value;
                for (int i = 0; i < positions.length; i++) {
                    // Positions
                    final int mi = i;
                    final ArrayPosition thisAPos = positions[mi];
                    final ArrayPosition nextAPos = (i < positions.length - 1) ? positions[mi + 1] : null;
                    // "Indent skip" (open/close tree) logic
                    if (hiddenUntilThisIndent != -1) {
                        if (thisAPos.coreIndent > hiddenUntilThisIndent) {
                            continue;
                        } else {
                            hiddenUntilThisIndent = -1;
                        }
                    }
                    // The guts
                    UIElement uie = thisAPos.core;
                    boolean clarifyEmpty = false;
                    if (uie != null) {
                        // Changes dependent on behavior.
                        Runnable onClick = null;
                        final UIElement originalUIE = uie;
                        // NOTE: At the end of this code, editor can only be a pure UIAppendButton group.
                        // The reason for this is because it simplifies releasing it all later.
                        if (selectedStart == -1) {
                            onClick = () -> {
                                selectedStart = mi;
                                selectedEnd = mi;
                                containerRCL();
                            };
                        } else {
                            // Selection, but not confirming delete
                            if (selectedStart == mi) {
                                final int fixedStart = selectedStart;
                                final int fixedEnd = selectedEnd;
                                if (positions[fixedStart].execDelete != null) {
                                    uie = new UIAppendButton(app, "Delete", uie, valid, new String[] {T.g.bConfirm}, new Runnable[] {
                                        () -> {
                                            deleteRange(fixedStart, fixedEnd);
                                        }
                                    }, app.f.schemaFieldTH);
                                }
                                onClick = () -> {
                                    int miL = tracePositionEnd(positions, mi);
                                    if (selectedEnd >= miL) {
                                        selectedStart = -1;
                                    } else {
                                        selectedStart = mi;
                                        selectedEnd = miL;
                                    }
                                    containerRCL();
                                };
                                uie = new UIAppendButton(T.g.bCopy, uie, () -> {
                                    copyRange(fixedStart, fixedEnd);
                                    selectedStart = -1;
                                    containerRCL();
                                }, app.f.schemaFieldTH);
                                uie = new UIAppendButton(T.s.array_bCutArr, uie, () -> {
                                    copyRange(fixedStart, fixedEnd);
                                    deleteRange(fixedStart, fixedEnd);
                                }, app.f.schemaFieldTH);
                            } else if ((mi < selectedStart) || (mi > selectedEnd)) {
                                onClick = () -> {
                                    if (mi < selectedStart)
                                        selectedStart = mi;
                                    if (mi > selectedEnd)
                                        selectedEnd = mi;
                                    containerRCL();
                                };
                            } else {
                                onClick = () -> {
                                    selectedStart = -1;
                                    containerRCL();
                                };
                            }
                        }
                        // Add indexes for clarity.
                        final UIElement editor = uie;
                        UIElement label = null;
                        if (hasIndexLabels) {
                            label = new UILabel(positions[mi].text, app.f.schemaFieldTH);
                            maxWidth.set(Math.max(label.getWantedSize().width, maxWidth.get()));
                        }
                        releasers.add(() -> {
                            UIElement edit = editor;
                            while (edit != originalUIE) {
                                if (edit instanceof UIAppendButton) {
                                    ((UIAppendButton) edit).release();
                                    edit = ((UIAppendButton) edit).getSubElement();
                                } else {
                                    throw new RuntimeException("Append chain didn't lead to element, oh dear");
                                }
                            }
                        });

                        // Prepend indent here!
                        int indent = Math.max(positions[mi].coreIndent, 0);
                        int selectedForce = UIIndentThingy.SELECTED_NONE;
                        if (selectedStart != -1) {
                            selectedForce = UIIndentThingy.SELECTED_NOT_THIS;
                            if ((selectedStart <= mi) && (selectedEnd >= mi))
                                selectedForce = UIIndentThingy.SELECTED_TRAIL;
                            if (selectedStart == mi)
                                selectedForce = UIIndentThingy.SELECTED_HEAD;
                        }
                        UIElement indentThingy = new UIIndentThingy(indentUnit, selectButtonUnit, indent, selectedForce, onClick);
                        if (label != null) {
                            label = new UIFieldLayout(label, indentThingy, maxWidth, true);
                        } else {
                            label = indentThingy;
                        }
                        if (nextAPos != null && nextAPos.coreIndent > thisAPos.coreIndent) {
                            // Needs tree thingy!
                            IRIO treeCheckKey = thisAPos.elements[0];
                            boolean inSet = indentTreeClosed.containsKey(treeCheckKey);
                            Theme.Attr<IIcon> sym = inSet ? Theme.IC_ARROW_RIGHT : Theme.IC_ARROW_DOWN;
                            label = new UIAppendButton(sym, label, () -> {
                                // Toggle
                                WeakHashMap<IRIO, Object> itc2 = new WeakHashMap<IRIO, Object>(indentTreeClosed);
                                if (inSet) {
                                    itc2.remove(treeCheckKey);
                                } else {
                                    itc2.put(treeCheckKey, Boolean.TRUE);
                                }
                                indentTreeClosedSlot.value = itc2;
                                containerRCL();
                            }, app.f.schemaFieldTH);
                            // And actually hide future nodes etc.
                            if (inSet)
                                hiddenUntilThisIndent = thisAPos.coreIndent;
                        }

                        final UISplitterLayout outerSplit = new UISplitterLayout(label, editor, false, 0);
                        releasers.add(() -> {
                            outerSplit.release();
                        });
                        uie = outerSplit;
                    } else {
                        // This is a blank position.
                        uie = new UIEmpty();
                        clarifyEmpty = true;
                    }
                    if (selectedStart == -1 && hiddenUntilThisIndent == -1)
                        if (mi + 1 < positions.length)
                            uie = addAdditionButton(uie, clarifyEmpty, positions[mi + 1].execInsert, positions[mi + 1].execInsertCopiedArray);
                    uiSVL.panelsAdd(uie);
                }
                uiSVL.panelsFinished();
            }

            private void copyRange(int fixedStart, int fixedEnd) {
                // the clipboard is very lenient...
                IRIOGeneric rio = new IRIOGeneric(app.ctxClipboardAppEncoding);
                rio.setArray();

                for (int j = fixedStart; j <= fixedEnd; j++)
                    if (positions[j].elements != null)
                        for (IRIO rio2 : positions[j].elements)
                            rio.addAElem(rio.getALen()).setDeepClone(rio2);
                app.theClipboard = rio;
            }

            private void deleteRange(int fixedStart, int fixedEnd) {
                ArrayPosition[] effectivePositions = positions;
                Runnable term = null;
                // allowedDelete is used to make sure that nothing gets deleted that shouldn't be.
                // The commented printlns here are for debugging.
                HashSet<IRIO> allowedDelete = new HashSet<IRIO>();
                for (int j = fixedStart; j <= fixedEnd; j++)
                    if (effectivePositions[j].elements != null)
                        Collections.addAll(allowedDelete, effectivePositions[j].elements);
                //System.err.println("ST" + selectedStart + ";" + selectedEnd);
                for (int j = fixedStart; j <= fixedEnd; j++) {
                    //System.err.println(j);
                    if (fixedStart >= effectivePositions.length) {
                        //System.err.println("NR");
                        break;
                    }
                    if (effectivePositions[fixedStart].execDelete == null) {
                        //System.err.println("NED");
                        break;
                    }
                    if (effectivePositions[fixedStart].elements == null) {
                        //System.err.println("NEL");
                        break;
                    }
                    boolean aok = true;
                    for (IRIO rio : effectivePositions[fixedStart].elements) {
                        if (!allowedDelete.contains(rio)) {
                            aok = false;
                            break;
                        }
                    }
                    if (!aok) {
                        //System.err.println("NOK");
                        break;
                    }
                    term = effectivePositions[fixedStart].execDelete.get();
                    effectivePositions = getPositions.get();
                }
                if (term != null)
                    term.run();
            }

            private int tracePositionEnd(ArrayPosition[] positions, int mi) {
                int idt = positions[mi].coreIndent;
                for (int i = mi + 1; i < positions.length; i++)
                    if (positions[i].coreIndent <= idt)
                        return i;
                // If it gets here...
                return positions.length - 1;
            }

            // This assumes it's being placed on a button 'before' the position
            private UIMenuButton genAdditionButton(boolean clarifyEmpty, final Runnable runnable, final Runnable runnable2) {
                if (runnable == null)
                    return null;
                LinkedList<String> optText = new LinkedList<String>();
                LinkedList<Runnable> optRuns = new LinkedList<Runnable>();
                // This keeps this string in the translation DB in case it's needed again; stuff should be tested first.
                // app.fmt.formatExtended(T.s.l193, new RubyIO().setString(text, true))
                optText.add(clarifyEmpty ? T.s.array_bInsert : T.s.array_bAddNext);
                optRuns.add(runnable);
                if (runnable2 != null) {
                    optText.add(T.s.array_bPasteArr);
                    optRuns.add(runnable2);
                }
                return new UIMenuButton(uiSVL.getApp(), T.s.array_bAdd, app.f.schemaArrayAddTH, valid, optText.toArray(new String[0]), optRuns.toArray(new Runnable[0]));
            }

            // This assumes it's being placed on a button 'before' the position
            private UIElement addAdditionButton(UIElement uie, boolean clarifyEmpty, final Runnable runnable, final Runnable runnable2) {
                UIMenuButton umb = genAdditionButton(clarifyEmpty, runnable, runnable2);
                if (umb == null)
                    return uie;
                return new UIAppendButton(umb, uie);
            }
        };
        runCompleteRelayout.run();

    }
}
