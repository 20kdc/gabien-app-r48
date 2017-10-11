#!/bin/sh
#
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

# Removes Y-lines from a translation file to get the baseline it was made from, then diff with that baseline.
# (This doesn't detect missing Y-lines, but that's easy enough to manually inspect.)

grep -v "^y " $1 > temp-oldbase.txt
lua translation.lua | diff -u - temp-oldbase.txt
rm temp-oldbase.txt
