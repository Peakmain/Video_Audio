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
    public static String writeContent(byte[] array,String textName) {
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
            writer = new FileWriter(Environment.getExternalStorageDirectory() + File.separator+textName, true);
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

    public static void writeBytes(byte[] array,String fileName) {
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator +fileName, true);
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
}
