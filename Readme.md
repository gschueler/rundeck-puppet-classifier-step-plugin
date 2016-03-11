# Puppet Classifier Plugins for Rundeck

[![Build Status](https://travis-ci.org/gschueler/rundeck-puppet-classifier-step-plugin.svg?branch=master)](https://travis-ci.org/gschueler/rundeck-puppet-classifier-step-plugin)

This plugin provides several Rundeck plugins which use the Puppet Classifier API:

## Workflow Step plugins

1. *Generate Puppet Classifier Group Options*: a step that generates an options.json file for using in Jobs with the
    other step plugins.  It lists the groups and stores them in Rundeck's Options JSON format.
2. *Pin Nodes To Group*: pins the selected nodes to the target Group
3. *Remove Nodes From Group*: removes pinned nodes from a Group
 
## Resource Model Source plugins

1. *Puppet Pinned Nodes with Groups*: a Resource Model Source that generates nodes tagged with Group names, by
 detecting the pinned nodes rules for each group.
 
You can use this with the [PuppetDB Nodes Plugin][] which generates node definitions from PuppetDB.  Configure
both Model Source plugins, and you can have the node attributes generated from Facts from PuppetDB and the tags
from the Groups from puppet Classifier API.