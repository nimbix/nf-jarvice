package nextflow.hello

import java.nio.file.Path
import java.nio.file.Paths

/*import com.google.cloud.batch.v1.GCS
import com.google.cloud.batch.v1.Volume
import com.google.cloud.storage.contrib.nio.CloudStoragePath */
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.executor.BashWrapperBuilder
import nextflow.extension.FilesEx
import nextflow.processor.TaskBean
import nextflow.processor.TaskRun
import nextflow.util.Escape
import nextflow.util.PathTrie

/**
 * Implement Nextflow task launcher script
 * 
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */

@Slf4j
@CompileStatic
class HelloBatchScriptLauncher extends BashWrapperBuilder {

    //private CloudStoragePath remoteWorkDir


/*    private static final String MOUNT_ROOT = '/mnt/disks'

    private CloudStoragePath remoteWorkDir
    private Path remoteBinDir
    private Set<String> buckets = new HashSet<>()
    private PathTrie pathTrie = new PathTrie() */

    /* ONLY FOR TESTING - DO NOT USE */
    protected HelloBatchScriptLauncher() {}

    /*
    BEN: this part allows to copy and alter original bean with our needs to
    create the final launcher bean.
    */
    HelloBatchScriptLauncher(TaskBean bean, Path remoteBinDir) {
        super(bean)       
        //this.remoteWorkDir = bean.workDir

        bean.headerScript = headerScript(bean)


//        log.info "[BEN] remote dir {}" , bean.workDir
//        log.info "[BEN-HelloBatchScriptLauncher] - main"
//        log.info "[BEN-HelloBatchScriptLauncher 1] {}" , this.script
//        log.info "[BEN-HelloBatchScriptLauncher 2] {}" , this.headerScript
//        log.info "[BEN-HelloBatchScriptLauncher 3] {}" , bean.wrapperScript
//        log.info "[BEN-HelloBatchScriptLauncher 4] {}" , this.beforeScript
//        log.info "[BEN-HelloBatchScriptLauncher 5] {}" , this.afterScript
        // keep track the google storage work dir
        //this.remoteWorkDir = (CloudStoragePath) bean.workDir
        //this.remoteBinDir = toContainerMount(remoteBinDir)

        // map bean work and target dirs to container mount
        // this is needed to create the command launcher using container local file paths
        //bean.workDir = toContainerMount(bean.workDir)
        //bean.targetDir = toContainerMount(bean.targetDir)
    }

    protected String headerScript(TaskBean bean) {
        def result = "NXF_CHDIR=${Escape.path(bean.workDir)}\n"
        return result
    }

//    @Override
    String runCommand() {
        "/bin/bash ${TaskRun.CMD_RUN} 2>&1 | tee ${TaskRun.CMD_LOG}"
    }

}
