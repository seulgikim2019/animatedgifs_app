package com.condition.umzzal.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.condition.umzzal.R;
import com.condition.umzzal.etc.*;
import com.condition.umzzal.object.*;
import com.condition.umzzal.retrofit.UploadService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 영상을 보고, 자신의 음성을 녹음하여 짤 만들기 요청을 하는 activity
 */
public class VideoDetailActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    Video video; // 사용자가 선택한 비디오

    ProgressDialog progressDialog; // 프로그래스 다이얼로그

    VideoView videoView; // 비디오뷰
    ImageView play_iv; // 비디오 재생 이미지뷰
    LinearLayout rec_ll; // 녹음 리니어레이아웃
    LinearLayout rec_play_ll; // 녹음 파일 재생 리니어레이아웃
    ImageView rec_iv; // 녹음 이미지뷰
//    Button rec_btn; // 녹음 버튼
//    Button play_record_btn; // 녹음파일 재생 버튼
    Button make_video_btn; // 비디오 만들기 버튼


    //백버튼 control
    BackPressCloseHandler backPressCloseHandler;
    //어떤 상황에 백버튼 눌렀는지
    String situation="playvideo";

    private File file; // 녹음한 음성 파일
    private String fileName; // 녹음한 음성 파일 이름

    private MediaRecorder recorder; // 녹음기
    private MediaPlayer rec_mediaPlayer; // 녹음된 파일 미디어 플레이어

    AudioManager audioManager; // 오디오 매니져
    int current_vol; // 현재 볼륨

    boolean is_rec; // 현재 녹음중인지 여부

    String nickName; // 멤버 닉네임
    String temp_time; // 음성 파일을 만드는 시간으로 나중에 영상 만들 때 이름으로 활용된다
    String temp_day; // 음성 파일을 만드는 일자로 나중에 영상 폴더를 만들 때 폴더 이름으로 활용된다

    Activity activity;

    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);


        backPressCloseHandler=new BackPressCloseHandler(VideoDetailActivity.this);

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        activity=VideoDetailActivity.this;
        SharedPreferences profileUpload=getSharedPreferences("profileUpload", Activity.MODE_PRIVATE); // 쉐어드
        nickName = profileUpload.getString("nickname","f");

        loading("잠시만 기다려 주세요"); // 프로그래스 다이얼로그 시작

        Intent intent = getIntent(); // intent로 video 정보 받아오기
        String video_json = intent.getStringExtra("video_json"); // 사용자가 선택한 비디오 json 형태
        Gson gson = new Gson();
        video = gson.fromJson(video_json,Video.class); // 사용자가 선택한 비디오
        Log.i(TAG,"video.getVideo_no() : "+video.getVideo_no());
        Log.i(TAG,"video.getVideo_name() : "+video.getVideo_name());
        Log.i(TAG,"video.getVideo_route() : "+video.getVideo_route());
        Log.i(TAG,"video.getVideo_thumbnail() : "+video.getVideo_thumbnail());

        // actionBar 설정
        setTitle(video.getVideo_name());

        videoView = (VideoView)findViewById(R.id.videoView); // 비디오뷰
        play_iv = (ImageView)findViewById(R.id.play_iv); // 비디오 재생 이미지뷰
        rec_iv = (ImageView)findViewById(R.id.rec_iv); // 녹음 이미지뷰
        rec_ll = (LinearLayout)findViewById(R.id.rec_ll); // 녹음 리니어레이아웃
        make_video_btn = findViewById(R.id.make_video_btn); // 비디오 만들기 버튼
        rec_play_ll = (LinearLayout)findViewById(R.id.rec_play_ll); // 녹음 파일 재생 리니어레이아웃

        // 녹음한 음성파일 경로 및 이름 지정
        temp_time = timeToString(Calendar.getInstance());
        temp_day = dayToString(Calendar.getInstance());
        fileName = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath();
        fileName += "/"+video.getVideo_no()+"_"+nickName+"_"+temp_time+"_"+temp_day+".mp4";
        Log.i(TAG,"fileName : "+fileName);

        permissionCheck(); // 외부저장소, 음성녹음 권한 요청

        String path = MYURL.VIDEO_URL+video.getVideo_route(); // 실행 할 비디오 URL

        videoView.setVideoPath(path);
        videoView.setMediaController(new MediaController(videoView.getContext()));

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE); // 시스템에서 오디오 매니져 가지고오기

        is_rec=false; // 녹음중이 아님


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) { // 비디오 재생 준비가 완료된 경우
                Log.i(TAG,"비디오 재생 준비 완료");
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                loadingEnd(); // 프로그래스 다이얼로그 종료
                               // Toast.makeText(getApplicationContext(),"재생 준비가 완료되었습니다. '재생'버튼을 눌러주세요!",Toast.LENGTH_SHORT).show();
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
                                play_iv.setVisibility(View.VISIBLE);
                                if(is_rec){ // 녹음중인경우
                                    is_rec=false;
                                    rec_iv.setImageResource(R.drawable.rec);
                                    rec_play_ll.setEnabled(true);
                                    make_video_btn.setEnabled(true);
                                    rec_play_ll.setVisibility(View.VISIBLE);
                                    make_video_btn.setVisibility(View.VISIBLE);
                                    recorder.stop();
                                    recorder.release();
                                    recorder = null;
                                    Toast.makeText(getApplicationContext(), "녹음 중지", Toast.LENGTH_LONG).show();
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current_vol, AudioManager.FLAG_SHOW_UI); // 현재 볼륨으로 조정
                                }else{ // 영상 재생인 경우
                                    rec_ll.setVisibility(View.VISIBLE);
                                }
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

    } // onCreate()

    @Override
    protected void onResume() {
        super.onResume();

        play_iv.setVisibility(View.VISIBLE);
        rec_ll.setVisibility(View.VISIBLE);


        play_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)==0){ // 만약 현재 볼륨이 0 이라면
                    Toast.makeText(getApplicationContext(),"볼륨이 0입니다. 오디오를 들으려면 볼륨을 올려주세요!",Toast.LENGTH_SHORT).show();
                }
                videoView.start(); // 비디오 재생
                play_iv.setVisibility(View.GONE); // 재생버튼 사라져라
                rec_ll.setVisibility(View.GONE); //녹음 리니어레이아웃 안보이게
            }
        });


        rec_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file != null && file.exists()){ // 녹음한 음성파일이 있다면
                    file.delete(); // 삭제해라
                }

                play_iv.setVisibility(View.GONE); // 재생버튼 사라져라

                rec_iv.setImageResource(R.drawable.recing);
                is_rec=true; // 녹음중

                current_vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 현재 볼륨을 가지고 오기

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI); // 볼륨 0으로 조정

                videoView.start(); // 비디오 재생

                rec_ll.setEnabled(false);
                rec_play_ll.setEnabled(false);
                make_video_btn.setEnabled(false);

                recorder = new MediaRecorder(); // MediaRecorder 설정
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                recorder.setOutputFile(fileName);

                try {
                    recorder.prepare();

                    Toast.makeText(getApplicationContext(), "녹음 시작", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "prepare() failed");
                }
                recorder.start();
            }
        });


        rec_play_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 녹음 파일 재생 리니어레이아웃 클릭시 이벤트

                if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)==0){ // 만약 현재 볼륨이 0 이라면
                    Toast.makeText(getApplicationContext(),"볼륨이 0입니다. 오디오를 들으려면 볼륨을 올려주세요!",Toast.LENGTH_SHORT).show();
                }

                rec_mediaPlayer = new MediaPlayer();
                try {
                    situation="recording";
                    rec_mediaPlayer.setDataSource(fileName);
                    rec_mediaPlayer.prepare();
                    rec_mediaPlayer.start();
                   // Toast.makeText(getApplicationContext(), "녹음파일 재생", Toast.LENGTH_LONG).show();

                    if (rec_mediaPlayer.isPlaying()){
                        loading(".. 녹음 파일 재생 중 ..");
                    }

                    //재생이 모두 완료되었을 때
                    rec_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            situation="playvideo";
                            loadingEnd();
                        }
                    });

                } catch (IOException e) {
                    Log.e(TAG, "prepare() failed");
                }
            }
        });



        make_video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 비디오 만들기 버튼 클릭시 이벤트
                Log.i(TAG,"비디오 만드리 버튼 클릭");
                situation="makevideo";
                UploadService service = MyRetrofit2.getRetrofit2().create(UploadService.class);

                File file = new File(fileName);

                Log.i(TAG,"file.getAbsolutePath() : "+file.getAbsolutePath());
                Log.i(TAG,"file.getName() : "+file.getName());

                // 오디오 파일 업로드
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                MultipartBody.Part body=MultipartBody.Part.createFormData("file", file.getName(), requestFile);

                Call<ResponseBody> call = service.uploadVoice(body);

                // 응답 오기 전까지 로딩문구 띄우기
                loading("잠시만 기다려 주세요");

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { // 응답 성공
                        Log.i(TAG,"success!!");
                        loadingEnd();
                        situation="playvideo";

                        // 다음 액티비티로 이동
                        Intent intent = new Intent(getApplicationContext(),MyVideoActivity.class);
                        intent.putExtra("fileName",video.getVideo_no()+"_"+nickName+"_"+temp_time+"_"+temp_day+".mp4");
                        startActivity(intent);
                        finish();

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) { // 응답 실패
                        Log.i(TAG,t.toString());
                        loadingEnd();
                        situation="playvideo";
                    }

                });
            }
        });


    } // onResume()

    @Override
    protected void onPause() {
        super.onPause();
    } // onPause()

    @Override
    protected void onStop() {
        super.onStop();

        if (recorder != null) { // 녹음기가 있으면
            recorder.release();
            recorder = null;
        }

        if (rec_mediaPlayer != null) { // 미디어 플레이어가 있으면
            rec_mediaPlayer.release();
            rec_mediaPlayer = null;
        }

    } // onStop()

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (file != null && file.exists()){ // 녹음한 음성파일이 있다면
            file.delete(); // 삭제해라
        }
    } // onDestroy()

    /**
     * 로딩 메소드
     */
    public void loading(String message) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(VideoDetailActivity.this);
                        progressDialog.setIndeterminate(true);
                        //진행중에는 cancel 못하게
                        progressDialog.setCancelable(false);

                        progressDialog.setMessage(message);

                        progressDialog.show();
                        //이 때  back버튼을 눌렀을 때 ->  cancel이 가능하게
                        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                Log.i("what key",event.toString());


                                //프로그래스 진행 중에 백키를 눌렀을 경우,
                                if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){
                                    Log.i("what key back",event.toString());

                                    if(situation.equals("playvideo")){ //비디오로딩중에 백버튼 누른경우
                                        videoView=null;
                                        rec_mediaPlayer = null;
                                        loadingEnd();
                                        finish();
                                    }else if(situation.equals("makevideo")){ //makevideo 중에 백버튼 누른경우
                                        backPressCloseHandler.onBackPressed();
                                    }else if(situation.equals("recording")){ //녹음듣기중에 백버튼 누른경우 -> 화면은 유지
                                        //오디오 멈추고 로딩화면 없애기
                                        rec_mediaPlayer.stop();
                                        loadingEnd();
                                    }
                                    return false;
                                }else if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){ //오디오 볼륨 듣는 중에 올리고 싶을 수도 있으므로
                                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI);
                                    return false;
                                }else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){//오디오 볼륨 듣는 중에 내리고 싶을 수도 있으므로
                                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER,AudioManager.FLAG_SHOW_UI);
                                    return false;
                                }
                                return true;
                            }
                        });

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

    /**
     * 외부저장소, 음성녹음 권한 요청
     */
    public void permissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        }
    } // permissionCheck()

    /**
     * Calendar를 년월일시분초로 반환 메소드
     */
    public String timeToString(Calendar time) {
        String timeToString = (time.get(Calendar.YEAR))+""+(time.get(Calendar.MONTH) + 1)+""+(time.get(Calendar.DAY_OF_MONTH))+ ""+(time.get(Calendar.HOUR_OF_DAY)) +""+(time.get(Calendar.MINUTE)) +""+(time.get(Calendar.SECOND))+"";
        return timeToString;
    } // timeToString()

    /**
     * Calendar를 년월일시분초로 반환 메소드><
     */
    public String dayToString(Calendar time) {
        String timeToString = (time.get(Calendar.YEAR))+""+(time.get(Calendar.MONTH) + 1)+""+(time.get(Calendar.DAY_OF_MONTH))+ "";
        return timeToString;
    } // timeToString()


} // VideoDetailActivity class
