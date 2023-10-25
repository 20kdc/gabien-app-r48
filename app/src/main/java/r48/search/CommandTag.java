/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.search;

import org.eclipse.jdt.annotation.Nullable;

import r48.dbs.RPGCommand;
import r48.io.data.RORIO;
import r48.tr.TrPage.FF0;

/**
 * Tags a command for search.
 * Created 18th August, 2023.
 */
public final class CommandTag implements ICommandClassifier.Immutable {
    public final String id;
    private final FF0 translated;

    public CommandTag(String i, FF0 ti) {
        id = i;
        translated = ti;
    }

    @Override
    public String getName() {
        return translated.r();
    }

    @Override
    public boolean matches(@Nullable RPGCommand target, @Nullable RORIO data) {
        if (target == null)
            return false;
        return target.tags.contains(CommandTag.this);
    }
}
