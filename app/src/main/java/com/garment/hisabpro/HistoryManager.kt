
package com.garment.hisabpro

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

object HistoryManager {

    fun sum(records: JSONArray, field: String): Double {
        var total = 0.0

        for (i in 0 until records.length()) {
            val item = records.optJSONObject(i) ?: continue
            total += item.optDouble(field, 0.0)
        }

        return total
    }

    fun totalPieces(entries: JSONArray): Double =
        sum(entries, "qty")

    fun totalEarnings(entries: JSONArray): Double =
        sum(entries, "amount")

    fun totalExpenses(expenses: JSONArray): Double =
        sum(expenses, "amount")

    fun totalReceived(payments: JSONArray): Double =
        sum(payments, "amount")

    fun remainingAmount(
        entries: JSONArray,
        payments: JSONArray
    ): Double {
        return totalEarnings(entries) - totalReceived(payments)
    }

    fun savings(
        entries: JSONArray,
        expenses: JSONArray
    ): Double {
        return totalEarnings(entries) - totalExpenses(expenses)
    }

    fun pieceSummary(
        entries: JSONArray,
        pieceName: String
    ): JSONObject {
        var quantity = 0.0
        var earning = 0.0
        var count = 0

        for (i in 0 until entries.length()) {
            val item = entries.optJSONObject(i) ?: continue

            if (item.optString("piece") == pieceName) {
                quantity += item.optDouble("qty", 0.0)
                earning += item.optDouble("amount", 0.0)
                count++
            }
        }

        return JSONObject()
            .put("piece", pieceName)
            .put("quantity", quantity)
            .put("earning", earning)
            .put("entries", count)
    }

    fun dateEntries(
        entries: JSONArray,
        dateText: String
    ): JSONArray {
        val result = JSONArray()

        for (i in 0 until entries.length()) {
            val item = entries.optJSONObject(i) ?: continue

            if (item.optString("date").contains(dateText)) {
                result.put(JSONObject(item.toString()))
            }
        }

        return result
    }

    fun formatMoney(value: Double): String =
        "₹" + String.format(Locale("en", "IN"), "%.2f", value)

    fun deleteRecord(records: JSONArray, index: Int): Boolean {
        if (index !in 0 until records.length()) return false
        records.remove(index)
        return true
    }

    fun updateAmount(
        records: JSONArray,
        index: Int,
        amount: Double
    ): Boolean {
        if (index !in 0 until records.length() || amount < 0) {
            return false
        }

        val item = records.optJSONObject(index) ?: return false
        item.put("amount", amount)
        return true
    }
}
