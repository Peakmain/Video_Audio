package com.peakmain.video_audio.utils;

import android.os.Environment;
import android.util.Log;

import com.peakmain.ui.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * author ：Peakmain
 * createTime：2021/8/25
 * mail:2726449200@qq.com
 * describe：
 */
public class FileUtils {
    public static String writeContent(byte[] array, String textName) {
        char[] hexCharTable = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        };
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(hexCharTable[(b & 0xf0) >> 4]);
            sb.append(hexCharTable[b & 0x0f]);
        }
        Log.i(BuildConfig.TAG, "writeContent: " + sb.toString());
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(Environment.getExternalStorageDirectory() + File.separator + textName, true);
            writer.write(sb.toString());
            writer.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void writeBytes(byte[] array, String fileName) {
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + fileName, true);
            writer.write(array);
            writer.write('\n');

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] nv21toNV12(byte[] nv21) {
        byte[] nv12;
        int size = nv21.length;
        nv12 = new byte[size];
        int len = size * 2 / 3;
        System.arraycopy(nv21, 0, nv12, 0, len);
        int i = len;
        while (i < size - 1) {
            nv12[i] = nv21[i + 1];
            nv12[i + 1] = nv21[i];
            i += 2;
        }
        return nv12;
    }

    /**
     * yuv转成nv21
     */
    public static void yuvToNv21(byte[] y, byte[] u, byte[] v, byte[] nv21, int stride, int height) {
        System.arraycopy(y, 0, nv21, 0, y.length);
        // 注意，若length值为 y.length * 3 / 2 会有数组越界的风险，需使用真实数据长度计算
        int length = y.length + u.length / 2 + v.length / 2;
        int uIndex = 0, vIndex = 0;
        for (int i = stride * height; i < length; i += 2) {
            nv21[i] = v[vIndex];
            nv21[i + 1] = u[uIndex];
            vIndex += 2;
            uIndex += 2;
        }
    }
    public static byte[] nv21RotateTo90(byte[] data, byte[] output, int width, int height)
    {
        int yLen = width * height;
        int buffserSize = yLen * 3 / 2;

        int i = 0;
        int startPos = (height - 1)*width;
        for (int x = 0; x < width; x++)
        {
            int offset = startPos;
            for (int y = height - 1; y >= 0; y--)
            {
                output[i] = data[offset + x];
                i++;
                offset -= width;
            }
        }
        // Rotate the U and V color components
        i = buffserSize - 1;
        for (int x = width - 1; x > 0; x = x - 2)
        {
            int offset = yLen;
            for (int y = 0; y < height / 2; y++)
            {
                output[i] = data[offset + x];
                i--;
                output[i] = data[offset + (x - 1)];
                i--;
                offset += width;
            }
        }
        return output;
    }
    /**
     * 旋转90度
     */
    public static void portraitData2Raw(byte[] data, byte[] output, int width, int height) {
        int yLen = width * height;
        int uvHeight = height >> 1;
        int k = 0;
        for (int j = 0; j < width; j++) {
            for (int i = height - 1; i >= 0; i--) {
                output[k++] = data[width * i + j];
            }
        }
        for (int j = 0; j < width; j += 2) {
            for (int i = uvHeight - 1; i >= 0; i--) {
                output[k++] = data[yLen + width * i + j];
                output[k++] = data[yLen + width * i + j + 1];
            }
        }
    }
}
