/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package buildscript;

import gabien.builder.api.CommandEnv;
import gabien.builder.api.ToolParam;

/**
 * Created 17th February, 2025.
 */
public class ReleaseBuildTool extends R48BuildTool {
    @ToolParam(name = "--android-version-code", desc = "Android version code", optional = false, valueMeaning = "AVC")
    public int androidVersionCode;

    public ReleaseBuildTool() {
        super("build-rel", "Creates a release build of R48.");
    }

    @Override
    public void run(CommandEnv env) throws Exception {
        runInnards(env, "R48", "t20kdc.experimental.r48", androidVersionCode, false);
    }
}
