/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import r48.io.cs.CSObjectBackend;
import r48.io.data.IRIO;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Allows for the creation of non-standard backends which don't use the normal Ruby marshal format.
 * Presumably for "flat binary file" formats, some emulation is involved.
 * In any case, this makes the whole thing more flexible.
 * Created on 1/27/17.
 */
public interface IObjectBackend {
    // Returns null on failure.
    ILoadedObject loadObject(String filename);

    // Also returns null on failure.
    ILoadedObject newObject(String filename);

    // Does this backend use userspace binders, and if so, what's the usersym prefix? Can be null.
    String userspaceBindersPrefix();

    interface ILoadedObject {
        IRIO getObject();
        // Overwrites this object with another if possible.
        // Returns false if not possible (slowpath)
        boolean overwriteWith(ILoadedObject other);

        void save() throws IOException;
    }

    public class MockLoadedObject implements ILoadedObject {
        private final IRIO obj;

        public MockLoadedObject(IRIO o) {
            obj = o;
        }

        @Override
        public IRIO getObject() {
            return obj;
        }

        @Override
        public void save() throws IOException {
        }

        @Override
        public boolean overwriteWith(ILoadedObject other) {
            return false;
        }
    }

    abstract class Factory {
        // Null so that things will error if it's unset.
        public static Charset encoding;

        public static IObjectBackend create(String odbBackend, String rootPath, String dataPath, String dataExt) {
            if (odbBackend.equals("r48")) {
                return new R48ObjectBackend(rootPath + dataPath, dataExt, IObjectBackend.Factory.encoding);
            } else if (odbBackend.equals("ika")) {
                return new IkaObjectBackend(rootPath + dataPath);
            } else if (odbBackend.equals("lcf2000")) {
                return new R2kObjectBackend(rootPath + dataPath);
            } else if (odbBackend.equals("json")) {
                return new JsonObjectBackend(rootPath + dataPath, dataExt);
            } else if (odbBackend.equals("cs")) {
                return new CSObjectBackend(rootPath + dataPath);
            } else {
                throw new RuntimeException("Unknown ODB backend " + odbBackend);
            }
        }
    }
}
