package app.minion.core.functions

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
}}
