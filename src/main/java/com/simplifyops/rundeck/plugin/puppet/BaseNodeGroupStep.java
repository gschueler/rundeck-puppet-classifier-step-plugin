package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.simplifyops.util.puppet.classifierapi.ClassifierService;
import com.simplifyops.util.puppet.classifierapi.Group;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by greg on 3/10/16.
 */
public class BaseNodeGroupStep extends BasePuppetStep {

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


    static List generateRules(final List<String> nodes) {
        ArrayList rules = new ArrayList<>();
        List<String> trusted = asList("trusted", "certname");

        for (String node : nodes) {
            rules.add(asList("=", "name", node));
        }
        return rules;
    }


    void validate(final PluginStepContext context) throws StepException {
        super.validate(context);
        requireValue(groupId, "groupId");
        if(!useNodes){
            requireValue(node, "node");
        }
    }

    protected Group getGroup(final PluginStepContext context, final ClassifierService service)
            throws StepException
    {
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
        return group;
    }
}
