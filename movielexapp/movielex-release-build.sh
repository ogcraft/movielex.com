#!/bin/bash
#set -x
APPNAME=MovieLexApp
MODE=release
KEYSTORE=$APPNAME.keystore

UNSIGNEDAPK=$APPNAME-$MODE-unsigned.apk
SIGNEDAPK=$APPNAME-$MODE-signed.apk
ALIGNEDAPK=$APPNAME-$MODE.apk

ant $MODE

if [ ! -f $KEYSTORE ]
then
	keytool -v -genkey\
	   	-validity 10000\
	   	-keystore $KEYSTORE\
	   	-storepass c17h19no3\
	   	-keypass c17h19no3\
	   	-alias movielexappkey\
	   	-keyalg RSA\
	   	-keysize 2048\
	   	-dname "CN=Oleg Galbert, OU=MovieLex, O=MovieLex.com, L=Israel, ST=Israel, C=IL"

fi

jarsigner -sigalg SHA1withRSA -digestalg SHA1 -keystore $KEYSTORE -storepass c17h19no3 -keypass c17h19no3\
   	-signedjar bin/$SIGNEDAPK bin/$UNSIGNEDAPK movielexappkey

zipalign -f -v 4  bin/$SIGNEDAPK bin/$ALIGNEDAPK

ls -lh bin/$ALIGNEDAPK 
#adb install bin/$SIGNEDAPK 
