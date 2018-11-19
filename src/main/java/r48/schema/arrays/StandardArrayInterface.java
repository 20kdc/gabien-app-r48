/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.arrays;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.ui.UIAppendButton;
import r48.ui.UIFieldLayout;
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
    @Override
    public void provideInterfaceFrom(final UIScrollLayout uiSVL, final IFunction<String, IProperty> prop, final ISupplier<ArrayPosition[]> getPositions) {
        final ArrayPosition[] positions = getPositions.get();
        // this object is needed as a pin to hold things together.
        // It used to be kind of redundant, but now with the selection stuff...
        final Runnable runCompleteRelayout = new Runnable() {
            // Only check selectedStart.
            int selectedStart = -1;
            int selectedEnd = -1;
            boolean confirmingSelectionDelete = false;

            // So IPCRESS did end up with one disadvantage: the exact parent tracking means that now this has to be explicitly unbound.
            LinkedList<Runnable> releasers = new LinkedList<Runnable>();

            // Because of name ambiguity, but also whacks uiSVL
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
                int indentUnit = UITextButton.getRecommendedTextSize("", FontSizes.schemaFieldTextHeight).height;
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
                            confirmingSelectionDelete = false;
                            onClick = new Runnable() {
                                @Override
                                public void run() {
                                    selectedStart = mi;
                                    selectedEnd = mi;
                                    containerRCL();
                                }
                            };
                            if (positions[mi].execDelete != null) {
                                uie = new UIAppendButton("-", uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        positions[mi].execDelete.get().run();
                                    }
                                }, FontSizes.schemaFieldTextHeight);
                            }
                        } else if (!confirmingSelectionDelete) {
                            // Selection, but not confirming delete
                            if (selectedStart == mi) {
                               if (positions[selectedStart].execDelete != null) {
                                    uie = new UIAppendButton("-", uie, new Runnable() {
                                        @Override
                                        public void run() {
                                            confirmingSelectionDelete = true;
                                            containerRCL();
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
                                uie = new UIAppendButton(TXDB.get("Copy Array"), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        // the clipboard is very lenient...
                                        RubyIO rio = new RubyIO();
                                        rio.type = '[';

                                        LinkedList<RubyIO> resBuild = new LinkedList<RubyIO>();
                                        for (int j = selectedStart; j <= selectedEnd; j++)
                                            if (positions[j].elements != null)
                                                for (RubyIO rio2 : positions[j].elements)
                                                    resBuild.add(new RubyIO().setDeepClone(rio2));

                                        rio.arrVal = resBuild.toArray(new RubyIO[0]);
                                        AppMain.theClipboard = rio;
                                        selectedStart = -1;
                                        containerRCL();
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
                        } else {
                            // Selection, confirming delete
                            if (selectedStart == mi) {
                                uie = new UIAppendButton(TXDB.get(" Cancel "), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        confirmingSelectionDelete = false;
                                        containerRCL();
                                    }
                                }, FontSizes.schemaFieldTextHeight);
                                if (positions[selectedStart].execDelete != null) {
                                    // freed up room means a good long "Delete" can be written
                                    uie = new UIAppendButton(TXDB.get("Delete"), uie, new Runnable() {
                                        @Override
                                        public void run() {
                                            ArrayPosition[] effectivePositions = positions;
                                            Runnable term = null;
                                            // allowedDelete is used to make sure that nothing gets deleted that shouldn't be.
                                            // The commented printlns here are for debugging.
                                            HashSet<RubyIO> allowedDelete = new HashSet<RubyIO>();
                                            for (int j = selectedStart; j <= selectedEnd; j++)
                                                if (effectivePositions[j].elements != null)
                                                    Collections.addAll(allowedDelete, effectivePositions[j].elements);
                                            //System.err.println("ST" + selectedStart + ";" + selectedEnd);
                                            for (int j = selectedStart; j <= selectedEnd; j++) {
                                                //System.err.println(j);
                                                if (selectedStart >= effectivePositions.length) {
                                                    //System.err.println("NR");
                                                    break;
                                                }
                                                if (effectivePositions[selectedStart].execDelete == null) {
                                                    //System.err.println("NED");
                                                    break;
                                                }
                                                if (effectivePositions[selectedStart].elements == null) {
                                                    //System.err.println("NEL");
                                                    break;
                                                }
                                                boolean aok = true;
                                                for (RubyIO rio : effectivePositions[selectedStart].elements) {
                                                    if (!allowedDelete.contains(rio)) {
                                                        aok = false;
                                                        break;
                                                    }
                                                }
                                                if (!aok) {
                                                    //System.err.println("NOK");
                                                    break;
                                                }
                                                term = effectivePositions[selectedStart].execDelete.get();
                                                effectivePositions = getPositions.get();
                                            }
                                            if (term != null)
                                                term.run();
                                        }
                                    }, FontSizes.schemaFieldTextHeight);
                                }
                            } else if ((mi > selectedStart) && (mi <= selectedEnd)) {
                                // Only the top one can confirm, others are for cancelling
                                uie = new UIAppendButton(TXDB.get("Cancel..."), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        confirmingSelectionDelete = false;
                                        containerRCL();
                                    }
                                }, FontSizes.schemaFieldTextHeight);
                            }
                        }
                        // Add indexes for clarity.
                        final UIElement editor = uie;
                        UIElement label = new UILabel(positions[mi].text, FontSizes.schemaFieldTextHeight);
                        maxWidth.set(Math.max(label.getWantedSize().width, maxWidth.get()));
                        releasers.add(new Runnable() {
                            @Override
                            public void run() {
                                UIElement edit = editor;
                                while (edit != originalUIE) {
                                    if (edit instanceof UIAppendButton) {
                                        ((UIAppendButton) edit).release();
                                        edit = ((UIAppendButton) edit).subElement;
                                    } else {
                                        throw new RuntimeException("Append chain didn't lead to element, oh dear");
                                    }
                                }
                            }
                        });

                        // Prepend indent here!
                        int indent = Math.max(positions[mi].coreIndent, 0);
                        int selectedForce = 0;
                        if (selectedStart != -1) {
                            selectedForce = 32;
                            if ((selectedStart <= mi) && (selectedEnd >= mi))
                                selectedForce = 192;
                            if (selectedStart == mi)
                                selectedForce = 255;
                        }
                        label = new UIFieldLayout(label, new UIIndentThingy(indentUnit, indent, selectedForce, onClick), maxWidth, true);

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
                        uie = addAdditionButton(uie, clarifyEmpty, positions[mi].execInsert, positions[mi].execInsertCopiedArray, positions[mi].text);
                    uiSVL.panelsAdd(uie);
                }
            }

            private int tracePositionEnd(ArrayPosition[] positions, int mi) {
                int idt = positions[mi].coreIndent;
                for (int i = mi + 1; i < positions.length; i++)
                    if (positions[i].coreIndent <= idt)
                        return i;
                // If it gets here...
                return positions.length - 1;
            }

            private UIElement addAdditionButton(UIElement uie, boolean clarifyEmpty, final Runnable runnable, final Runnable runnable2, final String text) {
                if (runnable == null)
                    return uie;
                LinkedList<String> optText = new LinkedList<String>();
                LinkedList<Runnable> optRuns = new LinkedList<Runnable>();
                // This keeps this string in the translation DB in case it's needed again; stuff should be tested first.
                // FormatSyntax.formatExtended(TXDB.get("Add #@ #A"), new RubyIO().setString(text, true))
                optText.add(clarifyEmpty ? TXDB.get("Add Here") : TXDB.get("Add Before"));
                optRuns.add(runnable);
                if (runnable2 != null) {
                    optText.add(TXDB.get("Paste Array"));
                    optRuns.add(runnable2);
                }
                return new UIAppendButton(TXDB.get("+"), uie, optText.toArray(new String[0]), optRuns.toArray(new Runnable[0]), FontSizes.schemaArrayAddTextHeight);
            }
        };
        runCompleteRelayout.run();

    }
}
