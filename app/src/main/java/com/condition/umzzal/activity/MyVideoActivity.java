package com.condition.umzzal.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.condition.umzzal.R;
import com.condition.umzzal.etc.*;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

/**
 * 자신이 만든 완성된 비디오 영상을 볼 수 있고 만들어진 영상을 다운로드 할 수 있는 activity
 */
public class MyVideoActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그
    VideoView videoView; // 비디오뷰
    ProgressDialog progressDialog; // 프로그래스 다이얼로그
    private ProgressDialog progressBar; // 파일 다운로드시 프로그래스 바
    private File outputFile; //파일명까지 포함한 경로
    private File path;//디렉토리경로

    String fileName;

    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_video);

        Log.i(TAG,"onCreate()");

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        loading(); // 프로그래스 다이얼로그 실행

        // intent로 완성될 비디오 파일 명을 받아오자!
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        Log.i(TAG,"fileName : "+fileName);
        String[] arr1 = fileName.split("\\.");
        Log.i(TAG,"arr1[0] : "+arr1[0]);
        String[] arr2 = arr1[0].split("_");
        String video_no = arr2[0];
        Log.i(TAG,"video_no : "+video_no);
        String member_nickname= arr2[1];
        Log.i(TAG,"member_nickname : "+member_nickname);
        String time = arr2[2];
        Log.i(TAG,"time : "+time);
        String day = arr2[3];
        Log.i(TAG,"day : "+day);

        progressBar = new ProgressDialog(MyVideoActivity.this); // 파일 다운로드 프로그래스 바 설정
        progressBar.setMessage("다운로드중");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setIndeterminate(true);
        progressBar.setCancelable(true);

        ImageView play_iv = findViewById(R.id.play_iv); // 재생 이미지뷰
        videoView = findViewById(R.id.videoView);
        videoView.setVideoPath(MYURL.FINISH_VIDEO_URL+"/"+day+"/"+fileName);
        videoView.setMediaController(new MediaController(videoView.getContext()));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) { // 비디오 재생 준비가 완료된 경우
                Log.i(TAG,"비디오 재생 준비 완료");
                loadingEnd();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) { // 비디오 재생이 완료된 경우
                Log.i(TAG,"비디오 재생 완료");
                play_iv.setVisibility(View.VISIBLE);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) { // 비디오관련 에러가 난 경우
                Log.i(TAG,"비디오 관련 에러 발생");
                loadingEnd();
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

        Button download_btn = findViewById(R.id.download_btn); // 다운로드 버튼
        download_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 다운로드 버튼 클릭시 이벤트
                path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);


                // 이미지가 저장될 폴더 이름 ( yeon )
               // path = new File("/sdcard/");
               // if (! path.exists())  path.mkdirs();

                Log.i("aaaa1",path.getPath());

                outputFile= new File(path, "Alight.avi"); //파일명까지 포함함 경로의 File 객체 생성
                final DownloadFilesTask downloadTask = new DownloadFilesTask(MyVideoActivity.this);
                downloadTask.execute(MYURL.FINISH_VIDEO_URL+"/"+day+"/"+fileName);
            }
        });

    } // onCreatea

    /**
     * 로딩 메소드
     */
    public void loading() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(MyVideoActivity.this);
                        progressDialog.setIndeterminate(true);
                        //진행중에는 cancel 못하게
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage("잠시만 기다려 주세요");
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

    /**
     * Calendar를 년월일시분초로 반환 메소드
     */
    public String timeToString(Calendar time) {
        String timeToString = (time.get(Calendar.MONTH) + 1)+""+(time.get(Calendar.DAY_OF_MONTH))+ ""+(time.get(Calendar.HOUR_OF_DAY)) +""+(time.get(Calendar.MINUTE)) +""+(time.get(Calendar.SECOND))+"";
        return timeToString;
    } // timeToString()


    /**
     * 파일 다운로드하는 AsyncTask
     */
    private class DownloadFilesTask extends AsyncTask<String, String, Long> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadFilesTask(Context context) {
            this.context = context;
        }


        //파일 다운로드를 시작하기 전에 프로그레스바를 화면에 보여줍니다.
        @Override
        protected void onPreExecute() { //2
            super.onPreExecute();

            //사용자가 다운로드 중 파워 버튼을 누르더라도 CPU가 잠들지 않도록 해서
            //다시 파워버튼 누르면 그동안 다운로드가 진행되고 있게 됩니다.
//            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
//            mWakeLock.acquire();

            progressBar.show();

        }


        //파일 다운로드를 진행합니다.
        @Override
        protected Long doInBackground(String... string_url) { //3
            int count;
            long FileSize = -1;
            InputStream input = null;
            OutputStream output = null;
            URLConnection connection = null;

            try {
                URL url = new URL(string_url[0]);
                connection = url.openConnection();
                connection.connect();

                //파일 크기를 가져옴
                FileSize = connection.getContentLength();

                //URL 주소로부터 파일다운로드하기 위한 input stream
                input = new BufferedInputStream(url.openStream(), 8192);

                // 이미지가 저장될 폴더 이름 ( yeon )
              //  path = new File(  "/sdcard/DCIM/");
             //   if (! path.exists())  path.mkdirs();


              //  Log.i("aaaa2",path.getPath());

                 path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                outputFile= new File(path.getPath(), timeToString(Calendar.getInstance())+".avi"); //파일명까지 포함함 경로의 File 객체 생성

                // SD카드에 저장하기 위한 Output stream
                output = new FileOutputStream(outputFile);

                byte data[] = new byte[1024];
                long downloadedSize = 0;
                while ((count = input.read(data)) != -1) {
                    //사용자가 BACK 버튼 누르면 취소가능
                    if (isCancelled()) {
                        input.close();
                        return Long.valueOf(-1);
                    }

                    downloadedSize += count;

                    if (FileSize > 0) {
                        float per = ((float)downloadedSize/FileSize) * 100;
                        String str = "Downloaded " + downloadedSize + "KB / " + FileSize + "KB (" + (int)per + "%)";
                        publishProgress("" + (int) ((downloadedSize * 100) / FileSize), str);

                    }

                    //파일에 데이터를 기록합니다.
                    output.write(data, 0, count);
                }
                // Flush output
                output.flush();

                // Close streams
                output.close();
                input.close();


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

//                mWakeLock.release();

            }
            return FileSize;
        } // doInBackground()

        /**
         * 다운로드 중 프로그레스바 업데이트
         */
        @Override
        protected void onProgressUpdate(String... progress) { //4
            super.onProgressUpdate(progress);

            // if we get here, length is known, now set indeterminate to false
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setCancelable(false);
            progressBar.setProgress(Integer.parseInt(progress[0]));
            progressBar.setMessage(progress[1]);
        }

        /**
         * 파일 다운로드 완료 후
         */
        @Override
        protected void onPostExecute(Long size) { //5
            super.onPostExecute(size);

            progressBar.dismiss(); // 프로그래스 바 종료

            if (size > 0) {
                Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다. 파일 크기=" + size.toString(), Toast.LENGTH_LONG).show();
                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(outputFile));
                sendBroadcast(mediaScanIntent);
            }else{
                Toast.makeText(getApplicationContext(), "다운로드 에러", Toast.LENGTH_LONG).show();
            }

        }

    } // DownloadFilesTask class


    @Override
    protected void onDestroy() {

        Log.i("myvideodestroy","destroy");
        super.onDestroy();
    }
} // MyVideoActivity class
