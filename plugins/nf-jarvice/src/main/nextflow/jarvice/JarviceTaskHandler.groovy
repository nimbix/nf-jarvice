/*
 * Copyright (c) 2023 Nimbix, Inc.  All Rights Reserved.
 *
 * Inspired from nf-google plugin,
 * from https://github.com/nextflow-io/nextflow/blob/master/plugins/nf-google
 * which is licensed under Apache License, Version 2.0
 *
 */

package nextflow.jarvice

import nextflow.processor.TaskConfig

import java.nio.file.Path
import java.lang.System

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import nextflow.executor.BashWrapperBuilder
import nextflow.processor.TaskHandler
import nextflow.processor.TaskRun
import nextflow.processor.TaskStatus
import nextflow.trace.TraceRecord
import nextflow.fusion.FusionAwareTask
import nextflow.fusion.FusionScriptLauncher
import nextflow.util.Escape

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper;
import groovy.json.StreamingJsonBuilder;

import nextflow.jarvice.client.BatchClient 


@Slf4j
@CompileStatic
class JarviceTaskHandler extends TaskHandler implements FusionAwareTask {

    private JarviceExecutor executor

    private BatchClient client

    private String jobId
    private String jobJarviceName
    private String jobJarviceId
    private Path exitFile
    private Path outputFile
    private Path errorFile

    private String jobState

    private volatile long timestamp


    JarviceTaskHandler(TaskRun task, JarviceExecutor executor) {
        super(task)
        this.client = executor.getClient()
        this.jobId = "nf-${task.hashLog.replace('/','')}-${System.currentTimeMillis()}"
        this.executor = executor
        this.outputFile = task.workDir.resolve(TaskRun.CMD_OUTFILE)
        this.errorFile = task.workDir.resolve(TaskRun.CMD_ERRFILE)
        this.exitFile = task.workDir.resolve(TaskRun.CMD_EXIT)
    }

    protected BashWrapperBuilder createTaskWrapper() {
        final taskBean = task.toTaskBean()
        return new JarviceBatchScriptLauncher(taskBean, executor.remoteBinDir)
    }

    protected buildScript(nextflow.executor.BashWrapperBuilder launcher) {

        String launchScript = "#!/bin/bash\n set -eo pipefail\n"
        launchScript = launchScript + "cd ${Escape.path(launcher.workDir)}\n"
        launchScript = launchScript + "export NXF_CHDIR=${Escape.path(launcher.workDir)}\n"
        def currentEnvironment = [:]
        currentEnvironment = System.getenv()
        for ( e in currentEnvironment ) {
           if ( e.key ==~ /^NXF_.*$/ ) {
               launchScript = launchScript + "export ${e.key}='${e.value}'\n"
           }
        }
        launchScript = launchScript + "/bin/bash -o pipefail ${TaskRun.CMD_RUN} 2>&1 | tee -a ${TaskRun.CMD_LOG}"

        return launchScript
    }

    @Override
    void submit() {
        // BEN: create our launcher wrapper
        final launcher = createTaskWrapper()
        launcher.build()

        // BEN: now lets build our script to launch Nexflow wrapper
        String launchScript = buildScript(launcher)

        // BEN: prepare json to be sent via POST
        def jsonBody = [:]
        jsonBody['vault'] = task.config['vault']
        jsonBody['user'] = task.config['user']
        jsonBody['machine'] = [type: task.config['machineType'], nodes: 1]
        jsonBody['job_label'] = this.jobId
        jsonBody['container'] = [jobscript: launchScript.bytes.encodeBase64().toString(), image: task.config['container']]
        log.debug (jsonBody as String)

        // BEN: prepare submition object
        Map jobobj = [:]
        jobobj['jobId'] = this.jobId
        jobobj['apiUrl'] = task.config['apiUrl']
        jobobj['jsonBody'] = jsonBody

        // BEN: submit via client
        final Map jobData = client.submitJob(jobobj)

        // BEN: check result and store Jarvice job ids
        if(jobData.containsKey('jobJarviceId')) {
            this.jobJarviceId = jobData['jobJarviceId']
            this.jobJarviceName = jobData['jobJarviceName']
            this.status = TaskStatus.SUBMITTED
            log.debug ("Jarvice job SUBMIT success")
            log.debug ("JOB JARVICE NAME: " + this.jobJarviceName.toString())
            log.debug ("JOB JARVICE ID: " + this.jobJarviceId.toString())
            log.info ("Jarvice job " + this.jobJarviceId.toString() + " submitted.")
        } else {
            log.error ("Failed to submit job!");
        }

    }

/*
    @Override
    void submit() {
        // BEN: create our launcher wrapper
        final launcher = createTaskWrapper()
        launcher.build()

        // BEN: now lets build our script to launch Nexflow wrapper

        launchScript = buildScript(launcher)

        String launchScript = "set -x\n set -e\n"
        launchScript = launchScript + "cd ${Escape.path(launcher.workDir)}\n"
        launchScript = launchScript + "export NXF_CHDIR=${Escape.path(launcher.workDir)}\n"
        def currentEnvironment = [:]
        currentEnvironment = System.getenv()
        for ( e in currentEnvironment ) {
           if ( e.key ==~ /^NXF_.*$/ ) {
               launchScript = launchScript + "export ${e.key}='${e.value}'\n"
           }
        }
        launchScript = launchScript + "/bin/bash -o pipefail ${TaskRun.CMD_RUN} 2>&1 | tee -a ${TaskRun.CMD_LOG}"

        log.debug ("Starting submit process for job ID: " + this.jobId.toString())

        def jsonBody = [:]
        jsonBody['vault'] = task.config['vault']
        jsonBody['user'] = task.config['user']
        jsonBody['machine'] = [type: task.config['machineType'], nodes: 1]
        jsonBody['job_label'] = this.jobId
        jsonBody['container'] = [jobscript: launchScript, image: task.config['container']]

        log.debug (jsonBody as String)
        def url = new URL(task.config['apiUrl'].toString() + '/jarvice/batch')
        log.debug (task.config['apiUrl'].toString() + '/jarvice/batch')
        HttpURLConnection connection = url.openConnection() as HttpURLConnection;
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream
        connection.outputStream.withWriter("UTF-8") { new StreamingJsonBuilder(it, jsonBody) }
        connection.connect();

        int responseCode = connection.getResponseCode();
        log.debug ("Submit POST response code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            log.debug ("Server reponse body:");
            log.debug (response.toString());
            def responseJson = new groovy.json.JsonSlurper().parseText(response.toString())
            log.debug ("Jarvice job SUBMIT success")
            log.debug ("JOB JARVICE NAME: " + responseJson['name'].toString())
            log.debug ("JOB JARVICE ID: " + responseJson['number'].toString())
            log.info ("Jarvice job " + responseJson['number'].toString() + " submitted.")
            this.jobJarviceId = responseJson['number'] 
            this.jobJarviceName = responseJson['name']
            this.status = TaskStatus.SUBMITTED
        } else {
            log.debug ("Submit POST request did not work.");
        }
    }*/

