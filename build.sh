#!/bin/bash

# shellcheck disable=SC2046
# shellcheck disable=SC2164
echo "Building PhotoManager..."

rm -rf bin
mkdir bin
cd bin
jar -xf ../lib/hutool-all-5.8.27.jar
cd ../

mkdir bin/res
cp -r src/res/* bin/res
javac -d bin -cp lib/hutool-all-5.8.27.jar $(find src -name "*.java")

jar cfm PhotoManager.jar src/META-INF/MANIFEST.MF -C bin .
rm -rf bin

echo "Build PhotoManager.jar complete! Do you want to run the program? (y/n)"
read -r run
if [ "$run" = "y" ]; then
    java -jar PhotoManager.jar
else 
    echo "Exiting..."
fi