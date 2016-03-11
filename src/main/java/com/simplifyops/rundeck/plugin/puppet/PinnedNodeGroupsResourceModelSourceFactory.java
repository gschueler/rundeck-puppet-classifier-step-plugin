package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.PluginAdapterUtility;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;

import java.util.Properties;

/**
 * Created by greg on 3/11/16.
 */
@Plugin(name = PinnedNodeGroupsResourceModelSourceFactory.PROVIDER_NAME,
        service = ServiceNameConstants.ResourceModelSource)
@PluginDescription(title = "Puppet Pinned Nodes with Groups",
                   description = "Generates discovered pinned nodes using the Puppet Classifier API and adds Group " +
                                 "names as tags")
public class PinnedNodeGroupsResourceModelSourceFactory extends BasePuppetStep implements ResourceModelSourceFactory,
        Describable
{
    static final String PROVIDER_NAME = "puppet-classifier-pinned-nodes";

    Framework framework;

    public PinnedNodeGroupsResourceModelSourceFactory(Framework framework) {
        this.framework = framework;
    }

    @Override
    public Description getDescription() {
        Description description = PluginAdapterUtility.buildDescription(this, DescriptionBuilder.builder(), true);
        return description;
    }

    @Override
    public void buildWith(final DescriptionBuilder builder) {
        builder.removeProperty("authTokenStoragePath");
    }

    @Override
    public ResourceModelSource createResourceModelSource(final Properties configuration) throws ConfigurationException {
        String baseUrl = configuration.getProperty("baseUrl");
        String authTokenConfg = configuration.getProperty("authToken");
        String authTokenFilepath = configuration.getProperty("authTokenFilepath");
        String authToken = resolveAuthToken(
                null,
                authTokenConfg,
                authTokenFilepath,
                null
        );
        if (null == authToken) {
            throw new ConfigurationException("authToken is required");
        }
        if (null == baseUrl) {
            throw new ConfigurationException("baseUrl is required");
        }
        return new PinnedNodeGroupsResourceModelSource(
                baseUrl,
                authToken
        );
    }
}
