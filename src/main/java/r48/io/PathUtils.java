/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.
 */

package r48.io;

import gabien.GaBIEn;

/**
 * Somewhere to move the rootPath fixer & capitalization fixer
 * Created on November 29th, 2017
 */
public class PathUtils {
    // Magically handles case issues.
    public static String autoDetectWindows(String s) {
        final String giveUp = s;
        try {
            // '/' is 'universal'. Not supposed to be, but that doesn't matter.
            // Firstly convert to '/' form.
            // We will be dealing with the following kinds of paths.
            // Relative: "([$PATHCHARS]*/)*[$PATHCHARS]*"
            // Windows Absolute: "?:/.*"
            // MLA Absolute / Windows NT Special Path Absolute: "/.*"
            s = s.replace('\\', '/');
            if (s.equals(""))
                return s;
            if (!s.contains("/"))
                if (s.contains(":"))
                    return s; // A: / B: / C:
            if (GaBIEn.fileOrDirExists(s))
                return s;
            // Deal with earlier path components...
            String st = GaBIEn.basename(s);
            // Sanity check.
            if (s.contains("/")) {
                if (!s.endsWith("/" + st))
                    throw new RuntimeException("Weird inconsistency in gabien path sanitizer. 'Should never happen' but safety first.");
            } else {
                // Change things to make sense.
                s = "./" + st;
            }
            String parent = autoDetectWindows(s.substring(0, s.length() - (st.length() + 1)));
            String[] subfiles = GaBIEn.listEntries(parent);
            if (subfiles != null)
                for (String s2 : subfiles)
                    if (s2.equalsIgnoreCase(st))
                        return parent + "/" + s2;
            // Oh well.
            return parent + "/" + st;
        } catch (Exception e) {
            // This will likely result from permissions errors & IO errors.
            // As this is just meant as a workaround for devs who can't use consistent case, it's not necessary to R48 operation.
            return giveUp;
        }
    }

    public static String fixRootPath(String rootPath) {
        if (!rootPath.equals(""))
            if (!rootPath.endsWith("/"))
                if (!rootPath.endsWith("\\"))
                    rootPath += "/";
        return rootPath;
    }
}
