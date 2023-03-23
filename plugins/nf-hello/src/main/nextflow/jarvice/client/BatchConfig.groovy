package nextflow.jarvice.client

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.util.MemoryUnit

@Slf4j
@CompileStatic
class BatchConfig {

    static BatchConfig create(Session session) {
        final result = new BatchConfig()
        return result
    }

}

