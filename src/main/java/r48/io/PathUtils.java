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
    // TO JAPANESE/KOREAN USERS WONDERING WHY THIS BREAKS THEIR DIRECTORY STRUCTURE:
    // This is Microsoft's fault.
    // I can't fix this in a way that won't cause other kinds of breakage.
    // If I disable this, you'll find you can't load games properly.
    // If you want to remove the Yen sign then you have to switch the Japanese encodings
    // back to "Cp943C" from "MS932".
    // IDK if there's a similar mechanism for Korean text.
    private static char[] allBackslashCandidates = new char[] {
            '\\', '¥', '₩'
    };

    // Magically handles case issues & such.
    public static String autoDetectWindows(String s) {
        final String giveUp = s;
        try {
            // '/' is 'universal'. Not supposed to be, but that doesn't matter.
            // Firstly convert to '/' form.
            // We will be dealing with the following kinds of paths.
            // Relative: "([$PATHCHARS]*/)*[$PATHCHARS]*"
            // Windows Absolute: "?:/.*"
            // MLA Absolute / Windows NT Special Path Absolute: "/.*"
            for (char ch : allBackslashCandidates)
                s = s.replace(ch, '/');
            if (s.equals(""))
                return s;
            if (!s.contains("/"))
                if (s.contains(":"))
                    return s; // A: / B: / C:
            if (GaBIEn.fileOrDirExists(s))
                return s;
            // Deal with earlier path components...
            // 'st' is the actual filename.
            String st = GaBIEn.basename(s);
            String parent;
            // Sanity check.
            if (s.contains("/")) {
                if (!s.endsWith("/" + st))
                    throw new RuntimeException("Weird inconsistency in gabien path sanitizer. 'Should never happen' but safety first. " + s);
                parent = autoDetectWindows(s.substring(0, s.length() - (st.length() + 1)));
            } else {
                // Change things to make sense.
                parent = ".";
            }
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
        if (!rootPath.equals("")) {
            // If it ends with any known backslash or forward slash candidate, just return as-is
            if (rootPath.endsWith("/"))
                return rootPath;
            for (char ch : allBackslashCandidates)
                if (rootPath.endsWith(Character.toString(ch)))
                    return rootPath;
            rootPath += "/";
        }
        return rootPath;
    }
}
