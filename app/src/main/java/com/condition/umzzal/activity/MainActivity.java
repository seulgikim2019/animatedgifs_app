package com.condition.umzzal.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.condition.umzzal.R;
import com.condition.umzzal.object.Video;
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
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    //프로필 사진 버튼
    Button profile_btn;
    //프로필 이미지
    ImageView profile_img;
    public static final int REQUEST_IMAGE = 100;

    //닉네임
    EditText nick_name;
    //닉네임중복확인버튼
    Button nick_btn;
    //프로필등록완료버튼
    Button profile_ok_btn;

    //사용중인 닉네임인 경우 -> 중복체크때
    TextView no_nickname;

    //프로필 업로드 할 때 -> 아이디, 중복체크
    SharedPreferences profileUpload;
    SharedPreferences.Editor profileEditor;

    //이미지 업로드 시도한 부분 체크
    String img_upload_try="once";
    Uri uri=null;

    String video_json; // 비디오 리스트를 클릭해서 들어온 경우 비디오 정보가 json 형태로 저장됨
    Video video; // 비디오 리스트를 클릭해서 들어온 경우 비디오 정보가 이 비디오 객체에 저장됨
    String nickName;




    // 프로그래스 다이얼로그
    ProgressDialog progressDialog;


    /**
     * 로딩 메소드
     */
    public void loading() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setIndeterminate(true);
                        //진행중에는 cancel 못하게
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage(".. 프로필 사진 업로드 중 ..");
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
        setContentView(R.layout.activity_main);

        setTitle("프로필 등록");
        Toolbar toolbar=findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        video_json = intent.getStringExtra("video_json"); // 사용자가 선택한 비디오 json 형태
        Gson gson = new Gson();
        video = gson.fromJson(video_json, Video.class); // 사용자가 선택한 비디오

        profileUpload=getSharedPreferences("profileUpload", Activity.MODE_PRIVATE);

        profileEditor=profileUpload.edit();
        profileEditor.putString("nickname_check","f");
        profileEditor.putString("nickname_test","");
        profileEditor.commit();
        nickName = profileUpload.getString("nickname","f");

        //프로필이미지
        profile_img=findViewById(R.id.profile_img);
        //프로필사진등록버튼
        profile_btn=findViewById(R.id.profile_btn);
        //닉네임
        nick_name=findViewById(R.id.nick_name);

        //한글, 영어, 숫자만 입력 가능 + 1~9자
        nick_name.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {


                Pattern pattern=Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                if(source.equals("")||pattern.matcher(source).matches()){

                    return source;
                }

                Toast.makeText(getApplicationContext(),"특수문자는 입력할 수 없습니다.",Toast.LENGTH_LONG).show();

                return "";

            }
        },new InputFilter.LengthFilter(9)});



        //닉네임중복확인버튼
        nick_btn=findViewById(R.id.nick_btn);
        //프로필등록완료버튼
        profile_ok_btn=findViewById(R.id.profile_ok_btn);

        //사용중인 닉네임 문구
        no_nickname=findViewById(R.id.no_nickname);

        loadProfileDefault();

    } // onCreate()

    //프로필사진 -> default
    private void loadProfileDefault() {
        Glide.with(this).load(R.mipmap.profle).apply(new RequestOptions().centerCrop().circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(profile_img);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("profile pic","resume");






        //닉네임 쓰일 때 -> 중복확인 체크를 할 것이므로 -> 중복체크하고도 닉네임을 쓰게 될 경우 문제가 생기므로 -> 새로운 변화가 감지될 때 -> 기존 중복체크 무효화
        nick_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //입력되기전 -> 딱히 지금 필요한 기능 없음.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            //입력되는 텍스트에 변화가 있을 때 ->  중복체크 무효화하였음.
                Log.i("변하고있는 text",s.toString());

                if(0 <= nick_name.getText().toString().length() &&nick_name.getText().toString().length()<2){
                    //닉네임 글자수가 2보다 작을때
                    no_nickname.setText("!!!!닉네임은 2글자 이상 입력해주세요!!!!");
                    no_nickname.setVisibility(View.VISIBLE);
                }else{
                    no_nickname.setText("!!!!닉네임 중복 여부 버튼을 눌러주세요!!!!");
                    no_nickname.setVisibility(View.VISIBLE);
                }

                profileEditor.putString("nickname_check","f");
                profileEditor.putString("nickname_test",nick_name.getText().toString());
                profileEditor.commit();

            }

            @Override
            public void afterTextChanged(Editable s) {




            }
        });


        //닉네임 중복확인 버튼 클릭할 때
        nick_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                //특수문자랑 띄어쓰기 확인해서 -> 디테일 조금 나중에 잡기

                String nickName=nick_name.getText().toString();

                if(0 <= nickName.length() && nickName.length()<2){
                    //닉네임 글자수가 2보다 작을때
                    no_nickname.setText("!!!!닉네임은 2글자 이상 입력해주세요!!!!");
                }else{ //입력을 했을 때
                     //서버에서 확인해와야함.
                    //서버에 업로드 시킴
                    UploadService service = MyRetrofit2_1.getRetrofit2().create(UploadService.class);

                    //id라는 이름으로 업로드<이거는 UploadService에 이름 정해주었음>
                    RequestBody id = RequestBody.create(
                            MediaType.parse("multipart/form-data"), nickName);

                    Call<JsonObject> call = service.nick_check(id);

                    call.enqueue(new Callback<JsonObject>() {
                        @Override //jsonObject 중복값 체크하게
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                            try {
                                JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
                                String id=jsonObject.getString("id");

                                if(id.equals("old")){ //기존에 등록된 닉네임이 존재하는 것임
                                    //사용중인 닉네임 문구 보이게
                                    no_nickname.setText("!!!!이미 사용중인 닉네임입니다!!!!");
                                    no_nickname.setVisibility(View.VISIBLE);
                                }else{ //닉네임 사용가능

                                    if(nick_name.getText().toString().equals("f")){
                                        no_nickname.setText("!!!!이미 사용중인 닉네임입니다!!!!");
                                        no_nickname.setVisibility(View.VISIBLE);
                                    }else{
                                        //사용가능한 닉네임 문구 보이게
                                        no_nickname.setText("!!!!사용가능한 닉네임입니다!!!!");
                                        no_nickname.setVisibility(View.VISIBLE);
                                        //중복체크 완료 쉐어드 저장
                                        profileEditor=profileUpload.edit();
                                        profileEditor.putString("nickname_check","t");
                                        profileEditor.putString("nickname_test",nick_name.getText().toString());
                                        profileEditor.commit();
                                    }

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





            }
        });


        //프로필 등록 완료 버튼 클릭
        profile_ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //닉네임 중복,프로필 사진이 제대로 등록되었는지 여부 체크
                //1.닉네임 중복 체크가 되었는지 확인 -> 안되어 있으면 -> 닉네임 중복체크 요구 문구 보여주기

                String nick_check=profileUpload.getString("nickname_check","f");

                if(nick_check.equals("f")){  //닉네임 체크가 안된것을 의미함.(중복된것이든, 체크를안했던 것이든)

                    if(0 <= nick_name.getText().length() && nick_name.getText().length()<2){
                        //닉네임 글자수가 2보다 작을때
                        no_nickname.setText("!!!!닉네임은 2글자 이상 입력해주세요!!!!");
                        no_nickname.setVisibility(View.VISIBLE);

                        String message="닉네임 2글자 이상 입력해주세요.";
                        if(img_upload_try.equals("once")){//프로필사진 등록이 안되어 있는 경우
                            message="닉네임과 프로필 사진을 등록해주세요.";
                        }
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

                    }else{
                        no_nickname.setText("!!!!닉네임 중복 여부를 확인해주세요!!!!");
                        no_nickname.setVisibility(View.VISIBLE);
                        String message="닉네임 중복 여부를 확인해주세요.";
                        if(img_upload_try.equals("once")){//프로필사진 등록이 안되어 있는 경우
                            message="닉네임 중복 여부/프로필 사진 등록이 요구됩니다.";
                        }
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

                    }


                }else{ //중복체크는 통과 된 것. 그러면 사진....

                    //이미지 유무 체크

                    //2.체크가 잘 되어 있다면 .. 프로필 사진 -> 프로필 사진을 가지고 가서 정면여부 체크받기



                    if(!img_upload_try.equals("once")){ //이미지 업로드 시도 -> 우선 이미지 값 가지고 넘어가 보기
                        String nickname=profileUpload.getString("nickname_test","f");

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
                                MediaType.parse("multipart/form-data"), nickname);

                        Call<JsonObject> call = service.uploadFile(id, body1);

                        //로딩문구
                        loading();

                        call.enqueue(new Callback<JsonObject>() {
                            @Override
                            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                                Log.i("success",response.toString());
                                //파일이 업로드에 성공했다면,,,,,shared에 아이디 프로필 사진 경로랑, 유무를 체크해서 저장해 놓는다.

                                try {
                                    JSONObject jsonObject = new JSONObject(new Gson().toJson(response.body()));
                                    String insertProfile=jsonObject.getString("insert");


                                    //로딩 끄기
                                    loadingEnd();

                                    if(insertProfile.equals("ok")){ //db에 제대로 저장한 것을 의미함.
                                        profileEditor=profileUpload.edit();
                                        profileEditor.putString("nickname",nick_name.getText().toString());
                                        profileEditor.commit();

                                        //현재 넘어온 activity를 finish 한다.
//                                        finish();

                                        Intent intent = new Intent(getApplicationContext(),VideoDetailActivity.class);
                                        intent.putExtra("video_json",video_json);
                                        startActivity(intent);
                                        finish();

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
                    }else{
                        Toast.makeText(getApplicationContext(),"프로필 사진을 등록해주세요.",Toast.LENGTH_LONG).show();
                    }


                }





            }
        });


        //프로필 사진 등록 버튼 클릭이벤트
        profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Log.i("프로필등록하고 싶어요!","click");
                    //Dexter -> 권한 확인 받는 것. 지금 2개(사진, 외부저장소)
                Dexter.withActivity(MainActivity.this)
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
    }

    //permission 영구적 거절 되었을 때 -> 왜 필요한지에 대해 알려주는 문구 넣기!
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
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
        Intent intent = new Intent(MainActivity.this, ImagePickerActivity.class);
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
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    //갤러리 선택
    private void launchGalleryIntent() {
        Intent intent = new Intent(MainActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // 원하는 비율 넣기  우선은 1:1 비율로 설정
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        //이후 크랍한 다음 -> 결과값 가져오기
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) { //잘 crop되었던 것이 넘어온다
                uri = data.getParcelableExtra("path");
                try {
                   //사진 보여주기
                   loadProfile(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadProfile(Uri uri) {
        Log.i("profile 업로드됩니다 : ", uri+"");

        img_upload_try=uri.toString();
//        File filePath=new File(img_upload_try);
//        File pathPlus = new File("/data/data/com.example.profile/cache",filePath.getName());


     //   Log.i("profile 업로드됩니다2 : ", pathPlus.toString());
        Glide.with(this).load(img_upload_try).apply(new RequestOptions().centerCrop().circleCrop().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)).into(profile_img);
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i("profile pic","pause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("profile pic","restart");
    }



}
