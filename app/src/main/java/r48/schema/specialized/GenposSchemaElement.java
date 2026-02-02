/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized;

import java.util.function.Function;
import java.util.function.Supplier;

import gabien.GaBIEn;
import gabien.ui.UIElement;
import gabien.ui.elements.UITextButton;
import r48.R48;
import r48.io.data.IRIO;
import r48.schema.SchemaElement;
import r48.schema.specialized.genpos.GenposAnimRootPanel;
import r48.schema.specialized.genpos.GenposFramePanelController;
import r48.schema.specialized.genpos.IGenposFrame;
import r48.schema.specialized.genpos.backend.*;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * This starts a UI-framework-based special editing package,
 * which calls back to this for edit notifications.
 * Created on 2/17/17. "General Positioning" refactor started on July 28th, 2017.
 */
public class GenposSchemaElement extends SchemaElement.Leaf {
    private String genposType, a1, a2, b1, b2;
    private int framerate;

    public GenposSchemaElement(R48 app, String type, String arg, String arg1, String arg2, String arg3, int fps) {
        super(app);
        genposType = type;
        a1 = arg;
        a2 = arg1;
        b1 = arg2;
        b2 = arg3;
        framerate = fps;
    }

    @Override
    public UIElement buildHoldingEditorImpl(final IRIO target, final ISchemaHost lBase, final SchemaPath pBase) {
        final R48 app = lBase.getApp();
        return new UITextButton(T.s.gpBeginButton, app.f.schemaFieldTH, new Runnable() {
            @Override
            public void run() {
                final ISchemaHost launcher = lBase.newBlank();
                TempDialogSchemaChoice boot = new TempDialogSchemaChoice(app, null, pBase);
                final SchemaPath path = pBase.newWindow(boot, target);
                if (genposType.equals("vxaAnimation") || genposType.equals("xpAnimation")) {
                    Runnable updater = new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    };
                    final SpriteCache sc = new SpriteCache(app, target, a1, a2, b1, b2, new Function<IRIO, Integer>() {
                        @Override
                        public Integer apply(IRIO rubyIO) {
                            return 192;
                        }
                    }, new Function<IRIO, String>() {
                        @Override
                        public String apply(IRIO rubyIO) {
                            return "Animations/";
                        }
                    });
                    final RGSSGenposFrame frame = new RGSSGenposFrame(app, sc, path, genposType.equals("vxaAnimation"), updater);
                    final RMGenposAnim anim = new RMGenposAnim(app, target.getIVar("@frames"), frame, updater, false);
                    frame.frameSource = new Supplier<IRIO>() {
                        @Override
                        public IRIO get() {
                            return anim.getFrame();
                        }
                    };
                    final GenposAnimRootPanel rmarp = new GenposAnimRootPanel(anim, launcher, framerate);
                    // Setup automatic-update safety net
                    safetyWrap(rmarp, launcher, () -> {
                        if (!anim.isStillValid())
                            return false;
                        sc.prepareFramesetCache();
                        rmarp.incomingModification();
                        return true;
                    }, boot, path);
                } else if (genposType.equals("r2kAnimation")) {
                    // This is particularly mucky because of it's direct interaction with a magically-bound element,
                    //  to increase performance when not dealing with this element.
                    // And also because of the way the battle2 falls back to the older sprites.
                    final Supplier<Boolean> actuallyBattle2 = new Supplier<Boolean>() {
                        @Override
                        public Boolean get() {
                            if (app.stuffRendererIndependent.imageLoader.getImage("Battle2/" + target.getIVar("@animation_name").decString(), false) == GaBIEn.getErrorImage())
                                return false;
                            return target.getIVar("@battle2_2k3").getType() == 'T';
                        }
                    };
                    final SpriteCache sc = new SpriteCache(app, target, a1, null, null, null, new Function<IRIO, Integer>() {
                        @Override
                        public Integer apply(IRIO rubyIO) {
                            if (actuallyBattle2.get())
                                return 128;
                            return 96;
                        }
                    }, new Function<IRIO, String>() {
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

                    final R2kGenposFrame frame = new R2kGenposFrame(app, sc, path, outbound);
                    final RMGenposAnim anim = new RMGenposAnim(app, framesObject, frame, outbound, true);

                    frame.frameSource = new Supplier<IRIO>() {
                        @Override
                        public IRIO get() {
                            return anim.getFrame();
                        }
                    };
                    final GenposAnimRootPanel rmarp = new GenposAnimRootPanel(anim, launcher, framerate);

                    safetyWrap(rmarp, launcher, () -> {
                        if (!anim.isStillValid())
                            return false;
                        sc.prepareFramesetCache();
                        rmarp.incomingModification();
                        return true;
                    }, boot, path);
                } else if (genposType.equals("r2kTroop")) {
                    launchFrame(launcher, path, new R2kTroopGenposFrame(app, target, path, () -> {
                        path.changeOccurred(false);
                    }), boot);
                } else if (genposType.equals("xpTroop")) {
                    launchFrame(launcher, path, new XPTroopGenposFrame(app, target, path, () -> {
                        path.changeOccurred(false);
                    }), boot);
                } else {
                    throw new RuntimeException("Unknown GP type");
                }
            }
        });
    }

    private void launchFrame(ISchemaHost launcher, SchemaPath path, IGenposFrame gpf, TempDialogSchemaChoice boot) {
        final GenposFramePanelController rmarp = new GenposFramePanelController(gpf, null, launcher);
        rmarp.frameChanged();
        // Setup automatic-update safety net
        safetyWrap(rmarp.rootLayout, launcher, () -> {
            if (!gpf.isStillValid())
                return false;
            rmarp.frameChanged();
            return true;
        }, boot, path);
    }

    private void safetyWrap(UIElement rmarp, ISchemaHost shi, Supplier<Boolean> update, TempDialogSchemaChoice sc, final SchemaPath path) {
        sc.heldDialog = rmarp;
        sc.update = update;
        shi.pushObject(path);
    }

    @Override
    public void modifyVal(IRIO target, SchemaPath path, boolean setDefault) {
        // "How should I know?" *commences shrugging*
    }
}
