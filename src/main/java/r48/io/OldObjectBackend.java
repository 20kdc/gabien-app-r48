/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import r48.io.data.IRIO;

import java.io.IOException;

/**
 * A simpler object backend interface that's somewhat less typesafe (but oh well)
 * Created on November 21, 2018.
 */
public abstract class OldObjectBackend<O extends IRIO> implements IObjectBackend {
    @Override
    public ILoadedObject loadObject(String filename) {
        O rio = loadObjectFromFile(filename);
        if (rio == null)
            return null;
        return new OldObjectBackendLoadedObject(rio, filename);
    }

    @Override
    public ILoadedObject newObject(String filename) {
        return new OldObjectBackendLoadedObject(newObjectO(filename), filename);
    }

    public abstract O newObjectO(String filename);

    public abstract O loadObjectFromFile(String filename);

    public abstract void saveObjectToFile(String filename, O obj) throws IOException;

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }

    private class OldObjectBackendLoadedObject implements ILoadedObject {
        private O intern;
        private final String fn;

        public OldObjectBackendLoadedObject(O rio, String filename) {
            intern = rio;
            fn = filename;
        }

        @Override
        public IRIO getObject() {
            return intern;
        }

        @Override
        public void save() throws IOException {
            saveObjectToFile(fn, intern);
        }

        @Override
        public boolean overwriteWith(ILoadedObject other) {
            if (other.getClass() == getClass()) {
                if (((OldObjectBackendLoadedObject) other).intern.getClass() == intern.getClass()) {
                    intern = ((OldObjectBackendLoadedObject) other).intern;
                    return true;
                }
            }
            return false;
        }
    }
}
