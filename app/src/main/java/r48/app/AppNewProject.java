/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gabien.GaBIEn;
import gabien.uslx.vfs.FSBackend;
import r48.App;
import r48.io.IObjectBackend;
import r48.schema.OpaqueSchemaElement;
import r48.schema.specialized.R2kSystemDefaultsInstallerSchemaElement;
import r48.schema.util.SchemaPath;
import r48.ui.dialog.UIChoicesMenu;

/**
 * An attempt to move as much as possible out of static variables.
 * Created 27th February, 2023
 */
public class AppNewProject extends App.Svc {
    public AppNewProject(App app) {
        super(app);
    }

    private void fileCopier(String[] mkdirs, String[] fileCopies) {
        for (String s : mkdirs)
            app.gameRoot.intoPath(s).mkdirs();
        for (int i = 0; i < fileCopies.length; i += 2) {
            String src = fileCopies[i];
            String dst = fileCopies[i + 1];
            InputStream inp = GaBIEn.getResource(src);
            if (inp != null) {
                FSBackend tgt = app.gameRoot.intoPath(dst);
                if (tgt.exists()) {
                    System.err.println("Didn't write " + dst + " as it is already present as " + tgt + ".");
                    try {
                        inp.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                OutputStream oup = GaBIEn.getOutFile(tgt);
                if (oup != null) {
                    try {
                        byte[] b = new byte[2048];
                        while (inp.available() > 0)
                            oup.write(b, 0, inp.read(b));
                    } catch (IOException ioe) {

                    }
                    try {
                        oup.close();
                    } catch (IOException ioe) {

                    }
                }
                try {
                    inp.close();
                } catch (IOException ioe) {
                }
            } else {
                System.err.println("Didn't write " + dst + " as " + src + " missing.");
            }
        }
    }

    // R2kSystemDefaultsInstallerSchemaElement uses this to indirectly access several things a SchemaElement isn't allowed to access.
    public void r2kProjectCreationHelperFunction() {
        final Runnable deploy2k = () -> {
            // Perform all mkdirs
            String[] mkdirs = {
                    "Backdrop",
                    "Battle",
                    "Battle2",
                    "BattleCharSet",
                    "BattleWeapon",
                    "CharSet",
                    "ChipSet",
                    "FaceSet",
                    "Frame",
                    "GameOver",
                    "Monster",
                    "Music",
                    "Panorama",
                    "Picture",
                    "Sound",
                    "System",
                    "System2",
                    "Title"
            };
            String[] fileCopies = {
                    "R2K/char.png", "CharSet/char.png",
                    "R2K/faceset.png", "FaceSet/faceset.png",
                    "R2K/backdrop.png", "Backdrop/backdrop.png",
                    "R2K/System.png", "System/System.png",
                    "R2K/templatetileset.png", "ChipSet/templatetileset.png",
                    "R2K/slime.png", "Monster/monster.png",
                    "R2K/templateconfig.ini", "RPG_RT.ini"
            };
            fileCopier(mkdirs, fileCopies);
            // Load map 1, save everything
            app.ui.mapContext.loadMap("Map.1");
            app.odb.ensureAllSaved();
            app.ui.launchDialog(T.u.np_synthOk);
        };
        app.ui.wm.createWindowSH(new UIChoicesMenu(app, T.u.np_synth2kQ, new String[] {
                T.u.np_r2k0,
                T.u.np_r2k3,
                T.u.np_nothing
        }, new Runnable[] {
                deploy2k,
                () -> {
                    IObjectBackend.ILoadedObject root = app.odb.getObject("RPG_RT.ldb");
                    R2kSystemDefaultsInstallerSchemaElement.upgradeDatabase(root.getObject());
                    app.odb.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(app), root));
                    deploy2k.run();
                }, () -> {
                }
        }));
    }
}
