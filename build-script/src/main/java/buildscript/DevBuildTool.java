/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package buildscript;

import gabien.builder.api.ToolEnvironment;

/**
 * Created 17th February, 2025.
 */
public class DevBuildTool extends R48BuildTool {
    public DevBuildTool() {
        super("build-dev", "Creates a dev build of R48.");
    }

    @Override
    public void run(ToolEnvironment env) throws Exception {
        runInnards(env, "R48-DEV", "t20kdc.experimental.r48dev", 1, true);
    }
}
