package com.example.data.model

const val MAX_TASK_PREDECESSORS = 4

/** Check the current workspace graph iteratively. A dangling reference cannot close a cycle. */
internal fun wouldCreateTaskCycle(taskId: String, candidateId: String, tasksById: Map<String, StudyTask>): Boolean {
    val pending = ArrayDeque<String>()
    val visited = mutableSetOf<String>()
    pending.add(candidateId)
    while (pending.isNotEmpty()) {
        val id = pending.removeLast()
        if (id == taskId) return true
        if (visited.add(id)) tasksById[id]?.predecessorIds?.let(pending::addAll)
    }
    return false
}
