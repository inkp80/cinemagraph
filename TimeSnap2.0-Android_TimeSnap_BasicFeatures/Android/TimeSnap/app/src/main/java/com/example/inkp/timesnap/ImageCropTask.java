package com.example.inkp.timesnap;

import android.graphics.Bitmap;

import com.example.inkp.timesnap.crop.CropImageView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by macbook on 2017. 11. 23..
 */

public class ImageCropTask extends Thread {
    private CropImageView cropImageView;

    public ImageCropTask(CropImageView cropImageView){
        this.cropImageView = cropImageView;
    }

    @Override
    public void run(){
        Bitmap bitmap = cropImageView.getCroppedImage();
        //TODO AsyncTask : Crop and Save
        cropImageView.setImageBitmap(bitmap);
        try {
            //TODO File exist check
            File file = new File("/sdcard/croptest.png");
            FileOutputStream filestream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, filestream);
        } catch (Exception e){

        }
    }
}
