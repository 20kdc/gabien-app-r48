
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

Classes in this folder are not allowed to refer to other R48 classes,
 with the following exceptions allowed:

 r48.RubyIO
 r48.RubyCT
 r48.RubyTable
 r48.ArrayUtils

This is so that if plan IMI goes through properly, the installer can be cut down in size.
This is also why dataPath, dataExt and odbBackend are explicitly recorded:
It's because there is no good reason for the IMI program to use schema.