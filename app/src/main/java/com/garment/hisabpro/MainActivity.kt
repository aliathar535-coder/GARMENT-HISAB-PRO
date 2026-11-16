
package com.garment.hisabpro

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
    private var photoEntryIndex = -1
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
        listOf("pieces" to pieces, "entries" to entries, "expenses" to expenses, "payments" to payments)
            .forEach { (key, target) ->
                try {
                    val saved = JSONArray(prefs.getString(key, "[]") ?: "[]")
                    for (i in 0 until saved.length()) target.put(saved.getJSONObject(i))
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
        var result = 0.0
        for (i in 0 until a.length()) result += a.optJSONObject(i)?.optDouble(key, 0.0) ?: 0.0
        return result
    }

    private fun money(v: Double) = "₹" + String.format(Locale("en", "IN"), "%.2f", v)
    private fun now() = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date())

    private fun page(title: String) {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
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

    private fun label(parent: LinearLayout, value: String, size: Float = 15f, color: Int = white) {
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
        label(box, value, 22f, color)
        root.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 10 })
    }

    private fun button(title: String, run: () -> Unit) {
        root.addView(Button(this).apply {
            text = title
            isAllCaps = false
            setTextColor(white)
            setBackgroundColor(accent)
            setOnClickListener { run() }
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
    }

    private fun input(hintText: String, numeric: Boolean = false) = EditText(this).apply {
        hint = hintText
        setSingleLine(true)
        if (numeric) inputType = android.text.InputType.TYPE_CLASS_NUMBER or
            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

    private fun showDashboard() {
        page("GARMENT HISAB PRO")
        label(root, "ऑफलाइन • आपका काम और कमाई का हिसाब", 13f, Color.LTGRAY)
        val earned = total(entries, "amount")
        val spent = total(expenses, "amount")
        val received = total(payments, "amount")
        var qty = 0.0
        for (i in 0 until entries.length()) qty += entries.optJSONObject(i)?.optDouble("qty", 0.0) ?: 0.0
        card("कुल पीस", String.format(Locale.getDefault(), "%.2f", qty), accent)
        card("कुल कमाई", money(earned), green)
        card("कुल खर्च", money(spent), red)
        card("प्राप्त / एडवांस रकम", money(received), accent)
        card("बाकी रकम (कमाई - खर्च - प्राप्त)", money(earned - spent - received), green)
        button("＋ नई पीस / कमाई एंट्री") { addProduction() }
        button("खर्चा जोड़ें") { addMoneyRecord(false) }
        button("प्राप्त रकम / एडवांस जोड़ें") { addMoneyRecord(true) }
        button("पीस और रेट सेटिंग") { showPieces() }
        button("पूरा हिसाब / हिस्ट्री") { showHistory() }
        button("PDF रिपोर्ट बनाएं") { createDocument(REPORT_REQUEST, "GARMENT-HISAB-REPORT.pdf", "application/pdf") }
        button("बैकअप Export करें") { createDocument(BACKUP_REQUEST, "GARMENT-HISAB-BACKUP.json", "application/json") }
        button("बैकअप Restore करें") { openBackup() }
        button("सारा डेटा शून्य करें") { resetData() }
    }

    private fun showPieces() {
        page("पीस और रेट सेटिंग")
        button("＋ नया पीस जोड़ें") { editPiece(-1) }
        for (i in 0 until pieces.length()) {
            val p = pieces.optJSONObject(i) ?: continue
            val ix = i
            card(p.optString("name"), "रेट: ${money(p.optDouble("rate"))}")
            button("बदलें: ${p.optString("name")}") { editPiece(ix) }
            button("हटाएँ: ${p.optString("name")}") {
                AlertDialog.Builder(this).setMessage("इस पीस को सूची से हटाएँ?")
                    .setPositiveButton("हटाएँ") { _, _ -> pieces.remove(ix); saveData(); showPieces() }
                    .setNegativeButton("रद्द", null).show()
            }
        }
        button("डैशबोर्ड") { showDashboard() }
    }

    private fun editPiece(index: Int) {
        val old = if (index >= 0) pieces.optJSONObject(index) else null
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 8, 20, 4) }
        val name = input("पीस का नाम").apply { setText(old?.optString("name") ?: "") }
        val rate = input("एक पीस का रेट", true).apply { if (old != null) setText(old.optDouble("rate").toString()) }
        box.addView(name); box.addView(rate)
        AlertDialog.Builder(this).setTitle("पीस सेटिंग").setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val n = name.text.toString().trim()
                val r = rate.text.toString().toDoubleOrNull()
                if (n.isEmpty() || r == null || r < 0) { toast("नाम और सही रेट भरें"); return@setPositiveButton }
                val item = JSONObject().put("name", n).put("rate", r)
                if (index < 0) pieces.put(item) else pieces.put(index, item)
                saveData(); showPieces()
            }.setNegativeButton("रद्द", null).show()
    }

    private fun addProduction() {
        if (pieces.length() == 0) {
            AlertDialog.Builder(this).setMessage("पहले पीस और रेट जोड़ें।")
                .setPositiveButton("पीस सेटिंग") { _, _ -> showPieces() }
                .setNegativeButton("बंद", null).show()
            return
        }
        val names = (0 until pieces.length()).map { pieces.optJSONObject(it)?.optString("name") ?: "" }
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 8, 20, 4) }
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, names)
        }
        val rate = input("रेट", true)
        val qty = input("कितने पीस?", true)
        val note = input("नोट (वैकल्पिक)")
        box.addView(spinner); box.addView(rate); box.addView(qty); box.addView(note)
        fun setRate() {
            rate.setText(pieces.optJSONObject(spinner.selectedItemPosition)?.optDouble("rate", 0.0).toString())
        }
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { setRate() }
        }
        setRate()
        AlertDialog.Builder(this).setTitle("नई पीस एंट्री").setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val r = rate.text.toString().toDoubleOrNull()
                val q = qty.text.toString().toDoubleOrNull()
                if (r == null || q == null || r < 0 || q <= 0) { toast("सही रेट और मात्रा भरें"); return@setPositiveButton }
                entries.put(JSONObject().put("piece", names[spinner.selectedItemPosition])
                    .put("rate", r).put("qty", q).put("amount", r * q)
                    .put("note", note.text.toString()).put("date", now()).put("photos", JSONArray()))
                saveData()
                photoEntryIndex = entries.length() - 1
                AlertDialog.Builder(this).setMessage("एंट्री सेव हो गई। क्या इस एंट्री में फोटो जोड़ना है?")
                    .setPositiveButton("फोटो जोड़ें") { _, _ -> pickPhotos() }
                    .setNegativeButton("अभी नहीं") { _, _ -> showDashboard() }.show()
            }.setNegativeButton("रद्द", null).show()
    }

    private fun addMoneyRecord(isPayment: Boolean) {
        val title = if (isPayment) "प्राप्त रकम / एडवांस" else "खर्चा जोड़ें"
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 8, 20, 4) }
        val description = input("विवरण / कैटेगरी")
        val amount = input("रकम", true)
        box.addView(description); box.addView(amount)
        AlertDialog.Builder(this).setTitle(title).setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val v = amount.text.toString().toDoubleOrNull()
                if (v == null || v <= 0) { toast("सही रकम भरें"); return@setPositiveButton }
                val obj = JSONObject().put("label", description.text.toString().ifBlank { title })
                    .put("amount", v).put("date", now())
                if (isPayment) payments.put(obj) else expenses.put(obj)
                saveData(); showDashboard(); toast("सेव हो गया")
            }.setNegativeButton("रद्द", null).show()
    }

    private fun showHistory() {
        page("पूरा हिसाब / हिस्ट्री")
        label(root, "पीस एंट्री", 18f, accent)
        for (i in entries.length() - 1 downTo 0) {
            val e = entries.optJSONObject(i) ?: continue
            val photoCount = e.optJSONArray("photos")?.length() ?: 0
            card(e.optString("piece"),
                "${e.optDouble("qty")} × ${money(e.optDouble("rate"))} = ${money(e.optDouble("amount"))}\n${e.optString("date")}\n${e.optString("note")}\nफोटो: $photoCount")
            val ix = i
            button("इस एंट्री में फोटो जोड़ें") { photoEntryIndex = ix; pickPhotos() }
            button("यह एंट्री हटाएँ") {
                AlertDialog.Builder(this).setMessage("यह एंट्री हटानी है?")
                    .setPositiveButton("हटाएँ") { _, _ ->
                        deleteEntryPhotos(entries.optJSONObject(ix))
                        entries.remove(ix)
                        saveData()
                        showHistory()
                    }
                    .setNegativeButton("रद्द", null).show()
            }
        }
        label(root, "खर्चे", 18f, red)
        for (i in expenses.length() - 1 downTo 0) {
            val e = expenses.optJSONObject(i) ?: continue
            label(root, "${e.optString("label")} • ${money(e.optDouble("amount"))} • ${e.optString("date")}")
            val ix = i
            button("खर्च बदलें") { editMoneyRecord(false, ix) }
            button("खर्च हटाएँ") { deleteMoneyRecord(false, ix) }
        }
        label(root, "प्राप्त रकम / एडवांस", 18f, green)
        for (i in payments.length() - 1 downTo 0) {
            val p = payments.optJSONObject(i) ?: continue
            label(root, "${p.optString("label")} • ${money(p.optDouble("amount"))} • ${p.optString("date")}")
            val ix = i
            button("प्राप्त रकम बदलें") { editMoneyRecord(true, ix) }
            button("प्राप्त रकम हटाएँ") { deleteMoneyRecord(true, ix) }
        }
        button("डैशबोर्ड") { showDashboard() }
    }

    private fun editMoneyRecord(isPayment: Boolean, index: Int) {
        val target = if (isPayment) payments else expenses
        val old = target.optJSONObject(index) ?: return
        val title = if (isPayment) "प्राप्त रकम / एडवांस बदलें" else "खर्च बदलें"
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 8, 20, 4) }
        val description = input("विवरण / कैटेगरी").apply { setText(old.optString("label")) }
        val amount = input("रकम", true).apply { setText(old.optDouble("amount").toString()) }
        box.addView(description)
        box.addView(amount)
        AlertDialog.Builder(this).setTitle(title).setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull()
                if (value == null || value <= 0) {
                    toast("सही रकम भरें")
                    return@setPositiveButton
                }
                target.put(index, JSONObject()
                    .put("label", description.text.toString().ifBlank { title })
                    .put("amount", value)
                    .put("date", old.optString("date", now())))
                saveData()
                showHistory()
                toast("बदलाव सेव हो गया")
            }.setNegativeButton("रद्द", null).show()
    }

    private fun deleteMoneyRecord(isPayment: Boolean, index: Int) {
        val target = if (isPayment) payments else expenses
        AlertDialog.Builder(this).setMessage("क्या यह रिकॉर्ड हटाना है?")
            .setPositiveButton("हटाएँ") { _, _ ->
                target.remove(index)
                saveData()
                showHistory()
                toast("रिकॉर्ड हटा दिया गया")
            }.setNegativeButton("रद्द", null).show()
    }

    private fun deleteEntryPhotos(entry: JSONObject?) {
        val photos = entry?.optJSONArray("photos") ?: return
        for (i in 0 until photos.length()) {
            PhotoManager.deletePhoto(photos.optString(i))
        }
    }

    private fun pickPhotos() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, PHOTO_REQUEST)
    }

    private fun openBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, RESTORE_REQUEST)
    }

    private fun createDocument(request: Int, name: String, mime: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, name)
        }
        startActivityForResult(intent, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null && data?.clipData == null) return
        try {
            when (requestCode) {
                PHOTO_REQUEST -> {
                    val uris = mutableListOf<Uri>()
                    data.clipData?.let { clip -> for (i in 0 until clip.itemCount) uris.add(clip.getItemAt(i).uri) }
                    if (uris.isEmpty()) data.data?.let { uris.add(it) }
                    val saved = PhotoManager.copyUrisToPrivateStorage(this, uris)
                    val entry = entries.optJSONObject(photoEntryIndex)
                    if (entry != null) {
                        val arr = entry.optJSONArray("photos") ?: JSONArray()
                        saved.forEach { arr.put(it) }
                        entry.put("photos", arr)
                        saveData()
                        toast("${saved.size} फोटो हिसाब में जोड़ दिए")
                    }
                    showHistory()
                }
                REPORT_REQUEST -> data.data?.let { ReportManager.writePdf(this, it, pieces, entries, expenses, payments) ; toast("PDF रिपोर्ट सेव हो गई") }
                BACKUP_REQUEST -> data.data?.let { ReportManager.writeBackup(this, it, pieces, entries, expenses, payments); toast("बैकअप सेव हो गया") }
                RESTORE_REQUEST -> data.data?.let { uri ->
                    val restored = ReportManager.readBackup(this, uri)
                    if (restored) {
                        replaceArray(pieces, ReportManager.restored.optJSONArray("pieces"))
                        replaceArray(entries, ReportManager.restored.optJSONArray("entries"))
                        replaceArray(expenses, ReportManager.restored.optJSONArray("expenses"))
                        replaceArray(payments, ReportManager.restored.optJSONArray("payments"))
                        saveData(); showDashboard(); toast("बैकअप Restore हो गया")
                    } else toast("बैकअप फाइल सही नहीं है")
                }
            }
        } catch (e: Exception) {
            toast("काम पूरा नहीं हुआ: ${e.message ?: "फाइल त्रुटि"}")
        }
    }

    private fun replaceArray(target: JSONArray, source: JSONArray?) {
        while (target.length() > 0) target.remove(target.length() - 1)
        if (source != null) for (i in 0 until source.length()) target.put(source.get(i))
    }

    private fun resetData() {
        AlertDialog.Builder(this).setTitle("पुष्टि करें")
            .setMessage("क्या पूरा हिसाब और पीस सूची शून्य करनी है? यह वापस नहीं आएगा।")
            .setPositiveButton("हाँ, शून्य करें") { _, _ ->
                for (i in 0 until entries.length()) {
                    deleteEntryPhotos(entries.optJSONObject(i))
                }
                listOf(pieces, entries, expenses, payments).forEach { a ->
                    while (a.length() > 0) a.remove(a.length() - 1)
                }
                saveData(); showDashboard()
            }.setNegativeButton("नहीं", null).show()
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    companion object {
        private const val PHOTO_REQUEST = 701
        private const val REPORT_REQUEST = 702
        private const val BACKUP_REQUEST = 703
        private const val RESTORE_REQUEST = 704
    }
}
