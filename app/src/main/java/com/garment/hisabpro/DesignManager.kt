
package com.garment.hisabpro

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable

data class ClockStyle(
    val name: String,
    val colors: List<Int>,
    val textColor: Int,
    val is3D: Boolean
)

object DesignManager {

    private fun c(value: String) = Color.parseColor(value)

    val clockStyles = listOf(
        ClockStyle("Neon Blue", listOf(c("#001B45"), c("#0066FF")), c("#42E8FF"), true),
        ClockStyle("Gold 3D", listOf(c("#392000"), c("#FFB300")), c("#FFE58A"), true),
        ClockStyle("Royal Purple", listOf(c("#210044"), c("#8A2BE2")), c("#E9B8FF"), true),
        ClockStyle("Cyber Pink", listOf(c("#43002D"), c("#FF008C")), c("#FF9CE0"), true),
        ClockStyle("Emerald", listOf(c("#003C27"), c("#00C878")), c("#8AFFC7"), true),
        ClockStyle("Red Fire", listOf(c("#4A0000"), c("#FF2929")), c("#FFD0D0"), true),
        ClockStyle("Ice Glass", listOf(c("#073B50"), c("#00BBDD")), c("#D8FAFF"), true),
        ClockStyle("Silver", listOf(c("#202633"), c("#9AA8BB")), c("#FFFFFF"), true),
        ClockStyle("Orange Glow", listOf(c("#542000"), c("#FF7800")), c("#FFE0A3"), true),
        ClockStyle("Lime 3D", listOf(c("#263900"), c("#A6E500")), c("#F0FF9A"), true),
        ClockStyle("Ocean", listOf(c("#001B35"), c("#007DAD")), c("#9BEAFF"), false),
        ClockStyle("Rose Gold", listOf(c("#481C28"), c("#E7A18E")), c("#FFE2D7"), true),
        ClockStyle("Matrix", listOf(c("#001B08"), c("#00A52B")), c("#8CFF9F"), true),
        ClockStyle("Royal Blue", listOf(c("#10105C"), c("#4040FF")), c("#D6D6FF"), true),
        ClockStyle("Sunset", listOf(c("#501500"), c("#FF5D36")), c("#FFE0B0"), false),
        ClockStyle("Aqua", listOf(c("#003C45"), c("#00D5C5")), c("#B9FFF7"), true),
        ClockStyle("Violet", listOf(c("#31004A"), c("#B34CFF")), c("#F1D0FF"), false),
        ClockStyle("Copper", listOf(c("#44200D"), c("#C8793B")), c("#FFE1C4"), true),
        ClockStyle("Ruby", listOf(c("#460018"), c("#D50045")), c("#FFC1D5"), true),
        ClockStyle("Electric", listOf(c("#101044"), c("#00A5FF")), c("#C5F5FF"), true),
        ClockStyle("White Glass", listOf(c("#303640"), c("#687586")), c("#FFFFFF"), false),
        ClockStyle("Toxic", listOf(c("#243500"), c("#77CC00")), c("#E5FF9C"), true),
        ClockStyle("Candy", listOf(c("#49005D"), c("#FF55B7")), c("#FFE0F5"), false),
        ClockStyle("Deep Sea", listOf(c("#00152E"), c("#005C80")), c("#B0E9FF"), true),
        ClockStyle("Amber", listOf(c("#492900"), c("#D98A00")), c("#FFF0B0"), true),
        ClockStyle("Mint", listOf(c("#003E39"), c("#54DDBB")), c("#D0FFF1"), false),
        ClockStyle("Galaxy", listOf(c("#16002E"), c("#5D22A8")), c("#E5C7FF"), true),
        ClockStyle("Laser", listOf(c("#001B38"), c("#00D9FF")), c("#FFFFFF"), true),
        ClockStyle("Black Gold", listOf(c("#080808"), c("#7D5700")), c("#FFD45A"), true),
        ClockStyle("Diamond", listOf(c("#33465B"), c("#B8E5FF")), c("#FFFFFF"), true)
    )

    fun saveName(context: Context, name: String) {
        context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .edit().putString("owner_name", name).apply()
    }

    fun getName(context: Context): String {
        return context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .getString("owner_name", "आपका नाम") ?: "आपका नाम"
    }

    fun saveClockStyle(context: Context, index: Int) {
        context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .edit().putInt("clock_style", index.coerceIn(0, 29)).apply()
    }

    fun getClockStyle(context: Context): ClockStyle {
        val index = context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .getInt("clock_style", 0).coerceIn(0, 29)
        return clockStyles[index]
    }

    fun saveThreeD(context: Context, enabled: Boolean) {
        context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .edit().putBoolean("three_d", enabled).apply()
    }

    fun isThreeDEnabled(context: Context): Boolean {
        return context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .getBoolean("three_d", true)
    }

    fun saveCustomTextColor(context: Context, color: Int) {
        context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .edit().putInt("custom_text_color", color).apply()
    }

    fun getTextColor(context: Context): Int {
        return context.getSharedPreferences("garment_design", Context.MODE_PRIVATE)
            .getInt("custom_text_color", Color.WHITE)
    }

    fun gradient(colors: List<Int>, radius: Float = 24f): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            colors.toIntArray()
        ).apply {
            cornerRadius = radius
            setStroke(2, Color.argb(180, 90, 210, 255))
        }
    }

    fun textTypeface(bold: Boolean = true): Typeface {
        return if (bold) Typeface.create("sans-serif-black", Typeface.BOLD)
        else Typeface.create("sans-serif", Typeface.NORMAL)
    }
}
