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

