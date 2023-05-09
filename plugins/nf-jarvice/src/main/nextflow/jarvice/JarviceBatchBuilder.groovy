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
import nextflow.executor.BashWrapperBuilder
import nextflow.processor.TaskBean
import nextflow.processor.TaskRun

@CompileStatic
class JarviceBashBuilder extends BashWrapperBuilder {

    JarviceBashBuilder(TaskRun task) {
        super(new TaskBean(task))
    }

    JarviceBashBuilder(TaskBean task) {
        super(task)
    }

}

