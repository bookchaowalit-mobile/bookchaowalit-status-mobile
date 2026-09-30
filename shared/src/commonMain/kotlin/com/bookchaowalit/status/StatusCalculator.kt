package com.bookchaowalit.status

/** Overall page banner derived from component states. */
enum class OverallStatus(val headline: String) {
    ALL_OPERATIONAL("All systems operational"),
    MAINTENANCE("Scheduled maintenance in progress"),
    DEGRADED("Some systems degraded"),
    PARTIAL_OUTAGE("Partial system outage"),
    MAJOR_OUTAGE("Major system outage"),
}

object StatusCalculator {

    /**
     * Worst component wins. Outages outrank maintenance, maintenance outranks
     * an all-green page. An empty component list reports all operational.
     */
    fun overall(components: List<Component>): OverallStatus {
        val states = components.map { it.status }.toSet()
        return when {
            ComponentStatus.MAJOR_OUTAGE in states -> OverallStatus.MAJOR_OUTAGE
            ComponentStatus.PARTIAL_OUTAGE in states -> OverallStatus.PARTIAL_OUTAGE
            ComponentStatus.DEGRADED in states -> OverallStatus.DEGRADED
            ComponentStatus.MAINTENANCE in states -> OverallStatus.MAINTENANCE
            else -> OverallStatus.ALL_OPERATIONAL
        }
    }

    /**
     * Uptime percentage (0.0..100.0) over the checks whose timestamp falls in
     * [fromMs, toMs). Returns null when there are no checks in the window so the
     * UI can show "no data" instead of a misleading 100%.
     */
    fun uptimePercent(checks: List<Check>, fromMs: Long, toMs: Long): Double? {
        require(fromMs <= toMs) { "fromMs must be <= toMs" }
        val window = checks.filter { it.timestampMs in fromMs until toMs }
        if (window.isEmpty()) return null
        return window.count { it.ok } * 100.0 / window.size
    }

    /**
     * Daily uptime bars (oldest first) for the last [days] days ending at [nowMs],
     * as rendered by status pages' 90-day history strip.
     */
    fun dailyUptime(checks: List<Check>, nowMs: Long, days: Int): List<Double?> {
        require(days > 0) { "days must be positive" }
        val dayMs = 24L * 60 * 60 * 1000
        return (days - 1 downTo 0).map { back ->
            val end = nowMs - back * dayMs
            uptimePercent(checks, end - dayMs, end)
        }
    }

    /** Average latency of successful checks that reported one, or null. */
    fun averageLatencyMs(checks: List<Check>): Double? {
        val samples = checks.filter { it.ok }.mapNotNull { it.latencyMs }
        return if (samples.isEmpty()) null else samples.average()
    }

    /** Open incidents first (newest activity first), then resolved ones. */
    fun sortIncidents(incidents: List<Incident>): List<Incident> =
        incidents.sortedWith(
            compareBy<Incident> { it.isResolved }
                .thenByDescending { it.timeline.last().timestampMs },
        )

    /** Components that are touched by at least one unresolved incident. */
    fun affectedComponentIds(incidents: List<Incident>): Set<String> =
        incidents.filterNot { it.isResolved }.flatMap { it.componentIds }.toSet()
}

/**
 * Formats an uptime value for display, e.g. 99.95 -> "99.95%", null -> "No data".
 * Rounds to two decimals, except that anything below 100 is capped at "99.99%"
 * so a window with failed checks is never shown as a perfect "100.00%", and
 * anything above 0 is raised to "0.01%" so a window with some successful
 * checks is never shown as a total "0.00%" outage. NaN shows "No data".
 */
fun formatUptime(percent: Double?): String {
    if (percent == null || percent.isNaN()) return "No data"
    val rounded = kotlin.math.round(percent * 100).toLong()
    val hundredths = when {
        percent < 100.0 && rounded >= 10_000 -> 9_999L
        percent > 0.0 && rounded <= 0 -> 1L
        else -> rounded
    }
    val whole = hundredths / 100
    val frac = (hundredths % 100).toString().padStart(2, '0')
    return "$whole.$frac%"
}
