package com.simplifyops.util.puppet.classifierapi;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 3/9/16.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:5050")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        ClassifierService service = retrofit.create(ClassifierService.class);
        Call<List<Group>> listCall = service.listGroups();
        Response<List<Group>> execute = listCall.execute();
        List<Group> body = execute.body();
        System.out.println("body: " + body);
        System.out.println("getid: " + service.getGroup("fc500c43-5065-469b-91fc-37ed0e500e81").execute().body());

    }
}
