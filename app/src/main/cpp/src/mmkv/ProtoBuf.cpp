
#include "../../include/mmkv/ProtoBuf.h"


ProtoBuf::ProtoBuf(int8_t *buf, int32_t size, bool isCopy) {
    m_size = size;
    m_position = 0;
    m_buf = buf;
    m_isCopy = isCopy;
    if (isCopy) {
        m_buf = static_cast<int8_t *>(malloc(size));
        memcpy(m_buf, buf, size);
    }
}

ProtoBuf::ProtoBuf(int32_t size) {
    m_position = 0;
    m_size = size;
    m_isCopy = true;
    if (size > 0) {
        m_buf = static_cast<int8_t *>(malloc(size));
    }
}


ProtoBuf::~ProtoBuf() {
    if (m_isCopy && m_buf) {
        free(m_buf);
    }
    m_buf = NULL;
}

int32_t ProtoBuf::computeInt32Size(int32_t value) {
    //0xffffffff 表示 uint 最大值
    //<< 7 则低7位变成0 与上value
    //如果value只要7位就够了则=0,编码只需要一个字节，否则进入其他判断
    if ((value & (0xffffffff << 7)) == 0) {
        return 1;
    } else if ((value & (0xffffffff << 14)) == 0) {
        return 2;
    } else if ((value & (0xffffffff << 21)) == 0) {
        return 3;
    } else if ((value & (0xffffffff << 28)) == 0) {
        return 4;
    }
    return 5;
}

int32_t ProtoBuf::computeItemSize(std::string key, ProtoBuf *value) {
    int32_t keyLength = key.length();
    // 保存key的长度与key数据需要的字节
    int32_t size = keyLength + ProtoBuf::computeInt32Size(keyLength);
    // 加上保存value的长度与value数据需要的字节
    size += value->length() + ProtoBuf::computeInt32Size(value->length());
    return size;
}


int32_t ProtoBuf::computeMapSize(std::unordered_map<std::string, ProtoBuf *> map) {
    auto iter = map.begin();
    int32_t size = 0;
    for (; iter != map.end(); iter++) {
        auto key = iter->first;
        ProtoBuf* value = iter->second;
        size += computeItemSize(key, value);
    }
    return size;
}
std::string ProtoBuf::readString() {
    //获得字符串长度
    int32_t size = readInt();
    //剩下的数据有这么多
    if (size <= (m_size - m_position) && size > 0) {
        std::string result((char *) m_buf + m_position, size);
        m_position += size;
        return result;
    }
    return "";
}

int32_t ProtoBuf::readInt() {
    //todo 不能是负数 uint8_t
    uint8_t tmp = readByte();
    //最高1位为0  这个字节是一个有效int。
    if ((tmp >> 7) == 0) {
        return tmp;
    }
    //获得低7位数据
    int32_t result = tmp & 0x7f;
    int32_t i = 1;
    do {
        //再读一个字节
        tmp = readByte();
        if (tmp < 0x80) {
            //读取后一个字节左移7位再拼上前一个数据的低7位
            result |= tmp << (7 * i);
        } else {
            result |= (tmp & 0x7f) << (7 * i);
        }
        i++;
    } while (tmp >= 0x80);
    return result;
}


int8_t ProtoBuf::readByte() {
    if (m_position == m_size) {
        return 0;
    }
    return m_buf[m_position++];
}


ProtoBuf *ProtoBuf::readData() {
    //获得数据长度
    int32_t size = readInt();
    //有效
    if (size <= m_size - m_position && size > 0) {
        ProtoBuf *data = new ProtoBuf(m_buf + m_position, size, true);
        m_position += size;
        return data;
    }
    return NULL;
}


void ProtoBuf::writeByte(int8_t value) {
    if (m_position == m_size) {
        //满啦，出错啦
        return;
    }
    //将byte放入数组
    m_buf[m_position++] = value;
}

void ProtoBuf::writeRawInt(int32_t value) {
    while (true) {
        //每次处理7位数据，如果写入的数据 <= 0x7f（7位都是1）那么使用7位就可以表示了
        if (value <= 0x7f) {
            writeByte(value);
            return;
        } else {
            //大于7位，则先记录低7位，并且将最高位置为1
            //1、& 0x7F 获得低7位数据
            //2、| 0x80 让最高位变成1，表示超过1个字节记录整个数据
            writeByte((value & 0x7F) | 0x80);
            //7位已经写完了，处理更高位的数据
            value >>= 7;
        }
    }
}

void ProtoBuf::writeString(std::string value) {
    size_t numberOfBytes = value.size();
    writeRawInt(numberOfBytes);
    memcpy(m_buf + m_position, value.data(), numberOfBytes);
    m_position += numberOfBytes;
}

void ProtoBuf::writeData(ProtoBuf *data) {
    writeRawInt(data->length());

    size_t numberOfBytes = data->length();
    memcpy(m_buf + m_position, data->getBuf(), numberOfBytes);
    m_position += numberOfBytes;
}

void ProtoBuf::restore() {
    m_position = 0;
}









