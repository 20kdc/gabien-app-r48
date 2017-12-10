#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

# Release Build Script, iteration 2.
# This part of the script is responsible for creating the IMI Installer.
# Expects the version name for the final JAR.
cd ../../gabien-app-r48/instimi &&
cp -r ../src/main/java/r48/ArrayUtils.java src/main/java/r48/ &&
mkdir -p src/main/java/gabien/ui &&
mkdir -p src/main/java/r48/io &&
rm -r src/main/java/r48/io &&
cp -r ../../gabien-common/src/main/java/gabien/ui/IConsumer.java src/main/java/gabien/ui &&
cp -r ../../gabien-common/src/main/java/gabien/ui/IFunction.java src/main/java/gabien/ui &&
cp -r ../../gabien-common/src/main/java/gabien/ui/ISupplier.java src/main/java/gabien/ui &&
cp -r ../src/main/java/r48/Ruby*.java src/main/java/r48/ &&
cp -r ../src/main/java/r48/io src/main/java/r48/ &&
gradle build &&
cd build/classes/java/main &&
find . -type f > ../../../../src/main/resources/world.txt &&
cd ../../../.. &&
gradle build &&
cp build/libs/gabien-app-r48-imi.jar staging.jar &&
cd ../staging &&
cp COPYING.txt CREDITS.txt ../instimi/src/main/resources/ &&
cd ../instimi &&
stripzip staging.jar &&
mv staging.jar $1.jar &&
echo "The desktop/imi build is complete with hash " `sha256sum $1.jar`

