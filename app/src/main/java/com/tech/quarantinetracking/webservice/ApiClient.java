package com.tech.quarantinetracking.webservice;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.content.CursorLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tech.quarantinetracking.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "http://192.168.0.102:5000/api/v1/";
    public static final String LOGIN_BASE_URL = "https://apscadvdgsapm01.azure-api.net/UserDetails/v1.3/";
    private static int REQUEST_CODE_PROFILE = 101;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;


    public static Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(HttpClientService.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    //    user?????
    public void setProfile(User user, final APICallback callback) {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        UserProfileRequest userProfileRequest = new UserProfileRequest(user.getName(),
                user.getMobile(),
                user.getEmergencyContact(),
                user.getDob(),
                user.getGender(),
                user.getLocality(),
                user.getLocal_police_station(),
                user.getLat(),
                user.getLon(),
                user.getDeviceId());
        Call<UserProfileResponse> call = apiService.uploadProfile(userProfileRequest);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                UserProfileResponse userProfileResponse = response.body();
                if (userProfileResponse != null) {
                    if (userProfileResponse.isSuccess()) {
                        callback.onSuccess(REQUEST_CODE_PROFILE, userProfileResponse, response.code());
                    } else {
                        userProfileResponse.setMessage("Failed to send profile");
                        callback.onFailure(REQUEST_CODE_PROFILE, userProfileResponse, response.code());
                    }
                } else {
                    userProfileResponse = new UserProfileResponse();
                    userProfileResponse.setMessage("Failed to send profile!");
                    callback.onFailure(REQUEST_CODE_PROFILE, userProfileResponse, response.code());
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                UserProfileResponse loginResponse = new UserProfileResponse();
                loginResponse.setMessage("Failed to send profile!");
                callback.onFailure(REQUEST_CODE_PROFILE, loginResponse, 0);
            }
        });
    }

    /*public void uploadUserDetails(Uri fileUri,UserProfileRequest userRequest, final APICallback callback){

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", userRequest.getName())
                .addFormDataPart("mobile", userRequest.getPhoneNumber())
                .build();
        //File file = new File(getRealPathFromURI(fileUri));
        File file1 = new File(getPathFromURI(MyApplication.getAppContext(),fileUri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
        //RequestBody descBody = RequestBody.create(MediaType.parse("form-data"), userRequest);
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<UserProfileResponse> call = apiInterface.uploadUserDetails(requestFile);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NotNull Call<UserProfileResponse> call, @NotNull Response<UserProfileResponse> response) {
                UserProfileResponse userProfileResponse = response.body();
                if (userProfileResponse != null) {
                    if (userProfileResponse.isSuccess()) {
                        callback.onSuccess(REQUEST_CODE_PROFILE, userProfileResponse, response.code());
                    } else {
                        userProfileResponse.setMessage("Failed to login");
                        callback.onFailure(REQUEST_CODE_PROFILE, userProfileResponse, response.code());
                    }
                } else {
                    userProfileResponse = new UserProfileResponse();
                    userProfileResponse.setMessage("Failed to login!");
                    callback.onFailure(REQUEST_CODE_PROFILE, userProfileResponse, response.code());
                }
            }

            @Override
            public void onFailure(@NotNull Call<UserProfileResponse> call, @NotNull Throwable t) {
                Toast.makeText(MyApplication.getAppContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }*/
}
