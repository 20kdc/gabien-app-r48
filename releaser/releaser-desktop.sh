#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.

# Release Build Script, iteration 3.
# This part of the script is responsible for using the 'staging' output to make the desktop build of R48.
# Expects the version name for the final JAR.
cd .. &&
cp ../gabien-common/javase/target/gabien-javase-0.666-SNAPSHOT.jar staging-javase.jar &&
cd staging &&
zip -q -r ../staging-javase.jar * &&
cd .. &&
stripzip staging-javase.jar 1> /dev/null 2> /dev/null &&
mv staging-javase.jar $1.jar &&
echo "The desktop build is complete with hash " `sha256sum $1.jar`

