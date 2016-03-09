package com.simplifyops.util.puppet;

import com.simplifyops.rundeck.plugin.puppet.StaticHeaderInterceptor;
import com.simplifyops.util.puppet.classifierapi.ClassifierService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by greg on 3/9/16.
 */
public class ClassifierAPI {
    public static ClassifierService getClassifierService(final String baseUrl, final String authToken) {
        Retrofit retrofit = new Retrofit.Builder()
                .callFactory(new OkHttpClient.Builder().addInterceptor(
                        new StaticHeaderInterceptor(
                                "X-Authentication",
                                authToken
                        )).build())
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return retrofit.create(ClassifierService.class);
    }
}
