// Declare syntax version
nextflow.enable.dsl=2
// Script parameters
params.query = "/tmp/message"
tutu = "hahaha"

process update_message {
  container 'us-docker.pkg.dev/jarvice/images/ubuntu-desktop:bionic'
  machineType 'n0'

  input:
    path query
  output:
    path "top_hits.txt"

    """
    #!/usr/bin/env bash
    pwd
    echo Step 1
    echo "Hello world! hohoho" > top_hits.txt
    """
}

process add_date {
  container 'us-docker.pkg.dev/jarvice/images/ubuntu-desktop:bionic'
  machineType 'n0'
  input:
    path top_hits

  output:
    path "sequences.txt"

    """
    echo Step 2
    echo \$(cat top_hits.txt) \$(date) > sequences.txt
    """
}

workflow {
   update_message(params.query) | add_date | view
//update_message(params.query) | add_date
}
