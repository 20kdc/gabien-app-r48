/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */
package r48.ui;

import gabien.GaBIEn;
import gabien.IGrInDriver;
import r48.AppMain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Manager for Konami code response.
 * Created on 07/06/17.
 */
public class Coco {
    private static char[] combuf = new char[10];

    public static void run(IGrInDriver igd) {
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

    private static void key(char d) {
        for (int i = 0; i < combuf.length - 1; i++)
            combuf[i] = combuf[i + 1];
        combuf[combuf.length - 1] = d;
        String r = new String(combuf);
        if (r.equals("UUDDLRLRBA"))
            coco();
    }

    private static void coco() {
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
                AppMain.launchDialog(bb);
                br.close();
            } else {
                AppMain.launchDialog("Not an actual release. You have likely compiled this yourself. If not, check where you got this from.\n-<twenty>kdc, fooling automated replacement tools since 2017");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
