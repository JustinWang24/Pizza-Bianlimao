package com.taihua.pishamachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;

import android.os.Environment;

/**
 * 日志打印
 */
public class

LogUtil {

    private static final String FILE_NAME = "/PisaMessage.txt";
    private static Boolean MYLOG_SWITCH = false; // 日志文件总开关

    public static void i(String tag, String str) {
    }

    /**
     * 对于异常，总是打印
     * @param e
     */
    public static void LogException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String str = sw.toString();
        writeException(str);
    }

    /**
     * 打印发生的异常详情
     * @param e 异常对象
     * @param code 错误的代码
     */
    public static void LogStackTrace(Exception e, String code){
        LogInfo(e.getClass().getName() + ": " + e.getMessage() + " -> " + code);
        _LogTrace(e.getStackTrace());
    }

    /**
     * 打印发生的异常详情
     * @param e
     * @param code
     */
    public static void LogStackTrace(Throwable e, String code){
        LogInfo(e.getClass().getName() + ": " + e.getMessage() + " -> " + code);
        _LogTrace(e.getStackTrace());
    }

    /**
     * 打印异常trace信息
     * @param st
     */
    private static void _LogTrace(StackTraceElement[] st){
        for (int i=0; i<st.length; i++){
            LogInfo(st[i].toString());
        }
        LogInfo("*******************************");
        LogInfo(" ");
    }

    public static void LogInfo(String str) {
        str+="\n";
        if (MYLOG_SWITCH) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            writeException(str);
        }
    }

    /**
     * 强制必须记录的Log
     * @param str 信息内容
     */
    public static void LogInfoForce(String str){
        str+="\n";
        writeException(str);
    }

    private static void writeException(String content) {
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // 获取SD卡的目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                File targetFile = new File(sdCardDir.getCanonicalPath()
                        + FILE_NAME);
                // 以指定文件创建RandomAccessFile对象
                RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
                // 将文件记录指针移动到最后
                raf.seek(targetFile.length());
                // 输出文件内容
                raf.write(content.getBytes());
                raf.close();
            }
        } catch (Exception e) {
            LogInfoForce(e.getMessage());
        }
    }

    private static String readException() {
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // 获得SD卡对应的存储目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                // 获取指定文件对应的输入流
                FileInputStream fis = new FileInputStream(
                        sdCardDir.getCanonicalPath() + FILE_NAME);
                // 将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        fis));
                StringBuilder sb = new StringBuilder("");
                String line = null;
                // 循环读取文件内容
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}