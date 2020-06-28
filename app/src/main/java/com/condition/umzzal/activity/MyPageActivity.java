package com.condition.umzzal.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.condition.umzzal.R;
import com.condition.umzzal.retrofit.UploadService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPageActivity extends AppCompatActivity {


    public static final int REQUEST_IMAGE1 = 100;


    //프로필이미지
    ImageView my_profile_img;
    //닉네임
    TextView nick_name_show;
    //프로필 수정
    Button profile_change_btn;
    //정보 삭제
    Button profile_delete_btn;

    //닉네임 정보 담고 있는 shared
    SharedPreferences profileUpload;
    String nickName;

    //이미지 업로드 시도한 부분 체크
    String img_upload_try="once";
    Uri uri=null;

    // 프로그래스 다이얼로그
    ProgressDialog progressDialog;


    //로딩중에 백 버튼을 몇번 눌렀는지 체크가 필요함
    int count=0;

    /**
     * 로딩 메소드
     */
    public void loading() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(MyPageActivity.this);
                        progressDialog.setIndeterminate(true);
                        //진행중에는 cancel 못하게 -> 설정
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage("...프로필 사진 변경 중...");
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        setTitle("마이페이지");
        my_profile_img=findViewById(R.id.my_profile_img);
        nick_name_show=findViewById(R.id.nick_name_show);
        profile_change_btn=findViewById(R.id.profile_change_btn);
        profile_delete_btn=findViewById(R.id.profile_delete_btn);

        profileUpload=getSharedPreferences("profileUpload", Activity.MODE_PRIVATE);
        nickName = profileUpload.getString("nickname","f");

        if(nickName.equals("f")){ //마이페이지는 nickname이 f일리가 없으므로 -> 혹시나 nickname이 f라면 본 activity 끄기
            finish();
        }


    }



    @Override
    protected void onResume() {
        super.onResume();

        //닉네임
        nick_name_show.setText(nickName);
        //프로필
        Glide.with(MyPageActivity.this).
                load("http://35.224.156.8/condition/upload_profile/"+nickName+".jpg")
                .thumbnail(0.1f)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.loading)
                        .centerCrop()
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(my_profile_img);

        //프로필 수정 버튼 눌렀을 때
        profile_change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(MyPageActivity.this)
                        .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new MultiplePermissionsListener() {
                            @Override //permisson checked
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                Log.i("permisson checked","permission");
                                if (report.areAllPermissionsGranted()) {
                                    //권한이 모두 체크되었을 때 -> 오픈되는 페이지 -> 갤러리와 사진 선택 페이지
                                    Log.i("permisson checked","permission all ok");
                                    showImagePickerOptions();
                                }

                                //다시보지 않기로 -> 권한 거부 눌렀을 때
                                if (report.isAnyPermissionPermanentlyDenied()) {

                                    Log.i("permisson checked","permission denied");
                                    showSettingsDialog();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        //나의 정보 삭제 버튼 눌렀을 때
        profile_delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //진짜로 삭제할 것인지 묻는 dialog
                AlertDialog alertDialog=new AlertDialog.Builder(MyPageActivity.this)
                        .setTitle("정말 삭제하시겠습니까?").setMessage(nickName+"님의 프로필과 닉네임이 모두 삭제됩니다.")
                        .setIcon(R.drawable.ic_sentiment_very_dissatisfied_black_24dp).setCancelable(true)
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //진짜 삭제한다고 눌른겁니다요

                                //쉐어드에 저장되어 있는 거 삭제 -> 프로필 사진 삭제,db 삭제까지

                                //delete profile
                                UploadService service = MyRetrofit2.getRetrofit2().create(UploadService.class);

                                //id라는 이름으로 업로드<이거는 UploadService에 이름 정해주었음>
                                RequestBody id = RequestBody.create(
                                        MediaType.parse("multipart/form-data"), nickName);

                                Call<JsonObject> call = service.deleteMyProfile(id);


                                call.enqueue(new Callback<JsonObject>() {
                                    @Override
                                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                                        Log.i("성공!","성공입니다!모든 정보가 삭제되었습니다~");
                                        try {
                                            JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
                                            String deleteProfile=jsonObject.getString("delete");
                                            if(deleteProfile.equals("ok")){ //서버에 저장된 프로필 사진과 db의 정보가 모두 삭제되었음을 의미.

                                                //shared에 저장되어 있던 프로필 정보를 삭제.
                                                SharedPreferences.Editor editor=profileUpload.edit();
                                                editor.clear();
                                                editor.commit();

                                                //mainpage로 이동시킴. -> videolist로.
                                                finish();

                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<JsonObject> call, Throwable t) {
                                        Log.i("fail",t.toString());
                                    }
                                });
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //삭제안하겠다고 눌렀네요.
                            }
                        })
                        .show();


            }
        });


    }






    //permission 영구적 거절 되었을 때 -> 왜 필요한지에 대해 알려주는 문구 넣기!
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            //애플리케이션 정보 페이지 오픈
            openSettings();
        });
        //취소 누르면 -> 다이얼로그 꺼지기
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    // 애플리케이션 정보 페이지 오픈 (영구적 권한 보지 않기 눌렀을 경우)
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    //권한이 모두 ok 설정이 되었을 때 -> 카메라, 갤러리 둘 중 하나 선택할 수 있도록
    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(MyPageActivity.this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            //카메라 선택
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            //갤러리 선택
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    //카메라선택
    private void launchCameraIntent() {
        Intent intent = new Intent(MyPageActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // 원하는 비율 넣기  우선은 1:1 비율로 설정
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // 이미지의 최대 화면 높이, 너비 넣기
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        //이후 크랍한 다음 -> 결과값 가져오기
        startActivityForResult(intent, REQUEST_IMAGE1);
    }

    //갤러리 선택
    private void launchGalleryIntent() {
        Intent intent = new Intent(MyPageActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // 원하는 비율 넣기  우선은 1:1 비율로 설정
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        //이후 크랍한 다음 -> 결과값 가져오기
        startActivityForResult(intent, REQUEST_IMAGE1);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE1) {
            if (resultCode == Activity.RESULT_OK) { //잘 crop되었던 것이 넘어온다
                uri = data.getParcelableExtra("path");
                try {

                    // server에 여기서 uri를 이용하여 올릴 수 있음. -> retrofit

                    //서버에 업로드 시킴
                    UploadService service = MyRetrofit2.getRetrofit2().create(UploadService.class);

                    //file이라는 이름으로 업로드

                    File filePath=new File(uri.toString());
                    File pathPlus = new File("/data/user/0/com.conditoin.umzzal/cache/",filePath.getName());

                    Log.i("filepath",pathPlus+"");
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),pathPlus);
                    MultipartBody.Part body1=MultipartBody.Part.createFormData("file", pathPlus.getName(), requestFile);

                    //id라는 이름으로 업로드<이거는 UploadService에 이름 정해주었음>
                    RequestBody id = RequestBody.create(
                            MediaType.parse("multipart/form-data"), nickName);

                    Call<JsonObject> call = service.reUploadFile(id, body1);


                    loading();

                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            //파일이 업로드에 성공했다면,,,,,shared에 아이디 프로필 사진 경로랑, 유무를 체크해서 저장해 놓는다.

                            Log.i("성공!","성공입니다!프로필사진바꿔줄게요~");


                                try {
                                    JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
                                    String insertProfile=jsonObject.getString("insert");
                                    //로딩끄기
                                    loadingEnd();

                                    Log.i("성공!",insertProfile);
                                    if(insertProfile.equals("ok")){ //db에 제대로 저장한 것을 의미함.
                                        SharedPreferences.Editor profileEditor=profileUpload.edit();
                                        profileEditor.putString("nickname",nickName);
                                        profileEditor.commit();


                                        //새로운애 보여주기
                                        Glide.with(MyPageActivity.this).load("http://35.224.156.8/condition/upload_profile/"+nickName+".jpg")
                                                .apply(new RequestOptions().centerCrop().circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(my_profile_img);
                                    }else{
                                        //정면사진의 얼굴을 재요청하는 문구를 보여줌
                                        Toast.makeText(getApplicationContext(),"한명의 정면사진 얼굴만 등록할 수 있습니다.",Toast.LENGTH_LONG).show();

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }






                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Log.i("fail",t.toString());
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
