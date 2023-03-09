/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.dbs;

import gabien.IGrDriver;
import gabien.IImage;
import gabien.uslx.append.*;
import r48.App;
import r48.RubyIO;
import r48.io.data.IRIO;
import r48.schema.*;
import r48.schema.displays.HWNDSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBindingSchemaElement;
import r48.schema.specialized.SpritesheetCoreSchemaElement;
import r48.tr.pages.TrSchema;
import r48.ui.dialog.ISpritesheetProvider;
import r48.ui.dialog.UIEnumChoice.EntryMode;

import java.util.HashMap;

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
    protected HashMap<String, IFunction<String, ISpritesheetProvider>> spritesheets = new HashMap<String, IFunction<String, ISpritesheetProvider>>();
    protected HashMap<String, String> spritesheetN = new HashMap<String, String>();

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
        final IFunction<String, ISpritesheetProvider> args2 = spritesheets.get(imgPfx);
        return new SpritesheetCoreSchemaElement(app, spritesheetN.get(imgPfx), 0, new IFunction<IRIO, IRIO>() {
            @Override
            public IRIO apply(IRIO rubyIO) {
                return varPath.get(rubyIO);
            }
        }, new IFunction<IRIO, ISpritesheetProvider>() {
            @Override
            public ISpritesheetProvider apply(IRIO rubyIO) {
                return args2.apply(imgPath.get(rubyIO).decString());
            }
        });
    }

    public int createSpritesheet(String[] args, int point, String text2) {
        final String imgPfx = args[point];
        spritesheetN.put(args[point], app.td(args[point] + "sprites", text2));
        if (args[point + 1].equals("r2kCharacter")) {
            spritesheets.put(args[point], new IFunction<String, ISpritesheetProvider>() {
                @Override
                public ISpritesheetProvider apply(String imgTxt) {
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
                }
            });
            return point + 1;
        } else if (args[point + 1].equals("vxaCharacter")) {
            spritesheets.put(args[point], new IFunction<String, ISpritesheetProvider>() {
                @Override
                public ISpritesheetProvider apply(String imgTxt) {
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
                }
            });
            return point + 1;
        } else {
            final int cellW = Integer.parseInt(args[point + 1]);
            final int cellH = Integer.parseInt(args[point + 2]);
            final int rowCells = Integer.parseInt(args[point + 3]);
            final int useX = Integer.parseInt(args[point + 4]);
            final int useY = Integer.parseInt(args[point + 5]);
            final int useW = Integer.parseInt(args[point + 6]);
            final int useH = Integer.parseInt(args[point + 7]);
            spritesheets.put(args[point], new IFunction<String, ISpritesheetProvider>() {
                @Override
                public ISpritesheetProvider apply(final String imgTxt) {
                    final IImage img = app.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                    return createSpritesheetProviderCore(imgTxt, img, useW, useH, rowCells, cellW, cellH, useX, useY, -1);
                }
            });
            return point + 8;
        }
    }

    public SchemaElement makePicPointerPatchID(SchemaElement varId, SchemaElement val) {
        final TrSchema S = varId.T.s;
        // Since this is much too complicated for a mere enum,
        //  use the magical binding to make it more in-line with R48's standards,
        //  with a minimal amount of code
        HashMap<String, String> types = new HashMap<String, String>();
        types.put("0", S.ppp_constant);
        types.put("1", S.ppp_idVar);
        types.put("2", S.ppp_idNSfx);
        HashMap<String, SchemaElement> disambiguations = new HashMap<String, SchemaElement>();
        ArrayElementSchemaElement idV = new ArrayElementSchemaElement(app, 1, S.ppp_idVarFN, varId, null, false);
        disambiguations.put("1", idV);
        disambiguations.put("2", idV);
        disambiguations.put("", new ArrayElementSchemaElement(app, 1, S.ppp_idFN, val, null, false));
        AggregateSchemaElement inner = new AggregateSchemaElement(app, new SchemaElement[] {
                new HalfsplitSchemaElement(
                        new ArrayElementSchemaElement(app, 0, S.ppp_typeFN, new EnumSchemaElement(app, types, new RubyIO().setFX(0), EntryMode.LOCK, ""), null, false),
                        new DisambiguatorSchemaElement(app, PathSyntax.compile(app, "]0"), disambiguations)
                ),
                new SubwindowSchemaElement(new HWNDSchemaElement(app, PathSyntax.compile(app, "]0"), "R2K/H_Internal_PPP"), new IFunction<IRIO, String>() {
                    @Override
                    public String apply(IRIO rubyIO) {
                        return S.ppp_explain;
                    }
                }),
        });
        return new MagicalBindingSchemaElement(app, new IMagicalBinder() {
            @Override
            public RubyIO targetToBoundNCache(IRIO target) {
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
                RubyIO base = new RubyIO();
                base.arrVal = new RubyIO[2];
                base.type = '[';
                base.arrVal[0] = new RubyIO().setFX(type);
                base.arrVal[1] = new RubyIO().setFX(t);
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

    public SchemaElement makePicPointerPatchVar(SchemaElement varId, String vname, SchemaElement val) {
        // Less complicated but still more than an enum is reasonable for.
        final TrSchema S = varId.T.s;
        HashMap<String, SchemaElement> disambiguations = new HashMap<String, SchemaElement>();
        disambiguations.put("0", new ArrayElementSchemaElement(app, 1, vname, val, null, false));
        disambiguations.put("", new ArrayElementSchemaElement(app, 1, S.ppp_valueVarFN, varId, null, false));
        SchemaElement inner = new HalfsplitSchemaElement(
                new ArrayElementSchemaElement(app, 0, S.ppp_isVarFN, new IntBooleanSchemaElement(app, false), null, false),
                new DisambiguatorSchemaElement(app, PathSyntax.compile(app, "]0"), disambiguations)
        );
        return new MagicalBindingSchemaElement(app, new IMagicalBinder() {
            @Override
            public RubyIO targetToBoundNCache(IRIO target) {
                // Split PPP address into components
                long t = target.getFX();
                long type = 0;
                if (t >= 10000) {
                    t -= 10000;
                    type++;
                }
                RubyIO base = new RubyIO();
                base.arrVal = new RubyIO[2];
                base.type = '[';
                base.arrVal[0] = new RubyIO().setFX(type);
                base.arrVal[1] = new RubyIO().setFX(t);
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