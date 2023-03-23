package nextflow.jarvice.client

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper;
import groovy.json.StreamingJsonBuilder;

@Slf4j
@CompileStatic
class BatchClient {


    BatchClient(BatchConfig config) {
    }

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

    String getJobOutput(Map jobobj) {

        log.debug ("Get output of completed job ID: " + jobobj.jobId.toString())
        String request_url = (jobobj['apiUrl'].toString() + "/jarvice/output?" + 
                          "username=" + jobobj['user']['username'].toString() + 
                          "&apikey=" + jobobj['user']['apikey'].toString() +
                          "&name=" + jobobj['jobJarviceName'] +
                          "&lines=0")
        Map request_output = genericHttpGetRequest(request_url)

        if(request_output['response_exit_code'] == 0) {
            return request_output['response_body'].toString()
        } else {
            return "error"
        }

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
            Boolean loop_first = true
            while ((inputLine = in.readLine()) != null) {
                if (loop_first) {
                    loop_first = false
                } else {
                    response.append("\n");
                }
                    response.append(inputLine);
            }
//            log.info "HAHAHAHAHA {}" ,  response.toString()
/*            def newinputline = [];
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                newinputline.add(inputLine);
            }
            log.info 'COUCOU'
            newinputline.each { val -> println "cocuou" + val }
*/
/*
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
*/
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



}

