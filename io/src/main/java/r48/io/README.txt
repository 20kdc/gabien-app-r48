
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

Classes in this folder are not allowed to refer to other R48/Gabien classes,
 with the following exceptions allowed (these are specifically copied into IMI worktree):

 r48.io.*
 r48.Ruby*
 r48.ArrayUtils
 gabien.GaBIEn (There's a fake GaBIEn class for IO in the instimi tree)
 gabien.ui.IConsumer/IFunction/ISupplier

This is so that if plan IMI goes through properly, the installer can be cut down in size.
This is also why dataPath, dataExt and odbBackend are explicitly recorded:
It's because there is no good reason for the IMI program to use schema.

Also, if adding a new backend to IObjectBackend.Factory, make sure IMI knows it's prefix,
 and make sure prefixes don't conflict.
(IMI strips out unnecessary IO code for size reasons. This allows a finalized IMI installer to be ~46K + IMI code size at this time.)