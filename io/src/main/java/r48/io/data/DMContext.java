/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.data;

import java.nio.charset.Charset;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.append.Entity;

/**
 * Responsible for setting up initialization of DM2 fields.
 * Created 5th April 2023 in response to needing to shuffle data around.
 * Moved to EntityType and renamed DMContext 8th May 2024.
 */
public final class DMContext extends Entity<DMContext> {
    public static final Entity.Registrar<DMContext> I = newRegistrar();

    public final @NonNull IDMChangeTracker changes;
    public final Charset encoding;

    public DMContext(@NonNull IDMChangeTracker changes, @NonNull Charset encoding) {
        this.changes = changes;
        this.encoding = encoding;
    }

    public static final class Key<T> extends Entity.Key<DMContext, T> {
        public Key() {
            super(I);
        }
    }
}
