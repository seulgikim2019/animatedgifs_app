package com.condition.umzzal.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.condition.umzzal.R;
import com.condition.umzzal.activity.MainActivity;
import com.condition.umzzal.activity.VideoDetailActivity;
import com.condition.umzzal.etc.*;
import com.condition.umzzal.object.*;
import com.google.gson.Gson;

import java.util.ArrayList;


/**
 * GameAdapter 클래스
 * - 게시글 리싸이클러뷰를 위한 어댑터
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    String TAG="yeon["+this.getClass().getSimpleName()+"]"; // log를 위한 태그

    ArrayList<Video> videos; // 게시글 리스트
    Activity activity;
    Context context;

    public VideoAdapter(ArrayList<Video> videos, Activity activity){
        this.videos = videos;
        this.activity = activity;
        this.context = activity.getApplicationContext();
    } // GameAdapter 생성자

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // onCreateViewHolder() 메소드
        LayoutInflater inflater  = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_video_item,parent,false); // 뷰 객체 생성
        VideoAdapter.ViewHolder viewHolder = new VideoAdapter.ViewHolder(view);
        return viewHolder;
    } // onCreateViewHolder() 메소드

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) { // onBindViewHolder() 메소드
        if(holder.getAdapterPosition()!= RecyclerView.NO_POSITION){ // 포지션 에러를 최소화 하기 위한 조건

            Video video = videos.get(holder.getAdapterPosition()); // 현재 포지션의 비디오 정보를 얻어오기
            final int video_no = video.getVideo_no();
            final String video_name = video.getVideo_name();
            final String video_thumbnail = video.getVideo_thumbnail();
            final String video_route = video.getVideo_route();

            Glide.with(context) // 비디오 썸네일 적용
                    .load(MYURL.VIDEO_URL+video_thumbnail)
                    .thumbnail(0.1f)
                    .into(holder.video_thumbnail_iv);

            holder.video_name_tv.setText(video_name); // 비디오 이름 적용

            holder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // 아이템 클릭 이벤트

                    Gson gson = new Gson();
                    String video_json = gson.toJson(video);

                    Log.i(TAG,"video_json : " + video_json);

                    //비디오 클릭했을 때 -> 쉐어드를 통해 닉네임과 프로필 등록 여부를 체크한다.
                    SharedPreferences profileUpload=activity.getApplicationContext().getSharedPreferences("profileUpload", Activity.MODE_PRIVATE);
                    String nickName = profileUpload.getString("nickname","f");
                    if(nickName.equals("f")){ // 프로필이 정상적으로 등록되어 있지 않은 경우
                        Log.i(TAG,"프로필이 정상적으로 등록되어 있지 않은 경우");
                        Toast.makeText(activity.getApplicationContext(),"짤을 생성하기 위해서는 프로필을 등록해야 합니다.",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("video_json",video_json);
                        activity.startActivity(intent);
                    }else{ // 프로필이 정상적으로 등록되어 있는 경우
                        Log.i(TAG,"프로필이 정상적으로 등록되어 있는 경우");
                        Intent intent = new Intent(context, VideoDetailActivity.class);
                        intent.putExtra("video_json",video_json);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        activity.startActivity(intent);
                    }
                }
            });

        }
    } // onBindViewHolder() 메소드

    @Override
    public int getItemCount() { // getItemCount() 메소드
        return videos.size();
    } // getItemCount() 메소드

    public class ViewHolder extends RecyclerView.ViewHolder{

        View item_view;
        ImageView video_thumbnail_iv;
        TextView video_name_tv;

        ViewHolder(View item_view){
            super(item_view);
            this.item_view = item_view;
            this.video_thumbnail_iv = item_view.findViewById(R.id.video_thumbnail_iv);
            this.video_name_tv = item_view.findViewById(R.id.video_name_tv);
        } // ViewHolder 생성자

    } // ViewHolder 클래스

} // VideoAdapter
