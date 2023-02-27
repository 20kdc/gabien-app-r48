/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import gabien.GaBIEn;
import r48.dbs.TXDB;
import r48.io.IObjectBackend;
import r48.io.PathUtils;
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
            GaBIEn.makeDirectories(PathUtils.autoDetectWindows(AppMain.rootPath + s));
        for (int i = 0; i < fileCopies.length; i += 2) {
            String src = fileCopies[i];
            String dst = fileCopies[i + 1];
            InputStream inp = GaBIEn.getResource(src);
            if (inp != null) {
                String tgt = PathUtils.autoDetectWindows(AppMain.rootPath + dst);
                if (GaBIEn.fileOrDirExists(tgt)) {
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
        final Runnable deploy2k = new Runnable() {
            @Override
            public void run() {
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
                AppMain.launchDialog(TXDB.get("The synthesis was completed successfully."));
            }
        };
        app.ui.wm.createWindowSH(new UIChoicesMenu(TXDB.get("Would you like a basic template, and if so, compatible with RPG Maker 2000 or 2003? All assets used for this are part of R48, and thus public-domain."), new String[] {
                TXDB.get("2000 Template"),
                TXDB.get("2003 Template"),
                TXDB.get("Do Nothing")
        }, new Runnable[] {
                deploy2k,
                new Runnable() {
                    @Override
                    public void run() {
                        IObjectBackend.ILoadedObject root = AppMain.objectDB.getObject("RPG_RT.ldb");
                        R2kSystemDefaultsInstallerSchemaElement.upgradeDatabase(root.getObject());
                        AppMain.objectDB.objectRootModified(root, new SchemaPath(new OpaqueSchemaElement(), root));
                        deploy2k.run();
                    }
                }, new Runnable() {
            @Override
            public void run() {

            }
        }
        }));
    }
}
