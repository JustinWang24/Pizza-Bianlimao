package com.taihua.pishamachine.command;

/**
 * Created by Justin Wang from SmartBro on 1/1/18.
 */

public class CommandHelper {

    /**
     * hexString转byte
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString){
        if (hexString==null||hexString.equals("")) return null;
        hexString=hexString.toUpperCase();
        int length=hexString.length()/2;
        char[] hexChars=hexString.toCharArray();
        byte[] d=new byte[length];
        for (int i = 0; i <length ; i++) {
            int pos=i*2;
            d[i]=(byte)(charToByte(hexChars[pos])<<4|charToByte(hexChars[pos+1]));
        }
        return d;
    }

    /**
     * 字符类型转字节类型的方法
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
