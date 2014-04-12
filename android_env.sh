#!/bin/sh


export ANDROID_NDK_ROOT=${HOME}/opt/android/android-ndk-r9c
export ANDROID_SDK_ROOT=${HOME}/opt/android/adt-bundle/sdk
PATH="$PATH:${ANDROID_NDK_ROOT}"
PATH="$PATH:${ANDROID_SDK_ROOT}/platform-tools"
PATH="$PATH:${ANDROID_SDK_ROOT}/tools"

echo "PATH=${PATH}"



