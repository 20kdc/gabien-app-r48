/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.schema.specialized;

import gabien.GaBIEn;
import gabien.IGrDriver;
import gabien.IImage;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.schema.SchemaElement;
import r48.schema.specialized.genpos.backend.SpriteCache;
import r48.schema.specialized.genpos2.*;
import r48.schema.util.ISchemaHost;
import r48.schema.util.SchemaPath;

/**
 * Created on October 10, 2018.
 */
public class GP2SchemaElement extends SchemaElement {
    @Override
    public UIElement buildHoldingEditor(final RubyIO target, final ISchemaHost launcher, SchemaPath path) {
        return new UITextButton(TXDB.get("Test GENPOS2 (Super-Early Totally Useless Test)"), FontSizes.schemaFieldTextHeight, new Runnable() {
            @Override
            public void run() {
                final ISupplier<Boolean> actuallyBattle2 = new ISupplier<Boolean>() {
                    @Override
                    public Boolean get() {
                        if (AppMain.stuffRendererIndependent.imageLoader.getImage("Battle2/" + target.getInstVarBySymbol("@animation_name").decString(), false) == GaBIEn.getErrorImage())
                            return false;
                        return target.getInstVarBySymbol("@battle2_2k3").type == 'T';
                    }
                };
                final SpriteCache cache = new SpriteCache(target, "@animation_name", null, null, null, new IFunction<RubyIO, Integer>() {
                    @Override
                    public Integer apply(RubyIO rubyIO) {
                        if (actuallyBattle2.get())
                            return 128;
                        return 96;
                    }
                }, new IFunction<RubyIO, String>() {
                    @Override
                    public String apply(RubyIO rubyIO) {
                        if (actuallyBattle2.get())
                            return "Battle2/";
                        return "Battle/";
                    }
                });

                final GP2File file = convertR2kAnimationIntoGP2(target);
                launcher.launchOther(new UIGenpos2(file, new IGP2Renderer() {
                    @Override
                    public IImage getBackground(int frame) {
                        return null;
                    }

                    @Override
                    public int[] getIndicators(int frame) {
                        return new int[] {
                                0, 0
                        };
                    }

                    @Override
                    public Rect getCellSelectionIndicator(GP2Cell selectedCell, int frame) {
                        int x = (Integer) selectedCell.fields[2].getValueAt(frame);
                        int y = (Integer) selectedCell.fields[3].getValueAt(frame);
                        return new Rect(x - (cache.spriteSize / 2), y - (cache.spriteSize / 2), cache.spriteSize, cache.spriteSize);
                    }

                    @Override
                    public void drawCells(int frame, int opx, int opy, IGrDriver igd) {
                        for (GP2Cell gp2c : file.allObjects) {
                            int visible = (Integer) gp2c.fields[0].getValueAt(frame);
                            if (visible == 0)
                                continue;
                            int op = (int) (Integer) gp2c.fields[9].getValueAt(frame);
                            op *= 255;
                            op /= 100;
                            op = 255 - op;
                            IImage img = cache.getFramesetCache(false, false, op);
                            int x = (Integer) gp2c.fields[2].getValueAt(frame);
                            int y = (Integer) gp2c.fields[3].getValueAt(frame);
                            int cid = (Integer) gp2c.fields[1].getValueAt(frame);
                            int sc = (Integer) gp2c.fields[4].getValueAt(frame);
                            int sz = cache.getScaledImageIconSize(sc);
                            int sprSz = cache.spriteSize;
                            x = opx + x - (sz / 2);
                            y = opy + y - (sz / 2);
                            if (sc == 100) {
                                igd.blitImage((cid % 5) * sprSz, (cid / 5) * sprSz, sprSz, sprSz, x, y, img);
                            } else {
                                igd.blitScaledImage((cid % 5) * sprSz, (cid / 5) * sprSz, sprSz, sprSz, x, y, sz, sz, img);
                            }
                        }
                    }
                }));
            }
        });
    }

    private GP2File convertR2kAnimationIntoGP2(RubyIO target) {
        GP2File gp2f = new GP2File();
        String[] mapFields = new String[] {
                "@visible",
                "@cell_id",
                "@x",
                "@y",
                "@scale",
                "@tone_r",
                "@tone_g",
                "@tone_b",
                "@tone_grey",
                "@transparency"
        };
        GP2CellType gct = new GP2CellType(new String[] {
                TXDB.get("Visible"),
                TXDB.get("CellID"),
                TXDB.get("X"),
                TXDB.get("Y"),
                TXDB.get("Scale"),
                TXDB.get("TR"),
                TXDB.get("TG"),
                TXDB.get("TB"),
                TXDB.get("TGr"),
                TXDB.get("Transparency")
        }, new GP2CellType.PropType[] {
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer,
                GP2CellType.PropType.Integer
        }, new Object[] {
                1,
                0,
                0,
                0,
                100,
                100,
                100,
                100,
                100,
                0
        });
        gp2f.allCellTypes.add(gct);
        RubyIO frames = target.getInstVarBySymbol("@frames");
        frames = MagicalBinders.getBinderFor(frames).targetToBoundNCache(frames);
        for (int i = 1; i < frames.arrVal.length; i++) {
            RubyIO cells = frames.arrVal[i].getInstVarBySymbol("@cells");
            while (gp2f.allObjects.size() < (cells.arrVal.length - 1))
                gp2f.allObjects.add(new GP2Cell(gct));
            for (int j = 1; j < cells.arrVal.length; j++) {
                GP2Cell tcl = gp2f.allObjects.get(j - 1);
                for (int k = 0; k < tcl.fields.length; k++) {
                    RubyIO fld = cells.arrVal[j].getInstVarBySymbol(mapFields[k]);
                    long n = fld.fixnumVal;
                    if (k == 0)
                        n = fld.type == 'T' ? 1 : 0;
                    tcl.fields[k].addPoint(new GP2Timeline.TimePoint<Integer>(i - 1, (int) n));
                }
            }
        }
        for (GP2Cell gp : gp2f.allObjects)
            for (GP2Timeline gp2t : gp.fields)
                gp2t.optimize();
        gp2f.setLength = frames.arrVal.length - 1;
        return gp2f;
    }

    @Override
    public void modifyVal(RubyIO target, SchemaPath path, boolean setDefault) {

    }
}
