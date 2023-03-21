package nextflow.hello

import groovy.transform.CompileStatic
import nextflow.executor.BashWrapperBuilder
import nextflow.processor.TaskBean
import nextflow.processor.TaskRun

@CompileStatic
class HelloBashBuilder extends BashWrapperBuilder {

    HelloBashBuilder(TaskRun task) {
        super(new TaskBean(task))
    }

    HelloBashBuilder(TaskBean task) {
        super(task)
    }

}

