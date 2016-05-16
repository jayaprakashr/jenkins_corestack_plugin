# Jenkins Corestack Orchestrator plugin
--------------------------------------

This plugin integrates [Corestack orchestrator][] with Jenkins. 

[Corestack orchestrator]: http://www.corestack.io/

This helps to notify the status of a jenkins job against the configured corestack job via REST API. Corestack server credentials are captured as part of jenkins global configuration. 

# Configurations:

Add Corestack server credentails to establish valid connection as part of jenkins global config

![Global Config](/doc/global_config.png)

On Job configuration page, add post build action "Corestack Notification" to provide corestack project & job details

![Corestack post build action](/doc/post_build_action.png)

![Corestack Notification Config](/doc/post_build_config.png)

* Jenkins : http://www.jenkins-ci.org
* Corestack : http://www.corestack.io/

Maintainer
----------
Jayapraksh R <mail2jayaprakashr@gmail.com>
