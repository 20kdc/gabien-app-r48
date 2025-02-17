#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

# Release Build Script, iteration 3
# This part of the script compiles the critical stuff.
# Supply with the version name and version code.
# This sets up the release metadata, then runs the Maven compile/package.

set -e

if [ "$#" -ne 3 ]; then
 echo "releaser-pre.sh RELEASEID ANDROIDVERSIONCODE DEVFLAG"
 exit 1
fi

# Write release metadata
rm -rf metadata/src
mkdir -p metadata/src/main/resources/assets

VERSIONFILE="metadata/src/main/resources/assets/version.txt"

cp ../CREDITS.txt ../COPYING.txt metadata/src/main/resources/assets/
# The date is represented with the last commit's date.
if [ "$3" = 1 ]; then
 echo "R48 $1 [debug]" >> $VERSIONFILE
else
 echo "R48 $1" >> $VERSIONFILE
fi
echo "AVC $2, last commit:" `git show-ref HEAD` `git log | grep Date | head -n 1` >> $VERSIONFILE
# Write in the boring details
echo "gabien-app-r48 - Editing program for various formats" >> $VERSIONFILE
echo "Written starting in 2016 by contributors (see CREDITS.txt)" >> $VERSIONFILE
echo "To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty." >> $VERSIONFILE
echo "A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>." >> $VERSIONFILE
echo "" >> $VERSIONFILE
cat ../CREDITS.txt >> $VERSIONFILE
echo "" >> $VERSIONFILE

# Now go into R48 root

cd ..

# Testing requires manual IDE intervention at the moment due to LTE.
umvn package -q
