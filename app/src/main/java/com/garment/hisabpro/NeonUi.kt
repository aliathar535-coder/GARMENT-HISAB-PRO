
package com.garment.hisabpro

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

object NeonUi {

    fun background(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 18, 14, 24)
            setBackgroundColor(Color.rgb(4, 7, 22))
        }
    }

    fun title(
        context: Context,
        text: String,
        size: Float = 25f,
        color: Int = Color.WHITE
    ): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            setPadding(6, 12, 6, 12)
            setShadowLayer(12f, 0f, 3f, color)
        }
    }

    fun card(
        context: Context,
        heading: String,
        value: String,
        colors: List<Int>,
        valueColor: Int = Color.WHITE
    ): LinearLayout {
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(10, 14, 10, 14)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                colors.toIntArray()
            ).apply {
                cornerRadius = 25f
                setStroke(3, Color.argb(220, 100, 220, 255))
            }
            elevation = 12f
        }

        box.addView(TextView(context).apply {
            text = heading
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        })

        box.addView(TextView(context).apply {
            text = value
            textSize = 22f
            setTextColor(valueColor)
            gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            setShadowLayer(8f, 0f, 2f, valueColor)
            setPadding(0, 7, 0, 0)
        })

        return box
    }

    fun actionButton(
        context: Context,
        text: String,
        colors: List<Int>,
        onClick: () -> Unit
    ): TextView {
        return TextView(context).apply {
            this.text = text
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            setPadding(18, 17, 18, 17)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                colors.toIntArray()
            ).apply {
                cornerRadius = 22f
                setStroke(2, Color.WHITE)
            }
            elevation = 10f
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    fun addSpace(parent: LinearLayout, height: Int = 10) {
        parent.addView(View(parent.context), LinearLayout.LayoutParams(1, height))
    }

    fun addFullWidth(parent: LinearLayout, child: View, bottom: Int = 10) {
        parent.addView(
            child,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = bottom
            }
        )
    }
}
