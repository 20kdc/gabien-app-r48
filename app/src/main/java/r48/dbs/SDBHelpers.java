/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.datum.DatumSrcLoc;
import gabien.render.IGrDriver;
import gabien.render.IImage;
import r48.App;
import r48.io.data.DMKey;
import r48.io.data.IRIO;
import r48.io.data.IRIOGeneric;
import r48.schema.*;
import r48.schema.displays.HWNDSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBindingSchemaElement;
import r48.schema.specialized.SpritesheetCoreSchemaElement;
import r48.tr.TrNames;
import r48.tr.TrPage.FF0;
import r48.tr.pages.TrSchema;
import r48.ui.dialog.ISpritesheetProvider;
import r48.ui.dialog.UIEnumChoice.EntryMode;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Things that SDB shouldn't have inside it,
 * for creating schema elements
 * Created on Sunday September 17th, 2017
 */
class SDBHelpers extends App.Svc {
    SDBHelpers(App app) {
        super(app);
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
        }, 0, new Function<IRIO, IRIO>() {
            @Override
            public IRIO apply(IRIO rubyIO) {
                return varPath.get(rubyIO);
            }
        }, new Function<IRIO, ISpritesheetProvider>() {
            @Override
            public ISpritesheetProvider apply(IRIO rubyIO) {
                return args2.apply(imgPath.get(rubyIO).decString());
            }
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
        // Since this is much too complicated for a mere enum,
        //  use the magical binding to make it more in-line with R48's standards,
        //  with a minimal amount of code
        HashMap<String, FF0> types = new HashMap<String, FF0>();
        types.put("0", () -> S.ppp_constant);
        types.put("1", () -> S.ppp_idVar);
        types.put("2", () -> S.ppp_idNSfx);
        HashMap<String, SchemaElement> disambiguations = new HashMap<String, SchemaElement>();
        ArrayElementSchemaElement idV = new ArrayElementSchemaElement(app, 1, () -> S.ppp_idVarFN, varId, null, false);
        disambiguations.put("1", idV);
        disambiguations.put("2", idV);
        disambiguations.put("", new ArrayElementSchemaElement(app, 1, () -> S.ppp_idFN, val, null, false));
        AggregateSchemaElement inner = new AggregateSchemaElement(app, new SchemaElement[] {
                new HalfsplitSchemaElement(
                        new ArrayElementSchemaElement(app, 0, () -> S.ppp_typeFN, new EnumSchemaElement(app, types, DMKey.of(0), EntryMode.LOCK, () -> ""), null, false),
                        new DisambiguatorSchemaElement(app, PathSyntax.compile(app, "]0"), disambiguations)
                ),
                new SubwindowSchemaElement(new HWNDSchemaElement(app, PathSyntax.compile(app, "]0"), "R2K/H_Internal_PPP"), new Function<IRIO, String>() {
                    @Override
                    public String apply(IRIO rubyIO) {
                        return S.ppp_explain;
                    }
                }),
        });
        return new MagicalBindingSchemaElement(app, new IMagicalBinder() {
            @Override
            public IRIOGeneric targetToBoundNCache(IRIO target) {
                // Split PPP address into components
                long t = target.getFX();
                long type = 0;
                if (t >= 10000) {
                    t -= 10000;
                    type++;
                    if (t >= 40000) {
                        t -= 40000;
                        type++;
                    }
                }
                IRIOGeneric base = new IRIOGeneric(app.encoding);
                base.setArray(2);
                base.getAElem(0).setFX(type);
                base.getAElem(1).setFX(t);
                return base;
            }

            @Override
            public boolean applyBoundToTarget(IRIO bound, IRIO target) {
                // Stitch it back together
                long type = bound.getAElem(0).getFX();
                long t = bound.getAElem(1).getFX();
                if (type == 2) {
                    t += 50000;
                } else if (type == 1) {
                    t += 10000;
                }
                if (target.getFX() != t) {
                    target.setFX(t);
                    return true;
                }
                return false;
            }

            @Override
            public boolean modifyVal(IRIO trueTarget, boolean setDefault) {
                if ((trueTarget.getType() != 'i') || setDefault) {
                    trueTarget.setFX(0);
                    return true;
                }
                return false;
            }
        }, inner);
    }

    public SchemaElement makePicPointerPatchVar(SchemaElement varId, FF0 vname, SchemaElement val) {
        // Less complicated but still more than an enum is reasonable for.
        final TrSchema S = varId.T.s;
        HashMap<String, SchemaElement> disambiguations = new HashMap<String, SchemaElement>();
        disambiguations.put("0", new ArrayElementSchemaElement(app, 1, vname, val, null, false));
        disambiguations.put("", new ArrayElementSchemaElement(app, 1, () -> S.ppp_valueVarFN, varId, null, false));
        SchemaElement inner = new HalfsplitSchemaElement(
                new ArrayElementSchemaElement(app, 0, () -> S.ppp_isVarFN, new IntBooleanSchemaElement(app, false), null, false),
                new DisambiguatorSchemaElement(app, PathSyntax.compile(app, "]0"), disambiguations)
        );
        return new MagicalBindingSchemaElement(app, new IMagicalBinder() {
            @Override
            public IRIOGeneric targetToBoundNCache(IRIO target) {
                // Split PPP address into components
                long t = target.getFX();
                long type = 0;
                if (t >= 10000) {
                    t -= 10000;
                    type++;
                }
                IRIOGeneric base = new IRIOGeneric(app.encoding);
                base.setArray(2);
                base.getAElem(0).setFX(type);
                base.getAElem(1).setFX(t);
                return base;
            }

            @Override
            public boolean applyBoundToTarget(IRIO bound, IRIO target) {
                // Stitch it back together
                long type = bound.getAElem(0).getFX();
                long t = bound.getAElem(1).getFX();
                if (type != 0)
                    t += 10000;
                if (target.getFX() != t) {
                    target.setFX(t);
                    return true;
                }
                return false;
            }

            @Override
            public boolean modifyVal(IRIO trueTarget, boolean setDefault) {
                if ((trueTarget.getType() != 'i') || setDefault) {
                    trueTarget.setFX(0);
                    return true;
                }
                return false;
            }
        }, inner);
    }
}