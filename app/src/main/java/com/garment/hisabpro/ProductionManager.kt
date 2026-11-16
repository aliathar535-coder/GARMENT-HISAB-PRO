
package com.garment.hisabpro

import org.json.JSONArray
import org.json.JSONObject

object ProductionManager {

    fun addEntry(
        entries: JSONArray,
        pieceName: String,
        quantity: Double,
        rate: Double,
        note: String,
        date: String,
        photos: JSONArray = JSONArray()
    ): JSONObject {
        require(pieceName.isNotBlank()) { "पीस का नाम लिखें" }
        require(quantity > 0) { "पीस की मात्रा सही लिखें" }
        require(rate >= 0) { "रेट सही लिखें" }

        val total = quantity * rate

        val entry = JSONObject()
            .put("id", System.currentTimeMillis().toString())
            .put("piece", pieceName)
            .put("qty", quantity)
            .put("rate", rate)
            .put("amount", total)
            .put("note", note)
            .put("date", date)
            .put("photos", photos)

        entries.put(entry)
        return entry
    }

    fun totalQuantity(entries: JSONArray): Double =
        sum(entries, "qty")

    fun totalEarning(entries: JSONArray): Double =
        sum(entries, "amount")

    fun pieceQuantity(entries: JSONArray, pieceName: String): Double {
        var total = 0.0

        for (i in 0 until entries.length()) {
            val item = entries.optJSONObject(i) ?: continue
            if (item.optString("piece") == pieceName) {
                total += item.optDouble("qty", 0.0)
            }
        }

        return total
    }

    fun sum(entries: JSONArray, key: String): Double {
        var total = 0.0

        for (i in 0 until entries.length()) {
            total += entries.optJSONObject(i)?.optDouble(key, 0.0) ?: 0.0
        }

        return total
    }

    fun deleteEntry(entries: JSONArray, index: Int) {
        require(index in 0 until entries.length()) {
            "एंट्री नहीं मिली"
        }
        entries.remove(index)
    }
}