    private List<String> RUNNING_AND_TERMINATED = ['PROCESSING STARTING', 'COMPLETED', 'COMPLETED WITH ERROR', 'CANCELED', 'TERMINATED']
    private List<String> TERMINATED = ['COMPLETED', 'COMPLETED WITH ERROR', 'CANCELED', 'TERMINATED']

    @Override
    boolean checkIfRunning() {
        log.debug ("Starting check if process is running for job ID: " + this.jobId.toString())

        Map jobobj = [:]
        jobobj['jobId'] = this.jobId
        jobobj['apiUrl'] = task.config['apiUrl']
        jobobj['user'] = task.config['user']
        jobobj['jobJarviceName'] = this.jobJarviceName
        jobobj['jobJarviceId'] = this.jobJarviceId

        final job_status = client.getJobStatus(jobobj)
        if (job_status in RUNNING_AND_TERMINATED) {
            this.status = TaskStatus.RUNNING
            return true
        } else {
            return false
        }
    }

/*    @Override
    boolean checkIfRunning() {
        log.debug ("Starting check if process is running for job ID: " + this.jobId.toString())
        String request = (task.config['apiUrl'].toString() + "/jarvice/status?username=" + task.config['user']['username'].toString() + "&apikey=" + task.config['user']['apikey'].toString() + "&name=" + this.jobJarviceName)
        log.debug "Check request URL: {}" , request
        def url = new URL(request)
        HttpURLConnection connection = url.openConnection() as HttpURLConnection;
        connection.requestMethod = "GET"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream
        connection.connect();

        int responseCode = connection.getResponseCode();
        log.debug ("Check GET response code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            log.debug ("Server reponse body:");
            log.debug (response.toString());
            def responseJson = new groovy.json.JsonSlurper().parseText(response.toString())
            log.debug ("Jarvice job STATUS success")
            log.debug ("JOB JARVICE STATUS: " + responseJson[this.jobJarviceId]['job_status'].toString())
            if (responseJson[this.jobJarviceId]['job_status'] == "SUBMITTED" || responseJson[this.jobJarviceId]['job_status'] == "PROCESSING_STARTING" || responseJson[this.jobJarviceId]['job_status'] == "COMPLETED" || responseJson[this.jobJarviceId]['job_status'] == "COMPLETED_WITH_ERROR" || responseJson[this.jobJarviceId]['job_status'] == "CANCELED" || responseJson[this.jobJarviceId]['job_status'] == "TERMINATED") {
                status = TaskStatus.RUNNING
                return true
            }
        } else {
            log.debug ("GET request did not work.");
        }
        return false
    }
*/


