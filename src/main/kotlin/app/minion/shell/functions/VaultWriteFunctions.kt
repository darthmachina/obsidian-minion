package app.minion.shell.functions

import MetadataCache
import TFile
import Vault
import app.minion.core.MinionError
import app.minion.core.model.FileData
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.toOption
import kotlinx.coroutines.await
import mu.KotlinLogging

private val logger = KotlinLogging.logger("VaultWriteFunctions")
interface VaultWriteFunctions { companion object {
    fun String.replaceLine(newLine: String, line: Int) : String {
        return this
            .split("\n")
            .toMutableList()
            .let {
                it[line] = newLine
                it
            }
            .joinToString("\n")
    }

    suspend fun String.writeToVault(file: FileData, vault: Vault, metadataCache: MetadataCache)
    : Either<MinionError, String> = either {
        metadataCache
            .getFirstLinkpathDest(file.path.v, "")
            .toOption()
            .toEither {
                MinionError.VaultReadError("Error reading ${file.path.v}")
            }
            .map { tfile ->
                this@writeToVault.writeToVault(vault, tfile)
            }.bind()
    }

    /**
     * Writes the string to a file in the vault.
     * Basically converts vault.modify() into a receiver function to allow it to be called in a
     * functional flow.
     *
     * @param vault The vault
     * @param file The file to write to
     * @this The file contents to write
     */
    suspend fun String.writeToVault(vault: Vault, file: TFile) : String {
        logger.info { "writeToVault()" }
        vault.modify(file, this).await()
        return this
    }

    /**
     * Replaces a line in a file and writes out the new content.
     *
     * @receiver The file to write
     * @param vault The Vault to act on
     * @param contents The contents of the new line
     * @param line The line to write the contents
     * @return The updated file contents
     */
    suspend fun TFile.writeLine(vault: Vault, contents: String, line: Int) : String {
        return vault
            .read(this)
            .then { it.replaceLine(contents, line) }
            .await()
            .let {
                it.writeToVault(vault, this)
                it
            }
    }
}}
