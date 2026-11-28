package com.kelsuita.starswish.data

/** Reorders one list of wishes while keeping the other list in its original slots. */
internal fun reorderGoalSlice(goals: List<SavingGoal>, ids: List<String>, archived: Boolean): List<SavingGoal> {
    val byId = goals.filter { it.archived == archived }.associateBy { it.id }
    require(ids.size == byId.size && ids.toSet() == byId.keys)
    val sorted = ids.iterator()
    return goals.map { goal -> if (goal.archived == archived) byId.getValue(sorted.next()) else goal }
}
