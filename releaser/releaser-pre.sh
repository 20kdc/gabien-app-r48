#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

# Release Build Script, iteration 3
# This part of the script compiles the critical stuff.
# Supply with the version name and version code.
# This builds Common and R48, creates the "staging" folder, and populates it.

# Start with leaving the releaser folder first
cd ../../gabien-common &&
mvn install &&
cd ../gabien-app-r48 &&
# Testing requires manual IDE intervention at the moment due to LTE.
mvn install -DskipTests &&
mkdir -p staging &&
rm -r staging &&
mkdir -p staging &&
cd staging &&
# Note that JavaSE never gets put into staging - instead R48 and Common are injected into the JavaSE Jar.
unzip -o ../../gabien-common/uslx/target/gabien-uslx-0.666-SNAPSHOT.jar &&
unzip -o ../../gabien-common/media/target/gabien-media-0.666-SNAPSHOT.jar &&
unzip -o ../../gabien-common/common/target/gabien-common-0.666-SNAPSHOT.jar &&
unzip -o ../io/target/r48-io-0.666-SNAPSHOT.jar &&
unzip -o ../app/target/r48-app-0.666-SNAPSHOT.jar &&
cd .. &&
lua translation.lua > staging/assets/Systerms/English.txt &&
# Prepare licensing information
cp CREDITS.txt staging/ &&
cp COPYING.txt staging/ &&
# The date is represented with the last commit's date.
# Note that refs/heads/master is used because HEAD points there - this is not good practice but it saves complexity.
echo "R48 $1 (AVC $2), last commit:" `cat .git/refs/heads/master` `git log | grep Date | head -n 1` >> staging-version.txt &&
# Write in the boring details
echo "gabien-app-r48 - Editing program for various formats" >> staging-version.txt &&
echo "Written starting in 2016 by contributors (see CREDITS.txt)" >> staging-version.txt &&
echo "To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty." >> staging-version.txt &&
echo "You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>." >> staging-version.txt &&
echo "" >> staging-version.txt &&
cat CREDITS.txt >> staging-version.txt &&
echo "" >> staging-version.txt &&
mv staging-version.txt staging/assets/version.txt &&
# get rid of the MANIFEST.MF file because it'll overwrite the backend's
rm -f staging/META-INF/MANIFEST.MF

