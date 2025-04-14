package com.aemiio.braillelens.objectdetection

/**
 * Helper class for processing detection results
 */
object BrailleResult {
    // Process raw detection data
    fun processRawDetection(box: FloatArray, displayWidth: Float, displayHeight: Float): Map<String, Any> {
        val modelWidth = ObjectDetector.INPUT_SIZE.toFloat()
        val modelHeight = ObjectDetector.INPUT_SIZE.toFloat()

        val ratioWidth = displayWidth / modelWidth
        val ratioHeight = displayHeight / modelHeight

        val x = box[0] * ratioWidth
        val y = box[1] * ratioHeight
        val w = box[2] * ratioWidth
        val h = box[3] * ratioHeight
        val conf = box[4]
        val classId = box[5].toInt()

        val left = Math.max(0f, x - w / 2)
        val top = Math.max(0f, y - h / 2)
        val right = Math.min(displayWidth, x + w / 2)
        val bottom = Math.min(displayHeight, y + h / 2)

        return mapOf(
            "x" to x, "y" to y, "w" to w, "h" to h,
            "conf" to conf, "classId" to classId,
            "left" to left, "top" to top, "right" to right, "bottom" to bottom
        )
    }

    // Process class ID and get related information
    fun getClassDetails(classId: Int, currentModel: String, classes: List<String>): Map<String, String> {
        val className = BrailleClass.getClassName(classId, currentModel, classes)

        // Get binary pattern and meaning based on which model was used
        val (binaryPattern, meaningText) = if (currentModel == ObjectDetector.BOTH_MODELS) {
            val bothModelsMerger = BothModelsMerger()
            val actualClassId = bothModelsMerger.getActualClassId(classId)
            val isG2 = bothModelsMerger.isG2Detection(classId)

            // Use appropriate map based on model type
            if (isG2) {
                val entry = BrailleMap.G2brailleMap[actualClassId]
                Pair(entry?.binary ?: "?", entry?.meaning ?: "?")
            } else {
                val entry = BrailleMap.G1brailleMap[actualClassId]
                Pair(entry?.binary ?: "?", entry?.meaning ?: "?")
            }
        } else if (currentModel == ObjectDetector.MODEL_G2) {
            val entry = BrailleMap.G2brailleMap[classId]
            Pair(entry?.binary ?: "?", entry?.meaning ?: "?")
        } else {
            val entry = BrailleMap.G1brailleMap[classId]
            Pair(entry?.binary ?: "?", entry?.meaning ?: "?")
        }

        return mapOf(
            "className" to className,
            "binaryPattern" to binaryPattern,
            "meaningText" to meaningText
        )
    }

}