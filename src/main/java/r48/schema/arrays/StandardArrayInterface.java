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
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.schema.ArrayElementSchemaElement;
import r48.ui.UIAppendButton;

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
                for (int i = 0; i < positions.length; i++) {
                    final int mi = i;
                    addAdditionButton(positions[mi].execInsert, positions[mi].execInsertCopiedArray, positions[mi].text);
                    UIElement uie = positions[mi].core;
                    final UIElement originalUIE = uie;
                    if (uie != null) {
                        // NOTE: At the end of this code, editor can only be a pure UIAppendButton group.
                        // The reason for this is because it simplifies releasing it all later.
                        if (selectedStart == -1) {
                            confirmingSelectionDelete = false;
                            uie = new UIAppendButton(TXDB.get("Sel"), uie, new Runnable() {
                                @Override
                                public void run() {
                                    selectedStart = mi;
                                    selectedEnd = mi;
                                    containerRCL();
                                }
                            }, FontSizes.schemaButtonTextHeight);
                            if (positions[mi].execDelete != null) {
                                uie = new UIAppendButton("-", uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        positions[mi].execDelete.get().run();
                                    }
                                }, FontSizes.schemaButtonTextHeight);
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
                                    }, FontSizes.schemaButtonTextHeight);
                                }
                                uie = new UIAppendButton(TXDB.get("DeSel"), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        selectedStart = -1;
                                        containerRCL();
                                    }
                                }, FontSizes.schemaButtonTextHeight);
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
                                }, FontSizes.schemaButtonTextHeight);
                            } else if ((mi < selectedStart) || (mi > selectedEnd)) {
                                uie = new UIAppendButton(TXDB.get("Select..."), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mi < selectedStart)
                                            selectedStart = mi;
                                        if (mi > selectedEnd)
                                            selectedEnd = mi;
                                        containerRCL();
                                    }
                                }, FontSizes.schemaButtonTextHeight);
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
                                }, FontSizes.schemaButtonTextHeight);
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
                                    }, FontSizes.schemaButtonTextHeight);
                                }
                            } else if ((mi > selectedStart) && (mi <= selectedEnd)) {
                                // Only the top one can confirm, others are for cancelling
                                uie = new UIAppendButton(TXDB.get("Cancel..."), uie, new Runnable() {
                                    @Override
                                    public void run() {
                                        confirmingSelectionDelete = false;
                                        containerRCL();
                                    }
                                }, FontSizes.schemaButtonTextHeight);
                            }
                        }
                        // Add indexes for clarity.
                        final UIElement editor = uie;
                        final UIElement label = new ArrayElementSchemaElement.UIOverridableWidthLabel(positions[mi].text, FontSizes.schemaFieldTextHeight, maxWidth, true);
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
                        final UISplitterLayout outerSplit = new UISplitterLayout(label, editor, false, 0);
                        releasers.add(new Runnable() {
                            @Override
                            public void run() {
                                outerSplit.release();
                            }
                        });
                        uiSVL.panelsAdd(outerSplit);
                    }
                }
            }

            private void addAdditionButton(final Runnable runnable, final Runnable runnable2, final String text) {
                if (runnable == null)
                    return;
                UIElement uie = new UITextButton(FormatSyntax.formatExtended(TXDB.get("Add #@ #A"), new RubyIO().setString(text, true)), FontSizes.schemaArrayAddTextHeight, runnable);
                if (runnable2 != null)
                    uie = new UIAppendButton(TXDB.get("Paste Array"), uie, runnable2, FontSizes.schemaButtonTextHeight);
                uiSVL.panelsAdd(uie);
            }
        };
        runCompleteRelayout.run();

    }
}
