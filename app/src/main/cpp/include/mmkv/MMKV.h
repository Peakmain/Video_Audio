//
// Created by admin on 2021/8/24.
//

#ifndef VIDEO_AUDIO_MMKV_H
#define VIDEO_AUDIO_MMKV_H

#include <malloc.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>
#include <string>
#include <unordered_map>
#include <jni.h>
#include "ProtoBuf.h"


static std::string g_rootDir;

#define DEFAULT_MMAP_ID "peakmain_mmkv"
class MMKV {
public:
    MMKV(const char *mmapID);
    static void initializeMMKV(const char *path);
    static MMKV *defaultMMKV();
    void putInt(const std::string& key, int32_t value);
    int32_t getInt(std::string key, int32_t defaultValue);
private:
    void loadFromFile();
    void zeroFillFile(int fd, int32_t startPos, int32_t size);
    void appendDataWithKey(std::string key, ProtoBuf* value);
public:
    //文件路径
    std::string m_path;

    //文件句柄
    int m_fd;
    //文件可写长度
    int32_t m_size;
    //内存映射地址
    int8_t *m_ptr;
    //记录原始数据
    ProtoBuf *m_output;
    //已经使用的长度  &
    int32_t m_actualSize = 0;
    std::unordered_map<std::string, ProtoBuf*> map;

    void putString(const std::string &key,const std::string &value);

    std::string getString(std::string key);
};



#endif //VIDEO_AUDIO_MMKV_H
