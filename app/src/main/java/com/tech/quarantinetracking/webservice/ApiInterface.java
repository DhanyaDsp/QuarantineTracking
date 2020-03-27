package com.tech.quarantinetracking.webservice;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiInterface {
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("Authenticate")
    Call<UserProfileResponse> uploadProfile(@Body UserProfileRequest userProfileRequest);

    @Multipart
    @POST("person_info")
    Call<UserProfileResponse> uploadUserDetails(@Part MultipartBody.Part file,
                                                @Part("name") RequestBody name,
                                                @Part("mobile") RequestBody mobile,
                                                @Part("em_contact") RequestBody emContact,
                                                @Part("dob") RequestBody dob,
                                                @Part("gender") RequestBody gender,
                                                @Part("locality") RequestBody locality,
                                                @Part("local_police_station") RequestBody localPoliceStation,
                                                @Part("lat") RequestBody latitude,
                                                @Part("lon") RequestBody longitude,
                                                @Part("deviceid") RequestBody deviceId);


//                                                @Part UserProfileRequest profileRequest);
//    Call<UserProfileResponse> uploadUserDetails(@Part("profile_img") MultipartBody.Part  file);
//
//                                                @Part UserProfileRequest profileRequest);
                        /*@Part("name") RequestBody name,
                        @Part("mobile") RequestBody mobile,
                        @Part("em_contact") RequestBody emContact,
                        @Part("dob") RequestBody dob,
                        @Part("gender") RequestBody gender,
                        @Part("locality") RequestBody locality,
                        @Part("local_police_station") RequestBody localPoliceStation,
                        @Part("lat") RequestBody latitude,
                        @Part("lon") RequestBody longitude,
                        @Part("deviceid") RequestBody deviceId);*/

    @Multipart
    @POST("person_info")
    Call<ResponseBody> uploadImage(
//            @Part MultipartBody.Part file,
                                   @Part("name") RequestBody name,
                                   @Part("mobile") RequestBody mobile,
                                   @Part("em_contact") RequestBody emContact,
                                   @Part("dob") RequestBody dob,
                                   @Part("gender") RequestBody gender,
                                   @Part("locality") RequestBody locality,
                                   @Part("local_police_station") RequestBody localPoliceStation,
                                   @Part("lat") RequestBody latitude,
                                   @Part("lon") RequestBody longitude,
                                   @Part("deviceid") RequestBody deviceId);

}
