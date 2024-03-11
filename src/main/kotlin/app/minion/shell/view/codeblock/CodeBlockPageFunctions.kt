package app.minion.shell.view.codeblock

import app.minion.core.MinionError
import app.minion.core.model.FileData
import app.minion.core.store.MinionStore
import arrow.core.Either
import arrow.core.raise.either

interface CodeBlockPageFunctions { companion object {
    fun MinionStore.applyCodeBlockConfig(config: CodeBlockConfig) : Either<MinionError, List<FileData>> = either {
        emptyList()
    }
}}