package com.taihua.pishamachine.command.marshall;

/**
 * Created by Justin Wang from SmartBro on 19/1/18.
 */

public class MarshallApi {
    /**
     * Amit source address Amit的设备地址
     */
    public static int MARSHALLAPI_AMIT_SOURCE_ADDR = 0;

    /**
     * Max packet length 包长度最大值
     */
    public static int MARSHALLAPI_MAX_PACKET_LENGTH = 512;

    /**
     * The Marshall functions code
     */
    public static class FunctionCodeTag{

        public static int MARSHALLAPI_CODE_RESPONSE               = 0x00;         /** Response code (ack/nak...) */
        public static int MARSHALLAPI_CODE_RESET                  = 0x01;         /** Reset the Peripheral */

        public static int MARSHALLAPI_CODE_FW_INFO                = 0x05;         /** Contains specific FW details */
        public static int MARSHALLAPI_CODE_CONFIG                 = 0x06;         /** Set parameters for communication */
        public static int MARSHALLAPI_CODE_KEEP_ALIVE             = 0x07;         /** Indication that the D200 is still connected */
        public static int MARSHALLAPI_CODE_DISPLAY_MSG            = 0x08;        	/** Displays Message on D200 LCD */
        public static int MARSHALLAPI_CODE_DISPLAY_MSG_STATUS     = 0x09;        	/** Response for displays Message */
        public static int MARSHALLAPI_CODE_TRANSFER_DATA		  = 0x0A;        	/** Transfer specific data  */
        public static int MARSHALLAPI_CODE_STATUS				  = 0x0B;        	/** Status message */


        public static int MARSHALLAPI_CODE_MODEM_STATUS           = 0x20;         /** Modem status */
        public static int MARSHALLAPI_CODE_OPEN_SOCKET            = 0x21;         /** Open a socket for comm. with a server */
        public static int MARSHALLAPI_CODE_CLOSE_SOCKET           = 0x22;         /** Close socket opened for comm. with server */
        public static int MARSHALLAPI_CODE_SEND_DATA              = 0x23;         /** Send Data to server */
        public static int MARSHALLAPI_CODE_REQ_DATA               = 0x24;         /** Reqeust data received from server */
        public static int MARSHALLAPI_CODE_RCV_DATA               = 0x25;         /** data received from server */

        public static int MARSHALLAPI_CODE_TRACE                  = 0x30;         /** Send Trace parameters to Amit */
        public static int MARSHALLAPI_CODE_ALERT                  = 0x31;         /** Send Alert to Amit */

        public static int MARSHALLAPI_CODE_MDB_CMD                = 0x80;         /** Retrieve received Data message from server */

        public static int MARSHALLAPI_CODE_PP_SET_EVENT           = 0x90;         /** Pin pad - Set event type to be activated on D200 */
        public static int MARSHALLAPI_CODE_PP_EVENT               = 0x91;       	/** Pin pad - Event type and its specific data */
        public static int MARSHALLAPI_CODE_PP_TRANS_REQUEST       = 0x92;       	/** Pin pad - Provide to D200 Transaction’s parameters */
        public static int MARSHALLAPI_CODE_PP_TRANS_DETAILS       = 0x93;      	/** Pin pad - Provides the Amit with Transaction’s details to be sent to Nayax’ Server */
        public static int MARSHALLAPI_CODE_PP_DISP_CONFIRM        = 0x94;         /** Pin pad - Indication whether the product was dispensed correctly or not */
        public static int MARSHALLAPI_CODE_PP_TRANS_CANCEL        = 0x95;         /** Pin pad - Two-way command in order to cancel a transaction */
        public static int MARSHALLAPI_CODE_PP_END_TRANS           = 0x96;         /** Pin pad - Indicates end of transaction */
    }

    public static class PacketHeaderTag{

    }

}
