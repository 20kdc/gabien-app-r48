/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import r48.io.data.DMContext;
import r48.io.data.IRIO;
import r48.io.data.RORIO;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A simpler object backend interface that's somewhat less typesafe (but oh well)
 * Created on November 21, 2018.
 */
public abstract class OldObjectBackend<R extends RORIO, W extends IRIO> implements IObjectBackend {
    @Override
    public ILoadedObject loadObject(String filename, @NonNull DMContext context) {
        W rio = loadObjectFromFile(filename, context);
        if (rio == null)
            return null;
        return new OldObjectBackendLoadedObject(rio, filename);
    }

    @Override
    public ILoadedObject newObject(String filename, @NonNull DMContext context) {
        return new OldObjectBackendLoadedObject(newObjectO(filename, context), filename);
    }

    public abstract W newObjectO(String filename, @NonNull DMContext context);

    public abstract W loadObjectFromFile(String filename, @NonNull DMContext context);

    public abstract void saveObjectToFile(String filename, R obj) throws IOException;

    private class OldObjectBackendLoadedObject implements ILoadedObject {
        private W intern;
        private final String fn;

        public OldObjectBackendLoadedObject(W rio, String filename) {
            intern = rio;
            fn = filename;
        }

        @Override
        public IRIO getObject() {
            return intern;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void save() throws IOException {
            saveObjectToFile(fn, (R) intern);
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean overwriteWith(ILoadedObject other) {
            if (other.getClass() == getClass()) {
                if (((OldObjectBackendLoadedObject) other).intern.getClass() == intern.getClass()) {
                    if (intern.context == ((OldObjectBackendLoadedObject) other).intern.context) {
                        intern = ((OldObjectBackendLoadedObject) other).intern;
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
