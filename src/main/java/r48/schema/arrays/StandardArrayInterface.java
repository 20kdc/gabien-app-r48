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
import r48.ui.Art;
import r48.ui.UIAppendButton;

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

            // Because of name ambiguity, but also whacks uiSVL
            public void containerRCL() {
                run();
                uiSVL.setBounds(uiSVL.getBounds());
            }

            @Override
            public void run() {
                uiSVL.panels.clear();
                // Work out how big each array index field has to be.
                final Rect maxSizePre = UILabel.getRecommendedSize("", FontSizes.schemaFieldTextHeight);
                final AtomicInteger maxWidth = new AtomicInteger(maxSizePre.width);
                for (int i = 0; i < positions.length; i++) {
                    final int mi = i;
                    addAdditionButton(positions[mi].execInsert, positions[mi].execInsertCopiedArray, positions[mi].text);
                    UIElement uie = positions[mi].core;
                    if (uie != null) {
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
                                            for (int j = selectedStart; j <= selectedEnd; j++) {
                                                if (j >= effectivePositions.length)
                                                    break;
                                                if (effectivePositions[selectedStart].execDelete == null)
                                                    break;
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
                        final UIElement label = new UILabel(positions[mi].text, FontSizes.schemaFieldTextHeight);
                        maxWidth.set(Math.max(maxWidth.get(), label.getBounds().width));
                        UIPublicPanel panel = new UIPublicPanel() {
                            @Override
                            public void setBounds(Rect r) {
                                super.setBounds(r);
                                label.setBounds(new Rect(0, 0, maxWidth.get(), maxSizePre.height));
                                editor.setBounds(new Rect(maxWidth.get(), 0, r.width - maxSizePre.width, r.height));
                            }
                        };

                        panel.addElement(label);
                        panel.addElement(editor);
                        panel.setBounds(new Rect(0, 0, 128, Math.max(editor.getBounds().height, maxSizePre.height)));
                        uiSVL.panels.add(panel);
                    }
                }
            }

            private void addAdditionButton(final Runnable runnable, final Runnable runnable2, final String text) {
                if (runnable == null)
                    return;
                UIElement uie = new UITextButton(FontSizes.schemaArrayAddTextHeight, FormatSyntax.formatExtended(TXDB.get("Add #@ #A"), new RubyIO().setString(text, true)), runnable);
                if (runnable2 != null)
                    uie = new UIAppendButton(TXDB.get("Paste Array"), uie, runnable2, FontSizes.schemaButtonTextHeight);
                uiSVL.panels.add(uie);
            }
        };
        runCompleteRelayout.run();

    }
}
