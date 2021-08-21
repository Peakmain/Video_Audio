//
// Created by admin on 2021/8/20.
//

#ifndef VIDEO_AUDIO_LOG_H
#define VIDEO_AUDIO_LOG_H

#include <android/log.h>
#define TAG "VIDEO_AUDIO_JNI_TAG"
#include <string.h>
# define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
# define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
# define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
# define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

#endif //VIDEO_AUDIO_LOG_H
