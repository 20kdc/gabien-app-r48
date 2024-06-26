/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io;

import r48.io.cs.CSObjectBackend;
import r48.io.data.DMContext;
import r48.io.data.IRIO;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import gabien.uslx.vfs.FSBackend;

/**
 * Allows for the creation of non-standard backends which don't use the normal Ruby marshal format.
 * Presumably for "flat binary file" formats, some emulation is involved.
 * In any case, this makes the whole thing more flexible.
 * Created on 1/27/17.
 */
public interface IObjectBackend {
    /**
     * Returns null on failure.
     * The caller is assumed to be holding a License To Unpack Data.
     */
    ILoadedObject loadObject(String filename, @NonNull DMContext context);

    /**
     * Also returns null on failure.
     * The caller is assumed to be holding a License To Unpack Data.
     */
    ILoadedObject newObject(String filename, @NonNull DMContext context);

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
        public static IObjectBackend create(FSBackend fs, String odbBackend, String dataPath, String dataExt) {
            if (odbBackend.equals("r48")) {
                return new R48ObjectBackend(fs, dataPath, dataExt);
            } else if (odbBackend.equals("ika")) {
                return new IkaObjectBackend(fs, dataPath);
            } else if (odbBackend.equals("lcf2000")) {
                return new R2kObjectBackend(fs, dataPath);
            } else if (odbBackend.equals("json")) {
                return new JsonObjectBackend(fs, dataPath, dataExt);
            } else if (odbBackend.equals("cs")) {
                return new CSObjectBackend(fs, dataPath);
            } else {
                throw new RuntimeException("Unknown ODB backend " + odbBackend);
            }
        }
    }
}