    @Override
    boolean checkIfCompleted() {
        log.debug ("Starting check if process is completed for job ID: " + this.jobId.toString())

        Map jobobj = [:]
        jobobj['jobId'] = this.jobId
        jobobj['apiUrl'] = task.config['apiUrl']
        jobobj['user'] = task.config['user']
        jobobj['jobJarviceName'] = this.jobJarviceName
        jobobj['jobJarviceId'] = this.jobJarviceId

        final job_status = client.getJobStatus(jobobj)
        if (job_status in TERMINATED) {
            this.status = TaskStatus.COMPLETED
            task.exitStatus = readExitFile()

            // Gather output
            String jobOutput = client.getJobOutput(jobobj)
//            log.info "BEN - OUTPUT1 {}" , jobOutput
            String[] splitJobOutput = jobOutput.split("\n")
//            log.info (splitJobOutput[1])
            def finalJobOutput = []
            Boolean start_logging = false
            for (eachSplit in splitJobOutput) {
//                log.info "HIHIHIH {}" , eachSplit.toString()
                if (start_logging) {
                    finalJobOutput.add(eachSplit)
                }
                if (eachSplit == "###############################################################################") {
                    start_logging = true
                }
            }  
//            log.info "BEN - OUTPUT {}" , finalJobOutput.join("\n")
            if (job_status == "COMPLETED") {
//                task.exitStatus = 0          
                task.stdout = finalJobOutput.join("\n")
                task.stderr = ""
            } else {
//                log.info "[BEN-BUG-1] - report completed with errors"
//                task.exitStatus = null
                task.stdout = ""
                task.stderr = finalJobOutput.join("\n")
            }
            return true
        } else {
            return false
        }
    }

    @PackageScope Integer readExitFile() {
        try {
            exitFile.text as Integer
        }
        catch (Exception e) {
            log.debug "[JARVICE] Cannot read exitstatus for task: `$task.name` | ${e.message}"
            null
        }
    }

/*
    @Override
    boolean checkIfCompleted() {
        log.debug ("Starting check if process is completed for job ID: " + this.jobId.toString())
        String request = (task.config['apiUrl'].toString() + "/jarvice/status?username=" + task.config['user']['username'].toString() + "&apikey=" + task.config['user']['apikey'].toString() + "&name=" + this.jobJarviceName)
        log.debug "Check request URL: {}" , request
        def url = new URL(request)
        HttpURLConnection connection = url.openConnection() as HttpURLConnection;
        connection.requestMethod = "GET"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream
        connection.connect();

        int responseCode = connection.getResponseCode();
        log.debug ("Check GET response code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            log.debug ("Server reponse body:");
            log.debug (response.toString());
            def responseJson = new groovy.json.JsonSlurper().parseText(response.toString())
            log.debug ("Jarvice job STATUS success")
            log.debug ("JOB JARVICE STATUS: " + responseJson[this.jobJarviceId]['job_status'].toString())
            if (responseJson[this.jobJarviceId]['job_status'] == "COMPLETED") {
                status = TaskStatus.COMPLETED
                task.exitStatus = 0 // To FIX
                task.stdout = "My Task STDOUT" // To FIX
                task.stderr = "" // To FIX
                return true
            }
            if (responseJson[this.jobJarviceId]['job_status'] == "COMPLETED_WITH_ERROR" || responseJson[this.jobJarviceId]['job_status'] == "CANCELED" || responseJson[this.jobJarviceId]['job_status'] == "TERMINATED") {
                status = TaskStatus.COMPLETED
                task.exitStatus = 1 // To FIX
                task.stdout = "My Task STDOUT" // To FIX
                task.stderr = "" // To FIX
                return true
            }

        } else {
            log.debug ("GET request did not work.");
        }

        return false
    }
*/


    @Override
    void kill() {
        log.debug ("Starting shutdown of job ID: " + this.jobId.toString())

        Map jobobj = [:]
        jobobj['jobId'] = this.jobId
        jobobj['apiUrl'] = task.config['apiUrl']
        jobobj['user'] = task.config['user']
        jobobj['jobJarviceName'] = this.jobJarviceName
        jobobj['jobJarviceId'] = this.jobJarviceId

        final killStatus = client.killJob(jobobj)

    }

/*
    @Override
    void kill() {
        log.debug ("Starting shutdown of job ID: " + this.jobId.toString())
        String request = (task.config['apiUrl'].toString() + "/jarvice/shutdown?username=" + task.config['user']['username'].toString() + "&apikey=" + task.config['user']['apikey'].toString() + "&name=" + this.jobJarviceName)
        log.debug "Check request URL: {}" , request
        def url = new URL(request)
//        def url = new URL('http://localhost:8888')
      // def connection = url.openConnection()
        HttpURLConnection connection = url.openConnection() as HttpURLConnection;
        connection.requestMethod = "GET"
        connection.doOutput = true
//        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream
//        connection.outputStream.withWriter("UTF-8") { new StreamingJsonBuilder(it, jsonBody) }
        connection.connect();

        int responseCode = connection.getResponseCode();
        log.debug ("Check GET response code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // print result
            log.debug ("Server reponse body:");
            log.debug (response.toString());
            def responseJson = new groovy.json.JsonSlurper().parseText(response.toString())
            if (responseJson[this.jobJarviceId]['status'] == "shutdown requested") {
                log.debug ("Jarvice job SHUTDOWN success")
                log.debug ("JOB JARVICE SHUTDOWN: " + responseJson[this.jobJarviceId]['status'].toString())                
            }
            else {
                log.debug ("Jarvice job SHUTDOWN failure")
            }

        } else {
            log.debug ("GET request did not work.");
        }
    } */

}
