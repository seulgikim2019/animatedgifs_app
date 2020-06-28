package com.condition.umzzal.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.condition.umzzal.R;
import com.condition.umzzal.etc.MYURL;

public class ExplainActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    VideoView videoView;
    ImageView play_iv;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain);

        setTitle("설명 영상");
        loading("잠시만 기다려주세요..");


        videoView = (VideoView)findViewById(R.id.videoView); // 비디오뷰
        play_iv = (ImageView)findViewById(R.id.play_iv); // 재생 이미지뷰

        String path = MYURL.VIDEO_URL+"explain.mp4"; // 실행 할 비디오 URL
        videoView.setVideoPath(path);
        videoView.setMediaController(new MediaController(videoView.getContext()));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) { // 비디오 재생 준비가 완료된 경우
                Log.i(TAG,"비디오 재생 준비 완료");
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                loadingEnd(); // 프로그래스 다이얼로그 종료

                            }
                        }, 0);
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) { // 비디오 재생이 완료된 경우
                Log.i(TAG,"비디오 재생 완료");
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                play_iv.setVisibility(View.GONE);

                            }
                        }, 0);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) { // 비디오관련 에러가 난 경우
                Log.i(TAG,"비디오 관련 에러 발생");
                return false;
            }
        });

        play_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoView.start();
                play_iv.setVisibility(View.GONE);
            }
        });
    } // onCreate()

    @Override
    protected void onResume() {
        super.onResume();
        play_iv.setVisibility(View.VISIBLE);
    }

    /**
     * 로딩 메소드
     */
    public void loading(String message) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(ExplainActivity.this);
                        progressDialog.setIndeterminate(true);
                        //진행중에는 cancel 못하게
                        progressDialog.setCancelable(false);



                        //이 때  back버튼을 눌렀을 때 ->  cancel이 가능하게
                        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                          @Override
                           public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {

                              //프로그래스 진행 중에 백키를 눌렀을 경우,
                              if(i==KeyEvent.KEYCODE_BACK && keyEvent.getAction()==KeyEvent.ACTION_DOWN) {
                                  Log.i("what key back", "back down");
                                  loadingEnd();
                                  finish();
                                  return false;
                              }
                                  return true;
                           }
                         });


                        progressDialog.setMessage(message);

                        progressDialog.show();



                    }
                }, 0);
    } // loading()

    /**
     * 로딩종료 메소드
     */
    public void loadingEnd() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 0);
    } // loadingEnd()
} // ExplainActivity class
