package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.simplifyops.util.puppet.ClassifierAPI;
import com.simplifyops.util.puppet.classifierapi.ClassifierService;
import com.simplifyops.util.puppet.classifierapi.Group;
import com.simplifyops.util.puppet.classifierapi.UpdateGroupRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Created by greg on 3/9/16.
 */

@Plugin(name = PinNodeToGroupStep.PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Pin Nodes To Group",
                   description = "Puppet Classifier API: Update a group rule to pin a node to the group")
public class PinNodeToGroupStep extends BasePuppetStep implements StepPlugin, DescriptionBuilder.Collaborator {

    public static final String PROVIDER_NAME = "puppet-classifier-pin-node-to-group-step";

    @PluginProperty(title = "Group ID",
                    description = "Group ID to update, can specify via an option value such as ${option.group}",
                    required = true
    )
    String groupId;

    @PluginProperty(title = "Node",
                    description = "Node name (certname) to pin to the group, can specify via option value e.g. " +
                                  "${option.node}",
                    required = false
    )
    String node;

    @PluginProperty(title = "Use Target Nodes",
                    description =
                            "Use the node targets from the Job to pin to the group. Requires that all matched nodes " +
                            "names are puppet nodes.",
                    required = false,
                    defaultValue = "false"

    )
    boolean useNodes;

    @Override
    public void executeStep(
            final PluginStepContext context, final Map<String, Object> configuration
    ) throws StepException
    {
        validate();
        ClassifierService service = getClassifierService(context);
        Group group;
        context.getLogger().log(3, String.format("Get group %s ...", groupId));
        group = performCall(service.getGroup(groupId), String.format("Get group %s", groupId));

        if (null == group) {
            //not found
            throw new StepException(
                    String.format("Group with specified ID was not found: %s", groupId),
                    ApiReason.NOT_FOUND
            );
        }
        context.getLogger().log(3, String.format("Get group %s result: %s", groupId, group));
        //determine nodes
        List<String> nodes = new ArrayList<>();
        if (null != node) {
            nodes.add(node);
        } else if (useNodes) {
            nodes.addAll(context.getNodes().getNodeNames());
        }
        List rules = generateRules(nodes);

        Group postGroup;

        UpdateGroupRules updates = ClassifierAPI.updateGroupRulesMerge(group, rules, false);

        context.getLogger().log(
                3,
                String.format(
                        "Original rules: %s ; Updated rules: %s",
                        group.getRule(),
                        updates.getRule()
                )
        );


        postGroup = performCall(service.updateGroup(groupId, updates), String.format("Update group %s", groupId));
        if (null == postGroup) {
            //post failed
            throw new StepException(
                    String.format("Error POST ing update to group with specified ID: %s", groupId),
                    ApiReason.UNKNOWN
            );
        }
        context.getLogger().log(
                2,
                String.format("Nodes %s pinned to group %s (%s)", nodes, group.getName(), groupId)
        );
    }

    static List generateRules(final List<String> nodes) {
        ArrayList rules = new ArrayList<>();
        List<String> trusted = asList("trusted", "certname");

        for (String node : nodes) {
            rules.add(asList("=", "name", node));
        }
        return rules;
    }

    void validate() throws StepException {
        super.validate();
        requireValue(groupId, "groupId");

    }

    @Override
    public void buildWith(final DescriptionBuilder builder) {
        super.buildWith(builder);
    }
}
