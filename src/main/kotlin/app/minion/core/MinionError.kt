package app.minion.core

import arrow.core.None
import arrow.core.Option

sealed class MinionError(
    open val message: String,
    open val throwable: Option<Throwable> = None,
    open val parent: Option<MinionError> = None
) {
    data class LoadSettingsError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class VaultReadError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class VaultWriteError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class VaultTaskReadError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class TaskConversionError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class DateParseError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class TaskModificationError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class RepeatDateError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class NoSubtasksError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class TagNotFoundError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class TagPrefixNotFoundError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class RepeatInfoParseError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class ConfigError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class ConfigParseError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class GroupByNotFoundError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class ImageNotFoundError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class FileNotFoundError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class FieldValueCoercionError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)

    data class FieldMissingError(
        override val message: String, override val throwable: Option<Throwable> = None,
        override val parent: Option<MinionError> = None
    ) : MinionError(message, throwable, parent)
}
