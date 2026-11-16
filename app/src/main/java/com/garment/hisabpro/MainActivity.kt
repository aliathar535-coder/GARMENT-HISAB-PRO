
package com.garment.hisabpro

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("garment_hisab_data", MODE_PRIVATE) }
    private val pieces = JSONArray()
    private val entries = JSONArray()
    private val expenses = JSONArray()
    private val payments = JSONArray()
    private lateinit var root: LinearLayout
    private val bg = Color.rgb(10, 15, 32)
    private val panel = Color.rgb(25, 35, 62)
    private val accent = Color.rgb(65, 145, 255)
    private val green = Color.rgb(48, 205, 145)
    private val red = Color.rgb(255, 105, 120)
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        showDashboard()
    }

    private fun loadData() {
        listOf(
            "pieces" to pieces,
            "entries" to entries,
            "expenses" to expenses,
            "payments" to payments
        ).forEach { pair ->
            try {
                val saved = JSONArray(
                    prefs.getString(pair.first, "[]") ?: "[]"
                )
                for (i in 0 until saved.length()) {
                    pair.second.put(saved.getJSONObject(i))
                }
            } catch (_: Exception) { }
        }
    }

    private fun saveData() {
        prefs.edit()
            .putString("pieces", pieces.toString())
            .putString("entries", entries.toString())
            .putString("expenses", expenses.toString())
            .putString("payments", payments.toString())
            .apply()
    }

    private fun total(a: JSONArray, key: String): Double {
        var v = 0.0
        for (i in 0 until a.length()) {
            v += a.optJSONObject(i)?.optDouble(key, 0.0) ?: 0.0
        }
        return v
    }

    private fun money(v: Double) =
        "₹" + String.format(Locale("en", "IN"), "%.2f", v)

    private fun now() =
        SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
            .format(Date())

    private fun page(title: String) {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(bg)
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 22, 18, 26)
        }

        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = title
            textSize = 23f
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 18)
        })

        setContentView(scroll)
    }

    private fun label(
        parent: LinearLayout,
        value: String,
        size: Float = 15f,
        color: Int = white
    ) {
        parent.addView(TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setPadding(2, 7, 2, 7)
        })
    }

    private fun card(title: String, value: String, color: Int = white) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(panel)
        }

        label(box, title, 14f, Color.LTGRAY)
        label(box, value, 24f, color)

        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = 10
        root.addView(box, lp)
    }

    private fun button(title: String, run: () -> Unit) {
        val b = Button(this).apply {
            text = title
            isAllCaps = false
            setTextColor(white)
            setBackgroundColor(accent)
            setOnClickListener { run() }
        }

        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.bottomMargin = 8
        root.addView(b, lp)
    }

    private fun input(hintText: String, numeric: Boolean = false) =
        EditText(this).apply {
            hint = hintText
            setSingleLine(true)
            if (numeric) {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }

    private fun showDashboard() {
        page("GARMENT HISAB PRO")

        label(
            root,
            "ऑफलाइन • आपका काम और कमाई का हिसाब",
            13f,
            Color.LTGRAY
        )

        val earned = total(entries, "amount")
        val spent = total(expenses, "amount")
        val received = total(payments, "amount")

        var qty = 0.0
        for (i in 0 until entries.length()) {
            qty += entries.optJSONObject(i)?.optDouble("qty", 0.0) ?: 0.0
        }

        card(
            "कुल पीस",
            String.format(Locale.getDefault(), "%.2f", qty),
            accent
        )

        card("कुल कमाई", money(earned), green)
        card("कुल खर्च", money(spent), red)
        card("प्राप्त / एडवांस रकम", money(received), accent)

        card(
            "बाकी रकम (कमाई - खर्च - प्राप्त)",
            money(earned - spent - received),
            green
        )

        button("＋ नई पीस / कमाई एंट्री") {
            addProduction()
        }

        button("खर्चा जोड़ें") {
            addMoneyRecord(false)
        }

        button("प्राप्त रकम / एडवांस जोड़ें") {
            addMoneyRecord(true)
        }

        button("पीस और रेट सेटिंग") {
            showPieces()
        }

        button("पूरा हिसाब / हिस्ट्री") {
            showHistory()
        }

        button("बैकअप डेटा कॉपी करें") {
            showBackup()
        }

        button("सारा डेटा शून्य करें") {
            resetData()
        }
    }

    private fun showPieces() {
        page("पीस और रेट सेटिंग")

        button("＋ नया पीस जोड़ें") {
            editPiece(-1)
        }

        for (i in 0 until pieces.length()) {
            val p = pieces.optJSONObject(i) ?: continue
            val ix = i

            card(
                p.optString("name"),
                "रेट: " + money(p.optDouble("rate"))
            )

            button("बदलें: " + p.optString("name")) {
                editPiece(ix)
            }

            button("हटाएँ: " + p.optString("name")) {
                AlertDialog.Builder(this)
                    .setMessage("इस पीस को सूची से हटाएँ?")
                    .setPositiveButton("हटाएँ") { _, _ ->
                        pieces.remove(ix)
                        saveData()
                        showPieces()
                    }
                    .setNegativeButton("रद्द", null)
                    .show()
            }
        }

        button("डैशबोर्ड") {
            showDashboard()
        }
    }

    private fun editPiece(index: Int) {
        val old = if (index >= 0) pieces.optJSONObject(index) else null

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 4)
        }

        val name = input("पीस का नाम").apply {
            setText(old?.optString("name") ?: "")
        }

        val rate = input("एक पीस का रेट", true).apply {
            if (old != null) {
                setText(old.optDouble("rate").toString())
            }
        }

        box.addView(name)
        box.addView(rate)

        AlertDialog.Builder(this)
            .setTitle("पीस सेटिंग")
            .setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val n = name.text.toString().trim()
                val r = rate.text.toString().toDoubleOrNull()

                if (n.isEmpty() || r == null || r < 0) {
                    toast("नाम और सही रेट भरें")
                    return@setPositiveButton
                }

                val item = JSONObject()
                    .put("name", n)
                    .put("rate", r)

                if (index < 0) {
                    pieces.put(item)
                } else {
                    pieces.put(index, item)
                }

                saveData()
                showPieces()
            }
            .setNegativeButton("रद्द", null)
            .show()
    }

    private fun addProduction() {
        if (pieces.length() == 0) {
            AlertDialog.Builder(this)
                .setMessage("पहले पीस और रेट जोड़ें।")
                .setPositiveButton("पीस सेटिंग") { _, _ ->
                    showPieces()
                }
                .setNegativeButton("बंद", null)
                .show()
            return
        }

        val names = (0 until pieces.length()).map {
            pieces.optJSONObject(it)?.optString("name") ?: ""
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 4)
        }

        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
        }

        val rate = input("रेट", true)
        val qty = input("कितने पीस?", true)
        val note = input("नोट (वैकल्पिक)")

        box.addView(spinner)
        box.addView(rate)
        box.addView(qty)
        box.addView(note)

        fun setRate() {
            rate.setText(
                pieces.optJSONObject(spinner.selectedItemPosition)
                    ?.optDouble("rate", 0.0).toString()
            )
        }

        spinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) { }

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    setRate()
                }
            }

        setRate()

        AlertDialog.Builder(this)
            .setTitle("नई पीस एंट्री")
            .setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val r = rate.text.toString().toDoubleOrNull()
                val q = qty.text.toString().toDoubleOrNull()

                if (r == null || q == null || r < 0 || q <= 0) {
                    toast("सही रेट और मात्रा भरें")
                    return@setPositiveButton
                }

                entries.put(
                    JSONObject()
                        .put("piece", names[spinner.selectedItemPosition])
                        .put("rate", r)
                        .put("qty", q)
                        .put("amount", r * q)
                        .put("note", note.text.toString())
                        .put("date", now())
                )

                saveData()
                showDashboard()
                toast("एंट्री सेव हो गई")
            }
            .setNegativeButton("रद्द", null)
            .show()
    }

    private fun addMoneyRecord(isPayment: Boolean) {
        val title = if (isPayment) {
            "प्राप्त रकम / एडवांस"
        } else {
            "खर्चा जोड़ें"
        }

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 4)
        }

        val description = input("विवरण / कैटेगरी")
        val amount = input("रकम", true)

        box.addView(description)
        box.addView(amount)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val v = amount.text.toString().toDoubleOrNull()

                if (v == null || v <= 0) {
                    toast("सही रकम भरें")
                    return@setPositiveButton
                }

                val obj = JSONObject()
                    .put("label", description.text.toString().ifBlank { title })
                    .put("amount", v)
                    .put("date", now())

                if (isPayment) {
                    payments.put(obj)
                } else {
                    expenses.put(obj)
                }

                saveData()
                showDashboard()
                toast("सेव हो गया")
            }
            .setNegativeButton("रद्द", null)
            .show()
    }

    private fun showHistory() {
        page("पूरा हिसाब / हिस्ट्री")

        label(root, "पीस एंट्री", 18f, accent)

        for (i in entries.length() - 1 downTo 0) {
            val e = entries.optJSONObject(i) ?: continue

            card(
                e.optString("piece"),
                "${e.optDouble("qty")} × ${money(e.optDouble("rate"))} = ${money(e.optDouble("amount"))}\n${e.optString("date")}\n${e.optString("note")}"
            )

            val ix = i

            button("यह एंट्री हटाएँ") {
                AlertDialog.Builder(this)
                    .setMessage("यह एंट्री हटानी है?")
                    .setPositiveButton("हटाएँ") { _, _ ->
                        entries.remove(ix)
                        saveData()
                        showHistory()
                    }
                    .setNegativeButton("रद्द", null)
                    .show()
            }
        }

        label(root, "खर्चे", 18f, red)

        for (i in expenses.length() - 1 downTo 0) {
            val e = expenses.optJSONObject(i) ?: continue

            label(
                root,
                "${e.optString("label")} • ${money(e.optDouble("amount"))} • ${e.optString("date")}"
            )
        }

        label(root, "प्राप्त रकम / एडवांस", 18f, green)

        for (i in payments.length() - 1 downTo 0) {
            val p = payments.optJSONObject(i) ?: continue

            label(
                root,
                "${p.optString("label")} • ${money(p.optDouble("amount"))} • ${p.optString("date")}"
            )
        }

        button("डैशबोर्ड") {
            showDashboard()
        }
    }

    private fun showBackup() {
        val data = JSONObject()
            .put("pieces", pieces)
            .put("entries", entries)
            .put("expenses", expenses)
            .put("payments", payments)
            .toString(2)

        val view = TextView(this).apply {
            text = data
            textSize = 11f
            setTextIsSelectable(true)
            setTextColor(white)
        }

        AlertDialog.Builder(this)
            .setTitle("बैकअप डेटा — टेक्स्ट कॉपी करें")
            .setView(ScrollView(this).apply {
                addView(view)
            })
            .setPositiveButton("बंद", null)
            .show()
    }

    private fun resetData() {
        AlertDialog.Builder(this)
            .setTitle("पुष्टि करें")
            .setMessage(
                "क्या पूरा हिसाब और पीस सूची शून्य करनी है? यह वापस नहीं आएगा।"
            )
            .setPositiveButton("हाँ, शून्य करें") { _, _ ->
                pieces.clear()
                entries.clear()
                expenses.clear()
                payments.clear()
                saveData()
                showDashboard()
            }
            .setNegativeButton("नहीं", null)
            .show()
    }

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
