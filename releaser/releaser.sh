#!/bin/sh
# gabien-app-r48 - Editing program for various formats
# Written starting in 2016 by contributors (see CREDITS.txt)
# To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
# You should have received a copy of the CC0 Public Domain Dedication along with this software. If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.

# Release Build Script, iteration 2
# Supply with the version name and Android version code.
# Expects the 'normal' layout, calls on Gradle to do building and calls stripzip to make sure the two builds will end up the same.
# Regarding that, it's the only thing I found that does the exact job I need: https://github.com/zeeaero/stripzip
# So, yeah, thanks to zeeareo for a critical part of the R48 build process!
# Please note that this tool is uploaded for reproducibility.
# This tool should theoretically create the same builds on different machines.
# Usage to create unofficial yet officially marked fake R48 builds,
#  while technically legal, may result in people disliking you.
# Responsibility for misuse of this tool is on the misuser.

# Unfortunately LANG has to be something UTF-8
LANG=en_US.UTF-8
LANGUAGE=en_US:en

./releaser-pre.sh $1 &&
./releaser-desktop.sh $1 &&
# Android
cd ../../gabien-android &&
./releaser.sh R48 t20kdc.experimental.r48 $1 $2 ../gabien-app-r48/staging &&
mv result.apk ../gabien-app-r48/$1.apk &&
echo "All builds completed successfully. Please move to testing phase."
