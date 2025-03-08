package app.minion.shell.functions

import App
import MarkdownView
import MetadataCache
import app.minion.core.model.Filename
import arrow.core.getOrElse
import arrow.core.toOption
import mu.KotlinLogging

private val logger = KotlinLogging.logger("VaultFunctions")

interface VaultFunctions { companion object {
    fun sourceFileExists(path: Filename, metadataCache: MetadataCache) : Boolean {
        return metadataCache
            .getFirstLinkpathDest(path.v, "")
            .toOption()
            .map { true }
            .getOrElse { false }
    }

    fun openSourceFile(path: Filename, app: App) {
        logger.debug { "openSourceFile()" }
        app.metadataCache.getFirstLinkpathDest(path.v, "")
            .toOption()
            .map { file ->
                app.workspace.getLeavesOfType("markdown")
                    .filter {
                        if (it.view is MarkdownView) (it.view as MarkdownView).file.path == file.path else false
                    }
                    .let { leafList ->
                        if (leafList.isNotEmpty()) {
                            app.workspace.setActiveLeaf(leafList[0])
                        } else {
                            app.workspace.getLeaf(true).openFile(file)
                        }
                    }
            }
            .getOrElse {
                logger.error { "Source path not found: $path" }
            }
    }
}}
