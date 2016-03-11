package com.simplifyops.util.puppet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplifyops.util.okhttp.StaticHeaderInterceptor;
import com.simplifyops.util.puppet.classifierapi.*;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by greg on 3/9/16.
 */
public class ClassifierAPI {
    final Retrofit retrofit;

    public ClassifierAPI(final String baseUrl, final String authToken) {
        this.retrofit = new Retrofit.Builder()
                .callFactory(new OkHttpClient.Builder().addInterceptor(
                        new StaticHeaderInterceptor(
                                "X-Authentication",
                                authToken
                        )).build())
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

    }

    public ErrorResponse readError(Response<?> execute) throws IOException {

        Converter<ResponseBody, ErrorResponse> errorConverter = retrofit.responseBodyConverter(
                ErrorResponse.class,
                new Annotation[0]
        );
        return errorConverter.convert(execute.errorBody());
    }

    public ClassifierService getClassifierService() {
        return retrofit.create(ClassifierService.class);
    }

    /**
     * Create update request body for updating group rules
     *
     * @param rules
     *
     * @return
     */
    public static UpdateGroupRules updateGroupRules(List rules) {
        UpdateGroupRules updateGroupRules = new UpdateGroupRules();
        updateGroupRules.setRule(rules);
        return updateGroupRules;
    }

    /**
     * Create update request body for updating group rules
     *
     * @param group        original group with optional rule
     * @param updateRules  new rules
     * @param andOperation use "and" to add the new rules, otherwise use "or"
     *
     * @return
     */
    public static UpdateGroupRules updateGroupRulesMerge(Group group, List updateRules, boolean andOperation) {
        List newrules;
        List originalRule = group.getRule();
        final String op = andOperation ? "and" : "or";
        if (null != originalRule && !originalRule.isEmpty()) {
            if (op.equals(originalRule.get(0))) {
                //add to existing top level operator
                newrules = new ArrayList(Arrays.asList(op));
                newrules.addAll(originalRule.subList(1, originalRule.size()));
                newrules.addAll(updateRules);
            } else {
                newrules = Arrays.asList(
                        op,
                        originalRule,
                        updateRules
                );
            }
        } else {
            newrules = updateRules;
        }
        UpdateGroupRules updateGroupRules = new UpdateGroupRules();
        updateGroupRules.setRule(newrules);
        return updateGroupRules;
    }
}
