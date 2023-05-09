/*
 * Copyright (c) 2023 Nimbix, Inc.  All Rights Reserved.
 *
 * Inspired from nf-google plugin,
 * from https://github.com/nextflow-io/nextflow/blob/master/plugins/nf-google
 * which is licensed under Apache License, Version 2.0
 *
 */

package nextflow.jarvice

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.Session
import nextflow.trace.TraceObserver

/**
 * Example workflow events observer
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class JarviceObserver implements TraceObserver {

    @Override
    void onFlowCreate(Session session) {
        log.info """
       _  _   _____   _____ ___ ___
    _ | |/_\\ | _ \\ \\ / /_ _/ __| __|
   | || / _ \\|   /\\ V / | | (__| _|
    \\__/_/ \\_\\_|_\\ \\_/ |___\\___|___|

         Jarvice Nextflow plugin
        """
        log.info "Pipeline is starting!\n\n"
    }

    @Override
    void onFlowComplete() {
        log.info "\n\nPipeline completed!"
    }
}
