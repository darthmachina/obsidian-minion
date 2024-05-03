package app.minion.core.functions

import app.minion.core.model.todoist.Project
import app.minion.core.model.todoist.Section

interface SectionFunctions { companion object {
    fun List<Section>.filterByProject(project: Project) : List<Section> {
        return this
            .filter { it.project.id == project.id }
    }
}}