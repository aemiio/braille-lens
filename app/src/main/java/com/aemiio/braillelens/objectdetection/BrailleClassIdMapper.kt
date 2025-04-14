package com.aemiio.braillelens.objectdetection
import android.util.Log

object BrailleClassIdMapper {
    private const val TAG = "BrailleClassIdMapper"

    // Maps class IDs to binary patterns for different models
    private val g1ClassIdToBinaryMap = mutableMapOf<Int, String>()
    private val g2ClassIdToBinaryMap = mutableMapOf<Int, String>()

    init {
        // Initialize maps from BrailleMap data
        BrailleMap.G1brailleMap.forEach { (id, entry) ->
            // Store the full binary pattern without splitting
            g1ClassIdToBinaryMap[id] = entry.binary
        }

        BrailleMap.G2brailleMap.forEach { (id, entry) ->
            g2ClassIdToBinaryMap[id] = entry.binary
        }
    }

    /**
     * Debug function to help with mapping issues
     */
    fun loadMappingsFromResources() {
        Log.d(TAG, "Initializing Braille class ID mappings...")

        // Log G1 mappings for debugging
        BrailleMap.G1brailleMap.entries.take(10).forEach { (id, entry) ->
            Log.d(TAG, "G1 Class $id: ${entry.binary} → ${entry.meaning}")
        }

        // Log G2 mappings for debugging
        BrailleMap.G2brailleMap.entries.take(10).forEach { (id, entry) ->
            Log.d(TAG, "G2 Class $id: ${entry.binary} → ${entry.meaning}")
        }

        Log.d(TAG, "Total mappings: ${g1ClassIdToBinaryMap.size} G1, ${g2ClassIdToBinaryMap.size} G2")
    }


    // Map class IDs to binary patterns and meanings directly
    fun getBrailleEntry(classId: Int, grade: Int): BrailleEntry? {
        return BrailleMap.getBrailleMap(grade)[classId]
    }

    // Get the meaning directly from the map
    fun getMeaning(classId: Int, grade: Int): String {
        return getBrailleEntry(classId, grade)?.meaning ?: "?"
    }

    fun getMeaningToClassId(meaning: String, grade: Int): Int {
        val brailleMap = when (grade) {
            1 -> BrailleMap.G1brailleMap
            2 -> BrailleMap.G2brailleMap
            else -> BrailleMap.G1brailleMap
        }

        // Find the entry with the matching meaning
        return brailleMap.entries.find { it.value.meaning == meaning }?.key ?: -1
    }
}