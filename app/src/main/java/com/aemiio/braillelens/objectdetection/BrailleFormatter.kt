package com.aemiio.braillelens.objectdetection

import android.util.Log

data class BrailleCell(
    val classId: Int,
    val binaryPattern: String,
    val meaning: String,
    val confidence: Float,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val box: Map<String, Any>
)

class BrailleFormatter {
    private val TAG = "BrailleFormatter"
    private var capitalizeNext = false
    private var numberMode = false

    /**
     * Convert raw detections to BrailleCell objects
     */
    fun convertToBrailleCells(
        detections: List<Map<String, Any>>,
        classDetails: List<Map<String, String>>
    ): List<BrailleCell> {
        val cells = mutableListOf<BrailleCell>()

        Log.d(TAG, "Converting ${detections.size} detections to BrailleCell objects")

        for (i in detections.indices) {
            val detection = detections[i]
            val details = classDetails[i]

            // Log class ID and binary pattern to debug prefix issues
            val classId = detection["classId"] as Int
            val binaryPattern = details["binaryPattern"] ?: "?"
            Log.d(TAG, "Cell $i: ClassID=$classId, Binary=$binaryPattern")

            cells.add(
                BrailleCell(
                    classId = classId,
                    binaryPattern = binaryPattern,
                    meaning = details["meaningText"] ?: "?",
                    confidence = detection["conf"] as Float,
                    x = detection["x"] as Float,
                    y = detection["y"] as Float,
                    width = detection["w"] as Float,
                    height = detection["h"] as Float,
                    box = detection
                )
            )
        }

        return cells
    }

    /**
     * Organize braille cells into lines based on vertical position
     */
    fun organizeCellsByLines(cells: List<BrailleCell>): List<List<BrailleCell>> {
        if (cells.isEmpty()) return emptyList()

        // Use average cell height to determine the line height tolerance
        val avgHeight = cells.map { it.height }.average().toFloat()
        val lineTolerance = avgHeight * 0.8f

        // Group cells by lines based on Y-coordinate
        val lines = mutableListOf<MutableList<BrailleCell>>()

        for (cell in cells) {
            var addedToLine = false
            for (line in lines) {
                val avgLineY = line.map { it.y }.average().toFloat()
                if (Math.abs(cell.y - avgLineY) < lineTolerance) {
                    line.add(cell)
                    addedToLine = true
                    break
                }
            }

            if (!addedToLine) {
                lines.add(mutableListOf(cell))
            }
        }

        // Sort each line by X-coordinate
        lines.forEach { line -> line.sortBy { it.x } }

        // Sort lines by Y-coordinate
        return lines.sortedBy { it.first().y }
    }

    /**
     * Group braille cells into words based on horizontal spacing
     */
    fun groupCellsIntoWords(line: List<BrailleCell>): List<List<BrailleCell>> {
        if (line.isEmpty()) return emptyList()

        // Use average cell width to determine word spacing tolerance
        val avgWidth = line.map { it.width }.average().toFloat()
        val wordSpacingThreshold = avgWidth * 1.5f

        val words = mutableListOf<MutableList<BrailleCell>>()
        var currentWord = mutableListOf<BrailleCell>()

        for (i in line.indices) {
            currentWord.add(line[i])

            // If this is the last cell or there's significant spacing to the next cell
            if (i == line.size - 1 ||
                (line[i + 1].x - (line[i].x + line[i].width)) > wordSpacingThreshold
            ) {
                words.add(currentWord)
                currentWord = mutableListOf()
            }
        }

        return words
    }

    /**
     * Format results for display, including detection info
     */
    fun formatDetectionResults(cells: List<BrailleCell>): String {
        val results = StringBuilder()
        results.append("Found ${cells.size} braille cells\n\n")

        for (i in cells.indices) {
            val cell = cells[i]
            results.append(
                "Cell $i: ${cell.meaning} (${(cell.confidence * 100).toInt()}%), " +
                        "Binary: ${cell.binaryPattern}\n"
            )
        }

        return results.toString()
    }

