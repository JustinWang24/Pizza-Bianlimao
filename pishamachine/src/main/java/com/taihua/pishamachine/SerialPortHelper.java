package com.taihua.pishamachine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.lang.ArrayIndexOutOfBoundsException;

public class SerialPortHelper {
    private SerialPort serialPort;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private ReadThread mReadThread;

    private volatile int size = -1;
    private static final int mMaxReadBufferSize = 512;
    private String path;
    private final LinkedList<byte[]> mReadedQueue = new LinkedList<byte[]>();

    // 默认读取的时延为50毫秒
    private int readInterval = 50;
    private volatile boolean stopReadingThread = false;

    private String TAG = "SerialPort";


    //构造函数
    public SerialPortHelper(String path, int baudrate, int flags, int nEvent) throws NullPointerException {

        try {
            serialPort = new SerialPort(new File(path), baudrate, flags, nEvent);
        } catch (IOException e) {
            LogUtil.LogStackTrace(e,"11111111");
        } catch (SecurityException e) {
            LogUtil.LogStackTrace(e,"22222222");
        }
        if (serialPort != null) {
            //设置读、写
            mInputStream = serialPort.getInputStream();
            mOutputStream = serialPort.getOutputStream();
        } else throw new NullPointerException("串口设置有误");

    }

    public void init() {
        this.stopReadingThread = false;
        //启动状态查询线程
        mReadThread = new ReadThread();
        mReadThread.start();
    }

    /**
     * 初始化方法, 可以改变读取串口的时延
     * @param readDelay
     */
    public void init(int readDelay){
        this.readInterval = readDelay;
        this.init();
    }

    /**
     * 关闭串口
     */
    public void close(){
        try {
            LogUtil.LogInfo("开始执行串口关闭的调用: " + new Date().toString());
            if(!this.stopReadingThread){
                this.serialPort.close();
                this.stopReadingThread = true;
            }
            Thread.sleep(20);
            LogUtil.LogInfo("结束执行串口关闭的调用 " + new Date().toString());
        } catch (InterruptedException e) {
            LogUtil.LogStackTrace(e,"98989898");
        }
    }


    //支持超时读取的read
    public synchronized int readInputStreamWithTimeout(byte[] b, int timeoutMillis)
            throws IOException {
        if (timeoutMillis == 0){
            // 如果指定了不需要读取返回值，那么就清理缓冲区，直接返回 0
//            try {
//                Thread.sleep(10);
//                this.pollReadedQueue();
//            } catch (InterruptedException e) {
//            }
            return 0;
        }

        // Timeout 大于0的时候才执行
        int bufferOffset = 0;
        long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < maxTimeMillis && bufferOffset < b.length) {
            final byte[] dataSrc = mReadedQueue.poll();
            if (dataSrc == null) continue;
//            LogUtil.LogInfo("\n");
//            String s = bytesToHexString(data, data.length);
//            s = s.substring(0,2);
//            if("01".equals(s)){
//                System.arraycopy(data, 0, b, bufferOffset, data.length);
//            }

//            if(bufferOffset > b.length - dataSrc.length -1 ){
//                bufferOffset = 0;
//            }
            try {
                System.arraycopy(dataSrc, 0, b, bufferOffset, dataSrc.length);

            }catch (ArrayIndexOutOfBoundsException e){
                LogUtil.LogInfo("越界异常读取到的数据: " + Arrays.toString(dataSrc));
                System.arraycopy(dataSrc, 0, b, bufferOffset, b.length - bufferOffset);
                LogUtil.LogStackTrace(e,"000000");
                // 读取到的数据太多了，把缓冲区清理一下
                this.pollReadedQueue();
            } finally {
                bufferOffset += dataSrc.length;
            }
        }
        return bufferOffset;
    }

    /**
     * 清理一下读取缓冲区
     */
    public synchronized void pollReadedQueue(){
        this.mReadedQueue.clear();
        LogUtil.LogInfo("串口数据清空完成: " + mReadedQueue.size());
    }

    //----------------------------------------------------
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!stopReadingThread && !isInterrupted()) {
                try {
                    if (mInputStream == null) return;
                    byte[] buffer = new byte[mMaxReadBufferSize];
                    int size = mInputStream.read(buffer);

                    if (size > 0) {
                        byte[] data = new byte[size];
                        //判断首位是不是01  是01添加  不是舍去下一位添加(buffer[0] 判断是不是01)
                        System.arraycopy(buffer, 0, data, 0, size);

                        mReadedQueue.push(data);
                    }
                    try {
                        // Todo 很重要的修改，从 50ms 变成 10ms 要检查是否会影响到PLC的读取
                        Thread.sleep(readInterval);//延时50ms
                    } catch (InterruptedException e) {
                        LogUtil.LogStackTrace(e, "33333333333");
                        break;
                    }
                } catch (Throwable e) {
                    LogUtil.LogStackTrace(e, "44444444");
                    break;
                }
            }
        }
    }

    /**
     * 发送数据并且读取返回，阻塞函数
     * @param data
     * @param outBuffer
     * @param timeout
     * @return
     * @throws NullPointerException
     */
    public synchronized int sentData(byte[] data, byte[] outBuffer, int timeout) throws NullPointerException {

        //判断是否为空
        if (mOutputStream == null) throw new NullPointerException("mOutputStream is null");

        //发送
        try {
            mOutputStream.write(data);
        } catch (IOException e) {
            LogUtil.LogStackTrace(e, "55555555555");
        }

        //查看接收
        try {
            // 即便是0 也要发送给读取串口的函数，这样可以清理一下缓冲区
            // 如果指定timeout为0, 表示不需要获取返回值，就是单纯的发送
            int readCount = readInputStreamWithTimeout(outBuffer, timeout);
            if (readCount == -1) {
                return -1;
            }
            return readCount;

        } catch (Exception e) {
            LogUtil.LogStackTrace(e, "6666666666");
        }

        return 0;
    }

    /**
     * byte转hexString
     *
     * @param buffer 数据
     * @param size   字符数
     * @return
     */
    public static String bytesToHexString(final byte[] buffer, final int size) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (buffer == null || size <= 0) return null;
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(buffer[i] & 0xff);
            if (hex.length() < 2) stringBuilder.append(0);
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    /**
     * hexString转byte
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) return null;
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
