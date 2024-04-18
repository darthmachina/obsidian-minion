package app.minion.core.functions

import app.minion.shell.view.ViewItems

interface StatisticsFunctions { companion object {
    fun Map<String, Set<Any>>.calculateTotalCount() : Int {
        return this
            .mapValues { it.value.size }
            .map { it.value }
            .sum()
    }

    fun Map<String, List<Any>>.calculateTotalCount() : Int {
        return this
            .mapValues { it.value.size }
            .map { it.value }
            .sum()
    }

    fun List<ViewItems>.calculateTotalCount() : Int {
        return this.sumOf { it.items.size }
    }
}}
