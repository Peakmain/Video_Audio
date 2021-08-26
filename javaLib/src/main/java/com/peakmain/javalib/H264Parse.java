package com.peakmain.javalib;

/**
 * author:Peakmain
 * createTime:2021/8/26
 * mail:2726449200@qq.com
 * describe：哥伦布算法
 */
public class H264Parse {
    /**
     * 5还原成4
     */
    public void test() {
        //5的字节码是101,转成8位字节码
        byte data = 6 & 0xFF;
        //0000 0101
        int i = 3;
        //统计0的个数
        int zeroNum = 0;
        while (i < 8) {
            //找到第一个不为0
            if ((data & (0x80 >> i)) != 0) {
                break;
            }
            zeroNum++;
            i++;
        }
        i++;
        //找到第一个不为0之后，往后找zeroNum个数
        int value = 0;
        for (int j = 0; j < zeroNum; j++) {
            value <<= 1;
            //找到元素不为0 的个数
            if ((data & (0x80 >> i)) != 0) {
                value += 1;
            }
            i++;
        }
        int result = (1 << zeroNum) + value - 1;
        System.out.println(result);
    }

    //哥伦布编码
    public static int columbusCode(byte[] pBuff) {
        //统计0的个数
        int zeroNum = 0;
        while (i < pBuff.length * 8) {
            //找到第一个不为0
            if ((pBuff[i / 8] & (0x80 >> (i % 8))) != 0) {
                break;
            }
            zeroNum++;
            i++;
        }
        i++;
        //找到第一个不为0之后，往后找zeroNum个数
        int value = 0;
        for (int j = 0; j < zeroNum; j++) {
            value <<= 1;
            //找到元素不为0 的个数
            if ((pBuff[i / 8] & (0x80 >> (i % 8))) != 0) {
                value += 1;
            }
            i++;
        }
        int result = (1 << zeroNum) + value - 1;
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        //十六进制转byte数组
        int len = s.length();
        byte[] bs = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bs[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return bs;
    }


    static int i = 0;

    /**
     * @bitIndex 字节数位数
     */
    private static int paraseH264(int bitIndex, byte[] h264) {
        int value = 0;
        for (int j = 0; j < bitIndex; j++) {
            value <<= 1;
            //获取到每个字节
            if ((h264[i / 8] & (0x80 >> (i % 8))) != 0) {
                value += 1;
            }
            i++;
        }
        return value;
    }

    public static void main(String[] args) {
        //十六进制转byte数组
        byte[] h264 = hexStringToByteArray("00 00 00 01 67 64 00 15 AC D9 41 70 C6 84 00 00 03 00 04 00 00 03 00 F0 3C 58 B6 58".replace(" ", ""));
        i = 4 * 8;
        //禁止位
        int forbidden_zero_bit = paraseH264(1, h264);
        System.out.println("forbidden_zero_bit:" + forbidden_zero_bit);
        //重要性
        int nal_ref_idc = paraseH264(2, h264);
        System.out.println("nal_ref_idc:" + nal_ref_idc);
        //帧类型
        int nal_unit_type = paraseH264(5, h264);
        System.out.println("nal_unit_type:" + nal_unit_type);
        if (nal_unit_type == 7) {
            //编码等级
            int profile_idc = paraseH264(8, h264);
            System.out.println("profile_idc:" + profile_idc);
            //64后面的(00)，基本用不到
            //当constrained_set0_flag值为1的时候，就说明码流应该遵循基线profile(Baseline profile)的所有约束.constrained_set0_flag值为0时，说明码流不一定要遵循基线profile的所有约束。
            int constraint_set0_flag = paraseH264(1, h264);//(h264[1] & 0x80)>>7;
            // 当constrained_set1_flag值为1的时候，就说明码流应该遵循主profile(Main profile)的所有约束.constrained_set1_flag值为0时，说明码流不一定要遵
            int constraint_set1_flag = paraseH264(1, h264);//(h264[1] & 0x40)>>6;
            //当constrained_set2_flag值为1的时候，就说明码流应该遵循扩展profile(Extended profile)的所有约束.constrained_set2_flag值为0时，说明码流不一定要遵循扩展profile的所有约束。
            int constraint_set2_flag = paraseH264(1, h264);//(h264[1] & 0x20)>>5;
            //注意：当constraint_set0_flag,constraint_set1_flag或constraint_set2_flag中不只一个值为1的话，那么码流必须满足所有相应指明的profile约束。
            int constraint_set3_flag = paraseH264(1, h264);//(h264[1] & 0x10)>>4;
            // 4个零位
            int reserved_zero_4bits = paraseH264(4, h264);

            //码流等级
            int level_idc = paraseH264(8, h264);
            System.out.println("level_idc:" + level_idc);
            //(AC)就是哥伦布编码
            int seq_parameter_set_id = columbusCode(h264);
            System.out.println("seq_parameter_set_id:" + seq_parameter_set_id);
            if (profile_idc == 100) {
                int chroma_format_idc = columbusCode(h264);
                System.out.println("chroma_format_idc:" + chroma_format_idc);
                int bit_depth_luma_minus8 = columbusCode(h264);
                System.out.println("bit_depth_luma_minus8:" + bit_depth_luma_minus8);
                int bit_depth_chroma_minus8 = columbusCode(h264);
                System.out.println("bit_depth_chroma_minus8:" + bit_depth_chroma_minus8);
                int qpprime_y_zero_transform_bypass_flag = paraseH264(1, h264);
                System.out.println("qpprime_y_zero_transform_bypass_flag:" + qpprime_y_zero_transform_bypass_flag);
                //缩放标志位
                int seq_scaling_matrix_present_flag = paraseH264(1, h264);
                System.out.println("seq_scaling_matrix_present_flag:" + seq_scaling_matrix_present_flag);
            }
            //最大帧率
            int log2_max_frame_num_minus4 = columbusCode(h264);
            System.out.println("log2_max_frame_num_minus4:" + log2_max_frame_num_minus4);
            //确定播放顺序和解码顺序的映射
            int pic_order_cnt_type = columbusCode(h264);
            System.out.println("pic_order_cnt_type:" + pic_order_cnt_type);
            int log2_max_pic_order_cnt_lsb_minus4 = columbusCode(h264);
            System.out.println("log2_max_pic_order_cnt_lsb_minus4:" + log2_max_pic_order_cnt_lsb_minus4);
            int num_ref_frames = columbusCode(h264);
            System.out.println("num_ref_frames:" + num_ref_frames);
            int gaps_in_frame_num_value_allowed_flag = paraseH264(1, h264);
            System.out.println("gaps_in_frame_num_value_allowed_flag:" + gaps_in_frame_num_value_allowed_flag);
            System.out.println("------startBit " + i);
            int pic_width_in_mbs_minus1 = columbusCode(h264);
            System.out.println("------startBit " + i);
            int pic_height_in_map_units_minus1 = columbusCode(h264);
            int width = (pic_width_in_mbs_minus1 + 1) * 16;
            int height = (pic_height_in_map_units_minus1 + 1) * 16;
            System.out.println("width :  " + width + "   height: " + height);
        }
    }
}