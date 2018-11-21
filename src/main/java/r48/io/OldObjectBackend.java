/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import r48.RubyIO;
import r48.io.data.IRIO;

import java.io.IOException;

/**
 * Created on November 21, 2018.
 */
public abstract class OldObjectBackend implements IObjectBackend {
    @Override
    public ILoadedObject loadObject(String filename) {
        RubyIO rio = loadObjectFromFile(filename);
        if (rio == null)
            return null;
        return new OldObjectBackendLoadedObject(rio, filename);
    }

    @Override
    public ILoadedObject newObject(String filename) {
        return new OldObjectBackendLoadedObject(new RubyIO().setNull(), filename);
    }

    public abstract RubyIO loadObjectFromFile(String filename);

    public abstract void saveObjectToFile(String filename, RubyIO obj) throws IOException;

    @Override
    public String userspaceBindersPrefix() {
        return null;
    }

    private class OldObjectBackendLoadedObject implements ILoadedObject {
        private final RubyIO intern;
        private final String fn;

        public OldObjectBackendLoadedObject(RubyIO rio, String filename) {
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
    }
}
