package app.minion.shell.view

import app.minion.core.model.Content
import app.minion.core.model.FileData
import app.minion.core.model.Task
import arrow.core.None
import arrow.core.Option

enum class ItemType {
    TASK,
    PAGE,
    NOTE
}

enum class PropertyType {
    DUE,
    DUE_IN_PAST,
    TAGS,
    SOURCE,
    DATAVIEW,
    IMAGE,
    FORMULA,
    LIFE_AREA_COLOR,
    EISENHOWER,
    REPEAT_INFO,
    COMPLETED_SUBTASK_PERCENT,
}

data class Property(
    val type: PropertyType,
    val name: String,
    val value: String
)

data class Item(
    val type: ItemType,
    val content: Content,
    val properties: List<Property>,
    val subItems: List<Item> = emptyList(),
    val notes: List<Item> = emptyList(),
    val fileData: Option<FileData> = None,
    val task: Option<Task> = None
)

data class ViewItems(
    val group: String,
    val items: List<Item>
)
