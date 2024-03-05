package app.minion.core

import arrow.core.None
import arrow.core.Option

sealed class MinionError(
    open val message: String,
    open val throwable: Option<Throwable> = None
) {
    data class VaultReadError(override val message: String, override val throwable: Option<Throwable> = None) : MinionError(message, throwable)
}
