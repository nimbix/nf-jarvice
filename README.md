# nf-jarvice

This plugin has been made to submit Nextflow pipelines into a Jarvice cluster.

User must have an api key along with the username, and submit jobs from a folder shared as vault into the cluster (files transfer is not supported for now).

In order to use the plugin, user must provide the following information into the `nextflow.config` file of the project:


```json
plugins {
  id 'nf-jarvice@1.0.0'
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
