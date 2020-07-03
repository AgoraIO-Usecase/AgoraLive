# AgoraLive

## Copy Necessary Resources

### FaceUnity SDK

AgoraLive currently supports FaceUnity SDK v6.4, please go to () to download this version. After you download the right version of FaceUnity SDK, unzip the zip file and copy files to project folders (create the folder if not exists).

Assume the SDK file is unzipped to folder PATH/TO/FaceUnity-SDK-v6.4, copy the following resource files to project folders like:

* `PATH/TO/FaceUnity-SDK-v6.4/Android/assets` to `faceunity/src/main/assets`
* `PATH/TO/FaceUnity-SDK-v6.4/Android/jniLibs` to `faceunity/src/main/jniLibs`
* `PATH/TO/FaceUnity-SDK-v6.4/Android/libs/nama.jar` to `faceunity/libs`


The project folder structure:

```
faceunity
   |_ libs
      |_ mana.jar
   
   |_ src
      |_ main
          |_ assets
             |_ face_beautification.bundle
                fxaa.bundle
                tongue.bundle
                v3.bundle

             |_ jniLibs
                |_ arm64-v8a
                    |_ libnama.so

                |_ armeabi-v7a
                    |_ libnama.so

                |_ x86
                    |_ libnama.so

                |_ x86_64
                    |_ libnama.so
``` 

### Virtual Host Image Resource

The virtual host image resources are not part of FaceUnity SDK, you need to download from (here). Unzip the archive and copy the following files to `app/src/main/assets` like:

```
app
   |_ src
      |_ main
         |_ assets
            |_ bg.bundle
               girl.bundle
               hashiqi.bundle
```

### Agora Video SDK

Different from some other Agora demos, you DO NOT need to sign up for a developer account and register an app id to run this project.