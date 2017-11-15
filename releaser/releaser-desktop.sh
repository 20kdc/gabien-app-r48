#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

# Release Build Script, iteration 2.
# This part of the script is responsible for using the 'staging' output to make the desktop build of R48.
# Expects the version name for the final JAR.
cd ../../gabien-javase &&
gradle build &&
cd ../gabien-app-r48 &&
cp ../gabien-javase/build/libs/gabien-javase.jar staging-javase.jar &&
cd staging &&
zip -r ../staging-javase.jar * &&
cd .. &&
stripzip staging-javase.jar &&
mv staging-javase.jar $1.jar &&
echo "The desktop build is complete with hash " `sha256sum $1.jar`

