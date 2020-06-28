package com.condition.umzzal.retrofit;


import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadService {

    //프로필 이미지, 아이디랑 서버에 전송
    @Multipart
    @POST("/upload_file")
    Call<JsonObject> uploadFile(
            @Part("id") RequestBody id,
            @Part MultipartBody.Part file);


    //프로필 이미지재등록, 아이디랑 서버에 전송
    @Multipart
    @POST("/reupload_file")
    Call<JsonObject> reUploadFile(
            @Part("id") RequestBody id,
            @Part MultipartBody.Part file);

//
//    //아이디, 파일번호(영상번호), 파일
    @Multipart
    @POST("/voiceupload")
    Call<ResponseBody> uploadVoice(
            @Part MultipartBody.Part file);

    //    //아이디, 파일번호(영상번호), 파일
    @Multipart
    @POST("/test_upload")
    Call<ResponseBody> test_upload(
            @Part MultipartBody.Part file);


    //닉네임 중복 체크
    @Multipart
    @POST("/condition/php/nick_check.php")
    Call<JsonObject> nick_check(
            @Part("id") RequestBody id);


    //내정보삭제버튼을 눌렀을 때 -> db, profile file 삭제
    @Multipart
    @POST("/delete_profile")
    Call<JsonObject> deleteMyProfile(
            @Part("id") RequestBody id);


}
