package com.taihua.pishamachine.MicroLightScanner.ParserImpl;

import android.util.Log;

import com.taihua.pishamachine.LogUtil;
import com.taihua.pishamachine.MicroLightScanner.CommandExecuteResult;
import com.taihua.pishamachine.MicroLightScanner.IResultParser;
import com.taihua.pishamachine.MicroLightScanner.ScannerCommand;
import com.taihua.pishamachine.command.CommandHelper;

/**
 * Created by Justin Wang from SmartBro on 30/12/18.
 */
public class QrCodeParserImpl implements IResultParser {

    private static final byte[] HEAD_TO_CHECK = {(byte)0xAA, 0x30};
//    private static final byte[] HEAD_TO_CHECK = {0x55, (byte)0xAA, 0x30};

    // 标识字:一字节， 0x00则代表成功应答，其它失败或错误
    private static final byte[] RIGHT_IDENTICAL_BYTES = {0x00};
    //
    private static final int BYTES_LENGTH_OF_DATA_DOMAIN_INDICATOR = 2;

    private static final String ERROR_NOT_FOUND_HEAD            = "没找到正确的头部 0x55, (byte)0xAA, 0x30";
    private static final String ERROR_NOT_FOUND_IDENTICAL_BYTE  = "没找到正确的标识字 0x00";
    private static final String ERROR_WRONG_IDENTICAL_BYTE      = "找到的标识字错误";
    private static final String ERROR_WRONG_BCC                 = "BCC验证失败";

    /**
     * 解析返回的二维码
     * @param resultBuffer 返回结果的字节数组
     * @return String
     */
    @Override
    public String go(byte[] resultBuffer) {
        // 如无数据则返回: 55 aa 30 00 00 00 cf
        final byte[] noDataReturnResult = {
//                0x55,
                (byte) 0xAA, 0x30, 0x00, 0x00, 0x00, (byte)0xCF
        };
        if(resultBuffer.length >= noDataReturnResult.length){
            boolean noData = true;
            boolean isAllZero = true;
            // 检查是否为无数据
            for (int i=0; i < noDataReturnResult.length; i++){
                if(noDataReturnResult[i] != resultBuffer[i+1]){
                    noData = false;
                    break;
                }
            }
            // 检查是否为全零
            for (int i=0; i < resultBuffer.length; i++){
                if(resultBuffer[i] != 0x00){
                    isAllZero = false;
                    break;
                }
            }
            if(!isAllZero && !noData){
                // 是有效的数据，返回解析出的二维码
                return bytesToAsciiString(resultBuffer).toString();
            }else {
                // 确认的无格式数据或者是全零
                return CommandExecuteResult.KEEP_WAITING;
            }
        }
        return CommandExecuteResult.NOT_OK;
    }

