# nf-jarvice plugin 
 
Config example

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
