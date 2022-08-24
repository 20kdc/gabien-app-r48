/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package gabienapp;

import gabien.uslx.append.*;

public interface IGPMenuPanel {
    String[] getButtonText();

    IFunction<LauncherState, IGPMenuPanel>[] getButtonActs();

    public class LauncherState {
        public String rootPath;
        public String secondaryImagePath;
        public LauncherState(String rp, String sip) {
            rootPath = rp;
            secondaryImagePath = sip;
        }
    }
}
