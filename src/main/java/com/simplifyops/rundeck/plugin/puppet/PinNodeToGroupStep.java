package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.simplifyops.util.puppet.ClassifierAPI;
import com.simplifyops.util.puppet.classifierapi.ClassifierService;
import com.simplifyops.util.puppet.classifierapi.Group;
import com.simplifyops.util.puppet.classifierapi.UpdateGroupRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by greg on 3/9/16.
 */

@Plugin(name = PinNodeToGroupStep.PROVIDER_NAME, service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Pin Nodes To Group",
                   description = "Puppet Classifier API: Update a group rule to pin a node to the group")
public class PinNodeToGroupStep extends BaseNodeGroupStep implements StepPlugin {

    public static final String PROVIDER_NAME = "puppet-classifier-pin-node-to-group-step";


    @Override
    public void executeStep(
            final PluginStepContext context, final Map<String, Object> configuration
    ) throws StepException
    {
        validate(context);
        ClassifierService service = getClassifierService(context);
        Group group = getGroup(context, service);
        //determine nodes
        List<String> nodes = new ArrayList<>();
        if (null != node) {
            nodes.add(node);
        } else if (useNodes) {
            nodes.addAll(context.getNodes().getNodeNames());
        }
        List rules = generateRules(nodes);

        Group postGroup;

        UpdateGroupRules updates = ClassifierAPI.updateGroupRulesMerge(group, rules);

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


}
