/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.arrays;

import gabien.ui.*;
import gabien.uslx.append.*;
import r48.App;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;
import r48.ui.UIMenuButton;
import r48.ui.spacing.UIIndentThingy;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Please work.
 * 25th October 2017.
 */
public class StandardArrayInterface implements IArrayInterface {
    public boolean hasIndexLabels = true;

    public StandardArrayInterface withoutIndexLabels() {
        hasIndexLabels = false;
        return this;
    }
    
    @Override
    public void provideInterfaceFrom(final Host uiSVL, final ISupplier<Boolean> valid, final IFunction<String, IProperty> prop, final ISupplier<ArrayPosition[]> getPositions) {
        final ArrayPosition[] positions = getPositions.get();
        final App app = uiSVL.getApp();
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
                final Size maxSizePre = UILabel.getRecommendedTextSize("", FontSizes.schemaFieldTextHeight);
                final AtomicInteger maxWidth = new AtomicInteger(maxSizePre.width);
                int selectButtonUnit = UITextButton.getRecommendedTextSize("", FontSizes.schemaFieldTextHeight).height;
                int indentUnit = FontSizes.scaleGuess(8);
                if (positions.length > 0) {
                    if (selectedStart == -1) {
                        UITextButton button = genAdditionButton(true, positions[0].execInsert, positions[0].execInsertCopiedArray);
                        if (button != null)
                            uiSVL.panelsAdd(button);
                    }
                }
                for (int i = 0; i < positions.length; i++) {
                    final int mi = i;
                    UIElement uie = positions[mi].core;
                    boolean clarifyEmpty = false;
                    if (uie != null) {
                        // Changes dependent on behavior.
                        Runnable onClick = null;
                        final UIElement originalUIE = uie;
                        // NOTE: At the end of this code, editor can only be a pure UIAppendButton group.
                        // The reason for this is because it simplifies releasing it all later.
                        if (selectedStart == -1) {
                            onClick = new Runnable() {
                                @Override
                                public void run() {
                                    selectedStart = mi;
                                    selectedEnd = mi;
                                    containerRCL();
                                }
                            };
                        } else {
                            // Selection, but not confirming delete
                            if (selectedStart == mi) {
                                final int fixedStart = selectedStart;
                                final int fixedEnd = selectedEnd;
                                if (positions[fixedStart].execDelete != null) {
                                    uie = new UIAppendButton(app, "Delete", uie, valid, new String[] {TXDB.get("Confirm")}, new Runnable[] {
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                deleteRange(fixedStart, fixedEnd);
                                            }
                                        }
                                    }, FontSizes.schemaFieldTextHeight);
                                }
                                onClick = new Runnable() {
                                    @Override
                                    public void run() {
                                        int miL = tracePositionEnd(positions, mi);
                                        if (selectedEnd >= miL) {
                                            selectedStart = -1;
                                        } else {
                                            selectedStart = mi;
                                            selectedEnd = miL;
                                        }
                                        containerRCL();
                                    }
                                };
                                uie = new UIAppendButton(TXDB.get("Copy"), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        copyRange(fixedStart, fixedEnd);
                                        selectedStart = -1;
                                        containerRCL();
                                    }
                                }, FontSizes.schemaFieldTextHeight);
                                uie = new UIAppendButton(TXDB.get("Cut Array"), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        copyRange(fixedStart, fixedEnd);
                                        deleteRange(fixedStart, fixedEnd);
                                    }
                                }, FontSizes.schemaFieldTextHeight);
                            } else if ((mi < selectedStart) || (mi > selectedEnd)) {
                                onClick = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mi < selectedStart)
                                            selectedStart = mi;
                                        if (mi > selectedEnd)
                                            selectedEnd = mi;
                                        containerRCL();
                                    }
                                };
                            } else {
                                onClick = new Runnable() {
                                    @Override
                                    public void run() {
                                        selectedStart = -1;
                                        containerRCL();
                                    }
                                };
                            }
                        }
                        // Add indexes for clarity.
                        final UIElement editor = uie;
                        UIElement label = null;
                        if (hasIndexLabels) {
                            label = new UILabel(positions[mi].text, FontSizes.schemaFieldTextHeight);
                            maxWidth.set(Math.max(label.getWantedSize().width, maxWidth.get()));
                        }
                        releasers.add(new Runnable() {
                            @Override
                            public void run() {
                                UIElement edit = editor;
                                while (edit != originalUIE) {
                                    if (edit instanceof UIAppendButton) {
                                        ((UIAppendButton) edit).release();
                                        edit = ((UIAppendButton) edit).getSubElement();
                                    } else {
                                        throw new RuntimeException("Append chain didn't lead to element, oh dear");
                                    }
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

                        final UISplitterLayout outerSplit = new UISplitterLayout(label, editor, false, 0);
                        releasers.add(new Runnable() {
                            @Override
                            public void run() {
                                outerSplit.release();
                            }
                        });
                        uie = outerSplit;
                    } else {
                        // This is a blank position.
                        uie = new UIPublicPanel(0, 0);
                        clarifyEmpty = true;
                    }
                    if (selectedStart == -1)
                        if (mi + 1 < positions.length)
                            uie = addAdditionButton(uie, clarifyEmpty, positions[mi + 1].execInsert, positions[mi + 1].execInsertCopiedArray);
                    uiSVL.panelsAdd(uie);
                }
            }

            private void copyRange(int fixedStart, int fixedEnd) {
                // the clipboard is very lenient...
                RubyIO rio = new RubyIO();
                rio.type = '[';

                LinkedList<RubyIO> resBuild = new LinkedList<RubyIO>();
                for (int j = fixedStart; j <= fixedEnd; j++)
                    if (positions[j].elements != null)
                        for (IRIO rio2 : positions[j].elements)
                            resBuild.add(new RubyIO().setDeepClone(rio2));
                rio.arrVal = resBuild.toArray(new RubyIO[0]);
                AppMain.theClipboard = rio;
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
                // FormatSyntax.formatExtended(TXDB.get("Add #@ #A"), new RubyIO().setString(text, true))
                optText.add(clarifyEmpty ? TXDB.get("Insert Here...") : TXDB.get("Add Next..."));
                optRuns.add(runnable);
                if (runnable2 != null) {
                    optText.add(TXDB.get("Paste Array"));
                    optRuns.add(runnable2);
                }
                return new UIMenuButton(uiSVL.getApp(), TXDB.get("Add..."), FontSizes.schemaArrayAddTextHeight, valid, optText.toArray(new String[0]), optRuns.toArray(new Runnable[0]));
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
