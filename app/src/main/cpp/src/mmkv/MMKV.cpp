//
// Created by admin on 2021/8/24.
//

#include <log.h>
#include "../../include/mmkv/MMKV.h"

int32_t DEFAULT_MMAP_SIZE =getpagesize();
void MMKV::initializeMMKV(const char *path) {
    g_rootDir = path;
    //创建文件夹
    mkdir(g_rootDir.c_str(), 0777);
}

MMKV::MMKV(const char *mmapID) {
    m_path = g_rootDir + "/" + mmapID;
    loadFromFile();
}

MMKV *MMKV::defaultMMKV() {
    MMKV *kv = new MMKV(DEFAULT_MMAP_ID);
    return kv;
}

void MMKV::loadFromFile() {
    m_fd = open(m_path.c_str(), O_RDWR | O_CREAT, S_IRWXU);
    //获取文件的具体大小
    struct stat st = {0};
    if (fstat(m_fd, &st) != -1) {
        m_size = st.st_size;
    }
    if (m_size < DEFAULT_MMAP_SIZE || (m_size % DEFAULT_MMAP_SIZE != 0)) {
        //调整为4k整数倍
        int32_t oldSize = m_size;
        //新的4k整数倍
        m_size = ((m_size / DEFAULT_MMAP_SIZE) + 1) * DEFAULT_MMAP_SIZE;
        if (ftruncate(m_fd, m_size) != 0) {
            m_size = st.st_size;
        }
        //如果文件大小被增加了， 让增加这些大小的内容变成空
        zeroFillFile(m_fd, oldSize, m_size - oldSize);
    }
    m_ptr = static_cast<int8_t *>(mmap(nullptr, m_size, PROT_READ | PROT_WRITE, MAP_SHARED, m_fd,
                                       0));
    //内容的总长度
    memcpy(&m_actualSize, m_ptr, 4);
    if (m_actualSize > 0) {
        ProtoBuf inputBuffer(m_ptr + 4, m_actualSize);
        //清空
        m_dic.clear();
        //已有的数据添加到Map
        while (!inputBuffer.isAtEnd()) {
            std::string key = inputBuffer.readString();
            LOGE("key=%s ", key.c_str());
            if (key.length() > 0) {
                ProtoBuf *value = inputBuffer.readData();
                if (value && value->length() > 0) {
                    m_dic.emplace(key, value);
                }
            }
        }
    }
    m_output = new ProtoBuf(m_ptr + 4 + m_actualSize,
                            m_size - 4 - m_actualSize);

}

void MMKV::zeroFillFile(int fd, int32_t startPos, int32_t size) {
    if (lseek(fd, startPos, SEEK_SET) < 0) {
        return;
    }


    static const char zeros[4096] = {0};
    while (size >= sizeof(zeros)) {
        if (write(fd, zeros, sizeof(zeros)) < 0) {
            return;
        }
        size -= sizeof(zeros);
    }
    if (size > 0) {
        if (write(fd, zeros, size) < 0) {
            return;
        }
    }
}

void MMKV::putInt(const std::string &key, int32_t value) {
    //value需要几个字节
    int32_t size = ProtoBuf::computeInt32Size(value);
    ProtoBuf *buf = new ProtoBuf(size);
    buf->writeRawInt(value);
    m_dic.emplace(key, buf);
    appendDataWithKey(key, buf);
}

int32_t MMKV::getInt(std::string key, int32_t defaultValue) {
    auto itr = m_dic.find(key);
    if (itr != m_dic.end()) {
        ProtoBuf *buf = itr->second;
        int32_t returnValue = buf->readInt();
        //多次读取，将position还原为0
        buf->restore();
        return returnValue;
    }
    return defaultValue;
}

void MMKV::appendDataWithKey(std::string key, ProtoBuf *value) {
    //待写入数据的大小
    int32_t itemSize = ProtoBuf::computeItemSize(key, value);
    if (itemSize > m_output->spaceLeft()) {
        //内存不够
        //计算map的大小
        int32_t needSize = ProtoBuf::computeMapSize(m_dic);
        //加上总长度
        needSize += 4;
        //扩容的大小
        //计算每个item的平均长度
        int32_t avgItemSize = needSize / std::max<int32_t>(1, m_dic.size());
        int32_t futureUsage = avgItemSize * std::max<int32_t>(8, (m_dic.size() + 1) / 2);
        if (needSize + futureUsage >= m_size) {
            int32_t oldSize = m_size;
            //如果在需要的与将来可能增加的加起来比扩容后还要大，继续扩容
            do {
                //扩充一倍
                m_size *= 2;

            } while (needSize + futureUsage >= m_size);
            //重新设定文件大小
            ftruncate(m_fd, m_size);
            zeroFillFile(m_fd, oldSize, m_size - oldSize);
            //解除映射
            munmap(m_ptr, oldSize);
            //重新映射
            m_ptr = (int8_t *) mmap(m_ptr, m_size, PROT_READ | PROT_WRITE, MAP_SHARED, m_fd, 0);
        }
        m_actualSize = needSize - 4;
        memcpy(m_ptr, &m_actualSize, 4);
        LOGE("extending  full write");
        delete m_output;
        m_output = new ProtoBuf(m_ptr + 4,
                                m_size - 4);
        auto iter = m_dic.begin();
        for (; iter != m_dic.end(); iter++) {
            auto k = iter->first;
            auto v = iter->second;
            m_output->writeString(k);
            m_output->writeData(v);
        }
    } else {
        //内存够
        m_actualSize += itemSize;
        memcpy(m_ptr, &m_actualSize, 4);
        m_output->writeString(key);
        m_output->writeData(value);
    }

}
