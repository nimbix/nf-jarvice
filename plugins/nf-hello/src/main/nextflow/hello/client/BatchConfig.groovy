package nextflow.hello.client


//import com.google.auth.oauth2.GoogleCredentials
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
//import nextflow.cloud.google.GoogleOpts
import nextflow.util.MemoryUnit

@Slf4j
@CompileStatic
class BatchConfig {

/*    private GoogleOpts googleOpts
    private GoogleCredentials credentials
    private List<String> allowedLocations
    private MemoryUnit bootDiskSize
    private String cpuPlatform
    private boolean spot
    private boolean preemptible
    private boolean usePrivateAddress
    private String network
    private String subnetwork
    private String serviceAccountEmail

    GoogleOpts getGoogleOpts() { return googleOpts }
    GoogleCredentials getCredentials() { return credentials }
    List<String> getAllowedLocations() { allowedLocations }
    MemoryUnit getBootDiskSize() { bootDiskSize }
    String getCpuPlatform() { cpuPlatform }
    boolean getPreemptible() { preemptible }
    boolean getSpot() { spot }
    boolean getUsePrivateAddress() { usePrivateAddress }
    String getNetwork() { network }
    String getSubnetwork() { subnetwork }
    String getServiceAccountEmail() { serviceAccountEmail } */

    static BatchConfig create(Session session) {
        //log.info "[BEN-BatchConfig] - main"
        final result = new BatchConfig()
/*        result.googleOpts = GoogleOpts.create(session)
        result.credentials = result.googleOpts.credentials
        result.allowedLocations = session.config.navigate('google.batch.allowedLocations', List.of()) as List<String>
        result.bootDiskSize = session.config.navigate('google.batch.bootDiskSize') as MemoryUnit
        result.cpuPlatform = session.config.navigate('google.batch.cpuPlatform')
        result.spot = session.config.navigate('google.batch.spot',false)
        result.preemptible = session.config.navigate('google.batch.preemptible',false)
        result.usePrivateAddress = session.config.navigate('google.batch.usePrivateAddress',false)
        result.network = session.config.navigate('google.batch.network')
        result.subnetwork = session.config.navigate('google.batch.subnetwork')
        result.serviceAccountEmail = session.config.navigate('google.batch.serviceAccountEmail')*/
        return result
    }
/*
    @Override
    String toString(){
        return "BatchConfig[googleOpts=$googleOpts"
    }
*/

}