    fun formatTranslatedText(cells: List<BrailleCell>, currentModel: String): String {
        // Reset formatting states
        capitalizeNext = false
        numberMode = false

        val result = StringBuilder("")
        val organizedLines = organizeCellsByLines(cells)

        for (lineIndex in organizedLines.indices) {
            val line = organizedLines[lineIndex]
            val words = groupCellsIntoWords(line)

            for (wordIndex in words.indices) {
                val word = words[wordIndex]
                val wordBuilder = StringBuilder()
                var i = 0

                numberMode = false

                while (i < word.size) {
                    val cell = word[i]

                    if (cell.meaning == "number") {
                        numberMode = true
                        i++
                        continue
                    }

                    // Handle special characters and modifiers
                    when (cell.meaning) {
                        "capital" -> {
                            capitalizeNext = true
                            i++
                            continue
                        }

                        "dot_4" -> {
                            // Check if next cell is 'n' or 'N' to form ñ/Ñ
                            if (i + 1 < word.size) {
                                val nextCell = word[i + 1]
                                if (nextCell.meaning == "n") {
                                    // dot_4 + n → ñ
                                    wordBuilder.append("ñ")
                                    i += 2  // Skip both cells
                                    continue
                                } else if (capitalizeNext && nextCell.meaning == "n") {
                                    // dot_4 + capital + n → Ñ
                                    wordBuilder.append("Ñ")
                                    capitalizeNext = false
                                    i += 2  // Skip both cells
                                    continue
                                } else if (nextCell.meaning == "N") {
                                    // dot_4 + N → Ñ
                                    wordBuilder.append("Ñ")
                                    i += 2  // Skip both cells
                                    continue
                                }
                            }
                            // When dot_4 is not part of ñ, we should turn off number mode
                            // as it's a modifier that interrupts number sequence
                            numberMode = false
                            i++
                            continue
                        }

                        "dot_5" -> {
                            // Skip prefix indicators but continue with next cell
                            i++
                            continue
                        }

                        else -> {
                            // Process regular cells
                            var text = cell.meaning

                            // Apply capitalization if needed
                            if (capitalizeNext) {
                                text = text.replaceFirstChar { it.uppercase() }
                                capitalizeNext = false
                            }
                            // Convert uppercase to lowercase if not preceded by capital sign
                            else if (isUppercase(text)) {
                                text = text.lowercase()
                            }

                            // Apply number mode if active
                            if (numberMode && text.length == 1 && text[0] in 'a'..'j') {
                                text = when (text) {
                                    "a" -> "1"
                                    "b" -> "2"
                                    "c" -> "3"
                                    "d" -> "4"
                                    "e" -> "5"
                                    "f" -> "6"
                                    "g" -> "7"
                                    "h" -> "8"
                                    "i" -> "9"
                                    "j" -> "0"
                                    else -> text
                                }
                            } else if (text.length > 1) {
                                // Turn off number mode when encountering a multi-character cell
                                numberMode = false

                            }

                            // Handle whole words in Grade 2 or combined mode
                            if ((currentModel == ObjectDetector.MODEL_G2 || currentModel == ObjectDetector.BOTH_MODELS) &&
                                isWholeWord(text) && !isPartWord(text)
                            ) {
                                // This is a whole word (not a part word), add it separately with spaces
                                if (wordBuilder.isNotEmpty()) {
                                    result.append(wordBuilder.toString()).append(" ")
                                    wordBuilder.clear()
                                }
                                result.append(text).append(" ")
                            } else {
                                // For part words and regular characters, just append to the wordBuilder
                                wordBuilder.append(text)
                            }
                        }
                    }
                    i++
                }

                if (wordBuilder.isNotEmpty()) {
                    result.append(wordBuilder.toString())
                    // Only add space if not the last word in line
                    if (wordIndex < words.size - 1) {
                        result.append(" ")
                    }
                }
            }

            // Add line breaks between lines of braille
            if (lineIndex < organizedLines.size - 1) {
                result.append("\n")
            }

            // Reset number mode at end of line
            numberMode = false
        }

        return result.toString()
    }

    /**
     * Check if a text represents a whole word
     */
    private fun isWholeWord(text: String): Boolean {
        val wholeWords = setOf(
            // One-cell whole words (alphabet and non-alphabet)
            "bakit", "kaniya", "dahil", "paano", "ganoon", "hindi", "ikaw", "hakbang", "kaya",
            "lamang", "mga", "ngayon", "para", "kailan", "rin", "sang-ayon", "tayo", "upang",
            "bagaman", "wala", "ito", "yaman", "sa", "ako", "anak", "ang", "araw", "at",
            "ay", "hanggang", "raw", "tunay", "kanila", "maging", "mahal", "na", "naging",
            "ng", "ibig", "ingay",

            // Two-cell whole words and non-alphabet
            "binata", "karaniwan", "dalaga", "ewan", "papaano", "gunita", "hapon", "isip",
            "halaman", "kailangan", "larawan", "mabuti", "noon", "opo", "patuloy", "kislap",
            "roon", "subalit", "talaga", "ugali", "buhay", "wasto", "eksamen", "ayaw", "salita",

            // Two-cell non-alphabet contractions
            "alam", "anggi", "bulaklak", "kabila", "masama", "nawa", "ngunit", "panahon",
            "sabi", "sinta", "tungkol", "ukol", "wakas"
        )
        return text in wholeWords
    }

    // Check if the word is a part-word contraction (Grade 2)
    private fun isPartWord(text: String): Boolean {
        val partWords = setOf(
            "an", "ang", "ar", "at", "aw", "er", "han", "ibig", "ing", "mag",
            "mahal", "nag", "ng", "pag", "tu"
        )
        return text in partWords
    }

    private fun isUppercase(text: String): Boolean {
        return text in setOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "Ñ", "NG", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"
        )
    }
}