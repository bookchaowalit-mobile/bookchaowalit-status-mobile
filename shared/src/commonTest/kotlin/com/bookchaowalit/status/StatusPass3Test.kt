package com.bookchaowalit.status

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatusPass3Test {

    @Test
    fun tinyButNonZeroUptimeIsNeverShownAsZero() {
        // 1 successful check out of 100,000 must not read as a total outage.
        assertEquals("0.01%", formatUptime(1 * 100.0 / 100_000))
        assertEquals("0.01%", formatUptime(0.004))
        assertEquals("0.00%", formatUptime(0.0))
        assertEquals("0.01%", formatUptime(0.006))
    }

    @Test
    fun invalidUptimeValuesShowNoData() {
        assertEquals("No data", formatUptime(Double.NaN))
    }

    @Test
    fun sameTimestampUpdatesResolveToTheLaterStage() {
        // APIs with second precision often stamp "monitoring" and "resolved" identically;
        // input order must not decide whether the incident is still open.
        val incident = Incident(
            id = "i1",
            title = "API errors",
            componentIds = setOf("api"),
            updates = listOf(
                IncidentUpdate(1_000, IncidentStage.INVESTIGATING, "Looking"),
                IncidentUpdate(5_000, IncidentStage.RESOLVED, "Fixed"),
                IncidentUpdate(5_000, IncidentStage.MONITORING, "Watching"),
            ),
        )
        assertEquals(IncidentStage.RESOLVED, incident.currentStage)
        assertTrue(incident.isResolved)
        assertEquals(4_000, incident.durationMs)
        assertEquals(emptySet(), StatusCalculator.affectedComponentIds(listOf(incident)))
    }
}
