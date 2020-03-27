package com.tech.quarantinetracking;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.tech.quarantinetracking.webservice.APICallback;
import com.tech.quarantinetracking.webservice.ApiInterface;
import com.tech.quarantinetracking.webservice.NetworkClient;
import com.tech.quarantinetracking.webservice.UserProfileRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class GoogleLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, AdapterView.OnItemSelectedListener, APICallback {
    Location mLocation;
    TextView latLng;
    GoogleApiClient mGoogleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private EditText name;
    private EditText dob;
    private EditText phoneNo;
    private EditText emergencyNo;
    private EditText locality;
    private EditText localPoliceStation;
    private Button registerButton;
    private ImageView userPhoto;
    private Calendar myCalendar;
    private int PICK_IMAGE_REQUEST = 1;
    private Uri filePath;
    private String genderText;
    private String latitude;
    private String longitude;
    private static int REQUEST_CODE_PROFILE = 101;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private String Document_img1 = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_location_activity);
        initializeViews();
        genderSetup();
        dobSetup();
        latLng = findViewById(R.id.latLng);
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);
        myCalendar = Calendar.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        userPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                editImage();
                selectImage();
            }
        });
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filePath != null) {

                        uploadUserData(filePath);

                }
            }
        });
    }

    private void dobSetup() {
        final Calendar myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy"; //In which you need put here
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                dob.setText(sdf.format(myCalendar.getTime()));
            }
        };

        dob.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(GoogleLocationActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else
                finish();

            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPlayServices()) {
            latLng.setText("Please install Google Play services.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @SuppressLint("RestrictedApi")
    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        long UPDATE_INTERVAL = 900000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        long FASTEST_INTERVAL = 900000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null)
            latLng.setText("Latitude : " + location.getLatitude() + " , Longitude : " + location.getLongitude());
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation != null) {
            latLng.setText("Latitude : " + mLocation.getLatitude() + " , Longitude : " + mLocation.getLongitude());
            latitude = String.valueOf(mLocation.getLatitude());
            longitude = String.valueOf(mLocation.getLongitude());
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(GoogleLocationActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        genderText = parent.getItemAtPosition(position).toString();
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + genderText, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            Log.d("sos", "onActivityResult: " + filePath);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                userPhoto.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    private void genderSetup() {
        Spinner spinner = findViewById(R.id.gender_spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> gender = new ArrayList<String>();
        gender.add("Select");
        gender.add("Male");
        gender.add("Female");
        gender.add("Others");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, gender);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void initializeViews() {
        userPhoto = findViewById(R.id.img_profile_photo);
        name = findViewById(R.id.edt_name);
        dob = findViewById(R.id.edt_dob);
        phoneNo = findViewById(R.id.edt_phone);
        emergencyNo = findViewById(R.id.edt_em_number);
        locality = findViewById(R.id.edt_locality);
        localPoliceStation = findViewById(R.id.edt_local_police_station);
        registerButton = findViewById(R.id.btn_register);

    }


    private HashMap<String, String> getUserDetailsMap() {
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("name", name.getText().toString().trim());
        userMap.put("mobile", phoneNo.getText().toString().trim());
        userMap.put("em_contact", emergencyNo.getText().toString().trim());
        userMap.put("dob", dob.getText().toString().trim());
        userMap.put("gender", genderText.trim());
        userMap.put("locality", locality.getText().toString().trim());
        userMap.put("local_police_station", localPoliceStation.getText().toString().trim());
        userMap.put("lat", latitude.trim());
        userMap.put("lon", longitude.trim());
        userMap.put("deviceid", getDeviceId().trim());
        return userMap;
    }


/*    private void editImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }*/

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                Uri selectedImage = data.getData();
                filePath = data.getData();

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                //Get the column index of MediaStore.Images.Media.DATA
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                //Gets the String value in the column
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                // Set the Image in ImageView after decoding the String
                userPhoto.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));


            }
        }
    }




    public String getDeviceId() {
        return UUID.randomUUID().toString();
    }

    public void uploadUserData(Uri uri)  {
        String nameText = name.getText().toString().trim();
        String phoneText = phoneNo.getText().toString().trim();
        String emergencyContactText = emergencyNo.getText().toString().trim();
        String dobText = dob.getText().toString().trim();
        String gender = genderText.trim();
        String localityText = locality.getText().toString().trim();
        String localPoliceStationText = localPoliceStation.getText().toString().trim();
        String latitudeText = latitude.trim();
        String longitudeText = longitude.trim();
        String deviceId = getDeviceId().trim();

        //ApiClient apiClient = new ApiClient();
        UserProfileRequest request = new UserProfileRequest(nameText, phoneText, emergencyContactText, dobText,
                gender, localityText, localPoliceStationText, latitudeText, longitudeText, deviceId);
        //apiClient.uploadUserDetails(uri, request, this);

//        uploadUserDetails(uri, request, this);
        uploadToServer(getPath(uri),request);

    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        Log.d("sos", "getPath: " +cursor.getString(column_index));
        return cursor.getString(column_index);
    }
    @Override
    public void onSuccess(int requestCode, Object obj, int code) {
        Log.d("sos", "onSuccess: " + requestCode);
    }

    @Override
    public void onFailure(int requestCode, Object obj, int code) {
        Log.d("sos", "onFailure: " + requestCode);

    }

    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(MyApplication.getAppContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();


        return result;
    }

/*
    public void uploadUserDetails(Uri fileUri, UserProfileRequest userRequest, final APICallback callback) throws URISyntaxException {
        Log.d("sos", "fileUri: " + fileUri);
        String path = fileUri.getPath(); // "file:///mnt/sdcard/FileName.mp3"
        File file = new File(fileUri.toString());
        Log.d("sos", "file: " + file);


//        File file = new File(getRealPathFromURI(fileUri));
//        File file1 = new File(getPathFromURI(GoogleLocationActivity.this, fileUri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("profile_image", file.getName(), requestFile);

        RequestBody descBody = RequestBody.create(MediaType.parse("form-data"), userRequest);
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<UserProfileResponse> call = apiInterface.uploadUserDetails(part,userRequest);
        Log.d("sos", "call request: " + call.request().url());
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
                Toast.makeText(GoogleLocationActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("sos", "onFailure: " + t.getMessage());
            }
        });
    }
*/


    private void uploadToServer(String filePath,UserProfileRequest userRequest) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        ApiInterface uploadAPIs = retrofit.create(ApiInterface.class);
        File file = new File(filePath);
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("profile_img", file.getName(), fileReqBody);
        RequestBody name = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getName());
        RequestBody mobile = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getPhoneNumber());
        RequestBody em_contact = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getEmergencyPhoneNumber());
        RequestBody dob = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getDob());
        RequestBody gender = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getGender());
        RequestBody locality =  RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getLocality());
        RequestBody local_police_station = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getLocalPoliceStation());
        RequestBody lat = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getLat());
        RequestBody lon = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getLon());
        RequestBody deviceid = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), userRequest.getDeviceId());

//        RequestBody descBody = RequestBody.create(MediaType.parse("form-data"), userRequest);

        //RequestBody description = RequestBody.create(MediaType.parse("text/plain"), "image-type");
        //
        Call call = uploadAPIs.uploadImage(name,mobile,em_contact,dob,gender,locality,local_police_station,lat,lon,deviceid);
        Log.d("sos", "name: " +name);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d("sos", "onResponse: "+response.body()+"msg: "+response.message());
            }
            @Override
            public void onFailure(Call call, Throwable t) {
                Log.d("sos", "onFailure: "+ t.getMessage());
            }
        });
    }

}