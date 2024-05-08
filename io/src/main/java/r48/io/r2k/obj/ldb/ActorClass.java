/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.obj.ldb;

import r48.io.data.DMContext;

/**
 * Migrated to ActorClassBase on December 7th 2018
 */
public class ActorClass extends ActorClassBase {
    public ActorClass(DMContext ctx) {
        super(ctx, "RPG::Class", 0);
    }
}
