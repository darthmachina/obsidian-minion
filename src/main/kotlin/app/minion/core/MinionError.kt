package app.minion.core

import arrow.core.None
import arrow.core.Option

sealed class MinionError(
    open val message: String,
    open val throwable: Option<Throwable> = None
)
