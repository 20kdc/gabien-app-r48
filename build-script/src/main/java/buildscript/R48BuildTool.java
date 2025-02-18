/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package buildscript;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import gabien.builder.api.MajorRoutines;
import gabien.builder.api.NativesInstallTester;
import gabien.builder.api.Tool;
import gabien.builder.api.ToolEnvironment;
import gabien.builder.api.ToolParam;
import gabien.builder.api.ToolSwitch;

/**
 * Created 17th February, 2025.
 */
public abstract class R48BuildTool extends Tool {
    @ToolParam(name = "--name", desc = "The name of the build.", optional = true, valueMeaning = "NAME")
    public String releaseName = "dev" + new SimpleDateFormat("yyyyLLddHHmmss").format(new Date());

    @ToolSwitch(name = "--skip-android", desc = "Skips the Android build.")
    public boolean skipAndroid;

    public R48BuildTool(String n, String d) {
        super(n, d);
    }

    public void runInnards(ToolEnvironment env, String brand, String androidPackage, int androidVersionCode, boolean isDev) throws Exception {
        MajorRoutines.ready(env);
        if (env.hasAnyErrorOccurred())
            return;
        if (!isDev) {
            env.info("Checking natives...");
            NativesInstallTester.PREREQUISITE.run();
        }
        env.info("Building R48...");
        ProcessBuilder pb = new ProcessBuilder("./releaser-core.sh", brand, androidPackage, releaseName, Integer.toString(androidVersionCode), isDev ? "1" : "0");
        if (isDev)
            pb.environment().put("GABIEN_NATIVES_DEV", "1");
        pb.directory(new File("releaser"));
        pb.inheritIO();
        if (pb.start().waitFor() != 0)
            env.error("Error in releaser-core.sh");
    }
}
