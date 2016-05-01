package com.shutup.ffmpegcmdutils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FFmpegController;
import org.ffmpeg.android.ShellUtils;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FFMpeg Cmd Utils";
    private final String IMAGE_TYPE = "image/*";
    private final String VIDEO_TYPE = "video/*";

    private final int RESULT_CODE = 0;   //这里的RESULT_CODE是自己任意定义的

    @InjectView(R.id.BtnClip)
    Button mBtnClip;
    @InjectView(R.id.BtnConcat)
    Button mBtnConcat;
    @InjectView(R.id.BtnSelect)
    Button mBtnSelect;
    @InjectView(R.id.info)
    TextView mInfo;
    @InjectView(R.id.editTextStartTime)
    EditText mEditTextStartTime;
    @InjectView(R.id.editTextDuration)
    EditText mEditTextDuration;
    @InjectView(R.id.editTextSplitName)
    EditText mEditTextSplitName;
    private String srcVideoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

    }

    @OnClick({R.id.BtnSelect, R.id.BtnClip, R.id.BtnConcat})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.BtnSelect:
                jumpToSelectVideo();
                break;
            case R.id.BtnClip:
                clipTheVideoTest();
                break;
            case R.id.BtnConcat:
                break;
        }
    }

    private void jumpToSelectVideo() {
        //使用intent调用系统提供的相册功能，使用startActivityForResult是为了获取用户选择的内容
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
//        getAlbum.setType(IMAGE_TYPE);
        getAlbum.setType(VIDEO_TYPE);

        startActivityForResult(getAlbum, RESULT_CODE);
    }

    private void clipTheVideoTest() {
        if (srcVideoPath == null) {
            Toast.makeText(MainActivity.this, "select the video first", Toast.LENGTH_SHORT).show();
            return;
        }
        String dstDirName = "ffmpegTest";
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dstDir = sdcardPath + File.separator + dstDirName;
        File dstDirFile = new File(dstDir);
        if (dstDirFile.exists()) {
            File []files = dstDirFile.listFiles();
            for (File fileVideoOutput :files) {
                fileVideoOutput.delete();
            }
        } else {
            dstDirFile.mkdir();
        }


        try {
            FFmpegController fc = new FFmpegController(MainActivity.this, new File(dstDir));
            Clip in = new Clip();
            in.startTime = mEditTextStartTime.getText().toString().trim();
            in.path = srcVideoPath;

            final Clip out = new Clip();
            out.duration = Double.parseDouble(mEditTextDuration.getText().toString().trim());
            out.audioCodec = "copy";
            out.videoCodec = "copy";
            out.path = dstDir + File.separator + mEditTextSplitName.getText().toString().trim();

            fc.clipVideo(in, out, true, new ShellUtils.ShellCallback() {
                @Override
                public void shellOut(String shellLine) {
                    Log.d(TAG, "shellOut() returned: " + shellLine);
                }

                @Override
                public void processComplete(int exitValue) {
                    Log.d(TAG, "processComplete() returned: " + exitValue);
                    Toast.makeText(MainActivity.this, "the new clip is at:" + out.path, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {        //此处的 RESULT_OK 是系统自定义得一个常量
            Log.e(TAG, "ActivityResult resultCode error");
            return;

        }
        //此处的用于判断接收的Activity是不是你想要的那个
        if (requestCode == RESULT_CODE) {
            Uri originalUri = data.getData();        //获得所选内容的uri
            srcVideoPath = originalUri.getPath();
            mBtnSelect.setText(srcVideoPath);
            Log.d(TAG, "onActivityResult: " + srcVideoPath);
        }
    }
}
