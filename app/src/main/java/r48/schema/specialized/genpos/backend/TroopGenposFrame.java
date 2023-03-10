/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.schema.specialized.genpos.backend;

import gabien.IGrDriver;
import gabien.IImage;
import gabien.uslx.append.*;
import gabien.ui.Rect;
import r48.App;
import r48.RubyIO;
import r48.imagefx.HueShiftImageEffect;
import r48.io.data.IRIO;
import r48.map.imaging.IImageLoader;
import r48.schema.BooleanSchemaElement;
import r48.schema.SchemaElement;
import r48.schema.integers.IntegerSchemaElement;
import r48.schema.specialized.genpos.IGenposFrame;
import r48.schema.specialized.genpos.IGenposTweeningProp;
import r48.schema.util.SchemaPath;

/**
 * Created on 28/07/17.
 */
public class TroopGenposFrame extends App.Svc implements IGenposFrame {

    public static final int[] gameBattleDisplay = new int[] {
            // 320x160 (RPG Maker 2000)
            0, 0,
            320, 0,
            0, 160,
            320, 160,
            // 2k3?
            0, 240,
            320, 240
    };

    public IImage battleBkg;
    public IRIO troop;
    public SchemaPath troopPath;
    public IImage[] enemies;
    public Runnable changed;

    public TroopGenposFrame(App app, IRIO t, SchemaPath path, Runnable change) {
        super(app);
        troop = t;
        troopPath = path;
        changed = change;
        // Immediately try and get needed resources
        IRIO database = app.odb.getObject("RPG_RT.ldb").getObject();
        IImageLoader img = app.stuffRendererIndependent.imageLoader;
        battleBkg = img.getImage("Backdrop/" + database.getIVar("@system").getIVar("@test_battle_background").decString(), true);
        long max = 0;

        IRIO enemi = database.getIVar("@enemies");

        for (IRIO rio : enemi.getHashKeys())
            max = Math.max(max, rio.getFX());
        enemies = new IImage[(int) (max + 1)];
        for (IRIO map : enemi.getHashKeys())
            enemies[(int) (map.getFX())] = readEnemy(enemi.getHashVal(map), img);
    }

    private IImage readEnemy(IRIO value, IImageLoader img) {
        IImage im = img.getImage("Monster/" + value.getIVar("@battler_name").decString(), false);
        return app.ui.imageFXCache.process(im, new HueShiftImageEffect((int) value.getIVar("@battler_hue").getFX()));
    }

    @Override
    public int[] getIndicators() {
        return gameBattleDisplay;
    }

    @Override
    public boolean canAddRemoveCells() {
        return true;
    }

    @Override
    public void addCell(int i2) {
        IRIO rio = troop.getIVar("@members").addAElem(i2 + 1);
        SchemaPath.setDefaultValue(rio, app.sdb.getSDBEntry("RPG::Troop::Member"), new RubyIO().setFX(i2 + 1));
        changed.run();
    }

    @Override
    public void deleteCell(int i2) {
        troop.getIVar("@members").rmAElem(i2 + 1);
        changed.run();
    }

    private SchemaElement[] getCellPropSchemas() {
        return new SchemaElement[] {
                app.sdb.getSDBEntry("enemy_id"),
                new IntegerSchemaElement(app, 0),
                new IntegerSchemaElement(app, 0),
                new BooleanSchemaElement(app, false)
        };
    }

    @Override
    public SchemaPath getCellProp(int ct, int i) {
        SchemaPath memberPath = troopPath.otherIndex("@members").arrayHashIndex(new RubyIO().setFX(ct + 1), "[" + (ct + 1) + "]");
        IRIO member = troop.getIVar("@members").getAElem(ct + 1);
        SchemaElement se = getCellPropSchemas()[i];
        if (i == 0)
            return memberPath.newWindow(se, member.getIVar("@enemy"));
        if (i == 1)
            return memberPath.newWindow(se, member.getIVar("@x"));
        if (i == 2)
            return memberPath.newWindow(se, member.getIVar("@y"));
        if (i == 3)
            return memberPath.newWindow(se, member.getIVar("@invisible"));
        throw new RuntimeException("Invalid cell prop.");
    }

    @Override
    public IGenposTweeningProp getCellPropTweening(int ct, int i) {
        return null;
    }

    @Override
    public void moveCell(int ct, IFunction<Integer, Integer> x, IFunction<Integer, Integer> y) {
        SchemaPath memberPath = troopPath.otherIndex("@members").arrayHashIndex(new RubyIO().setFX(ct + 1), "[" + (ct + 1) + "]");
        IRIO member = troop.getIVar("@members").getAElem(ct + 1);
        member.getIVar("@x").setFX(x.apply((int) member.getIVar("@x").getFX()));
        member.getIVar("@y").setFX(y.apply((int) member.getIVar("@y").getFX()));
        memberPath.changeOccurred(false);
    }

    @Override
    public int getCellCount() {
        return Math.max(0, troop.getIVar("@members").getALen() - 1);
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                T.z.l120,
                T.z.l121,
                T.z.l122,
                T.z.l123
        };
    }

    @Override
    public Rect getCellSelectionIndicator(int i) {
        int x = (int) getCellProp(i, 1).targetElement.getFX();
        int y = (int) getCellProp(i, 2).targetElement.getFX();
        int w = 32;
        int h = 32;
        int enemy = (int) getCellProp(i, 0).targetElement.getFX();
        IImage enemyImg = enemies[enemy];
        if (enemyImg != null) {
            w = enemyImg.getWidth();
            h = enemyImg.getHeight();
        }
        return new Rect(x - (w / 2), y - (h / 2), w, h);
    }

    @Override
    public void drawCell(int i, int opx, int opy, IGrDriver igd) {
        // hm.
        int enemy = (int) getCellProp(i, 0).targetElement.getFX();
        opx += getCellProp(i, 1).targetElement.getFX();
        opy += getCellProp(i, 2).targetElement.getFX();
        if (enemy < 0)
            return;
        if (enemy >= enemies.length)
            return;
        IImage enemyImg = enemies[enemy];
        if (enemyImg != null)
            igd.blitImage(0, 0, enemyImg.getWidth(), enemyImg.getHeight(), opx - (enemyImg.getWidth() / 2), opy - (enemyImg.getHeight() / 2), enemyImg);
    }

    @Override
    public IImage getBackground() {
        return battleBkg;
    }
}
