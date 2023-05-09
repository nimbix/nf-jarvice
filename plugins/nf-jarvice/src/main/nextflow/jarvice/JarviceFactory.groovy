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
import nextflow.trace.TraceObserverFactory
/**
 * Implements the validation observer factory
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class JarviceFactory implements TraceObserverFactory {

    @Override
    Collection<TraceObserver> create(Session session) {
        final result = new ArrayList()
        result.add( new JarviceObserver() )
        //log.info "BEN - Factory "
        return result
    }
}
