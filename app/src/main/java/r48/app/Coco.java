/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.app;

import gabien.GaBIEn;
import gabien.audio.IRawAudioDriver.IRawAudioSource;
import gabien.wsi.IDesktopPeripherals;
import gabien.wsi.IGrInDriver;
import r48.App;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Manager for easter egg of unspecified origin response.
 * Created on 07/06/17.
 */
public class Coco extends App.Svc implements Consumer<IDesktopPeripherals> {
    private char[] combuf = new char[10];
    public int helpDisplayMode;

    public Coco(App app) {
        super(app);
    }

    @Override
    public void accept(IDesktopPeripherals igd) {
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
        if (igd.isKeyJustPressed(IGrInDriver.VK_I))
            key('I');
    }

    private void key(char d) {
        for (int i = 0; i < combuf.length - 1; i++)
            combuf[i] = combuf[i + 1];
        combuf[combuf.length - 1] = d;
        String r = new String(combuf);
        if (r.equals("UUDDLRLRBA"))
            launch();
        if (r.equals("UUDDLRUDUD"))
            helpDisplayMode = (helpDisplayMode + 1) % 3;
        if (r.equals("UUDDLRLRUI")) {
            app.ui.copyUITree();
            // Acknowledge without disturbing UI state using an audio cue.
            // Sorry! If it's any consolation, this is a debug tool *ONLY*.
            // There's a menu to access it but that doesn't work so well when you have a modal on-screen
            GaBIEn.getRawAudio().setRawAudioSource(new IRawAudioSource() {
                short flipper = 0;
                int totalFrames = 0;
                @Override
                public void pullData(@NonNull short[] interleaved, int ofs, int frames) {
                    for (int i = 0; i < frames; i++) {
                        interleaved[ofs++] = flipper;
                        interleaved[ofs++] = flipper;
                        if (totalFrames < 22050) {
                            if ((totalFrames & 7) == 0)
                                flipper ^= 0x0800;
                        } else {
                            flipper = 0;
                        }
                        totalFrames++;
                    }
                }
            });
        }
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
            e.printStackTrace();
            return "EXCEPTION";
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
                app.ui.launchDialog(T.u.notRelease);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
