package com.simplifyops.util.puppet.classifierapi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ClassifierService {
    @GET("classifier-api/v1/groups")
    Call<List<Group>> listGroups();

    @GET("classifier-api/v1/groups/{id}")
    Call<Group> getGroup(@Path("id") String id);
}