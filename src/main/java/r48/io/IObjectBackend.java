/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import r48.RubyIO;

import java.io.IOException;

/**
 * Allows for the creation of non-standard backends which don't use the normal Ruby marshal format.
 * Presumably for "flat binary file" formats, some emulation is involved.
 * In any case, this makes the whole thing more flexible.
 * Created on 1/27/17.
 */
public interface IObjectBackend {
    RubyIO loadObjectFromFile(String filename);

    void saveObjectToFile(String filename, RubyIO object) throws IOException;
}
