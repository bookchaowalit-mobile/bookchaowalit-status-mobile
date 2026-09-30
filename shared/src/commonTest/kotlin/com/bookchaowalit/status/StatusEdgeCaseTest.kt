package com.bookchaowalit.status

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class StatusEdgeCaseTest {
    private val day = 24L * 60 * 60 * 1000

    @Test
    fun nearlyPerfectUptimeIsNeverShownAsHundredPercent() {
        assertEquals("99.99%", formatUptime(99.999))
        assertEquals("99.99%", formatUptime(9_999_999 * 100.0 / 10_000_000)) // 1 failure in 10M checks
        assertEquals("100.00%", formatUptime(100.0))
        assertEquals("0.00%", formatUptime(0.0))
        assertEquals("0.00%", formatUptime(0.004))
    }

    @Test
    fun emptyInputs() {
        assertEquals(OverallStatus.ALL_OPERATIONAL, StatusCalculator.overall(emptyList()))
        assertNull(StatusCalculator.uptimePercent(emptyList(), 0, day))
        assertNull(StatusCalculator.averageLatencyMs(emptyList()))
        assertEquals(emptyList(), StatusCalculator.sortIncidents(emptyList()))
        assertEquals(emptySet(), StatusCalculator.affectedComponentIds(emptyList()))
    }

    @Test
    fun uptimeWindowIsHalfOpenAndValidated() {
        val checks = listOf(Check(0, ok = false), Check(day, ok = true))
        assertEquals(0.0, StatusCalculator.uptimePercent(checks, 0, day))
        assertEquals(100.0, StatusCalculator.uptimePercent(checks, day, 2 * day))
        assertNull(StatusCalculator.uptimePercent(checks, 5, 5)) // empty window
        assertFailsWith<IllegalArgumentException> { StatusCalculator.uptimePercent(checks, 10, 5) }
    }

    @Test
    fun dailyUptimeHasOneBarPerDayWithGapsAsNull() {
        val now = 10 * day
        val checks = listOf(Check(now - 1, ok = true), Check(now - 3 * day + 1, ok = false))
        val bars = StatusCalculator.dailyUptime(checks, now, 3)
        assertEquals(listOf(0.0, null, 100.0), bars)
        assertFailsWith<IllegalArgumentException> { StatusCalculator.dailyUptime(checks, now, 0) }
    }

    @Test
    fun incidentTimelineIgnoresInputOrderAndReopening() {
        val reopened = Incident(
            "i", "API errors", setOf("api"),
            listOf(
                IncidentUpdate(300, IncidentStage.INVESTIGATING, "errors again"),
                IncidentUpdate(100, IncidentStage.INVESTIGATING, "errors"),
                IncidentUpdate(200, IncidentStage.RESOLVED, "fixed"),
            ),
        )
        assertEquals(IncidentStage.INVESTIGATING, reopened.currentStage)
        assertNull(reopened.durationMs)
        assertEquals(setOf("api"), StatusCalculator.affectedComponentIds(listOf(reopened)))
        assertFailsWith<IllegalArgumentException> { Incident("x", "x", emptySet(), emptyList()) }
    }

    @Test
    fun maintenanceOnlyPage() {
        val comps = listOf(Component("a", "A", ComponentStatus.MAINTENANCE), Component("b", "B", ComponentStatus.OPERATIONAL))
        assertEquals(OverallStatus.MAINTENANCE, StatusCalculator.overall(comps))
    }
}
