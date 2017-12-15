package r48.schema.arrays;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.FormatSyntax;
import r48.dbs.TXDB;
import r48.ui.UIAppendButton;

import java.util.LinkedList;

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
                // NOTE: maxSize gets modified by this loop.
                final Rect maxSize = UILabel.getRecommendedSize("", FontSizes.schemaFieldTextHeight);
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
                                uie = new UIAppendButton(TXDB.get("Cp."), uie, new Runnable() {
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
                        maxSize.width = Math.max(maxSize.width, label.getBounds().width);
                        UIPublicPanel panel = new UIPublicPanel() {
                            @Override
                            public void setBounds(Rect r) {
                                super.setBounds(r);
                                label.setBounds(new Rect(0, 0, maxSize.width, maxSize.height));
                                editor.setBounds(new Rect(maxSize.width, 0, r.width - maxSize.width, r.height));
                            }
                        };

                        panel.addElement(label);
                        panel.addElement(editor);
                        panel.setBounds(new Rect(0, 0, 128, Math.max(editor.getBounds().height, maxSize.height)));
                        uiSVL.panels.add(panel);
                    }
                }
            }

            private void addAdditionButton(final Runnable runnable, final Runnable runnable2, final String text) {
                if (runnable == null)
                    return;
                UIElement uie = new UITextButton(FontSizes.schemaArrayAddTextHeight, FormatSyntax.formatExtended(TXDB.get("Add #@ #A"), new RubyIO().setInternString(text)), runnable);
                if (runnable2 != null)
                    uie = new UIAppendButton(TXDB.get("Ps."), uie, runnable2, FontSizes.schemaButtonTextHeight);
                uiSVL.panels.add(uie);
            }
        };
        runCompleteRelayout.run();

    }
}
