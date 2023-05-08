/*
 * Copyright (c) 2023 Nimbix, Inc.  All Rights Reserved.
 *
 * Inspired from nf-google plugin,
 * from https://github.com/nextflow-io/nextflow/blob/master/plugins/nf-google
 * which is licensed under Apache License, Version 2.0
 *
 */

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
        request_out['response_exit_code'] = checkHTTPCode(responseCode)
        if (request_out['response_exit_code'] == 0) { // success
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
            log.error ("HTTP POST request did not work!");
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
        request_out['response_exit_code'] = checkHTTPCode(responseCode)
        if (request_out['response_exit_code'] == 0) { // success
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
            log.error ("HTTP GET request did not work!");
        }
        return request_out
    }

    Integer checkHTTPCode(Integer http_code) {
        // Return 0 only in case if HTTP 200 answer
        if (http_code == HttpURLConnection.HTTP_ACCEPTED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 202: Accepted.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_BAD_GATEWAY) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 502: Bad Gateway.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_BAD_METHOD) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 405: Method Not Allowed.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_BAD_REQUEST) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 400: Bad Request.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_CLIENT_TIMEOUT) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 408: Request Time-Out.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_CONFLICT) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 409: Conflict.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_CREATED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 201: Created.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_ENTITY_TOO_LARGE) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 413: Request Entity Too Large.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_FORBIDDEN) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 403: Forbidden.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_GATEWAY_TIMEOUT) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 504: Gateway Timeout.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_GONE) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 410: Gone.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 500: Internal Server Error.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_LENGTH_REQUIRED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 411: Length Required.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_MOVED_PERM) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 301: Moved Permanently.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_MOVED_TEMP) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 302: Temporary Redirect.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_MULT_CHOICE) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 300: Multiple Choices.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_NO_CONTENT) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 204: No Content.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 406: Not Acceptable.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_NOT_AUTHORITATIVE) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 203: Non-Authoritative Information.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_NOT_FOUND) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 404: Not Found.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_NOT_IMPLEMENTED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 501: Not Implemented.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_NOT_MODIFIED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 304: Not Modified.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_OK) {
            log.info ("HTTP request to Jarvice cluster - Status-Code 200: OK.")
            return 0
        } else if (http_code == HttpURLConnection.HTTP_PARTIAL) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 206: Partial Content.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_PAYMENT_REQUIRED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 402: Payment Required.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_PRECON_FAILED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 412: Precondition Failed.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_PROXY_AUTH) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 407: Proxy Authentication Required.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_REQ_TOO_LONG) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 414: Request-URI Too Large.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_RESET) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 205: Reset Content.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_SEE_OTHER) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 303: See Other.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 401: Unauthorized.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_UNAVAILABLE) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 503: Service Unavailable.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_UNSUPPORTED_TYPE) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 415: Unsupported Media Type.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_USE_PROXY) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 305: Use Proxy.")
            return 1
        } else if (http_code == HttpURLConnection.HTTP_VERSION) {
            log.error ("HTTP request to Jarvice cluster - Status-Code 505: HTTP Version Not Supported.")
            return 1
        } else {
            log.error ("HTTP request to Jarvice cluster - Status-Code unknown")
            return 1
        }
    }


}

