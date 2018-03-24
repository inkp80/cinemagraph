package com.example.inkp.timesnap;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

/**
 * Created by macbook on 2017. 11. 21..
 */

public class FfmpegModule {
    // TODO rename..? Module..? Helper..? Wrapper..?
    private FFmpeg fFmpeg;

    public FfmpegModule(Context context){
        fFmpeg = FFmpeg.getInstance(context);
    }

    public void loadFfmpegBinary(LoadBinaryResponseHandler loadBinaryResponseHandler)
        throws FFmpegNotSupportedException {

            fFmpeg.loadBinary(loadBinaryResponseHandler);
    }

    public void execFfmpegBinary(
            final String[] command,
            ExecuteBinaryResponseHandler executeBinaryResponseHandler)
            throws FFmpegCommandAlreadyRunningException {

        fFmpeg.execute(command, executeBinaryResponseHandler);
    }

    public void killRunningProcesses() {
        fFmpeg.killRunningProcesses();
    }

    public boolean isFfmpegRunning(){
       return fFmpeg.isFFmpegCommandRunning();
    }

}