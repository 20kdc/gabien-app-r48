#!/bin/sh
#
# This is released into the public domain.
# No warranty is provided, implied or otherwise.

# Removes Y-lines from a translation file to get the baseline it was made from, then diff with that baseline.
# (This doesn't detect missing Y-lines, but that's easy enough to manually inspect.)

grep -v "^y " $1 > temp-oldbase.txt
lua translation.lua | diff -u - temp-oldbase.txt
rm temp-oldbase.txt
