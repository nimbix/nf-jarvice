# nf-jarvice

This plugin has been made to submit Nextflow pipelines into a Jarvice cluster.

User must have a Jarvice cluster username and its associated apikey. Jobs must be submited from a folder shared as vault into the cluster (files transfer is not supported for now).

In order to use the plugin, user must provide the following information into the `nextflow.config` file of the project:


```
plugins {
  id 'nf-jarvice@0.6.0'
}
process {
    executor = 'jarvice'
    apiUrl = 'https://cloud.nimbix.net/api'
    machineType = 'n0'
    vault {
        name = 'persistent'
        readonly = false
        force = false
    }
    user {
        username = 'myusername'
        apikey = 'XXXXXXXXXXXXXXXXXXXXXXXXXXXX'
    }
}
```

Replace apiUrl, machineType, vault, and user (username and apikey) with Jarvice user values.
