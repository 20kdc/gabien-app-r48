/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.uslx.append.*;
import gabien.uslx.append.*;
import gabien.ui.UIElement;
import gabien.ui.UITextButton;
import r48.AppMain;
import r48.FontSizes;
import r48.dbs.TXDB;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.genpos.GenposAnimRootPanel;
import r48.schema.specialized.genpos.GenposFramePanelController;
import r48.schema.specialized.genpos.backend.*;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * This starts a UI-framework-based special editing package,
 * which calls back to this for edit notifications.
 * Created on 2/17/17. "General Positioning" refactor started on July 28th, 2017.
 */
public class GenposSchemaElement extends SchemaElement {
    private String genposType, a1, a2, b1, b2;
    private int framerate;

    public GenposSchemaElement(String type, String arg, String arg1, String arg2, String arg3, int fps) {
        genposType = type;
        a1 = arg;
        a2 = arg1;
        b1 = arg2;
        b2 = arg3;
        framerate = fps;
    }

    @Override
    public UIElement buildHoldingEditor(final IRIO target, final ISchemaHost lBase, final SchemaPath pBase) {
        return new UITextButton(TXDB.get("Graphically edit this..."), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                final ISchemaHost launcher = lBase.newBlank();
                TempDialogSchemaChoice boot = new TempDialogSchemaChoice(null, null, pBase);
                final SchemaPath path = pBase.newWindow(boot, target);
                if (genposType.equals("vxaAnimation") || genposType.equals("xpAnimation")) {
                    Runnable updater = new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    };
                    final SpriteCache sc = new SpriteCache(target, a1, a2, b1, b2, new IFunction<IRIO, Integer>() {
                        @Override
                        public Integer apply(IRIO rubyIO) {
                            return 192;
                        }
                    }, new IFunction<IRIO, String>() {
                        @Override
                        public String apply(IRIO rubyIO) {
                            return "Animations/";
                        }
                    });
                    final RGSSGenposFrame frame = new RGSSGenposFrame(sc, path, genposType.equals("vxaAnimation"), updater);
                    final RMGenposAnim anim = new RMGenposAnim(target.getIVar("@frames"), frame, updater, false);
                    frame.frameSource = new ISupplier<IRIO>() {
                        @Override
                        public IRIO get() {
                            return anim.getFrame();
                        }
                    };
                    final GenposAnimRootPanel rmarp = new GenposAnimRootPanel(anim, launcher, framerate);
                    // Setup automatic-update safety net
                    safetyWrap(rmarp, launcher, new Runnable() {
                        @Override
                        public void run() {
                            sc.prepareFramesetCache();
                            rmarp.incomingModification();
                        }
                    }, boot, path);
                } else if (genposType.equals("r2kAnimation")) {
                    // This is particularly mucky because of it's direct interaction with a magically-bound element,
                    //  to increase performance when not dealing with this element.
                    // And also because of the way the battle2 falls back to the older sprites.
                    final ISupplier<Boolean> actuallyBattle2 = new ISupplier<Boolean>() {
                        @Override
                        public Boolean get() {
                            if (AppMain.stuffRendererIndependent.imageLoader.getImage("Battle2/" + target.getIVar("@animation_name").decString(), false) == GaBIEn.getErrorImage())
                                return false;
                            return target.getIVar("@battle2_2k3").getType() == 'T';
                        }
                    };
                    final SpriteCache sc = new SpriteCache(target, a1, null, null, null, new IFunction<IRIO, Integer>() {
                        @Override
                        public Integer apply(IRIO rubyIO) {
                            if (actuallyBattle2.get())
                                return 128;
                            return 96;
                        }
                    }, new IFunction<IRIO, String>() {
                        @Override
                        public String apply(IRIO rubyIO) {
                            if (actuallyBattle2.get())
                                return "Battle2/";
                            return "Battle/";
                        }
                    });

                    final IRIO framesObject = target.getIVar("@frames");

                    // This handles "outbound" changes made within the animator.

                    Runnable outbound = new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    };

                    final R2kGenposFrame frame = new R2kGenposFrame(sc, path, outbound);
                    final RMGenposAnim anim = new RMGenposAnim(framesObject, frame, outbound, true);

                    frame.frameSource = new ISupplier<IRIO>() {
                        @Override
                        public IRIO get() {
                            return anim.getFrame();
                        }
                    };
                    final GenposAnimRootPanel rmarp = new GenposAnimRootPanel(anim, launcher, framerate);

                    safetyWrap(rmarp, launcher, new Runnable() {
                        @Override
                        public void run() {
                            // usual stuff
                            rmarp.incomingModification();
                            sc.prepareFramesetCache();
                        }
                    }, boot, path);
                } else if (genposType.equals("r2kTroop")) {
                    final GenposFramePanelController rmarp = new GenposFramePanelController(new TroopGenposFrame(target, path, new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    }), null, launcher);
                    rmarp.frameChanged();
                    // Setup automatic-update safety net
                    safetyWrap(rmarp.rootLayout, launcher, new Runnable() {
                        @Override
                        public void run() {
                            rmarp.frameChanged();
                        }
                    }, boot, path);
                } else {
                    throw new RuntimeException("Unknown GP type");
                }
            }
        });
    }

    private void safetyWrap(UIElement rmarp, ISchemaHost shi, Runnable update, TempDialogSchemaChoice sc, final SchemaPath path) {
        sc.heldDialog = rmarp;
        sc.update = update;
        shi.pushObject(path);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // "How should I know?" *commences shrugging*
    }
}
