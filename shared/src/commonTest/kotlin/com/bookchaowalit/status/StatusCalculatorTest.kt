package com.bookchaowalit.status

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StatusCalculatorTest {
    private fun c(id: String, s: ComponentStatus) = Component(id, id.uppercase(), s)

    @Test
    fun overallIsWorstComponent() {
        assertEquals(OverallStatus.ALL_OPERATIONAL, StatusCalculator.overall(emptyList()))
        assertEquals(
            OverallStatus.ALL_OPERATIONAL,
            StatusCalculator.overall(listOf(c("api", ComponentStatus.OPERATIONAL))),
        )
        assertEquals(
            OverallStatus.DEGRADED,
            StatusCalculator.overall(listOf(c("api", ComponentStatus.OPERATIONAL), c("db", ComponentStatus.DEGRADED))),
        )
        assertEquals(
            OverallStatus.MAJOR_OUTAGE,
            StatusCalculator.overall(
                listOf(c("a", ComponentStatus.PARTIAL_OUTAGE), c("b", ComponentStatus.MAJOR_OUTAGE), c("m", ComponentStatus.MAINTENANCE)),
            ),
        )
    }

    @Test
    fun outageOutranksMaintenance() {
        assertEquals(
            OverallStatus.MAINTENANCE,
            StatusCalculator.overall(listOf(c("a", ComponentStatus.OPERATIONAL), c("m", ComponentStatus.MAINTENANCE))),
        )
        assertEquals(
            OverallStatus.PARTIAL_OUTAGE,
            StatusCalculator.overall(listOf(c("m", ComponentStatus.MAINTENANCE), c("a", ComponentStatus.PARTIAL_OUTAGE))),
        )
    }

    @Test
    fun uptimeCountsOnlyChecksInsideWindow() {
        val checks = listOf(
            Check(0, ok = true), Check(10, ok = false), Check(20, ok = true), Check(30, ok = true),
            Check(100, ok = false), // outside
        )
        assertEquals(75.0, StatusCalculator.uptimePercent(checks, 0, 100))
        assertNull(StatusCalculator.uptimePercent(checks, 200, 300))
        assertFailsWith<IllegalArgumentException> { StatusCalculator.uptimePercent(checks, 5, 1) }
    }

    @Test
    fun dailyUptimeIsOldestFirst() {
        val day = 24L * 60 * 60 * 1000
        val now = 3 * day
        val checks = listOf(
            Check(0, ok = false), Check(1, ok = true), // day 1 -> 50%
            Check(day + 5, ok = true), // day 2 -> 100%
            // day 3 empty -> null
        )
        assertEquals(listOf(50.0, 100.0, null), StatusCalculator.dailyUptime(checks, now, 3))
    }

    @Test
    fun averageLatencyIgnoresFailuresAndMissingSamples() {
        val checks = listOf(
            Check(0, true, 100), Check(1, true, 300), Check(2, false, 5000), Check(3, true, null),
        )
        assertEquals(200.0, StatusCalculator.averageLatencyMs(checks))
        assertNull(StatusCalculator.averageLatencyMs(listOf(Check(0, false, 10))))
    }

    @Test
    fun incidentTimelineAndDuration() {
        val incident = Incident(
            id = "inc-1",
            title = "API errors",
            componentIds = setOf("api"),
            updates = listOf(
                IncidentUpdate(300, IncidentStage.RESOLVED, "Fixed"),
                IncidentUpdate(100, IncidentStage.INVESTIGATING, "Looking"),
                IncidentUpdate(200, IncidentStage.MONITORING, "Deployed fix"),
            ),
        )
        assertEquals(IncidentStage.RESOLVED, incident.currentStage)
        assertTrue(incident.isResolved)
        assertEquals(200L, incident.durationMs)
        assertEquals(listOf(100L, 200L, 300L), incident.timeline.map { it.timestampMs })
        assertFailsWith<IllegalArgumentException> { Incident("x", "x", emptySet(), emptyList()) }
    }

    @Test
    fun openIncidentsSortFirstAndMarkAffectedComponents() {
        val resolved = Incident("r", "old", setOf("db"), listOf(IncidentUpdate(900, IncidentStage.RESOLVED, "done")))
        val openOld = Incident("o1", "a", setOf("api"), listOf(IncidentUpdate(100, IncidentStage.INVESTIGATING, "?")))
        val openNew = Incident("o2", "b", setOf("cdn", "api"), listOf(IncidentUpdate(500, IncidentStage.IDENTIFIED, "!")))
        val sorted = StatusCalculator.sortIncidents(listOf(resolved, openOld, openNew))
        assertEquals(listOf("o2", "o1", "r"), sorted.map { it.id })
        assertEquals(setOf("api", "cdn"), StatusCalculator.affectedComponentIds(sorted))
        assertFalse(openNew.isResolved)
        assertNull(openNew.durationMs)
    }

    @Test
    fun formatsUptime() {
        assertEquals("No data", formatUptime(null))
        assertEquals("100.00%", formatUptime(100.0))
        assertEquals("99.95%", formatUptime(99.949))
        assertEquals("75.00%", formatUptime(75.0))
        assertEquals("0.05%", formatUptime(0.05))
    }
}
