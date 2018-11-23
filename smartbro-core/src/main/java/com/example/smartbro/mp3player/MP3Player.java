package com.example.smartbro.mp3player;

import android.media.MediaPlayer;
import android.support.annotation.RawRes;

import com.example.smartbro.app.Smartbro;

/**
 * Created by Justin Wang from SmartBro on 22/12/17.
 */

public class MP3Player {
    private static MediaPlayer PLAYER = null;

    public static void play(@RawRes int rawFile){
        PLAYER = MediaPlayer.create(Smartbro.getApplication(), rawFile);
        if(!PLAYER.isPlaying()){
            PLAYER.start();
        }
    }
}
