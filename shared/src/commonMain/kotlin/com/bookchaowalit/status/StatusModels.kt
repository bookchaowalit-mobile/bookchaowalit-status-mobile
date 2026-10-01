package com.bookchaowalit.status

/** Health of a single monitored component, ordered from best to worst. */
enum class ComponentStatus(val label: String) {
    OPERATIONAL("Operational"),
    DEGRADED("Degraded performance"),
    PARTIAL_OUTAGE("Partial outage"),
    MAJOR_OUTAGE("Major outage"),
    MAINTENANCE("Under maintenance"),
}

data class Component(val id: String, val name: String, val status: ComponentStatus)

/** One health-check probe result. [timestampMs] is epoch milliseconds. */
data class Check(val timestampMs: Long, val ok: Boolean, val latencyMs: Long? = null)

enum class IncidentStage { INVESTIGATING, IDENTIFIED, MONITORING, RESOLVED }

data class IncidentUpdate(val timestampMs: Long, val stage: IncidentStage, val message: String)

data class Incident(
    val id: String,
    val title: String,
    val componentIds: Set<String>,
    val updates: List<IncidentUpdate>,
) {
    init {
        require(updates.isNotEmpty()) { "incident $id needs at least one update" }
    }

    /**
     * Updates sorted oldest-first, regardless of input order. Updates sharing a
     * timestamp are ordered by stage so e.g. "resolved" always follows
     * "monitoring" and input order cannot reopen a resolved incident.
     */
    val timeline: List<IncidentUpdate>
        get() = updates.sortedWith(compareBy<IncidentUpdate> { it.timestampMs }.thenBy { it.stage.ordinal })

    val currentStage: IncidentStage get() = timeline.last().stage

    val isResolved: Boolean get() = currentStage == IncidentStage.RESOLVED

    /** Milliseconds from first report to resolution, or null while still open. */
    val durationMs: Long?
        get() = if (isResolved) timeline.last().timestampMs - timeline.first().timestampMs else null
}
