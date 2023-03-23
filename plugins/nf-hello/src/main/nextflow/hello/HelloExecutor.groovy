package nextflow.hello

import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.exception.AbortOperationException
import nextflow.executor.Executor
import nextflow.extension.FilesEx
import nextflow.fusion.FusionHelper
import nextflow.processor.TaskHandler
import nextflow.processor.TaskMonitor
import nextflow.processor.TaskPollingMonitor
import nextflow.processor.TaskRun
import nextflow.util.Duration
import nextflow.util.ServiceName
import org.pf4j.ExtensionPoint

import nextflow.hello.client.BatchClient
import nextflow.hello.client.BatchConfig

@Slf4j
@ServiceName(value='hello')
@CompileStatic
class HelloExecutor extends Executor implements ExtensionPoint {

    private BatchClient client
    private BatchConfig config

    private Path remoteBinDir

    BatchClient getClient() { 
        return client 
    }

    BatchConfig getConfig() { return config }


    protected void createConfig() {
        this.config = BatchConfig.create(session)
        log.debug "[JARVICE BATCH] Executor config=$config"
    }

    protected void createClient() {
        this.client = new BatchClient(config)
    }

    @Override
    protected void register() {
        super.register()
        createConfig()
        createClient()
    }

    @Override
    final boolean isContainerNative() {
        return true
    }

    @Override
    String containerConfigEngine() {
        return 'docker'
    }

    @Override
    final Path getWorkDir() {
        return session.bucketDir ?: session.workDir
    }

    @Override
    void shutdown() {
        log.info "[BEN-BUG-1] - executor shutdown"
        //client.shutdown()
    }

    @Override
    TaskHandler createTaskHandler(TaskRun task) {
        return new HelloTaskHandler(task, this)
    }

    @Override
    protected TaskMonitor createTaskMonitor() {
        TaskPollingMonitor.create(session, name, 1000, Duration.of('10 sec'))
    }

    @Override
    boolean isFusionEnabled() {
        return FusionHelper.isFusionEnabled(session)
    }

}
