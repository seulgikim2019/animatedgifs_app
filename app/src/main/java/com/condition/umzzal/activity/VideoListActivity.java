package com.condition.umzzal.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.condition.umzzal.R;
import com.condition.umzzal.adapter.VideoAdapter;
import com.condition.umzzal.etc.AppHelper;
import com.condition.umzzal.etc.MYURL;
import com.condition.umzzal.object.Video;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 영상을 고르는 activity
 *  - 영상을 고르면 VideoDetailActivity 로 화면 전환
 *      >> 이때 프로필 사진 또는 닉네임이 등록되어있지 않으면 닉네임과 프로필 사진을 받는 화면으로 이동
 */
public class VideoListActivity extends AppCompatActivity {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Video> videos; // 원본 영상 리스트

    RecyclerView video_list_recyclerview; // 비디오 리스트 리싸이클러뷰


    MenuItem menuItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        Log.i(TAG,"onCreate()");

        getVideoListRequest(); // 서버에 저장된 비디오를 가지고 오기

    } // onCreate()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG,"onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.mypage,menu);
        menuItem=menu.findItem(R.id.my_page_icon);

        SharedPreferences profileUpload=getSharedPreferences("profileUpload", Activity.MODE_PRIVATE);
        String nickName = profileUpload.getString("nickname","f");

        if("f".equals(nickName)){ // 프로필 정보가 등록되어있지 않으면
            menuItem.setVisible(false);
        }else{ // 등록되어있으면
            menuItem.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.my_page_icon){
            Intent intent = new Intent(getApplicationContext(),MyPageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            return true;
        }else if(id==R.id.explain_icon){
            Intent intent = new Intent(getApplicationContext(),ExplainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        SharedPreferences profileUpload=getSharedPreferences("profileUpload", Activity.MODE_PRIVATE);
        String nickName = profileUpload.getString("nickname","f");

        if(menuItem!=null){
           if("f".equals(nickName)){ // 프로필 정보가 등록되어있지 않으면
            menuItem.setVisible(false);
            }else{ // 등록되어있으면
                menuItem.setVisible(true);
            }
        }

    } // onResume()

    /**
     * 서버에서 비디오 리스트를 받아오는 메소드
     */
    private void getVideoListRequest(){
        Log.i(TAG,"getVideoListRequest() 호출");

        videos = new ArrayList<>();

        // 요청 인자
        Map<String,String> params = new HashMap<String,String>();
        params.put("mode","get_video_list");

        JSONObject jsonObject = new JSONObject(params); // Map을 json으로 만듬

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, MYURL.URL, jsonArray,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response_arr) { // 응답 성공
                        Log.d(TAG,"응답 성공: "+response_arr.toString());

                        try{
                            for(int i=0; i<response_arr.length();i++){
                                JSONObject response = response_arr.getJSONObject(i);
                                Gson gson = new Gson();
                                Video video = gson.fromJson(response.toString(), Video.class);
                                videos.add(video);
                            }
                        }catch (JSONException e){
                            Log.d(TAG,"JSONException: "+e.toString());
                        }

                        /*
                         * game recyclerview 관련 코드
                         */
                        video_list_recyclerview = findViewById(R.id.video_list_recyclerview);
                        video_list_recyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        VideoAdapter videoAdapter = new VideoAdapter(videos,VideoListActivity.this);
                        sort(videos);
                        video_list_recyclerview.setAdapter(videoAdapter);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { // 응답 실패
                Log.d(TAG,"응답 실패: "+error.toString());
            }
        });

        /*
         * requestQueue가 없으면 requestQueue 생성
         */
        if(AppHelper.requestQueue == null){ // requestQueue 가 없을 경우
            AppHelper.requestQueue = Volley.newRequestQueue(getApplicationContext()); // requestQueue 생성
        }

        AppHelper.requestQueue.add(jsonArrayRequest); // 요청 큐에 위 요청 추가

    } // getBoardRequest() 메소드

    /**
     * 비디오 리스트 정렬 메소드
     */
    ArrayList<Video> sort(ArrayList<Video> videos){

        for (int i = 0; i < videos.size()-1; i++) {
            for (int j = 1; j < videos.size(); j++) {
                if(videos.get(j-1).getVideo_no() < videos.get(j).getVideo_no()){
                    Video temp_video_item = videos.get(j-1);
                    videos.set(j-1,videos.get(j));
                    videos.set(j,temp_video_item);
                }
            }
        }
        return videos;

    } // sort()

} // VideoListActivity class