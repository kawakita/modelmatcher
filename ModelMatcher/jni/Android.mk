LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off

include c:/OpenCV4Android/OpenCV-2.4.6-android-sdk/sdk/native/jni/OpenCV.mk

//LOCAL_C_INCLUDES:= /cygdrive/c/NVPACK/OpenCV-2.4.5-Tegra-sdk-r2/sdk/native/jni/include
LOCAL_C_INCLUDES:= /cygdrive/c/NVPACK/opencv-master/include
LOCAL_MODULE    := hausdorffFinder
LOCAL_SRC_FILES := hausdorffFinder.cpp
LOCAL_CFLAGS    := -Werror -O3 -ffast-math
LOCAL_LDLIBS +=  -llog -ldl 

include $(BUILD_SHARED_LIBRARY)

