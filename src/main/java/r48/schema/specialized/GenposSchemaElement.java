/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized;

import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.UIMapToolWrapper;
import r48.schema.SchemaElement;
import r48.schema.specialized.genpos.GenposFramePanelController;
import r48.schema.specialized.genpos.RMAnimRootPanel;
import r48.schema.specialized.genpos.TroopGenposFrame;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaHostImpl;
import r48.schema.util.SchemaPath;

/**
 *
 * This starts a UI-framework-based special editing package,
 * which calls back to this for edit notifications.
 * Created on 2/17/17. "General Positioning" refactor started on July 28th, 2017.
 */
public class GenposSchemaElement extends SchemaElement {
    private String genposType, a, b;
    private int framerate;

    public GenposSchemaElement(String type, String arg, String arg1, int fps) {
        genposType = type;
        a = arg;
        b = arg1;
        framerate = fps;
    }

    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, final SchemaPath path) {
        return new UITextButton(FontSizes.schemaButtonTextHeight, TXDB.get("Graphically edit this..."), new Runnable() {
            @Override
            public void run() {
                if (genposType.equals("vxaAnimation") || genposType.equals("xpAnimation")) {
                    final RMAnimRootPanel rmarp = new RMAnimRootPanel(target, genposType.equals("vxaAnimation"), new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    }, a, b, framerate);
                    // Setup automatic-update safety net
                    safetyWrap(rmarp, target, new Runnable() {
                        @Override
                        public void run() {
                            rmarp.frameChanged();
                        }
                    }, launcher, path);
                }
                if (genposType.equals("r2kTroop")) {
                    final GenposFramePanelController rmarp = new GenposFramePanelController(new TroopGenposFrame(target, new Runnable() {
                        @Override
                        public void run() {
                            path.changeOccurred(false);
                        }
                    }));
                    rmarp.frameChanged();
                    // Setup automatic-update safety net
                    safetyWrap(rmarp.rootLayout, target, new Runnable() {
                        @Override
                        public void run() {
                            rmarp.frameChanged();
                        }
                    }, launcher, path);
                }
            }
        });
    }

    private void safetyWrap(UIElement rmarp, final RubyIO targ, final Runnable consumer, final ISchemaHost launcher, final SchemaPath path) {
        rmarp.setBounds(new Rect(0, 0, 320, 200));
        SchemaElement boot = new TempDialogSchemaChoice(rmarp, consumer, path);
        launcher.launchOther(boot, targ);
    }

    @Override
    public int maxHoldingHeight() {
        return UITextButton.getRecommendedSize("", FontSizes.schemaButtonTextHeight).height;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {
        // "How should I know?" *commences shrugging*
    }
}
