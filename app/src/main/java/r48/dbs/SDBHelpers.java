/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.render.IGrDriver;
import gabien.render.IImage;
import r48.App;
import r48.schema.*;
import r48.schema.displays.EPGDisplaySchemaElement;
import r48.schema.displays.HuePickerSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.LowerBoundIntegerSchemaElement;
import r48.schema.integers.NamespacedIntegerSchemaElement;
import r48.schema.integers.ROIntegerSchemaElement;
import r48.schema.specialized.OSStrHashMapSchemaElement;
import r48.schema.specialized.ScriptControlSchemaElement;
import r48.schema.specialized.SpritesheetCoreSchemaElement;
import r48.schema.specialized.StringBlobSchemaElement;
import r48.schema.specialized.ZLibBlobSchemaElement;
import r48.tr.TrNames;
import r48.tr.TrPage.FF0;
import r48.tr.pages.TrSchema;
import r48.ui.dialog.ISpritesheetProvider;

import java.util.HashMap;
import java.util.function.Function;

import datum.DatumSrcLoc;

/**
 * Things that SDB shouldn't have inside it,
 * for creating schema elements
 * Created on Sunday September 17th, 2017
 */
public class SDBHelpers extends App.Svc {
    public final OpaqueSchemaElement opaque;

    public SDBHelpers(App app) {
        super(app);
        opaque = new OpaqueSchemaElement(app);

        app.sdb.setSDBEntry("nil", opaque);
        app.sdb.setSDBEntry("int", new IntegerSchemaElement(app, 0));
        app.sdb.setSDBEntry("roint", new ROIntegerSchemaElement(app, 0));
        app.sdb.setSDBEntry("int+0", new LowerBoundIntegerSchemaElement(app, 0, 0));
        app.sdb.setSDBEntry("int+1", new LowerBoundIntegerSchemaElement(app, 1, 1));
        app.sdb.setSDBEntry("index", new AMAISchemaElement(app));
        app.sdb.setSDBEntry("float", new FloatSchemaElement(app, "0", false));
        app.sdb.setSDBEntry("jnum", new FloatSchemaElement(app, "0", true));
        app.sdb.setSDBEntry("string", new StringSchemaElement(app, () -> "", '\"'));
        app.sdb.setSDBEntry("boolean", new BooleanSchemaElement(app, false));
        app.sdb.setSDBEntry("booleanDefTrue", new BooleanSchemaElement(app, true));
        app.sdb.setSDBEntry("int_boolean", new IntBooleanSchemaElement(app, false));
        app.sdb.setSDBEntry("int_booleanDefTrue", new IntBooleanSchemaElement(app, true));
        app.sdb.setSDBEntry("OPAQUE", opaque);
        app.sdb.setSDBEntry("hue", new HuePickerSchemaElement(app));

        app.sdb.setSDBEntry("percent", new LowerBoundIntegerSchemaElement(app, 0, 100));

        app.sdb.setSDBEntry("zlibBlobEditor", new ZLibBlobSchemaElement(app));
        app.sdb.setSDBEntry("stringBlobEditor", new StringBlobSchemaElement(app));

        app.sdb.setSDBEntry("internal_EPGD", new EPGDisplaySchemaElement(app));
        app.sdb.setSDBEntry("internal_scriptIE", new ScriptControlSchemaElement(app));

        app.sdb.setSDBEntry("internal_LF_INDEX", new OSStrHashMapSchemaElement(app));

        if (app.engine.defineIndent) {
            if (app.engine.allowIndentControl) {
                app.sdb.setSDBEntry("indent", new ROIntegerSchemaElement(app, 0));
            } else {
                app.sdb.setSDBEntry("indent", new IntegerSchemaElement(app, 0));
            }
        }
    }


    // Spritesheet definitions are quite opaque lists of numbers defining how a grid sheet should appear. See spriteSelector.
    protected HashMap<String, Function<String, ISpritesheetProvider>> spritesheets = new HashMap<String, Function<String, ISpritesheetProvider>>();
    protected HashMap<String, FF0> spritesheetN = new HashMap<String, FF0>();

    // cellW/cellH is the skip size.
    // useX/useY/useW/useH is what to use of a given cell.
    // countOvr forces a specific cell count if not -1.
    public ISpritesheetProvider createSpritesheetProviderCore(final String imgTxt, final IImage img, final int useW, final int useH, final int rowCells, final int cellW, final int cellH, final int useX, final int useY, final int countOvr) {
        return new ISpritesheetProvider() {
            @Override
            public int itemWidth() {
                return useW;
            }

            @Override
            public int itemHeight() {
                return useH;
            }

            @Override
            public int itemCount() {
                // Use this to inform the user of image issues
                if (imgTxt.equals(""))
                    app.ui.launchDialog(T.u.spr_msgNoImage);
                if (countOvr != -1)
                    return countOvr;
                return ((img.getHeight() + (cellH - 1)) / cellH) * rowCells;
            }

            @Override
            public int mapValToIdx(long itemVal) {
                return (int) itemVal;
            }

            @Override
            public long mapIdxToVal(int idx) {
                return idx;
            }

            @Override
            public void drawItem(long t, int x, int y, int spriteScale, IGrDriver igd) {
                int row = ((int) t) / rowCells;
                int col = ((int) t) % rowCells;
                igd.blitScaledImage((col * cellW) + useX, (row * cellH) + useY, useW, useH, x, y, useW * spriteScale, useH * spriteScale, img);
            }
        };
    }

