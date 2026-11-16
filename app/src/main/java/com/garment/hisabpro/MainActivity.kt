
package com.garment.hisabpro

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val bg = Color.rgb(13, 18, 38)
    private val white = Color.WHITE
    private val blue = Color.rgb(35, 115, 245)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(bg)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 28, 20, 24)
        }

        scroll.addView(root)

        val heading = TextView(this).apply {
            text = "GARMENT HISAB PRO"
            textSize = 25f
            setTextColor(white)
            gravity = Gravity.CENTER
        }
        root.addView(heading)

        val subtitle = TextView(this).apply {
            text = "आपका अपना काम और कमाई का हिसाब"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 25)
        }
        root.addView(subtitle)

        addSummary(root, "कुल पीस", "0")
        addSummary(root, "कुल कमाई", "₹0")
        addSummary(root, "कुल खर्च", "₹0")
        addSummary(root, "एडवांस / प्राप्त रकम", "₹0")
        addSummary(root, "बाकी रकम", "₹0")

        addAction(root, "➕  नई पीस एंट्री")
        addAction(root, "💰  खर्चा जोड़ें")
        addAction(root, "📋  पीस और रेट सेटिंग")
        addAction(root, "📊  सप्ताह / 15 दिन की रिपोर्ट")
        addAction(root, "🎨  डिजाइन और कस्टमाइजेशन")
        addAction(root, "💾  बैकअप और रीस्टोर")

        setContentView(scroll)
    }

    private fun addSummary(
        parent: LinearLayout,
        title: String,
        value: String
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundColor(Color.rgb(28, 39, 68))
        }

        val label = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(Color.LTGRAY)
        }

        val amount = TextView(this).apply {
            text = value
            textSize = 25f
            setTextColor(white)
            setPadding(0, 7, 0, 0)
        }

        card.addView(label)
        card.addView(amount)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 12
        parent.addView(card, params)
    }

    private fun addAction(
        parent: LinearLayout,
        title: String
    ) {
        val button = Button(this).apply {
            text = title
            textSize = 15f
            setTextColor(white)
            setBackgroundColor(blue)
            isAllCaps = false
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 10
        parent.addView(button, params)
    }
}
