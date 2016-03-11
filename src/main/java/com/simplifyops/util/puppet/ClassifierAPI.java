package com.simplifyops.util.puppet;

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
     * @return
     */
    public static UpdateGroupRules updateGroupRulesMerge(Group group, List updateRules) {
        List newrules;
        List originalRule = group.getRule();
        final String op = "or";
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
            newrules = new ArrayList(Arrays.asList(op));
            newrules.addAll(updateRules);
        }
        UpdateGroupRules updateGroupRules = new UpdateGroupRules();
        updateGroupRules.setRule(newrules);
        return updateGroupRules;
    }

    /**
     * Create update request body for updating group rules
     *
     * @param group original group with optional rule
     * @param nodes nodes to remove
     *
     * @return
     */
    public static UpdateGroupRules updateGroupRulesRemoveNodes(Group group, List<String> nodes) {
        List newrules = new ArrayList();
        List originalRule = group.getRule();
        final String op = "or";
        boolean changed = false;
        if (null != originalRule && !originalRule.isEmpty()) {
            if (op.equals(originalRule.get(0))) {
                //iterate through rules... if they match [=,name,<node>], don' add it
                newrules.add(op);
                for (Object o : originalRule.subList(1, originalRule.size())) {
                    if (o instanceof List) {
                        List orule = (List) o;
                        if (orule.size() == 3) {
                            if (
                                    "=".equals(orule.get(0))
                                    && "name".equals(orule.get(1))
                                    && nodes.contains(orule.get(2))
                                    ) {
                                //remove this rule by skipping
                                changed = true;
                                continue;
                            }
                        }
                    }
                    //if not a match, keep the rule
                    newrules.add(o);
                }
            }
        }
        if (!changed) {
            return null;
        }
        if (newrules.size() == 1 && "or".equals(newrules.get(0))) {
            newrules.clear();//empty
        }
        UpdateGroupRules updateGroupRules = new UpdateGroupRules();
        updateGroupRules.setRule(newrules);
        return updateGroupRules;
    }
}
