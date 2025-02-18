/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package buildscript;

import java.io.File;

import gabien.builder.api.Commands;
import gabien.builder.api.ToolEnvironment;
import gabien.builder.api.ToolSwitch;

/**
 * Created 17th February, 2025.
 */
public class DevBuildTool extends R48BuildTool {
    @ToolSwitch(name = "--no-adb", desc = "Skips attempting to install the APK using ADB.")
    public boolean noADB;

    public DevBuildTool() {
        super("build-dev", "Creates a dev build of R48.");
    }

    @Override
    public void run(ToolEnvironment env) throws Exception {
        runInnards(env, "R48-DEV", "t20kdc.experimental.r48dev", 1, true);
        if (env.hasAnyErrorOccurred())
            return;
        if (!(skipAndroid || noADB)) {
            env.info("Installing to Android device...");
            Commands.runOptional(env, new File("."), "adb", "install", "-r", releaseName + ".apk");
        }
    }
}
