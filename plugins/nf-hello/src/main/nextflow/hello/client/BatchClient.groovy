package nextflow.hello.client

/*import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.FixedHeaderProvider
import com.google.auth.Credentials
import com.google.cloud.batch.v1.BatchServiceClient
import com.google.cloud.batch.v1.BatchServiceSettings
import com.google.cloud.batch.v1.Job
import com.google.cloud.batch.v1.JobName
import com.google.cloud.batch.v1.JobStatus
import com.google.cloud.batch.v1.LocationName
import com.google.cloud.batch.v1.TaskGroupName*/
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper;
import groovy.json.StreamingJsonBuilder;

@Slf4j
@CompileStatic
class BatchClient {

   // protected String projectId
   // protected String location
    //protected BatchServiceClient batchServiceClient

    BatchClient(BatchConfig config) {
    //log.info "[BEN-BatchClient] - main"
    //    this.projectId = "benid" //config.googleOpts.projectId
    //    this.location = "benlocation" //config.googleOpts.location
//        this.batchServiceClient = createBatchService(config)
    }

    /** Only for testing - do not use */
    protected BatchClient() {}

    Map submitJob(Map jobobj) {

        log.debug ("Starting submit process for job ID: " + jobobj['jobId'].toString())
        Map jobData = [:]
        String request_url = jobobj['apiUrl'].toString() + '/jarvice/batch'
        log.debug (jobobj['apiUrl'].toString() + '/jarvice/batch')

        Map jsonBody = [:]
        jsonBody = jobobj['jsonBody']
        Map request_output = genericHttpPostRequest(request_url, jsonBody)

        if(request_output['response_exit_code'] == 0) {
            def responseBodyJson = new groovy.json.JsonSlurper().parseText(request_output['response_body'].toString())
            jobData['jobJarviceId'] = responseBodyJson['number']
            jobData['jobJarviceName'] = responseBodyJson['name']
        }

        return jobData
    }

    Map genericHttpPostRequest(String request_url, Map jsonBody) {
        log.debug "Generic POST request URL: {}" , request_url
        Map request_out = [:]
        def url = new URL(request_url)
        HttpURLConnection connection = url.openConnection() as HttpURLConnection;
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream
        connection.outputStream.withWriter("UTF-8") { new StreamingJsonBuilder(it, jsonBody) }
        connection.connect();

        int responseCode = connection.getResponseCode();
        log.debug ("POST response code: " + responseCode);
        request_out['response_code'] = responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            request_out['response_exit_code'] = 0
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            log.debug ("Server reponse body:");
            log.debug (response.toString());
            request_out['response_body'] = response.toString()
        } else {
            log.debug ("POST request did not work.");
            request_out['response_exit_code'] = 1
        }
        return request_out
    }


    String getJobStatus(Map jobobj) {

        log.debug ("Check status of job ID: " + jobobj.jobId.toString())
        String request_url = (jobobj['apiUrl'].toString() + "/jarvice/status?" + 
                          "username=" + jobobj['user']['username'].toString() + 
                          "&apikey=" + jobobj['user']['apikey'].toString() +
                          "&name=" + jobobj['jobJarviceName'])
        Map request_output = genericHttpGetRequest(request_url)

        if(request_output['response_exit_code'] == 0) {
            def responseBodyJson = new groovy.json.JsonSlurper().parseText(request_output['response_body'].toString())
            //assert responseBodyJson instanceof Map
            //if(responseBodyJson[jobobj.jobJarviceId].containsKey('job_status')) {
                return responseBodyJson[jobobj.jobJarviceId.toString()]['job_status']
            //} else {
            //    return "error"
            //}
        } else {
            return "error"
        }

    }

    String killJob(Map jobobj) {

        log.debug ("Killing job ID: " + jobobj.jobId.toString())
        String request_url = (jobobj['apiUrl'].toString() + "/jarvice/shutdown?" + 
                          "username=" + jobobj['user']['username'].toString() + 
                          "&apikey=" + jobobj['user']['apikey'].toString() +
                          "&name=" + jobobj['jobJarviceName'])
        Map request_output = genericHttpGetRequest(request_url)

        if(request_output['response_exit_code'] == 0) {
            return "ok"
        } else {
            return "error"
        }

    }

    Map genericHttpGetRequest(String request_url) {
        log.debug "Generic GET request URL: {}" , request_url
        Map request_out = [:]
        def url = new URL(request_url)
        HttpURLConnection connection = url.openConnection() as HttpURLConnection;
        connection.requestMethod = "GET"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream
        connection.connect();
        int responseCode = connection.getResponseCode();
        log.debug ("GET response code: " + responseCode);
        request_out['response_code'] = responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            request_out['response_exit_code'] = 0
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            log.debug ("Server reponse body:");
            log.debug (response.toString());
            request_out['response_body'] = response.toString()
        } else {
            log.debug ("GET request did not work.");
            request_out['response_exit_code'] = 1
        }
        return request_out
    }


/*
    protected CredentialsProvider createCredentialsProvider(BatchConfig config) {
        if( !config.getCredentials() )
            return null
        return new CredentialsProvider() {
            @Override
            Credentials getCredentials() throws IOException {
                return config.getCredentials()
            }
        }
    }

    protected BatchServiceClient createBatchService(BatchConfig config) {
        final provider = createCredentialsProvider(config)
        if( provider ) {
            log.debug "[GOOGLE BATCH] Creating service client with config credentials"
            final userAgent = FixedHeaderProvider.create('user-agent', 'Nextflow')
            final settings = BatchServiceSettings
                            .newBuilder()
                            .setHeaderProvider(userAgent)
                            .setCredentialsProvider(provider)
                            .build()
            return BatchServiceClient.create(settings)
        }
        else {
            log.debug "[GOOGLE BATCH] Creating service client with default settings"
            return BatchServiceClient.create()

        }
    }

    Job submitJob(String jobId, Job job) {
        final parent = LocationName.of(projectId, location)

        return batchServiceClient.createJob(parent, job, jobId)
    }

    Job describeJob(String jobId) {
        final name = JobName.of(projectId, location, jobId)

        return batchServiceClient.getJob(name)
    }

    Iterable<?> listTasks(String jobId) {
        final parent = TaskGroupName.of(projectId, location, jobId, 'group0')

        return batchServiceClient.listTasks(parent).iterateAll()
    }

    void deleteJob(String jobId) {
        final name = JobName.of(projectId, location, jobId).toString()

        batchServiceClient.deleteJobAsync(name)
    }

    JobStatus getJobStatus(String jobId) {
        final job = describeJob(jobId)
        return job.getStatus()
    }

    String getJobState(String jobId) {
        final status = getJobStatus(jobId)
        return status ? status.getState().toString() : null
    }

    void shutdown() {
        batchServiceClient.close()
    }

    String getLocation() {
        return location
    }
*/
}