    public SchemaElement makeSpriteSelector(final PathSyntax varPath, final PathSyntax imgPath, final String imgPfx) {
        final Function<String, ISpritesheetProvider> args2 = spritesheets.get(imgPfx);
        return new SpritesheetCoreSchemaElement(app, (v) -> {
            // used to be a FormatSyntax step buried in SpritesheetCoreSchemaElement, but it never got used here
            return spritesheetN.get(imgPfx).r();
        }, 0, varPath, (rubyIO) -> {
            return args2.apply(imgPath.getRO(rubyIO).decString());
        });
    }

    public void createSpritesheet(DatumSrcLoc srcLoc, String[] args) {
        final String text2 = args[1];
        final String imgPfx = args[2];
        spritesheetN.put(imgPfx, app.dTr(srcLoc, TrNames.sdbSpritesheet(imgPfx), text2));
        if (args[3].equals("r2kCharacter")) {
            spritesheets.put(imgPfx, (imgTxt) -> {
                final boolean extended = imgTxt.startsWith("$");
                int effectiveW = 288;
                int effectiveH = 256;
                final IImage img = app.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                if (extended) {
                    // EasyRPG Extended Mode
                    effectiveW = img.getWidth();
                    effectiveH = img.getHeight();
                }
                final int useW = effectiveW / 12;
                final int useH = effectiveH / 8;

                final int cellW = useW * 3;
                final int cellH = useH * 4;
                final int useX = useW;
                final int useY = useH * 2;
                final int rowCells = 4;
                return createSpritesheetProviderCore(imgTxt, img, useW, useH, rowCells, cellW, cellH, useX, useY, -1);
            });
        } else if (args[3].equals("vxaCharacter")) {
            spritesheets.put(imgPfx, (imgTxt) -> {
                final IImage img = app.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                int cellW = img.getWidth() / 4;
                int cellH = img.getHeight() / 2;
                int sprW = cellW / 3;
                int sprH = cellH / 4;
                int ovr = 8;
                if (imgTxt.startsWith("!$") || imgTxt.startsWith("$")) {
                    // Character index doesn't work on these
                    sprW = img.getWidth() / 3;
                    sprH = img.getHeight() / 4;
                    cellW = 0;
                    cellH = 0;
                    ovr = 1;
                }
                int useX = sprW;
                int useY = 0;
                return createSpritesheetProviderCore(imgTxt, img, sprW, sprH, 4, cellW, cellH, useX, useY, ovr);
            });
        } else {
            final int cellW = Integer.parseInt(args[3]);
            final int cellH = Integer.parseInt(args[4]);
            final int rowCells = Integer.parseInt(args[5]);
            final int useX = Integer.parseInt(args[6]);
            final int useY = Integer.parseInt(args[7]);
            final int useW = Integer.parseInt(args[8]);
            final int useH = Integer.parseInt(args[9]);
            spritesheets.put(imgPfx, (imgTxt) -> {
                final IImage img = app.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                return createSpritesheetProviderCore(imgTxt, img, useW, useH, rowCells, cellW, cellH, useX, useY, -1);
            });
        }
    }

    public SchemaElement makePicPointerPatchID(SchemaElement varId, SchemaElement val) {
        final TrSchema S = varId.T.s;
        return new NamespacedIntegerSchemaElement(app, new NamespacedIntegerSchemaElement.Namespace[] {
                new NamespacedIntegerSchemaElement.Namespace(() -> S.ppp_constant, () -> S.ppp_constant_h,     0,           9999,     0, val),
                new NamespacedIntegerSchemaElement.Namespace(() -> S.ppp_idVarFN , () -> S.ppp_idVar     , 10000,          49999, 10000, varId),
                new NamespacedIntegerSchemaElement.Namespace(() -> S.ppp_idNSfxFN, () -> S.ppp_idNSfx    , 50000, Long.MAX_VALUE, 50000, varId),
                new NamespacedIntegerSchemaElement.Namespace(() -> S.ppp_unknown , null, Long.MIN_VALUE,    -1,     0, new IntegerSchemaElement(app, -1)),
        });
    }

    public SchemaElement makePicPointerPatchVar(SchemaElement varId, FF0 vname, SchemaElement val) {
        // Less complicated but still more than an enum is reasonable for.
        final TrSchema S = varId.T.s;
        return new NamespacedIntegerSchemaElement(app, new NamespacedIntegerSchemaElement.Namespace[] {
                new NamespacedIntegerSchemaElement.Namespace(vname, null,     0,           9999,     0, val),
                new NamespacedIntegerSchemaElement.Namespace(() -> S.ppp_valVarFN, null, 10000, Long.MAX_VALUE, 10000, varId),
                new NamespacedIntegerSchemaElement.Namespace(() -> S.ppp_unknown , null, Long.MIN_VALUE,    -1,     0, new IntegerSchemaElement(app, -1)),
        });
   }
}