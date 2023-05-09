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

