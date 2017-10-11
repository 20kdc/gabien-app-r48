/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.ui.*;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
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
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost lBase, final SchemaPath pBase) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Graphically edit this..."), new Runnable() {
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
                    final SpriteCache sc = new SpriteCache(target, a1, a2, b1, b2, new IFunction<RubyIO, Integer>() {
                        @Override
                        public Integer apply(RubyIO rubyIO) {
                            return 192;
                        }
                    }, new IFunction<RubyIO, String>() {
                        @Override
                        public String apply(RubyIO rubyIO) {
                            return "Animations/";
                        }
                    });
                    final RGSSGenposFrame frame = new RGSSGenposFrame(sc, path, genposType.equals("vxaAnimation"), updater);
                    final RMGenposAnim anim = new RMGenposAnim(target, frame, updater, false);
                    frame.frameSource = new ISupplier<RubyIO>() {
                        @Override
                        public RubyIO get() {
                            return anim.getFrame();
                        }
                    };
                    final GenposAnimRootPanel rmarp = new GenposAnimRootPanel(anim, launcher, framerate);
                    // Setup automatic-update safety net
                    safetyWrap(rmarp, launcher, new Runnable() {
                        @Override
                        public void run() {
                            rmarp.frameChanged();
                            sc.prepareFramesetCache();
                        }
                    }, boot, path);
                } else if (genposType.equals("r2kAnimation")) {
                    Runnable updater = new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    };
                    final SpriteCache sc = new SpriteCache(target, a1, null, null, null, new IFunction<RubyIO, Integer>() {
                        @Override
                        public Integer apply(RubyIO rubyIO) {
                            if (rubyIO.getInstVarBySymbol("@battle2_2k3").fixnumVal == 1)
                                return 128;
                            return 96;
                        }
                    }, new IFunction<RubyIO, String>() {
                        @Override
                        public String apply(RubyIO rubyIO) {
                            if (rubyIO.getInstVarBySymbol("@battle2_2k3").fixnumVal == 1)
                                return "Battle2/";
                            return "Battle/";
                        }
                    });
                    final R2kGenposFrame frame = new R2kGenposFrame(sc, path, updater);
                    final RMGenposAnim anim = new RMGenposAnim(target, frame, updater, true);
                    frame.frameSource = new ISupplier<RubyIO>() {
                        @Override
                        public RubyIO get() {
                            return anim.getFrame();
                        }
                    };
                    final GenposAnimRootPanel rmarp = new GenposAnimRootPanel(anim, launcher, framerate);
                    // Setup automatic-update safety net
                    safetyWrap(rmarp, launcher, new Runnable() {
                        @Override
                        public void run() {
                            rmarp.frameChanged();
                            sc.prepareFramesetCache();
                        }
                    }, boot, path);
                } else if (genposType.equals("r2kTroop")) {
                    final GenposFramePanelController rmarp = new GenposFramePanelController(new TroopGenposFrame(target, path, new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    }), launcher);
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
        rmarp.setBounds(new Rect(0, 0, 320, 200));
        sc.heldDialog = rmarp;
        sc.update = update;
        shi.switchObject(path);
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // "How should I know?" *commences shrugging*
    }
}
