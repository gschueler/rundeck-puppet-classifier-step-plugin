package com.simplifyops.rundeck.plugin.puppet;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import com.simplifyops.util.puppet.ClassifierAPI;
import com.simplifyops.util.puppet.classifierapi.ClassifierService;
import com.simplifyops.util.puppet.classifierapi.ErrorResponse;
import com.simplifyops.util.puppet.classifierapi.Group;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

/**
 * Created by greg on 3/11/16.
 */

public class PinnedNodeGroupsResourceModelSource extends BasePuppetStep implements ResourceModelSource {
    public PinnedNodeGroupsResourceModelSource(
            String baseUrl,
            String authToken
    )
    {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    ClassifierAPI api;

    @Override
    public INodeSet getNodes() throws ResourceModelSourceException {
        try {
            validate(null);
        } catch (StepException e) {
            throw new ResourceModelSourceException(e.getMessage(), e);
        }
        ClassifierService classifierService = getClassifierService();
        Call<List<Group>> listCall = classifierService.listGroups();
        try {
            Response<List<Group>> execute = listCall.execute();
            if (!execute.isSuccess()) {
                ErrorResponse error = api.readError(execute);
                boolean unauth = execute.code() == 401;
                throw new ResourceModelSourceException(
                        String.format(
                                "%s was not %s: %s: %s",
                                "get Groups",
                                unauth ? "authorized" : "successful",
                                execute.message(),
                                error
                        )
                );
            }
            return convertNodes(execute.body());
        } catch (IOException e) {
            throw new ResourceModelSourceException(e.getMessage(), e);
        }
    }

    private INodeSet convertNodes(final List<Group> body) {
        NodeSetImpl iNodeEntries = new NodeSetImpl();
        for (Group group : body) {
            List rules = group.getRule();
            if (null != rules && rules.size() > 1 && "or".equals(rules.get(0))) {
                //look for pinned node rules
                for (Object o : rules.subList(1, rules.size())) {
                    if (o instanceof List) {
                        List rule = (List) o;
                        if (rule.size() == 3 && "=".equals(rule.get(0)) && "name".equals(rule.get(1))) {
                            addNodeTag((String) rule.get(2), iNodeEntries, group.getName());
                        }
                    }
                }
            }
        }
        return iNodeEntries;
    }

    private void addNodeTag(final String node, final NodeSetImpl nodeSet, final String tag) {
        INodeEntry node1 = nodeSet.getNode(node);
        if (node1 == null) {
            node1 = new NodeEntryImpl(node);
            nodeSet.putNode(node1);
        }
        node1.getTags().add(tag);
    }

    private ClassifierService getClassifierService() {
        this.api = new ClassifierAPI(baseUrl, authToken);
        return api.getClassifierService();
    }
}
