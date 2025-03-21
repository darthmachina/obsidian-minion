package app.minion.shell.view.codeblock

import View
import app.minion.core.MinionError
import app.minion.core.formulas.FormulaEvaluator.Companion.eval
import app.minion.core.formulas.FormulaExpression
import app.minion.core.formulas.MinionFormulaGrammar
import app.minion.core.model.Content
import app.minion.core.model.DataviewField
import app.minion.core.model.DataviewValue
import app.minion.core.model.FileData
import app.minion.core.model.Filename
import app.minion.core.model.Tag
import app.minion.core.store.State
import app.minion.shell.functions.LogFunctions.Companion.logLeft
import app.minion.shell.view.Item
import app.minion.shell.view.ItemType
import app.minion.shell.view.Property
import app.minion.shell.view.PropertyType
import app.minion.shell.view.ViewItems
import app.minion.shell.view.codeblock.CodeBlockPageExcludeFunctions.Companion.applyExclude
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.populateProperties
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.toItems
import app.minion.shell.view.codeblock.CodeBlockPageFunctions.Companion.toPropertyList
import app.minion.shell.view.codeblock.CodeBlockPageIncludeFunctions.Companion.applyInclude
import arrow.core.Either
import arrow.core.flatten
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.some
import arrow.core.toOption
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import mu.KotlinLogging

private val logger = KotlinLogging.logger("CodeBlockPageFunctions")

interface CodeBlockPageFunctions { companion object {
    fun State.applyCodeBlockConfig(config: CodeBlockConfig)
    : Either<MinionError, List<ViewItems>> = either {
        logger.debug { "config: $config" }
        this@applyCodeBlockConfig
            .files
            .map { it.value }
            .applyExclude(config.exclude).bind()
            .applyInclude(config.include).bind()
            .applySort(config).bind()
            .applyGroupBy(config).bind()
            .toViewItems(config).bind()
    }

    fun Map<String, List<FileData>>.toViewItems(config: CodeBlockConfig)
    : Either<MinionError, List<ViewItems>> = either {
        config.groupByOrder
            .map { order ->
                val orderSplit = order.split(" AS ", ignoreCase = true)
                val orderName = if (orderSplit.size == 2) orderSplit[1] else orderSplit[0]
                this@toViewItems[orderSplit[0]]
                    .toOption()
                    .map { ViewItems(orderName, it.toItems(config).bind()) }
                    .getOrElse { ViewItems(orderName, emptyList()) }
            }
            .plus(
                this@toViewItems
                    .filter { entry ->
                        !config.groupByOrder
                            .map { it.split(" AS ", ignoreCase = true)[0] }
                            .contains(entry.key)
                    }
                    .map { entry -> ViewItems(entry.key, entry.value.toItems(config).bind()) }
            )
    }

    fun Set<Filename>.getFileData(fileData: Map<Filename, FileData>) : Either<MinionError, Set<FileData>> = either {
        fileData
            .filterKeys {
                this@getFileData.contains(it)
            }
            .values
            .toSet()
    }

    fun Set<Filename>.applyIncludeTags(tagCache: Map<Tag, Set<Filename>>, config: CodeBlockConfig)
    : Either<MinionError, Set<Filename>> = either {
        if (config.include.tags.isNotEmpty()) {
            tagCache
                .findFilesMatchingTags(config.include.tags.map { Tag(it) }.toSet()).bind()
                .let { tagFiles ->
                    this@applyIncludeTags
                        .filter {
                            tagFiles.contains(it)
                        }
                }
                .toSet()
       } else {
            this@applyIncludeTags
        }
    }

    fun Map<Tag, Set<Filename>>.findFilesMatchingTags(tags: Set<Tag>) : Either<MinionError, Set<Filename>> = either {
        this@findFilesMatchingTags
            .filterKeys { tag -> tags.contains(tag) }
            .values
            .flatten()
            .toSet()
    }

    fun List<FileData>.applySort(config: CodeBlockConfig) : Either<MinionError, List<FileData>> = either {
        if (config.sort.isNotEmpty()) {
            if (config.sort.contains(SORT_BY_EISENHOWER)) {
                raise(MinionError.ConfigError("$SORT_BY_EISENHOWER is not a valid page sort"))
            }

            // For now assume that any sort options are properties, as that is all to sort on for now.
            // Applies sorting in order defined in the sort list
            var comparator = compareBy<FileData, DataviewValue?>(nullsLast()) {
                it.dataview[DataviewField(config.sort.first())]
            }
            if (config.sort.size > 1) {
                for(i in 1..<config.sort.size) {
                    comparator = comparator.thenBy(nullsLast()) {
                        it.dataview[DataviewField(config.sort[i])]
                    }
                }
            }
            comparator = comparator.thenBy {
                it.name.v
            }
            this@applySort.sortedWith(comparator)
        } else {
            sortedWith(compareBy { it.name.v })
        }
    }

    fun List<FileData>.toItems(config: CodeBlockConfig) : Either<MinionError, List<Item>> = either {
        this@toItems
            .map { fileData ->
                Item(
                    ItemType.PAGE,
                    Content(fileData.name.v),
                    fileData.toPropertyList(config).bind(),
                    fileData = fileData.some()
                )
            }
    }

