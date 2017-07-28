/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.schema.specialized.genpos;

import gabien.IGrInDriver;
import gabien.ui.UILabel;
import r48.AppMain;
import r48.RubyIO;
import r48.dbs.TXDB;
import r48.map.imaging.IImageLoader;

import java.util.Map;

/**
 * Created on 28/07/17.
 */
public class TroopGenposFrame implements IGenposFrame {
    public IGrInDriver.IImage battleBkg;
    public RubyIO troop;
    public IGrInDriver.IImage[] enemies;
    public Runnable changed;
    public TroopGenposFrame(RubyIO t, Runnable change) {
        troop = t;
        changed = change;
        // Immediately try and get needed resources
        RubyIO database = AppMain.objectDB.getObject("RPG_RT.ldb");
        IImageLoader img = AppMain.stuffRendererIndependent.imageLoader;
        battleBkg = img.getImage("Backdrop/" + database.getInstVarBySymbol("@system").getInstVarBySymbol("@test_battle_background").decString(), true);
        long max = 0;
        for (RubyIO rio : database.getInstVarBySymbol("@enemies").hashVal.keySet())
            max = Math.max(max, rio.fixnumVal);
        enemies = new IGrInDriver.IImage[(int) (max + 1)];
        for (Map.Entry<RubyIO, RubyIO> map : database.getInstVarBySymbol("@enemies").hashVal.entrySet())
            enemies[(int) (map.getKey().fixnumVal)] = readEnemy(map.getValue(), img);
    }

    private IGrInDriver.IImage readEnemy(RubyIO value, IImageLoader img) {
        return img.getImage("Monster/" + value.getInstVarBySymbol("@battler_name").decString(), false);
    }

    @Override
    public int[] getIndicators() {
        return new int[] {
                // 320x160 (RPG Maker 2000)
                0, 0,
                320, 0,
                0, 160,
                320, 160,
                // 2k3?
                0, 240,
                320, 240
        };
    }

    @Override
    public boolean canAddRemoveCells() {
        return false;
    }

    @Override
    public void addCell(int i2) {

    }

    @Override
    public void deleteCell(int i2) {

    }

    @Override
    public int getCellProp(int ct, int i) {
        RubyIO member = troop.getInstVarBySymbol("@members").arrVal[ct + 1];
        if (i == 0)
            return (int) member.getInstVarBySymbol("@enemy").fixnumVal;
        if (i == 1)
            return (int) member.getInstVarBySymbol("@x").fixnumVal;
        if (i == 2)
            return (int) member.getInstVarBySymbol("@y").fixnumVal;
        if (i == 3)
            return member.getInstVarBySymbol("@invisible").type == 'T' ? 1 : 0;
        return 0;
    }

    @Override
    public void setCellProp(int ct, int i, int number) {
        RubyIO member = troop.getInstVarBySymbol("@members").arrVal[ct + 1];
        if (i == 0)
            member.getInstVarBySymbol("@enemy").fixnumVal = number;
        if (i == 1)
            member.getInstVarBySymbol("@x").fixnumVal = number;
        if (i == 2)
            member.getInstVarBySymbol("@y").fixnumVal = number;
        if (i == 3)
            member.getInstVarBySymbol("@invisible").type = (number != 0) ? 'T' : 'F';
        changed.run();
    }

    @Override
    public int getCellCount() {
        return Math.max(0, troop.getInstVarBySymbol("@members").arrVal.length - 1);
    }

    @Override
    public String[] getCellProps() {
        return new String[] {
                TXDB.get("enemyId"),
                TXDB.get("x"),
                TXDB.get("y"),
                TXDB.get("invisible")
        };
    }

    @Override
    public void drawCell(int i, int opx, int opy, IGrInDriver igd) {
        int enemy = getCellProp(i, 0);
        opx += getCellProp(i, 1);
        opy += getCellProp(i, 2);
        if (enemy < 0)
            return;
        if (enemy >= enemies.length)
            return;
        IGrInDriver.IImage enemyImg = enemies[enemy];
        if (enemyImg == null) {
            // What to do?
        } else {
            igd.blitImage(0, 0, enemyImg.getWidth(), enemyImg.getHeight(), opx - (enemyImg.getWidth() / 2), opy - (enemyImg.getHeight() / 2), enemyImg);
        }
    }

    @Override
    public IGrInDriver.IImage getBackground() {
        return battleBkg;
    }
}
