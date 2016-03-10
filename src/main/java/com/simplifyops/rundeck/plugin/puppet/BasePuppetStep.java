package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepFailureReason;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import com.dtolabs.rundeck.core.storage.StorageTree;
import com.dtolabs.rundeck.plugins.descriptions.Password;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption;
import com.dtolabs.rundeck.plugins.descriptions.RenderingOptions;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.simplifyops.util.puppet.ClassifierAPI;
import com.simplifyops.util.puppet.classifierapi.ClassifierService;
import org.rundeck.storage.api.Resource;
import retrofit2.Call;
import retrofit2.Response;

import java.io.*;

/**
 * Created by greg on 3/9/16.
 */
public abstract class BasePuppetStep {
    public static final String PUPPET_CLASSIFIER_BASEURL_PROPERTY = "puppet.classifier-api.baseUrl";
    public static final String PUPPET_CLASSIFIER_TOKEN_PROPERTY = "puppet.classifier-api.authToken";

    @PluginProperty(title = "API Base URL",
                    description = "Puppet Classifier API base URL",
                    scope = PropertyScope.Project)
    String baseUrl;
    @PluginProperty(title = "Auth Token",
                    description = "Puppet Classifier API auth token",
                    scope = PropertyScope.Project)
    @Password
    String authToken;
    @PluginProperty(title = "Auth Token File Path",
                    description = "File path for the Puppet Classifier API auth token",
                    scope = PropertyScope.Project)
    String authTokenFilepath;
    @PluginProperty(title = "Auth Token Storage Path",
                    description = "Key Storage Path for the Puppet Classifier API auth token",
                    scope = PropertyScope.Project)
    @RenderingOptions(
            {
                    @RenderingOption(key = StringRenderingConstants.SELECTION_ACCESSOR_KEY,
                                     value = "STORAGE_PATH"),
                    @RenderingOption(key = StringRenderingConstants.STORAGE_PATH_ROOT_KEY,
                                     value = "keys"),
                    @RenderingOption(key = StringRenderingConstants.STORAGE_FILE_META_FILTER_KEY,
                                     value = "Rundeck-data-type=password")
            }
    )
    String authTokenStoragePath;

    protected ClassifierService getClassifierService(final PluginStepContext context) throws StepException {
        return ClassifierAPI.getClassifierService(baseUrl, resolveAuthToken(context));
    }

    protected String resolveAuthToken(final PluginStepContext context) throws StepException {
        if (null != authToken) {
            return authToken;
        } else if (null != authTokenFilepath) {
            //try to read file
            try {
                try (FileInputStream in = new FileInputStream(new File(authTokenFilepath))) {
                    BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(in));
                    return inputStreamReader.readLine();
                }
            } catch (IOException e) {
                throw new StepException(
                        String.format("Unable to read authTokenFilepath file %s ", authTokenFilepath) +
                        e.getLocalizedMessage(),
                        StepFailureReason.ConfigurationFailure
                );
            }
        } else if (null != authTokenStoragePath) {
            //try to read from key storage
            StorageTree storageTree = context.getExecutionContext().getStorageTree();
            try {
                Resource<ResourceMeta> resource = storageTree.getResource(authTokenStoragePath);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resource.getContents().writeContent(byteArrayOutputStream);
                return new String(byteArrayOutputStream.toByteArray());
            } catch (StorageException | IOException | IllegalArgumentException e) {
                throw new StepException(
                        String.format("Unable to read authTokenStoragePath %s ", authTokenStoragePath) +
                        e.getLocalizedMessage(),
                        StepFailureReason.ConfigurationFailure
                );
            }
        } else {
            throw new StepException("authToken is required", StepFailureReason.ConfigurationFailure);
        }
    }

    <T> T performCall(final Call<T> call, final String name) throws StepException {
        Response<T> execute = null;
        try {
            execute = call.execute();
            if (!execute.isSuccess()) {
                String responseBody = execute.errorBody().string();
                throw new StepException(
                        String.format(
                                "%s was not successful: %s: %s",
                                name,
                                execute.message(),
                                responseBody
                        ),
                        ApiReason.HTTP_ERROR
                );
            }
        } catch (IOException e) {
            throw new StepException(
                    String.format(
                            "IO error making request for %s: %s",
                            name,
                            e.getLocalizedMessage()
                    ),
                    StepFailureReason.IOFailure
            );
        }
        return execute.body();
    }

    void validate() throws StepException {
        requireValue(baseUrl, "baseUrl");
        if (null == authToken && null == authTokenFilepath && null == authTokenStoragePath) {
            throw new StepException(
                    "Configuration invalid: one of [ authToken, authtokenFilepath, authTokenStoragePath ] must be set."
                    //+                    " Define in project.properties as plugin.x.y.authToken",
                    ,
                    StepFailureReason.ConfigurationFailure
            );
        }
    }

    void requireValue(final String value, final String name) throws StepException {
        if (null == value) {
            throw new StepException(
                    "Configuration invalid: " + name + " is required",
                    StepFailureReason.ConfigurationFailure
            );
        }
    }

    enum ApiReason implements FailureReason {
        NOT_FOUND,
        HTTP_ERROR,
        UNKNOWN,
    }

    public void buildWith(final DescriptionBuilder builder) {
        builder.mapping("baseUrl", PUPPET_CLASSIFIER_BASEURL_PROPERTY);
        builder.frameworkMapping("baseUrl", PUPPET_CLASSIFIER_BASEURL_PROPERTY);
        builder.mapping("authToken", PUPPET_CLASSIFIER_TOKEN_PROPERTY);
        builder.frameworkMapping("authToken", PUPPET_CLASSIFIER_TOKEN_PROPERTY);
    }
}
