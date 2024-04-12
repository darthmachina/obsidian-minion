package app.minion.shell.view

import app.minion.core.model.Content

enum class ItemType {
    TASK,
    PAGE,
    NOTE
}

enum class PropertyType {
    DUE,
    TAGS,
    SOURCE,
    DATAVIEW,
    IMAGE,
    FORMULA
}

data class Property(
    val type: PropertyType,
    val value: String
)

data class Item(
    val type: ItemType,
    val content: Content,
    val properties: List<Property>,
    val subItems: List<Item> = emptyList(),
    val notes: List<Item> = emptyList()
)

data class ViewItems(
    val group: String,
    val items: List<Item>
)
