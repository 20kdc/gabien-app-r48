/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package gabienapp;

import gabien.GaBIEn;
import gabien.ui.IConsumer;
import gabien.ui.UIPopupMenu;
import gabien.ui.WindowCreatingUIElementConsumer;
import r48.AppMain;
import r48.dbs.DBLoader;
import r48.dbs.IDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

/**
 * Created on 1/27/17.
 */
public class Application {

    private static IConsumer<Double> appTicker = null;

    public static void gabienmain() throws IOException {
        final WindowCreatingUIElementConsumer uiTicker = new WindowCreatingUIElementConsumer();

        final LinkedList<String> gamepakList = new LinkedList<String>();
        final LinkedList<String> gamepakNameList = new LinkedList<String>();

        gamepakList.add("");
        gamepakNameList.add("Choose Game");

        DBLoader dbl = new DBLoader(new BufferedReader(new InputStreamReader(GaBIEn.getFile("Gamepaks.txt"))), new IDatabase() {
            @Override
            public void newObj(int objId, String objName) throws IOException {
                InputStream tester = GaBIEn.getFile(objName + "/Schema.txt");
                if (tester != null) {
                    tester.close();
                } else {
                    return;
                }
                gamepakList.add(objName);
                String status = "?";
                if (objId == 0)
                    status = "Working";
                if (objId == 1)
                    status = "Mostly OK";
                if (objId == 2)
                    status = "Minimal/Useless";
                if (objId == 3)
                    status = "Non-existent";
                gamepakNameList.add(objName + " (" + status + ")");
            }

            @Override
            public void execCmd(char c, String[] args) throws IOException {
                if (c == '.') {
                    String rn = "";
                    for (String s : args)
                        rn += s + " ";
                    gamepakNameList.set(gamepakNameList.size() - 1, rn);
                }
            }
        });

        final String[] gamepakNames = gamepakNameList.toArray(new String[0]);
        Runnable[] gamepakButtons = new Runnable[gamepakNames.length];

        for (int i = 0; i < gamepakButtons.length; i++) {
            final int ie = i;
            gamepakButtons[i] = new Runnable() {
                @Override
                public void run() {
                    String id = gamepakList.get(ie);
                    if (id.length() == 0)
                        return;
                    if (appTicker == null)
                        try {
                            appTicker = AppMain.initializeAndRun(uiTicker, id + "/");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            };
        }

        uiTicker.accept(new UIPopupMenu(gamepakNames, gamepakButtons, true, true));

        while (uiTicker.runningWindows() > 0) {
            double dT = GaBIEn.timeDelta(false);
            while (dT < 0.02d) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dT = GaBIEn.timeDelta(false);
            }
            dT = GaBIEn.timeDelta(true);
            if (appTicker != null)
                appTicker.accept(dT);
            uiTicker.runTick(dT);
        }
        GaBIEn.ensureQuit();
    }
}
