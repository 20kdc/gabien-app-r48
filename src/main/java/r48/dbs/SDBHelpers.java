/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.dbs;

import gabien.IGrInDriver;
import gabien.IImage;
import gabien.ui.IFunction;
import r48.AppMain;
import r48.RubyIO;
import r48.schema.*;
import r48.schema.integers.IntBooleanSchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.integers.LowerBoundIntegerSchemaElement;
import r48.schema.specialized.HWNDSchemaElement;
import r48.schema.specialized.IMagicalBinder;
import r48.schema.specialized.MagicalBindingSchemaElement;
import r48.schema.specialized.SpritesheetCoreSchemaElement;
import r48.ui.ISpritesheetProvider;

import java.util.HashMap;

/**
 * Things that SDB shouldn't have inside it,
 *  for creating schema elements
 * Created on Sunday September 17th, 2017
 */
class SDBHelpers {
    // Spritesheet definitions are quite opaque lists of numbers defining how a grid sheet should appear. See spriteSelector.
    protected HashMap<String, IFunction<String, ISpritesheetProvider>> spritesheets = new HashMap<String, IFunction<String, ISpritesheetProvider>>();
    protected HashMap<String, String> spritesheetN = new HashMap<String, String>();
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
                    AppMain.launchDialog(TXDB.get("The image wasn't specified."));
                if (countOvr != -1)
                    return countOvr;
                return ((img.getHeight() + (cellH - 1)) / cellH) * rowCells;
            }

            @Override
            public int mapValToIdx(int itemVal) {
                return itemVal;
            }

            @Override
            public int mapIdxToVal(int idx) {
                return idx;
            }

            @Override
            public void drawItem(int t, int x, int y, IGrInDriver igd) {
                int row = t / rowCells;
                t %= rowCells;
                igd.blitImage((t * cellW) + useX, (row * cellH) + useY, useW, useH, x, y, img);
            }
        };
    }

    public SchemaElement makeSpriteSelector(final String varPath, final String imgPath, final String imgPfx) {
        final IFunction<String, ISpritesheetProvider> args2 = spritesheets.get(imgPfx);
        return new SpritesheetCoreSchemaElement(spritesheetN.get(imgPfx), 0, new IFunction<RubyIO, RubyIO>() {
            @Override
            public RubyIO apply(RubyIO rubyIO) {
                return PathSyntax.parse(rubyIO, varPath);
            }
        }, new IFunction<RubyIO, ISpritesheetProvider>() {
            @Override
            public ISpritesheetProvider apply(RubyIO rubyIO) {
                return args2.apply(PathSyntax.parse(rubyIO, imgPath).decString());
            }
        });
    }

    public int createSpritesheet(String[] args, int point, String text2) {
        final String imgPfx = args[point];
        spritesheetN.put(args[point], TXDB.get(args[point] + "sprites", text2));
        if (args[point + 1].equals("r2kCharacter")) {
            spritesheets.put(args[point], new IFunction<String, ISpritesheetProvider>() {
                @Override
                public ISpritesheetProvider apply(String imgTxt) {
                    final boolean extended = imgTxt.startsWith("$");
                    int effectiveW = 288;
                    int effectiveH = 256;
                    final IImage img = AppMain.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
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
                    final IImage img = AppMain.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                    int sprW = img.getWidth() / 12;
                    int sprH = img.getHeight() / 8;
                    int cellW = sprW;
                    int cellH = sprH;
                    int ovr = -1;
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
                    final IImage img = AppMain.stuffRendererIndependent.imageLoader.getImage(imgPfx + imgTxt, false);
                    return createSpritesheetProviderCore(imgTxt, img, useW, useH, rowCells, cellW, cellH, useX, useY, -1);
                }
            });
            return point + 8;
        }
    }

    public SchemaElement makePicPointerPatchID(SchemaElement varId) {
        // Since this is much too complicated for a mere enum,
        //  use the magical binding to make it more in-line with R48's standards,
        //  with a minimal amount of code
        HashMap<Integer, String> types = new HashMap<Integer, String>();
        types.put(0, TXDB.get("Constant"));
        types.put(1, TXDB.get("From Id Var. (PPP/EasyRPG/2k3 1.12)"));
        types.put(2, TXDB.get("From Id/Name Suffix Var. Pair (PPP/EasyRPG/2k3 1.12)"));
        HashMap<Integer, SchemaElement> disambiguations = new HashMap<Integer, SchemaElement>();
        ArrayElementSchemaElement idV = new ArrayElementSchemaElement(1, TXDB.get("idVar"), varId, null, false);
        disambiguations.put(1, idV);
        disambiguations.put(2, idV);
        AggregateSchemaElement inner = new AggregateSchemaElement(new SchemaElement[] {
                new HalfsplitSchemaElement(
                        new ArrayElementSchemaElement(0, TXDB.get("type "), new EnumSchemaElement(types, 0, ""), null, false),
                        new DisambiguatorSchemaElement("]0", new ArrayElementSchemaElement(1, TXDB.get("id "), new LowerBoundIntegerSchemaElement(1, 1), null, false), disambiguations)
                ),
                new SubwindowSchemaElement(new HWNDSchemaElement("]0", "R2K/H_Internal_PPP"), new IFunction<RubyIO, String>() {
                    @Override
                    public String apply(RubyIO rubyIO) {
                        return TXDB.get("Explain this picture mode...");
                    }
                }),
        });
        return new MagicalBindingSchemaElement(new IMagicalBinder() {
            @Override
            public RubyIO targetToBound(RubyIO target) {
                // Split PPP address into components
                long t = target.fixnumVal;
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
            public boolean applyBoundToTarget(RubyIO bound, RubyIO target) {
                // Stitch it back together
                long type = bound.arrVal[0].fixnumVal;
                long t = bound.arrVal[1].fixnumVal;
                if (type == 2) {
                    t += 50000;
                } else if (type == 1) {
                    t += 10000;
                }
                if (target.fixnumVal != t) {
                    target.fixnumVal = t;
                    return true;
                }
                return false;
            }
        }, inner);
    }
    public SchemaElement makePicPointerPatchVar(SchemaElement varId) {
        // Less complicated but still more than an enum is reasonable for.
        HashMap<Integer, SchemaElement> disambiguations = new HashMap<Integer, SchemaElement>();
        disambiguations.put(0, new ArrayElementSchemaElement(1, TXDB.get("value "), new LowerBoundIntegerSchemaElement(0, 0), null, false));
        SchemaElement inner = new HalfsplitSchemaElement(
                new ArrayElementSchemaElement(0, TXDB.get("isVar "), new IntBooleanSchemaElement(false), null, false),
                new DisambiguatorSchemaElement("]0", new ArrayElementSchemaElement(1, TXDB.get("valueVar "), varId, null, false), disambiguations)
        );
        return new MagicalBindingSchemaElement(new IMagicalBinder() {
            @Override
            public RubyIO targetToBound(RubyIO target) {
                // Split PPP address into components
                long t = target.fixnumVal;
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
            public boolean applyBoundToTarget(RubyIO bound, RubyIO target) {
                // Stitch it back together
                long type = bound.arrVal[0].fixnumVal;
                long t = bound.arrVal[1].fixnumVal;
                if (type != 0)
                    t += 10000;
                if (target.fixnumVal != t) {
                    target.fixnumVal = t;
                    return true;
                }
                return false;
            }
        }, inner);
    }
}