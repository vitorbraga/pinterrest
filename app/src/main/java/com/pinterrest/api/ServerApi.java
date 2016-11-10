package com.pinterrest.api;

import com.pinterrest.models.Post;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;

public interface ServerApi {

    @GET("/raw/wgkJgazE")
    void getData(@Header("Content-Type") String contentType, Callback<ArrayList<Post>> response);

}
