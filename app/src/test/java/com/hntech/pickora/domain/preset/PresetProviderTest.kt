package com.hntech.pickora.domain.preset

import org.junit.Assert.*
import org.junit.Test

class PresetProviderTest {

    @Test
    fun `getAllPresets returns 8 presets`() {
        val presets = PresetProvider.getAllPresets()
        assertEquals(8, presets.size)
    }

    @Test
    fun `all presets have unique ids`() {
        val presets = PresetProvider.getAllPresets()
        val ids = presets.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `all presets have non-empty emoji`() {
        PresetProvider.getAllPresets().forEach { preset ->
            assertTrue("Preset ${preset.id} should have emoji", preset.emoji.isNotBlank())
        }
    }

    @Test
    fun `all presets have non-empty itemResIds`() {
        PresetProvider.getAllPresets().forEach { preset ->
            assertTrue("Preset ${preset.id} should have items", preset.itemResIds.isNotEmpty())
        }
    }

    @Test
    fun `all presets have valid nameRes`() {
        PresetProvider.getAllPresets().forEach { preset ->
            assertTrue("Preset ${preset.id} should have nameRes > 0", preset.nameRes > 0)
        }
    }

    @Test
    fun `what_to_eat preset has 10 items`() {
        val preset = PresetProvider.getAllPresets().find { it.id == "what_to_eat" }
        assertNotNull(preset)
        assertEquals(10, preset!!.itemResIds.size)
    }

    @Test
    fun `truth_or_dare preset has 2 items`() {
        val preset = PresetProvider.getAllPresets().find { it.id == "truth_or_dare" }
        assertNotNull(preset)
        assertEquals(2, preset!!.itemResIds.size)
    }

    @Test
    fun `who_pays preset targets name route`() {
        val preset = PresetProvider.getAllPresets().find { it.id == "who_pays" }
        assertNotNull(preset)
        assertEquals("name", preset!!.targetRoute)
    }

    @Test
    fun `most presets target wheel route by default`() {
        val presets = PresetProvider.getAllPresets().filter { it.id != "who_pays" }
        presets.forEach { preset ->
            assertEquals("Preset ${preset.id} should target wheel", "wheel", preset.targetRoute)
        }
    }

    @Test
    fun `preset ids match expected set`() {
        val expectedIds = setOf(
            "what_to_eat", "truth_or_dare", "random_punishment", "movie_genre",
            "workout", "drink_order", "travel", "who_pays"
        )
        val actualIds = PresetProvider.getAllPresets().map { it.id }.toSet()
        assertEquals(expectedIds, actualIds)
    }

    @Test
    fun `Preset data class defaults targetRoute to wheel`() {
        val preset = Preset(
            id = "test",
            nameRes = 1,
            emoji = "🎯",
            itemResIds = listOf(1, 2, 3)
        )
        assertEquals("wheel", preset.targetRoute)
    }
}
