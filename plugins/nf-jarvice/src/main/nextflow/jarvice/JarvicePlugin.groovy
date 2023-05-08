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
import nextflow.plugin.BasePlugin
import nextflow.plugin.Scoped
import org.pf4j.PluginWrapper

/**
 * Implements the Jarvice plugins entry point
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class JarvicePlugin extends BasePlugin {

    JarvicePlugin(PluginWrapper wrapper) {
        super(wrapper)
    }
}