    fun FileData.toPropertyList(config: CodeBlockConfig) : Either<MinionError, List<Property>> = either {
        optionsToProperties(config).bind()
            .plus(populateProperties(config, config.createFormulas().bind()).bind())
    }

    fun CodeBlockConfig.createFormulas() : Either<MinionError, Map<String, FormulaExpression>> = either {
        val parser = MinionFormulaGrammar()
        properties
            .filter { it.contains(PROPERTY_FORMULA_TOKEN) }
            .map { it.split(PROPERTY_FORMULA_TOKEN) }
            .groupBy(keySelector = { it[0] }, valueTransform = { parser.parseToEnd(it[1]) })
            .mapValues { entry ->
                if (entry.value.size != 1) {
                    raise(MinionError.ConfigError("FormulaExpression issue for ${entry.key}"))
                } else {
                    entry.value.first()
                }
            }
    }

    fun FileData.populateProperties(config: CodeBlockConfig, formulas: Map<String, FormulaExpression>)
    : Either<MinionError, List<Property>> = either {
        config.properties.map { configProperty ->
            when (configProperty) {
                else -> {
                    if (configProperty.contains(PROPERTY_FORMULA_TOKEN)) {
                        getFormulaResultProperty(configProperty, formulas).bind()
                    } else {
                        getDataviewProperty(configProperty, config).bind()
                    }
                }
            }
        }
    }

    fun FileData.getFormulaResultProperty(property: String, formulas: Map<String, FormulaExpression>)
    : Either<MinionError, Property> = either {
        property
            .split(PROPERTY_FORMULA_TOKEN)
            .let { propertySplit ->
                formulas[propertySplit[0]]
                    .toOption()
                    .map { expression ->
                        Property(
                            PropertyType.FORMULA,
                            propertySplit[0],
                            expression
                                .eval(
                                    this@getFormulaResultProperty.toFormulaFieldMap().bind()
                                )
                                .map { it.toString() }
                                .logLeft(logger)
                                .getOrElse { "-" }
                        )
                    }
                    .getOrElse {
                        raise(MinionError.ConfigError("Cannot find formula for $propertySplit[0]"))
                    }
            }
    }

    fun FileData.getDataviewProperty(property: String, config: CodeBlockConfig)
    : Either<MinionError, Property> = either {
        this@getDataviewProperty.dataview[DataviewField(property)]
            .toOption()
            .map {
                Property(
                    PropertyType.DATAVIEW,
                    property,
                    it.v
                )
            }.getOrElse {
                Property(
                    PropertyType.DATAVIEW,
                    property,
                    "-"
                )
            }
    }

    fun FileData.toFormulaFieldMap() : Either<MinionError, Map<String, String>> = either {
        this@toFormulaFieldMap
            .dataview
            .mapKeys { it.key.v }
            .mapValues { it.value.v }
    }

    fun FileData.optionsToProperties(config: CodeBlockConfig) : Either<MinionError, List<Property>> = either {
        config.options.mapNotNull { option ->
            when (option) {
                CodeBlockOptions.image_on_cover -> {
                    this@optionsToProperties.dataview
                        .filterKeys { it.v == FIELD_IMAGE || it.v == FIELD_BANNER }
                        .mapValues { entry ->
                            logger.debug { "Found image property : $entry" }
                            Property(PropertyType.IMAGE, entry.key.v, entry.value.v)
                        }
                        .toList()
                        .map { item ->
                            item.second
                        }
                        .let { list ->
                            if (list.isEmpty()) {
                                null
                            } else {
                                list.first()
                            }
                        }
                }
            }
        }
    }

    fun List<FileData>.applyGroupBy(config: CodeBlockConfig)
    : Either<MinionError, Map<String, List<FileData>>> = either {
        if (config.groupBy == GroupByOptions.NONE) {
            mapOf(GROUP_BY_SINGLE to this@applyGroupBy)
        } else {
            when (config.groupBy) {
                GroupByOptions.dataview -> {
                    this@applyGroupBy
                        .applyGroupByForDataview(config).bind()
                        .mapKeys { it.key.v }
                }
                else -> raise(MinionError.ConfigError("${config.groupBy} not implement yet"))
            }
            // TODO Sort by groupByOrder
        }
    }

    /**
     * Groups a Set of FileData by the criteria specified in the config
     */
    fun List<FileData>.applyGroupByForDataview(config: CodeBlockConfig)
    : Either<MinionError, Map<DataviewValue, List<FileData>>> = either {
        if (config.groupBy == GroupByOptions.NONE) {
            raise(MinionError.ConfigError("applyGroupBy called with no groupBy specified"))
        }
        if (config.groupBy == GroupByOptions.dataview && config.groupByField.isEmpty()) {
            raise(MinionError.ConfigError("groupBy.dataview specified with no groupByField"))
        }
        // Collect valid group values
        // Group FileData into buckets by group values
        // Return map
        this@applyGroupByForDataview
            .groupBy { fileData ->
                fileData
                    .dataview[DataviewField(config.groupByField)]
                    .toOption()
                    .getOrElse {
                        DataviewValue(GROUP_BY_UNKNOWN)
                    }
            }
    }
}}