    /**
     * 字节转成ASCII字符串. 如果解析失败，那么会返回 CommandExecuteResult.NOT_OK ， 同时把调试过程与错误原因写入Log文件
     * @param readBuffer
     * @return
     */
    public static StringBuilder bytesToAsciiString(byte[] readBuffer){
//        StringBuilder QrCode = new StringBuilder("");

        // 获取标示字是否为 0x00
        final int lengthToCheck = readBuffer.length - HEAD_TO_CHECK.length;
        int dataStartOffset = -1; // 是否找到了正确的头部 0x55, (byte)0xAA, 0x30, 0x00

        if(lengthToCheck > 0){
            for (int i=0; i<lengthToCheck; i++){
                if(
                        HEAD_TO_CHECK[0] == readBuffer[i] &&
                        HEAD_TO_CHECK[1] == readBuffer[i+1]
//                        HEAD_TO_CHECK[2] == readBuffer[i+2]
                    ){
                    dataStartOffset = i + HEAD_TO_CHECK.length;
                }
            }
            if(dataStartOffset > -1){
                // 表示已经找到了正确的头部
                // 开始解析标识字
                if(readBuffer.length > dataStartOffset+RIGHT_IDENTICAL_BYTES.length){
                    if(readBuffer[dataStartOffset] == RIGHT_IDENTICAL_BYTES[0]){
                        dataStartOffset += RIGHT_IDENTICAL_BYTES.length;
                        // 开始解析数据域的长度
                        int lengthInLow = readBuffer[dataStartOffset];
                        int lengthInHigh = readBuffer[dataStartOffset+1];
//                        QrCode.append(dataStartOffset);
//                        QrCode.append(":低位字节值 ");
//                        QrCode.append(lengthInLow);
//                        QrCode.append(":高位字节值 ");
//                        QrCode.append(lengthInHigh);
//                        QrCode.append(":转换结果");
                        // 得到最终的数据域的真实长度
                        final int totalDataDomainLength = (lengthInHigh << 8 & 0xFF00) | (lengthInLow & 0xFF);
//                        QrCode.append(totalDataDomainLength);

                        dataStartOffset += BYTES_LENGTH_OF_DATA_DOMAIN_INDICATOR; // 到这里得到了数据域开始的位置

                        if(readBuffer.length >= dataStartOffset + totalDataDomainLength + 1 ){
                            // 读取缓冲区的长度足够 可以开始验证BCC了
                            final int lengthOfAllHeaders = HEAD_TO_CHECK.length +
                                    RIGHT_IDENTICAL_BYTES.length +
                                    BYTES_LENGTH_OF_DATA_DOMAIN_INDICATOR;
                            final int totalLengthWithoutBCC = lengthOfAllHeaders + totalDataDomainLength;

                            final byte[] realDataWithoutBCC = new byte[totalLengthWithoutBCC];
                            for (int i = 0; i < totalLengthWithoutBCC; i++){
                                realDataWithoutBCC[i] = readBuffer[dataStartOffset - lengthOfAllHeaders + i];
                            }

                            final byte[] fakeData = new byte[realDataWithoutBCC.length+1];
                            fakeData[0] = 0x55;
                            for (int j=0;j<realDataWithoutBCC.length;j++){
                                fakeData[j+1] = realDataWithoutBCC[j];
                            }

                            final byte[] realDataWithBcc =  ScannerCommand._AppendVerificationCode_BCC(fakeData);

//                            if(realDataWithBcc[totalLengthWithoutBCC+1] == readBuffer[dataStartOffset + totalDataDomainLength]){

                            if(realDataWithBcc[totalLengthWithoutBCC+1] == readBuffer[dataStartOffset + totalDataDomainLength]){
                                // BCC验证通过 开始对数据域的内容进行转换
                                final byte[] verifiedDataDomain = new byte[totalDataDomainLength];
                                for (int i = 0; i < totalDataDomainLength; i++){
                                    verifiedDataDomain[i] = realDataWithoutBCC[lengthOfAllHeaders+i];
                                }
                                try {
                                    // 数据域转换成二维码字符串
//                                    QrCode = new StringBuilder(new String(verifiedDataDomain,"UTF-8"));
//                                    return QrCode;
                                    return new StringBuilder(new String(verifiedDataDomain,"UTF-8"));
                                }catch (Exception e){
                                    LogUtil.LogStackTrace(e, "8989");
                                }
                            }else {
                                // BCC验证失败
//                                final byte[] calc = new byte[1];
//                                calc[0] = realDataWithBcc[totalLengthWithoutBCC];
//                                QrCode = new StringBuilder(
//                                        ERROR_WRONG_BCC +
//                                        " 计算结果:" + CommandHelper.bytesToHexString(calc,1) + ", "
//                                        + "扫描枪结果:" +  CommandHelper.bytesToHexString(new byte[]{readBuffer[dataStartOffset + totalDataDomainLength]},1)
//                                );
                            }
                        }
                    }else{
                        // 表示找到了标示字的位置，但是其值不是所期待的 0x00
//                        QrCode = new StringBuilder(ERROR_WRONG_IDENTICAL_BYTE + Byte.toString(readBuffer[dataStartOffset]));
                    }
                }else {
                    // 不存在标示字位
//                    QrCode = new StringBuilder(ERROR_NOT_FOUND_IDENTICAL_BYTE);
                }
            }else {
//                QrCode = new StringBuilder(ERROR_NOT_FOUND_HEAD);
            }
        }

        // 运行到这里， 表示解析失败了
        return new StringBuilder(CommandExecuteResult.NOT_OK);
    }
}
