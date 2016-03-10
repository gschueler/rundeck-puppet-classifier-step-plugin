package com.simplifyops.util.puppet.classifierapi;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ClassifierService {
    @GET("classifier-api/v1/groups")
    Call<List<Group>> listGroups();

    @GET("classifier-api/v1/groups/{id}")
    Call<Group> getGroup(@Path("id") String id);

    @POST("classifier-api/v1/groups/{id}")
    Call<Group> updateGroup(@Path("id") String id, @Body UpdateGroup updates);

    @POST("classifier-api/v1/groups/{id}")
    Call<Group> updateGroup(@Path("id") String id, @Body UpdateGroupRules updates);


    @GET("classifier-api/v1/classes")
    Call<List<ClassifierClass>> listClasses();

    @GET("classifier-api/v1/environment/{env}/classes")
    Call<List<ClassifierClass>> listClassesInEnvironment(@Path("env") String environment);

    @GET("classifier-api/v1/environment/{env}/classes/{class}")
    Call<ClassifierClass> getClass(@Path("class") String className);

    @POST("classifier-api/v1/classified/nodes/{name}")
    Call<Classification> getClassification(@Path("name") String nodeName, @Body NodeClassificationFacts node);


    @GET("classifier-api/v1/environments")
    Call<List<Environment>> listEnvironments();

    @GET("classifier-api/v1/environments/{name}")
    Call<Environment> getEnvironment(@Path("name") String environment);

    @PUT("classifier-api/v1/environments/{name}")
    Call<Environment> createEnvironment(@Path("name") String environment);

    @POST("classifier-api/v1/update-classes")
    void updateClasses();
}