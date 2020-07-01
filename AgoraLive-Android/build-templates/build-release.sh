#!/bin/sh

fuSdkUrl='https://github.com/Faceunity/FULiveDemoDroid/releases/download/v6.4/Faceunity-Android-v6.4.zip'
fuSdkZip='faceunity.zip'
fuSdkDir='fusdk'

echo "Working folder:"
pwd

curl -L $fuSdkUrl -o $fuSdkZip

# Unzip face unity sdk and copy resource files to appropriate project folders
# Unzip shows error because it cannot recognize Chinsese names of doc files, do not care about it.

# Must be under project root folder here.

echo 'Unzip faceunity sdk zip file:'
rm -r ./$fuSdkDir
mkdir $fuSdkDir
tar xvf $fuSdkZip -C ./$fuSdkDir

# Copy faceunity .so libraries to project
echo 'Copy faceunity .so libraries'
fuProjJniLibsDir='faceunity/src/main/'
fuSdkJniLibsDir="./$fuSdkDir/Android/jniLibs"
rm -rf "./$fuProjJniLibsDir/jniLibs"
cp -rf $fuSdkJniLibsDir $fuProjJniLibsDir

# Copy faceunity jar library to project
echo 'Copy faceunity jar library'
fuProjJarLibDir='faceunity/libs'
fuSdkJarLibsPath="./$fuSdkDir/Android/libs/nama.jar"
rm -rf ./$fuProjJarLibDir
mkdir ./$fuProjJarLibDir
cp ./$fuSdkJarLibsPath ./$fuProjJarLibDir

# Copy faceunity bundles
echo 'Copy faceunity bundles'
fuProjAssetsDir='faceunity/src/main/assets'
fuSdkAssetsDir="./$fuSdkDir/Android/assets/"
rm -rf "./$fuProjAssetsDir"
mkdir $fuProjAssetsDir
cp ./$fuSdkAssetsDir/v3.bundle ./$fuProjAssetsDir
cp ./$fuSdkAssetsDir/fxaa.bundle ./$fuProjAssetsDir
cp ./$fuSdkAssetsDir/tongue.bundle ./$fuProjAssetsDir
cp ./$fuSdkAssetsDir/face_beautification.bundle ./$fuProjAssetsDir

# Replace authpack license
echo 'Replace faceunity license file'
authpackPath="faceunity/src/main/java/com/faceunity/authpack.java"
rm -f "./$authpackPath"
chmod +x "$authpack"
cp "$authpack" "./$authpackPath"

# Copy virtual image
echo 'Copy virtual image resource'
appProjecAssetsDir="app/src/main/assets"
rm -rf $appProjecAssetsDir
mkdir -p $appProjecAssetsDir

cp "$bg" $appProjecAssetsDir
cp "$girl" $appProjecAssetsDir
cp "$hashiqi" $appProjecAssetsDir

rm -rf ./$fuSdkDir

# Replace signing info
python ./build-templates/replace.py

# Build release
chmod +x ./gradlew
./gradlew assembleRelease