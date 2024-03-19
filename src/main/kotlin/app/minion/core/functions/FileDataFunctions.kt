package app.minion.core.functions

import app.minion.core.MinionError
import app.minion.core.functions.TaskFunctions.Companion.maybeAddDataviewValues
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.MinionSettings
import arrow.core.Either
import arrow.core.raise.either

interface FileDataFunctions { companion object {
    fun FileData.addPageTags(settings: MinionSettings, dataview: Map<DataviewField, DataviewValue>)
    : Either<MinionError, FileData> = either {
        this@addPageTags.copy(
            tasks = this@addPageTags.tasks.maybeAddDataviewValues(settings, dataview).bind()
        )
    }
}}