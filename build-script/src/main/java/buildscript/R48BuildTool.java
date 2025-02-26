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

import gabien.builder.api.CommandEnv;
import gabien.builder.api.MajorRoutines;
import gabien.builder.api.NativesInstallTester;
import gabien.builder.api.Tool;
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

    public void runInnards(CommandEnv env, String brand, String androidPackage, int androidVersionCode, boolean isDev) throws Exception {
        env = env.clone();
        if (isDev)
            env.envOverrides.put("GABIEN_NATIVES_DEV", "1");
        MajorRoutines.ready(env);
        if (env.toolEnv.hasAnyErrorOccurred())
            return;
        if (!isDev) {
            env.toolEnv.info("Checking natives...");
            NativesInstallTester.PREREQUISITE.run();
        }
        env.toolEnv.info("");
        env.toolEnv.info("R48 Release Process");
        env.toolEnv.info("Name: " + brand + " Package: " + androidPackage + " RID: " + releaseName + " AVC: " + androidVersionCode);
        env.toolEnv.info("Dev: " + isDev);
        env.toolEnv.info("");
        env.toolEnv.info("Building R48...");
        env.cd("releaser").run("./releaser-pre.sh", releaseName, Integer.toString(androidVersionCode), isDev ? "1" : "0");
        if (env.toolEnv.hasAnyErrorOccurred())
            return;
        env.toolEnv.info("Building R48 [OK]");
        env.toolEnv.info("");
        env.toolEnv.info("Finalizing desktop version...");
        env.cd("releaser").run("./releaser-desktop.sh", releaseName);
        if (env.toolEnv.hasAnyErrorOccurred())
            return;
        env.toolEnv.info("Finalizing desktop version [OK]");
        env.toolEnv.info("");
        if (!skipAndroid) {
            env.toolEnv.info("Finalizing Android version...");
            env.cd(new File(CommandEnv.GABIEN_HOME, "android")).run("./releaser.sh", brand, androidPackage, releaseName, Integer.toString(androidVersionCode), "../../gabien-app-r48/releaser/android/target/r48-android-0.666-SNAPSHOT-jar-with-dependencies.jar", "../../gabien-app-r48/releaser/icon.png", "android.permission.WRITE_EXTERNAL_STORAGE");
            if (!env.toolEnv.hasAnyErrorOccurred())
                new File(CommandEnv.GABIEN_HOME, "android/result.apk").renameTo(new File(releaseName + ".apk"));
            env.toolEnv.info("Finalizing Android version [OK]");
            env.toolEnv.info("");
        }
        if (!env.toolEnv.hasAnyErrorOccurred()) {
            env.toolEnv.info("All builds completed successfully. Please move to testing phase.");
        } else {
            env.toolEnv.error("An error did occur during R48 build.");
        }
    }
}
