/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.wm;

import gabien.GaBIEn;
import gabien.IDesktopPeripherals;
import gabien.IGrInDriver;
import r48.App;
import r48.dbs.TXDB;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Manager for Konami code response.
 * Created on 07/06/17.
 */
public class Coco extends App.Svc {
    private char[] combuf = new char[10];

    public Coco(App app) {
        super(app);
    }

    public void run(IDesktopPeripherals igd) {
        if (igd.isKeyJustPressed(IGrInDriver.VK_LEFT))
            key('L');
        if (igd.isKeyJustPressed(IGrInDriver.VK_DOWN))
            key('D');
        if (igd.isKeyJustPressed(IGrInDriver.VK_RIGHT))
            key('R');
        if (igd.isKeyJustPressed(IGrInDriver.VK_UP))
            key('U');
        if (igd.isKeyJustPressed(IGrInDriver.VK_B))
            key('B');
        if (igd.isKeyJustPressed(IGrInDriver.VK_A))
            key('A');
    }

    private void key(char d) {
        for (int i = 0; i < combuf.length - 1; i++)
            combuf[i] = combuf[i + 1];
        combuf[combuf.length - 1] = d;
        String r = new String(combuf);
        if (r.equals("UUDDLRLRBA"))
            launch();
    }

    public static String getVersion() {
        try {
            InputStream r = GaBIEn.getResource("version.txt");
            if (r != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(r));
                String bb = br.readLine();
                br.close();
                return bb;
            } else {
                return "NOT-A-RELEASE";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void launch() {
        // read from prep-final-release.sh output
        try {
            InputStream r = GaBIEn.getResource("version.txt");
            if (r != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(r));
                String bb = "";
                while (br.ready()) {
                    if (!bb.isEmpty())
                        bb += "\n";
                    bb += br.readLine();
                }
                app.ui.launchDialog(bb);
                br.close();
            } else {
                app.ui.launchDialog(TXDB.get("Not an actual release - you have likely compiled this yourself.") + "\n" + TXDB.get("The file 'assets/version.txt' needs to exist for text to appear here."));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
