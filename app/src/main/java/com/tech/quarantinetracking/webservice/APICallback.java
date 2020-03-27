package com.tech.quarantinetracking.webservice;

public interface APICallback {
     void onSuccess(int requestCode, Object obj, int code);
     void onFailure(int requestCode, Object obj, int code);
}
