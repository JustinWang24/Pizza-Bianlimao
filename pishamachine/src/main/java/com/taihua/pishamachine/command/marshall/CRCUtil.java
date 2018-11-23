package com.taihua.pishamachine.command.marshall;

/**
 * Created by Justin Wang from SmartBro on 22/1/18.
 */

public class CRCUtil {
    /**
     * converts the given String to CRC16
     *
     * @param inputStr
     *            - the input string to get the CRC
     * @param polynomial
     *            - the polynomial (divisor)
     * @param crc
     *            - the CRC mask
     * @param isHex
     *            - if true, treat input string as hex, otherwise, treat as
     *            ASCII
     * @return
     */
    public static String getCRC16CCITT(String inputStr, int polynomial,
                                       int crc, boolean isHex) {

        int strLen = inputStr.length();
        int[] intArray;

        if (isHex) {
            if (strLen % 2 != 0) {
                inputStr = inputStr.substring(0, strLen - 1) + "0"
                        + inputStr.substring(strLen - 1, strLen);
                strLen++;
            }

            intArray = new int[strLen / 2];
            int ctr = 0;
            for (int n = 0; n < strLen; n += 2) {
                intArray[ctr] = Integer.valueOf(inputStr.substring(n, n + 2), 16);
                ctr++;
            }
        } else {
            intArray = new int[inputStr.getBytes().length];
            int ctr=0;
            for(byte b : inputStr.getBytes()){
                intArray[ctr] = b;
                ctr++;
            }
        }

        // main code for computing the 16-bit CRC-CCITT
        for (int b : intArray) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }

        crc &= 0xFFFF;
        String crcStr = Integer.toHexString(crc).toUpperCase();
        int n = crcStr.length();
        for(int i=0; i<(4-n); i++){
            crcStr = "0" + crcStr;
        }
        return crcStr;
    }
}
