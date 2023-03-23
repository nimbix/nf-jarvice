package nextflow.jarvice

import java.nio.file.Path
import java.nio.file.Paths
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.executor.BashWrapperBuilder
import nextflow.extension.FilesEx
import nextflow.processor.TaskBean
import nextflow.processor.TaskRun
import nextflow.util.Escape
import nextflow.util.PathTrie

@Slf4j
@CompileStatic
class JarviceBatchScriptLauncher extends BashWrapperBuilder {

    protected JarviceBatchScriptLauncher() {}

    /*
    BEN: this part allows to copy and alter original bean with our needs to
    create the final launcher bean.
    */
    JarviceBatchScriptLauncher(TaskBean bean, Path remoteBinDir) {
        super(bean)       
        bean.headerScript = headerScript(bean)
    }

    protected String headerScript(TaskBean bean) {
        def result = "NXF_CHDIR=${Escape.path(bean.workDir)}\n"
        return result
    }

    String runCommand() {
        "/bin/bash ${TaskRun.CMD_RUN} 2>&1 | tee ${TaskRun.CMD_LOG}"
    }

}
