package com.example.inkp.timesnap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.inkp.timesnap.common.RequestCodes;
import com.example.inkp.timesnap.crop.CropImageView;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.inkp.timesnap.common.RequestCodes.VIDEO_COMPOSE_REQUEST_CODE;
import static com.example.inkp.timesnap.common.RequestCodes.VIDEO_GALLERY_REQUEST_CODE;

/**
 * Created by macbook on 2017. 11. 21..
 */

public class VideoComposeActivity extends AppCompatActivity {
    private static final String TAG = VideoComposeActivity.class.getSimpleName();

    //TODO 파일 경로 어떻게 관리할지 고민 해보기
    public static final String TEMPORARY_FRAME_OUTPUT_FILE_PATH = "/sdcard/temp_frame.png";
    public static final String TEMPERARY_VIDEO_OUTPUT_FILE_PATH = "/sdcard/temp_video.mp4";
    public static final String TEMPERARY_GIF_OUTPUT_FILE_PATH = "/sdcard/temp_gif.gif";
    public static final String TEMPERARY_GIF_PALETTE_OUTPUT_FILE_PATH = "/sdcard/temp_palette.png";
    private String videoPath;

    private ProgressDialog progressDialog;

    FfmpegModule ffmpegModule;

    private CropImageView cropViewer;
    private RelativeLayout lnSelectLayout;
    private Button btSelectVideo;
    private Button btCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_compose);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);

        ffmpegModule = new FfmpegModule(this);

        try {
            ffmpegModule.loadFfmpegBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    super.onFailure();
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.e(TAG, "Device doesn't support FFmpeg :" + e);
            showUnsupportedExceptionDialog();
            e.printStackTrace();
        }

        lnSelectLayout = (RelativeLayout) findViewById(R.id.composer_ln_selector);

        btSelectVideo = (Button) findViewById(R.id.composer_bt_select_video);
        btSelectVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(intent, RequestCodes.VIDEO_GALLERY_REQUEST_CODE);
            }
        });

        cropViewer = (CropImageView) findViewById(R.id.composer_cv_crop_image_view);
        cropViewer.setVisibility(View.INVISIBLE);

        btCrop = (Button) findViewById(R.id.composer_bt_crop_button);
        btCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO 비동기로 진행
                //TODO Image crop & Video compose에 대한 Progress 알림 표시

                Bitmap bitmap = cropViewer.getCroppedImage();
                cropViewer.setImageBitmap(bitmap);
                try {
                    //TODO File exist check
                    File file = new File(TEMPORARY_FRAME_OUTPUT_FILE_PATH);
                    FileOutputStream filestream = new FileOutputStream(file, false);

                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, filestream);
                } catch (Exception e) {

                }
                // TODO Service에서 동작하도록.. 변경 고민해보기
                startComposeVideo(videoPath, TEMPORARY_FRAME_OUTPUT_FILE_PATH);
            }
        });
        btCrop.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {

            if (requestCode == VIDEO_GALLERY_REQUEST_CODE) {

                Uri selectedImageUri = data.getData();

                if (selectedImageUri != null) {

                    String[] filePathColumn = {MediaStore.Video.Media.DATA};

                    Cursor cursor = getContentResolver().query(
                            selectedImageUri, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    videoPath = cursor.getString(columnIndex);

                    cursor.close();

                    Toast.makeText(this, videoPath, Toast.LENGTH_SHORT).show();
                    getFrameFromVideo(videoPath);

                    // TODO : request to user select section for compose video
                    // requestUserToSelectSection()?
                }
            } else {
                return;
            }
        } else {
            return;
        }
    }

    private void getFrameFromVideo(final String videoPath) {

        File file = new File(TEMPORARY_FRAME_OUTPUT_FILE_PATH);
        if (file.exists()) {
            Log.d(TAG, "File exist, Remove this file");
            file.delete();
        }

        String extractFrame = "-i " + videoPath + " -vf scale=500:-1:flags=lanczos " + TEMPORARY_FRAME_OUTPUT_FILE_PATH;

        String[] cmd = extractFrame.split(" ");

        try {
            ffmpegModule.execFfmpegBinary(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    super.onSuccess(message);
                    Log.d(TAG, message);

                }

                @Override
                public void onProgress(String message) {
                    super.onProgress(message);
                    Log.d(TAG, "progress: " + message);
                }

                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
                    Log.d(TAG, message);
                }

                @Override
                public void onStart() {
                    super.onStart();
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    cropViewer.setImageBitmap(BitmapFactory.decodeFile(TEMPORARY_FRAME_OUTPUT_FILE_PATH));

                    cropViewer.setVisibility(View.VISIBLE);

                    btCrop.setVisibility(View.VISIBLE);

                    lnSelectLayout.setVisibility(View.INVISIBLE);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e(TAG, "Command already running exception : " + e);
            e.printStackTrace();
        }
    }

    private void startComposeVideo(final String targetVideoPath, final String targetFramePath) {

        File gifFile = new File(TEMPERARY_GIF_OUTPUT_FILE_PATH);
        if(gifFile.exists()){
            try {
                gifFile.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        File gifPaletteFile = new File(TEMPERARY_GIF_PALETTE_OUTPUT_FILE_PATH);
        if(gifPaletteFile.exists()){
            try {
                gifPaletteFile.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // TODO : rename outputPath, Need to decide how to make output-file-name


        //MP4
        //String cmd = "-i " + targetVideoPath + " -i " + targetFramePath + " -filter_complex overlay=1:1 " + TEMPERARY_VIDEO_OUTPUT_FILE_PATH;


        getPalettFromVideo(targetVideoPath, targetFramePath);

    }

    // GIF 품질 향상을 위해 팔레트를 이용한다. (Ffmpeg :  http://blog.pkh.me/p/21-high-quality-gif-with-ffmpeg.html)
    public void getPalettFromVideo(final String targetVideoPath, final String targetFramePath){
        String paletteCmd = "-ss 1 -i " + targetVideoPath + " -vf fps=1,scale=500:-1:flags=lanczos,palettegen=stats_mode=full " +  TEMPERARY_GIF_PALETTE_OUTPUT_FILE_PATH;

        String[] command = paletteCmd.split(" ");
        try {
            ffmpegModule.execFfmpegBinary(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "compose fail : "+s);
                }

                @Override
                public void onSuccess(String s) {
                }

                @Override
                public void onProgress(String s) {
                    //TODO frame record status
                    progressDialog.setMessage("불러오는 중..");
                }

                @Override
                public void onStart() {
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg ");
                    composeToGifWithCropFrame(targetVideoPath,targetFramePath);
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e(TAG, "Command already running exception : " + e);
            e.printStackTrace();
        }
    }


    public void composeToGifWithCropFrame(final String targetVideoPath, final String targetFramePath){
        //TODO 함수 분리하기
        final String makeGifCmd = "-i " + targetVideoPath + " -i " + targetFramePath + " -i " + TEMPERARY_GIF_PALETTE_OUTPUT_FILE_PATH
                + " -filter_complex [0]fps=10,scale=500:-1:flags=lanczos[bg];[bg][1]overlay=1:1[x];[x][2]paletteuse=dither=sierra2_4a "
                + TEMPERARY_GIF_OUTPUT_FILE_PATH;

        String[] command = makeGifCmd.split(" ");
        try {
            ffmpegModule.execFfmpegBinary(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "compose fail : "+s);
                }

                @Override
                public void onSuccess(String s) {
                }

                @Override
                public void onProgress(String s) {
                    //TODO frame record status
                    progressDialog.setMessage("Composing...\n" + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg ");
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg ");
                    progressDialog.dismiss();
                    setResult(RESULT_OK);
                    finish();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e(TAG, "Command already running exception : " + e);
            e.printStackTrace();
        }

    }


    private void showUnsupportedExceptionDialog() {

        new AlertDialog.Builder(VideoComposeActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VideoComposeActivity.this.finish();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onPause(){
        super.onPause();
        if(ffmpegModule.isFfmpegRunning()) {
            Toast.makeText(this, "FFMPEG - Process", Toast.LENGTH_SHORT).show();
            ffmpegModule.killRunningProcesses();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        ffmpegModule.killRunningProcesses();

    }
}
