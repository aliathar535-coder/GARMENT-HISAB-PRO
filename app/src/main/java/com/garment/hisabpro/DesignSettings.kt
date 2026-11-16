
package com.garment.hisabpro

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.*

object DesignSettings {

    fun show(context: Context, onSaved: () -> Unit) {
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 12, 22, 8)
        }

        val name = EditText(context).apply {
            hint = "आपका नाम"
            setSingleLine(true)
            setText(DesignManager.getName(context))
        }

        val clockLabel = TextView(context).apply {
            text = "घड़ी का डिजाइन चुनें"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 14, 0, 6)
        }

        val clockNames = DesignManager.clockStyles.map { it.name }

        val clockSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                clockNames
            )
            setSelection(
                DesignManager.clockStyles.indexOf(
                    DesignManager.getClockStyle(context)
                ).coerceAtLeast(0)
            )
        }

        val threeD = CheckBox(context).apply {
            text = "3D घड़ी इफेक्ट चालू रखें"
            setTextColor(Color.BLACK)
            isChecked = DesignManager.isThreeDEnabled(context)
        }

        val colorLabel = TextView(context).apply {
            text = "अतिरिक्त टेक्स्ट रंग"
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, 14, 0, 6)
        }

        val colors = listOf(
            "सफेद" to Color.WHITE,
            "गोल्ड" to Color.rgb(255, 205, 70),
            "नीला" to Color.rgb(50, 150, 255),
            "हरा" to Color.rgb(0, 210, 140),
            "गुलाबी" to Color.rgb(255, 100, 190),
            "लाल" to Color.rgb(255, 80, 80),
            "बैंगनी" to Color.rgb(190, 120, 255),
            "सियान" to Color.CYAN
        )

        val colorSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                colors.map { it.first }
            )
        }

        box.addView(name)
        box.addView(clockLabel)
        box.addView(clockSpinner)
        box.addView(threeD)
        box.addView(colorLabel)
        box.addView(colorSpinner)

        AlertDialog.Builder(context)
            .setTitle("डिजाइन और नाम सेटिंग")
            .setView(box)
            .setPositiveButton("सेव करें") { _, _ ->
                DesignManager.saveName(
                    context,
                    name.text.toString().trim().ifBlank { "आपका नाम" }
                )

                DesignManager.saveClockStyle(
                    context,
                    clockSpinner.selectedItemPosition
                )

                DesignManager.saveThreeD(context, threeD.isChecked)

                DesignManager.saveCustomTextColor(
                    context,
                    colors[colorSpinner.selectedItemPosition].second
                )

                onSaved()
            }
            .setNegativeButton("रद्द", null)
            .show()
    }
}
