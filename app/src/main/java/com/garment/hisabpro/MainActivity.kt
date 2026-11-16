
package com.garment.hisabpro

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("garment_hisab_data", MODE_PRIVATE)
    }

    private val pieces = JSONArray()
    private val entries = JSONArray()
    private val expenses = JSONArray()
    private val payments = JSONArray()

    private lateinit var root: LinearLayout
    private var photoEntryIndex = -1

    private val blue = listOf(Color.rgb(0, 40, 100), Color.rgb(0, 130, 255))
    private val gold = listOf(Color.rgb(75, 42, 0), Color.rgb(220, 155, 20))
    private val green = listOf(Color.rgb(0, 65, 40), Color.rgb(0, 180, 110))
    private val red = listOf(Color.rgb(85, 0, 20), Color.rgb(220, 35, 65))
    private val purple = listOf(Color.rgb(45, 0, 85), Color.rgb(145, 55, 220))
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
        showDashboard()
    }

    private fun loadData() {
        val names = listOf(
            "pieces" to pieces,
            "entries" to entries,
            "expenses" to expenses,
            "payments" to payments
        )

        names.forEach { (key, target) ->
            try {
                val saved = JSONArray(prefs.getString(key, "[]") ?: "[]")
                for (i in 0 until saved.length()) {
                    target.put(saved.getJSONObject(i))
                }
            } catch (_: Exception) {
            }
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

    private fun now(): String =
        SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())
            .format(Date())

    private fun money(value: Double): String =
        "₹" + String.format(Locale("en", "IN"), "%.2f", value)

    private fun sum(array: JSONArray, key: String): Double =
        HistoryManager.sum(array, key)

    private fun page(title: String) {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(4, 7, 22))
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 18, 16, 25)
        }

        scroll.addView(root)
        root.addView(NeonUi.title(this, title, 25f))
        setContentView(scroll)
    }

    private fun addCard(
        title: String,
        value: String,
        colors: List<Int>,
        valueColor: Int = white,
        action: (() -> Unit)? = null
    ) {
        val card = NeonUi.card(this, title, value, colors, valueColor)

        if (action != null) {
            card.isClickable = true
            card.isFocusable = true
            card.setOnClickListener { action() }
        }

        NeonUi.addFullWidth(root, card)
    }

    private fun addButton(
        title: String,
        colors: List<Int> = blue,
        action: () -> Unit
    ) {
        NeonUi.addFullWidth(
            root,
            NeonUi.actionButton(this, title, colors, action)
        )
    }

    private fun addLabel(
        text: String,
        color: Int = white,
        size: Float = 16f
    ) {
        root.addView(TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            setPadding(4, 10, 4, 8)
        })
    }

    private fun input(
        hintText: String,
        numeric: Boolean = false
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            setSingleLine(true)
            setTextColor(Color.BLACK)
            setHintTextColor(Color.DKGRAY)
            setPadding(14, 10, 14, 10)

            if (numeric) {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }
    }

    private fun showDashboard() {
        page("GARMENT HISAB PRO")

        val earned = sum(entries, "amount")
        val spent = sum(expenses, "amount")
        val received = sum(payments, "amount")
        val qty = sum(entries, "qty")
        val balance = earned - received
        val profit = earned - spent
        val percent = if (earned > 0) profit / earned * 100 else 0.0

        val name = DesignManager.getName(this)
        addLabel("नमस्कार, $name", Color.rgb(255, 210, 90), 18f)
        addLabel("ऑफलाइन हिसाब • ${now()}", Color.LTGRAY, 12f)

        addCard("कुल कमाई", money(earned), green) {
            showHistory("entries")
        }

        addCard("कुल खर्च", money(spent), red) {
            showHistory("expenses")
        }

        addCard("कुल पीस", String.format("%.2f", qty), blue) {
            showHistory("entries")
        }

        addCard("प्राप्त रकम", money(received), purple) {
            showHistory("payments")
        }

        addCard("बाकी प्राप्त होना है", money(balance), gold) {
            showHistory("payments")
        }

        addCard(
            if (profit >= 0) "प्रॉफिट" else "लॉस",
            "${money(profit)}  (${String.format("%.2f", percent)}%)",
            if (profit >= 0) green else red
        )

        addButton("＋ नई पीस / कमाई एंट्री", green) {
            addProduction()
        }

        addButton("पीस और रेट सेटिंग", gold) {
            showPieces()
        }

        addButton("खर्चा जोड़ें", red) {
            addMoneyRecord(false)
        }

        addButton("प्राप्त रकम / एडवांस जोड़ें", purple) {
            addMoneyRecord(true)
        }

        addButton("पूरा हिसाब और हिस्ट्री", blue) {
            showHistory("all")
        }

        addButton("डिजाइन और नाम सेटिंग", gold) {
            DesignSettings.show(this) { showDashboard() }
        }

        addButton("PDF रिपोर्ट बनाएं") {
            createDocument(
                REPORT_REQUEST,
                "GARMENT-HISAB-REPORT.pdf",
                "application/pdf"
            )
        }

        addButton("बैकअप Export करें") {
            createDocument(
                BACKUP_REQUEST,
                "GARMENT-HISAB-BACKUP.json",
                "application/json"
            )
        }

        addButton("बैकअप Restore करें") {
            openBackup()
        }

        addButton("सारा डेटा शून्य करें", red) {
            resetData()
        }
    }

    private fun showPieces() {
        page("पीस और रेट सेटिंग")

        addButton("＋ नया पीस जोड़ें", green) {
            editPiece(-1)
        }

        if (pieces.length() == 0) {
            addLabel("अभी कोई पीस नहीं जोड़ा गया है।")
        }

        for (i in 0 until pieces.length()) {
            val item = pieces.optJSONObject(i) ?: continue
            val index = i
            val name = item.optString("name")
            val rate = item.optDouble("rate")

            addCard(name, "रेट: ${money(rate)}", gold)

            addButton("रेट / नाम बदलें: $name") {
                editPiece(index)
            }

            addButton("पीस हटाएँ: $name", red) {
                AlertDialog.Builder(this)
                    .setMessage("क्या यह पीस सूची से हटाना है?")
                    .setPositiveButton("हाँ, हटाएँ") { _, _ ->
                        pieces.remove(index)
                        saveData()
                        showPieces()
                    }
                    .setNegativeButton("रद्द", null)
                    .show()
            }
        }

        addButton("डैशबोर्ड") { showDashboard() }
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
            if (old != null) setText(old.optDouble("rate").toString())
        }

        box.addView(name)
        box.addView(rate)

        AlertDialog.Builder(this)
            .setTitle("पीस और रेट सेटिंग")
            .setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val n = name.text.toString().trim()
                val r = rate.text.toString().toDoubleOrNull()

                if (n.isBlank() || r == null || r < 0) {
                    toast("नाम और सही रेट भरें")
                    return@setPositiveButton
                }

                val item = JSONObject()
                    .put("name", n)
                    .put("rate", r)

                if (index < 0) pieces.put(item)
                else pieces.put(index, item)

                saveData()
                showPieces()
            }
            .setNegativeButton("रद्द", null)
            .show()
    }

    private fun addProduction() {
        if (pieces.length() == 0) {
            AlertDialog.Builder(this)
                .setMessage("पहले पीस और उसका रेट जोड़ें।")
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

        val rate = input("रेट अपने आप आएगा", true)
        val qty = input("कितने पीस बनाए?", true)
        val note = input("नोट (वैकल्पिक)")

        box.addView(spinner)
        box.addView(rate)
        box.addView(qty)
        box.addView(note)

        fun updateRate() {
            val selected = spinner.selectedItemPosition
            rate.setText(
                pieces.optJSONObject(selected)
                    ?.optDouble("rate", 0.0)
                    .toString()
            )
        }

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updateRate()
                }
            }

        updateRate()

        AlertDialog.Builder(this)
            .setTitle("नई पीस एंट्री")
            .setView(box)
            .setPositiveButton("सेव करें") { _, _ ->
                val selected = spinner.selectedItemPosition
                val pieceName = names.getOrNull(selected) ?: ""
                val r = rate.text.toString().toDoubleOrNull()
                val q = qty.text.toString().toDoubleOrNull()

                if (r == null || q == null || r < 0 || q <= 0) {
                    toast("रेट और मात्रा सही भरें")
                    return@setPositiveButton
                }

                ProductionManager.addEntry(
                    entries,
                    pieceName,
                    q,
                    r,
                    note.text.toString(),
                    now()
                )

                saveData()
                photoEntryIndex = entries.length() - 1

                AlertDialog.Builder(this)
                    .setMessage("एंट्री सेव हो गई। क्या फोटो भी जोड़नी है?")
                    .setPositiveButton("फोटो जोड़ें") { _, _ ->
                        pickPhotos()
                    }
                    .setNegativeButton("अभी नहीं") { _, _ ->
                        showDashboard()
                    }
                    .show()
            }
            .setNegativeButton("रद्द", null)
            .show()
    }
    private fun addMoneyRecord(isPayment: Boolean) {
        val title = if (isPayment) "प्राप्त रकम / एडवांस" else "खर्चा जोड़ें"

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
                val value = amount.text.toString().toDoubleOrNull()

                if (value == null || value <= 0) {
                    toast("सही रकम भरें")
                    return@setPositiveButton
                }

                val record = JSONObject()
                    .put(
                        "label",
                        description.text.toString().ifBlank { title }
                    )
                    .put("amount", value)
                    .put("date", now())

                if (isPayment) payments.put(record)
                else expenses.put(record)

                saveData()
                showDashboard()
                toast("रिकॉर्ड सेव हो गया")
            }
            .setNegativeButton("रद्द", null)
            .show()
    }

    private fun showHistory(type: String) {
        page("पूरा हिसाब / हिस्ट्री")

        if (type == "all" || type == "entries") {
            addLabel("पीस और कमाई की एंट्री", Color.CYAN, 18f)

            for (i in entries.length() - 1 downTo 0) {
                val item = entries.optJSONObject(i) ?: continue
                val index = i

                val photos = item.optJSONArray("photos") ?: JSONArray()

                addCard(
                    item.optString("piece"),
                    "मात्रा: ${item.optDouble("qty")} पीस\n" +
                        "रेट: ${money(item.optDouble("rate"))}\n" +
                        "कुल: ${money(item.optDouble("amount"))}\n" +
                        "दिनांक: ${item.optString("date")}\n" +
                        "नोट: ${item.optString("note")}\n" +
                        "फोटो: ${photos.length()}",
                    blue
                )

                addButton("इस एंट्री में फोटो जोड़ें") {
                    photoEntryIndex = index
                    pickPhotos()
                }

                addButton("एंट्री हटाएँ", red) {
                    AlertDialog.Builder(this)
                        .setMessage("क्या यह पीस एंट्री हटानी है?")
                        .setPositiveButton("हाँ, हटाएँ") { _, _ ->
                            deleteEntryPhotos(entries.optJSONObject(index))
                            entries.remove(index)
                            saveData()
                            showHistory(type)
                        }
                        .setNegativeButton("रद्द", null)
                        .show()
                }
            }
        }

        if (type == "all" || type == "expenses") {
            addLabel("खर्चे की हिस्ट्री", Color.RED, 18f)

            for (i in expenses.length() - 1 downTo 0) {
                val item = expenses.optJSONObject(i) ?: continue
                val index = i

                addCard(
                    item.optString("label"),
                    "${money(item.optDouble("amount"))}\n${item.optString("date")}",
                    red
                )

                addButton("खर्च बदलें") {
                    editMoneyRecord(false, index)
                }

                addButton("खर्च हटाएँ", red) {
                    deleteMoneyRecord(false, index)
                }
            }
        }

        if (type == "all" || type == "payments") {
            addLabel("प्राप्त रकम / एडवांस हिस्ट्री", Color.GREEN, 18f)

            for (i in payments.length() - 1 downTo 0) {
                val item = payments.optJSONObject(i) ?: continue
                val index = i

                addCard(
                    item.optString("label"),
                    "${money(item.optDouble("amount"))}\n${item.optString("date")}",
                    green
                )

                addButton("रकम बदलें") {
                    editMoneyRecord(true, index)
                }

                addButton("रकम हटाएँ", red) {
                    deleteMoneyRecord(true, index)
                }
            }
        }

        addButton("डैशबोर्ड") {
            showDashboard()
        }
    }

    private fun editMoneyRecord(isPayment: Boolean, index: Int) {
        val target = if (isPayment) payments else expenses
        val old = target.optJSONObject(index) ?: return

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 8, 20, 4)
        }

        val description = input("विवरण").apply {
            setText(old.optString("label"))
        }

        val amount = input("रकम", true).apply {
            setText(old.optDouble("amount").toString())
        }

        box.addView(description)
        box.addView(amount)

        AlertDialog.Builder(this)
            .setTitle("रिकॉर्ड बदलें")
            .setView(box)
            .setPositiveButton("सेव") { _, _ ->
                val value = amount.text.toString().toDoubleOrNull()

                if (value == null || value <= 0) {
                    toast("सही रकम भरें")
                    return@setPositiveButton
                }

                target.put(
                    index,
                    JSONObject()
                        .put(
                            "label",
                            description.text.toString().ifBlank { "रिकॉर्ड" }
                        )
                        .put("amount", value)
                        .put("date", old.optString("date", now()))
                )

                saveData()
                showHistory(if (isPayment) "payments" else "expenses")
                toast("बदलाव सेव हो गया")
            }
            .setNegativeButton("रद्द", null)
            .show()
    }

    private fun deleteMoneyRecord(isPayment: Boolean, index: Int) {
        val target = if (isPayment) payments else expenses

        AlertDialog.Builder(this)
            .setMessage("क्या यह रिकॉर्ड हटाना है?")
            .setPositiveButton("हाँ, हटाएँ") { _, _ ->
                target.remove(index)
                saveData()
                showHistory(if (isPayment) "payments" else "expenses")
            }
            .setNegativeButton("रद्द", null)
            .show()
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

    private fun createDocument(
        request: Int,
        name: String,
        mime: String
    ) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
            putExtra(Intent.EXTRA_TITLE, name)
        }

        startActivityForResult(intent, request)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK || data == null) return

        try {
            when (requestCode) {

                PHOTO_REQUEST -> {
                    val uris = mutableListOf<Uri>()

                    data.clipData?.let { clip ->
                        for (i in 0 until clip.itemCount) {
                            uris.add(clip.getItemAt(i).uri)
                        }
                    }

                    if (uris.isEmpty()) {
                        data.data?.let { uris.add(it) }
                    }

                    val saved =
                        PhotoManager.copyUrisToPrivateStorage(this, uris)

                    val entry = entries.optJSONObject(photoEntryIndex)

                    if (entry != null) {
                        val photos =
                            entry.optJSONArray("photos") ?: JSONArray()

                        saved.forEach { photos.put(it) }

                        entry.put("photos", photos)
                        saveData()

                        toast("${saved.size} फोटो जोड़ दिए गए")
                    }

                    showHistory("entries")
                }

                REPORT_REQUEST -> {
                    data.data?.let { uri ->
                        ReportManager.writePdf(
                            this,
                            uri,
                            pieces,
                            entries,
                            expenses,
                            payments
                        )

                        toast("PDF रिपोर्ट सेव हो गई")
                    }
                }

                BACKUP_REQUEST -> {
                    data.data?.let { uri ->
                        ReportManager.writeBackup(
                            this,
                            uri,
                            pieces,
                            entries,
                            expenses,
                            payments
                        )

                        toast("बैकअप सेव हो गया")
                    }
                }

                RESTORE_REQUEST -> {
                    data.data?.let { uri ->
                        val restored =
                            ReportManager.readBackup(this, uri)

                        if (restored) {
                            replaceArray(
                                pieces,
                                ReportManager.restored.optJSONArray("pieces")
                            )

                            replaceArray(
                                entries,
                                ReportManager.restored.optJSONArray("entries")
                            )

                            replaceArray(
                                expenses,
                                ReportManager.restored.optJSONArray("expenses")
                            )

                            replaceArray(
                                payments,
                                ReportManager.restored.optJSONArray("payments")
                            )

                            saveData()
                            showDashboard()
                            toast("बैकअप Restore हो गया")
                        } else {
                            toast("बैकअप फाइल सही नहीं है")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            toast("त्रुटि: ${e.message ?: "फाइल नहीं खुली"}")
        }
    }

    private fun replaceArray(
        target: JSONArray,
        source: JSONArray?
    ) {
        while (target.length() > 0) {
            target.remove(target.length() - 1)
        }

        if (source != null) {
            for (i in 0 until source.length()) {
                target.put(source.get(i))
            }
        }
    }

    private fun resetData() {
        AlertDialog.Builder(this)
            .setTitle("एक बार पुष्टि करें")
            .setMessage(
                "क्या पूरा हिसाब, पीस सूची, खर्चे और प्राप्त रकम शून्य करनी है? यह वापस नहीं आएगा।"
            )
            .setPositiveButton("हाँ, सब शून्य करें") { _, _ ->

                for (i in 0 until entries.length()) {
                    deleteEntryPhotos(entries.optJSONObject(i))
                }

                listOf(
                    pieces,
                    entries,
                    expenses,
                    payments
                ).forEach { array ->
                    while (array.length() > 0) {
                        array.remove(array.length() - 1)
                    }
                }

                saveData()
                showDashboard()
                toast("सारा हिसाब शून्य हो गया")
            }
            .setNegativeButton("नहीं, रहने दें", null)
            .show()
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        private const val PHOTO_REQUEST = 701
        private const val REPORT_REQUEST = 702
        private const val BACKUP_REQUEST = 703
        private const val RESTORE_REQUEST = 704
    }
}
