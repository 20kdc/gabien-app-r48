#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

# Release Build Script, iteration 3 (though this file didn't change much from 2 to 3)
# Supply with the version name and Android version code.
# Expects the 'normal' layout, calls on Gradle to do building and calls stripzip to make sure the two builds will end up the same.
# Regarding that, it's the only thing I found that does the exact job I need: https://github.com/zeeaero/stripzip
# So, yeah, thanks to zeeareo for a critical part of the R48 build process!

# Please note that this script is uploaded for reproducibility.
# This tool should theoretically create the same builds on different machines.
# Usage to create unofficial yet officially marked fake R48 builds,
#  while technically legal, may result in people disliking you.
# Responsibility for misuse of this tool is on the misuser.

# Unfortunately LANG has to be something UTF-8
LANG=en_US.UTF-8
LANGUAGE=en_US:en

if [ "$#" -ne 4 ]; then
 echo "releaser-core.sh NAME PACKAGE RELEASEID ANDROIDVERSIONCODE"
 exit
fi

echo
echo "- R48 Release Process -"
echo "Name: $1 Package: $2 RID: $3 AVC: $4"
echo
echo "Building GaBIEn..."
(cd ../../gabien-common && ./ready.sh) || exit
echo "Building GaBIEn [OK]"
echo
echo "Building R48..."
./releaser-pre.sh $3 $4 || exit
echo "Building R48 [OK]"
echo
echo "Finalizing desktop version..."
./releaser-desktop.sh $3 || exit
echo "Finalizing desktop version [OK]"
echo
# Android
echo "Finalizing Android version..."
cd ../../gabien-common/android || exit
./releaser.sh $1 $2 $3 $4 ../../gabien-app-r48/releaser/android/target/r48-android-0.666-SNAPSHOT-jar-with-dependencies.jar ../../gabien-app-r48/releaser/icon.png android.permission.WRITE_EXTERNAL_STORAGE || exit
mv result.apk ../../gabien-app-r48/$3.apk || exit
echo "Finalizing Android version [OK]"
echo
echo "All builds completed successfully. Please move to testing phase."

