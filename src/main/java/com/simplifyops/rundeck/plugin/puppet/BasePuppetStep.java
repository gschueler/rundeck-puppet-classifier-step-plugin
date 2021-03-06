package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.common.IRundeckProject;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.execution.utils.ResolverUtil;
import com.dtolabs.rundeck.core.execution.workflow.steps.FailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepFailureReason;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
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
import com.simplifyops.util.puppet.classifierapi.ErrorResponse;
import org.rundeck.storage.api.Resource;
import org.rundeck.storage.api.StorageException;
import retrofit2.Call;
import retrofit2.Response;

import java.io.*;

/**
 * Created by greg on 3/9/16.
 */
public abstract class BasePuppetStep implements DescriptionBuilder.Collaborator {
    public static final String PUPPET_CLASSIFIER_BASEURL_PROPERTY = "puppet.classifier-api.baseUrl";
    public static final String PUPPET_CLASSIFIER_TOKEN_PROPERTY = "puppet.classifier-api.authToken";
    public static final String PUPPET_CLASSIFIER_TOKEN_FILEPATH_PROPERTY = "puppet.classifier-api.authTokenFilepath";
    public static final String PUPPET_CLASSIFIER_TOKEN_STORAGE_PATH_PROPERTY = "puppet.classifier-api" +
                                                                               ".authTokenStoragePath";

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

    private ClassifierAPI api;

    protected ClassifierService getClassifierService(final PluginStepContext context) throws StepException {
        String authToken = null;
        try {
            authToken = resolveAuthToken(
                    context.getExecutionContext().getStorageTree(),
                    this.authToken,
                    authTokenFilepath,
                    authTokenStoragePath
            );
        } catch (ConfigurationException e) {
            throw new StepException(e.getMessage(), e, StepFailureReason.ConfigurationFailure);
        }
        if (authToken == null) {
            throw new StepException("authToken is required", StepFailureReason.ConfigurationFailure);
        }
        this.api = new ClassifierAPI(baseUrl, authToken);
        return api.getClassifierService();
    }

    protected String resolveAuthToken(
            final StorageTree storageTree,
            final String authToken, final String authTokenFilepath, final String authTokenStoragePath
    ) throws ConfigurationException
    {
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
                throw new ConfigurationException(
                        String.format("Unable to read authTokenFilepath file %s ", authTokenFilepath) +
                        e.getLocalizedMessage(),
                        e
                );
            }
        } else if (null != authTokenStoragePath) {
            //try to read from key storage
            try {
                Resource<ResourceMeta> resource = storageTree.getResource(authTokenStoragePath);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                resource.getContents().writeContent(byteArrayOutputStream);
                return new String(byteArrayOutputStream.toByteArray());
            } catch (StorageException | IOException | IllegalArgumentException e) {
                throw new ConfigurationException(
                        String.format("Unable to read authTokenStoragePath %s ", authTokenStoragePath) +
                        e.getLocalizedMessage(),
                        e
                );
            }
        } else {
            return null;
        }
    }

    private boolean didLoadMapping = false;

    private void loadViaMapping(final PluginStepContext context) {
        if (didLoadMapping) {
            return;
        }
        NodeEntryImpl dummy = new NodeEntryImpl();
        Framework framework = context.getFramework();
        IRundeckProject project = framework
                .getFrameworkProjectMgr()
                .getFrameworkProject(context.getFrameworkProject());
        baseUrl = ResolverUtil.resolveProperty(
                PUPPET_CLASSIFIER_BASEURL_PROPERTY,
                null,
                dummy,
                project,
                framework
        );
        authToken = ResolverUtil.resolveProperty(
                PUPPET_CLASSIFIER_TOKEN_PROPERTY,
                null,
                dummy,
                project,
                framework
        );
        authTokenFilepath = ResolverUtil.resolveProperty(
                PUPPET_CLASSIFIER_TOKEN_FILEPATH_PROPERTY,
                null,
                dummy,
                project,
                framework
        );
        authTokenStoragePath = ResolverUtil.resolveProperty(
                PUPPET_CLASSIFIER_TOKEN_STORAGE_PATH_PROPERTY,
                null,
                dummy,
                project,
                framework
        );

        didLoadMapping = true;

    }

    <T> T performCall(final Call<T> call, final String name) throws StepException {
        Response<T> execute = null;
        try {
            execute = call.execute();
            if (!execute.isSuccess()) {
                boolean unauth = execute.code() == 401;

                ErrorResponse error = api.readError(execute);

                throw new StepException(
                        String.format(
                                "%s was not %s: %s: %s",
                                name,
                                unauth ? "authorized" : "successful",
                                execute.message(),
                                error
                        ),
                        unauth ? ApiReason.API_UNAUTHORIZED : ApiReason.API_ERROR
                );
            }
        } catch (IOException e) {
            throw new StepException(
                    String.format(
                            "IO error making request for %s: %s",
                            name,
                            e.getLocalizedMessage()
                    ),
                    e,
                    StepFailureReason.IOFailure
            );
        }
        return execute.body();
    }

    void validate(final PluginStepContext context) throws StepException {
        if (null == baseUrl) {
            loadViaMapping(context);
        }
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

    String ansiColor(final String message) {
        return "\u001B[32m" + message + "\u001B[0m";
    }

    enum ApiReason implements FailureReason {
        API_NOT_FOUND,
        API_UNAUTHORIZED,
        API_ERROR,
        UNKNOWN,
    }

    public void buildWith(final DescriptionBuilder builder) {
        //NB: does not work automatically yet with rundeck, we manually do it via loadViaMapping
        builder.mapping("baseUrl", "project." + PUPPET_CLASSIFIER_BASEURL_PROPERTY);
        builder.frameworkMapping("baseUrl", "framework." + PUPPET_CLASSIFIER_BASEURL_PROPERTY);
        builder.mapping("authToken", "project." + PUPPET_CLASSIFIER_TOKEN_PROPERTY);
        builder.frameworkMapping("authToken", "framework." + PUPPET_CLASSIFIER_TOKEN_PROPERTY);
        builder.mapping("authTokenFilepath", "project." + PUPPET_CLASSIFIER_TOKEN_FILEPATH_PROPERTY);
        builder.frameworkMapping("authTokenFilepath", "framework." + PUPPET_CLASSIFIER_TOKEN_FILEPATH_PROPERTY);
        builder.mapping("authTokenStoragePath", "project." + PUPPET_CLASSIFIER_TOKEN_STORAGE_PATH_PROPERTY);
        builder.frameworkMapping("authTokenStoragePath", "framework." + PUPPET_CLASSIFIER_TOKEN_STORAGE_PATH_PROPERTY);
    }
}
