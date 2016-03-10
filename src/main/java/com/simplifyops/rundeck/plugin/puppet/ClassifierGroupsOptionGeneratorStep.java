package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.dispatcher.DataContextUtils;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepFailureReason;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyScope;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplifyops.util.puppet.classifierapi.ClassifierService;
import com.simplifyops.util.puppet.classifierapi.Group;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates Job option json from Groups api result list
 */
@Plugin(name = ClassifierGroupsOptionGeneratorStep.PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Generate Puppet Classifier Group Options",
                   description = "Generates a Job Options json file from the Puppet Classifier Groups list")
public class ClassifierGroupsOptionGeneratorStep extends BasePuppetStep implements StepPlugin,
        DescriptionBuilder.Collaborator
{
    public static final String PROVIDER_NAME = "puppet-classifier-groups-options-generator-step";
    @PluginProperty(title = "Option Name Template",
                    description = "Template for generating option name from groups.\n\n" +
                                  "Can use property references like `${group.name}` or `${group.id}`.",
                    required = true,
                    defaultValue = "${group.name}")
    String optionNameTemplate;
    @PluginProperty(title = "Option Value Template",
                    description = "Template for generating option value from groups.\n\n" +
                                  "Can use property references like `${group.name}` or `${group.id}`.",
                    required = true,
                    defaultValue = "${group.id}")
    String optionValueTemplate;
    @PluginProperty(title = "File Path",
                    description = "Location on disk to create the options file, enter full path and file name.",
                    scope = PropertyScope.Instance)
    String filePath;


    @Override
    public void executeStep(
            final PluginStepContext context, final Map<String, Object> configuration
    ) throws StepException
    {
        validate();

        ClassifierService service = getClassifierService(context);

        List<Group> groups = performCall(service.listGroups(), "list groups");

        context.getLogger().log(4, "filepath: " + filePath);

        for (Group group : groups) {
            context.getLogger().log(4, "group: " + group.getName() + "/" + group.getId());
        }
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> options = generateOptions(groups);
        File outfile = new File(filePath);
        File tmpfile = new File(filePath + ".tmp");
        try {
            try (FileOutputStream out = new FileOutputStream(tmpfile)) {
                mapper.writeValue(out, options);
            }
            Files.move(tmpfile.toPath(), outfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StepException(
                    "Failure writing json to local file: " + e.getLocalizedMessage(),
                    StepFailureReason.IOFailure
            );
        }

    }

    /*
        [
          {"name":"X Label", "value":"x value"},
          {"name":"Y Label", "value":"y value"},
          {"name":"A Label", "value":"a value"}
        ]
     */
    private List<Map<String, String>> generateOptions(final List<Group> groups) {
        ArrayList<Map<String, String>> maps = new ArrayList<>();
        for (Group group : groups) {
            HashMap<String, String> data = new HashMap<>();
            data.put("name", valueFromTemplate(optionNameTemplate, group));
            data.put("value", valueFromTemplate(optionValueTemplate, group));
            maps.add(data);
        }
        return maps;
    }

    private String valueFromTemplate(String template, final Group group) {
        Map<String, String> groupDataMap = dataMap(group);
        return DataContextUtils.replaceDataReferences(
                template,
                DataContextUtils.addContext("group", groupDataMap, null),
                null,
                true
        );
    }

    private Map<String, String> dataMap(final Group group) {
        HashMap<String, String> data = new HashMap<>();
        data.put("name", group.getName());
        data.put("id", group.getId());
        data.put("description", group.getDescription());
        data.put("environment", group.getEnvironment());
        data.put("parent", group.getParent());
        return data;
    }


    void validate() throws StepException {
        super.validate();
        if (null == filePath) {
            throw new StepException(
                    "Configuration invalid: filePath is not set.",
                    StepFailureReason.ConfigurationFailure
            );
        }
        if (null == optionNameTemplate) {
            throw new StepException(
                    "Configuration invalid: optionNameTemplate is not set.",
                    StepFailureReason.ConfigurationFailure
            );
        }
        if (null == optionValueTemplate) {
            throw new StepException(
                    "Configuration invalid: optionNameTemplate is not set.",
                    StepFailureReason.ConfigurationFailure
            );
        }
    }

    @Override
    public void buildWith(final DescriptionBuilder builder) {
        super.buildWith(builder);
    }
}
