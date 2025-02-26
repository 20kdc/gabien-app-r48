/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package buildscript;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        if (isDev)
            env = env.env("GABIEN_NATIVES_DEV", "1");
        MajorRoutines.ready(env);
        if (!isDev) {
            env.info("Checking natives...");
            NativesInstallTester.PREREQUISITE.run();
        }
        env.info("");
        env.info("R48 Release Process");
        env.info("Name: " + brand + " Package: " + androidPackage + " RID: " + releaseName + " AVC: " + androidVersionCode);
        env.info("Dev: " + isDev);
        env.info("");
        env.info("Building R48...");
        // metadata setup
        MajorRoutines.recursivelyDelete(new File("releaser/metadata/src"));
        File mdAssets = new File("releaser/metadata/src/main/resources/assets");
        mdAssets.mkdirs();
        Files.copy(new File("CREDITS.txt").toPath(), new File(mdAssets, "CREDITS.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(new File("COPYING.txt").toPath(), new File(mdAssets, "COPYING.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
        try (PrintStream ps = new PrintStream(new File(mdAssets, "version.txt"), "UTF-8")) {
            if (isDev) {
                ps.print("R48 " + releaseName + " [debug]\n");
            } else {
                ps.print("R48 " + releaseName + "\n");
            }
            ps.print("AVC " + androidVersionCode + "\n");
            ps.print("gabien-app-r48 - Editing program for various formats\n");
            ps.print("Written starting in 2016 by contributors (see CREDITS.txt)\n");
            ps.print("To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.\n");
            ps.print("A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.\n");
            ps.print("\n");
            for (String s : Files.readAllLines(new File("CREDITS.txt").toPath(), StandardCharsets.UTF_8)) {
                ps.print(s + "\n");
            }
            ps.print("\n");
        }
        // actual package
        env.run(CommandEnv.UMVN_COMMAND, "package", "-q");
        env.info("Building R48 [OK]");
        env.info("");
        env.info("Finalizing desktop version...");
        Files.copy(new File("releaser/javase/target/r48-javase-0.666-SNAPSHOT-jar-with-dependencies.jar").toPath(), new File(releaseName + ".jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
        env.info("Finalizing desktop version [OK]");
        env.info("");
        if (!skipAndroid) {
            env.info("Finalizing Android version...");
            env.cd(new File(CommandEnv.GABIEN_HOME, "android")).run("./releaser.sh", brand, androidPackage, releaseName, Integer.toString(androidVersionCode), "../../gabien-app-r48/releaser/android/target/r48-android-0.666-SNAPSHOT-jar-with-dependencies.jar", "../../gabien-app-r48/releaser/icon.png", "android.permission.WRITE_EXTERNAL_STORAGE");
            if (!env.hasAnyErrorOccurred())
                new File(CommandEnv.GABIEN_HOME, "android/result.apk").renameTo(new File(releaseName + ".apk"));
            env.info("Finalizing Android version [OK]");
            env.info("");
        }
        if (!env.hasAnyErrorOccurred()) {
            env.info("All builds completed successfully. Please move to testing phase.");
        } else {
            env.error("An error did occur during R48 build.");
        }
    }
}
