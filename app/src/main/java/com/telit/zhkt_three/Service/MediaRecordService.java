package com.telit.zhkt_three.Service;

import android.annotation.SuppressLint;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class MediaRecordService extends Thread {
    private static final String TAG = "MediaRecordService";

    private int mWidth;
    private int mHeight;
    private int mBitRate;
    private int mDpi;
    private String mDstPath;
    private MediaRecorder mMediaRecorder;
    private MediaProjection mMediaProjection;
    private static final int FRAME_RATE = 60; // 60 fps

    private VirtualDisplay mVirtualDisplay;

    public MediaRecordService(int width, int height, int bitrate, int dpi, MediaProjection mp, String dstPath) {
        mWidth = width;
        mHeight = height;
        mBitRate = bitrate;
        mDpi = dpi;
        mMediaProjection = mp;
        mDstPath = dstPath;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void run() {
        try {
            initMediaRecorder();

            //在mediarecorder.prepare()方法后调用
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display", mWidth, mHeight, mDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mMediaRecorder.getSurface(), null, null);

            mMediaRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化MediaRecorder
     *
     * @return
     */
    public void initMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mDstPath);
        mMediaRecorder.setVideoSize(mWidth, mHeight);
        mMediaRecorder.setVideoFrameRate(FRAME_RATE);
        mMediaRecorder.setVideoEncodingBitRate(mBitRate);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "media recorder" + mBitRate + "kps");
    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void release() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaProjection.stop();
            }
            mMediaRecorder.reset();
            mMediaRecorder.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "release");
    }

}
