#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

# Release Build Script, iteration 3
# This part of the script compiles the critical stuff.
# Supply with the version name and version code.
# This sets up the release metadata, then runs the Maven compile/package.

# Write release metadata
rm -rf metadata/src || exit
mkdir -p metadata/src/main/resources/assets || exit

VERSIONFILE="metadata/src/main/resources/assets/version.txt"

cp ../CREDITS.txt metadata/src/main/resources/assets/ || exit
cp ../COPYING.txt metadata/src/main/resources/assets/ || exit
# The date is represented with the last commit's date.
echo "R48 $1 (AVC $2), last commit:" `git show-ref HEAD` `git log | grep Date | head -n 1` >> $VERSIONFILE || exit
# Write in the boring details
echo "gabien-app-r48 - Editing program for various formats" >> $VERSIONFILE || exit
echo "Written starting in 2016 by contributors (see CREDITS.txt)" >> $VERSIONFILE || exit
echo "To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty." >> $VERSIONFILE || exit
echo "You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>." >> $VERSIONFILE || exit
echo "" >> $VERSIONFILE || exit
cat ../CREDITS.txt >> $VERSIONFILE || exit
echo "" >> $VERSIONFILE || exit

# Now go into R48 root

cd .. || exit

# Testing requires manual IDE intervention at the moment due to LTE.
mvn clean -q || exit
mvn package -q -DskipTests || exit

