package com.example.sushiking

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ThemeActivity : AppCompatActivity() {

    private var selectedIndex = 0
    private var isDark = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvPreviewTitle: TextView
    private lateinit var previewCard: CardView
    private lateinit var switchDark: Switch
    private lateinit var previewHeader: View
    private lateinit var btnApply: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        selectedIndex = ThemeManager.getColorIndex(this)
        isDark = ThemeManager.isDarkMode(this)

        recyclerView = findViewById(R.id.recyclerView)
        tvPreviewTitle = findViewById(R.id.tvPreviewTitle)
        previewCard = findViewById(R.id.previewCard)
        switchDark = findViewById(R.id.switchDark)
        previewHeader = findViewById(R.id.previewHeader)
        btnApply = findViewById(R.id.btnApply)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        switchDark.isChecked = isDark
        switchDark.setOnCheckedChangeListener { _, checked ->
            isDark = checked
            updatePreview()
            loadThemes()
        }

        recyclerView.layoutManager = GridLayoutManager(this, 3)
        loadThemes()
        updatePreview()

        btnApply.setOnClickListener {
            // Save theme
            ThemeManager.saveTheme(this, selectedIndex, isDark)

            // Show confirmation
            MaterialAlertDialogBuilder(this)
                .setTitle("✅ Theme Saved!")
                .setMessage("Your theme has been saved!\n\nClose and reopen the app to see the full effect on all screens.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        }
    }

    private fun loadThemes() {
        val themes = if (isDark) ThemeManager.darkThemes else ThemeManager.lightThemes
        recyclerView.adapter = ThemeAdapter(themes)
    }

    private fun updatePreview() {
        val themes = if (isDark) ThemeManager.darkThemes else ThemeManager.lightThemes
        val theme = themes[selectedIndex]
        previewCard.setCardBackgroundColor(Color.parseColor(theme.backgroundColor))
        tvPreviewTitle.setTextColor(Color.parseColor(theme.primaryColor))
        tvPreviewTitle.text = "Preview: ${theme.name}"
        previewHeader.setBackgroundColor(Color.parseColor(theme.primaryColor))
        btnApply.setBackgroundColor(Color.parseColor(theme.primaryColor))
    }

    inner class ThemeAdapter(private val list: List<ThemeManager.ThemeOption>) :
        RecyclerView.Adapter<ThemeAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val colorCircle: View = view.findViewById(R.id.colorCircle)
            val tvName: TextView = view.findViewById(R.id.tvThemeName)
            val checkIcon: TextView = view.findViewById(R.id.checkIcon)
            val card: CardView = view.findViewById(R.id.themeCard)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_theme, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val theme = list[position]

            val circle = GradientDrawable()
            circle.shape = GradientDrawable.OVAL
            circle.setColor(Color.parseColor(theme.primaryColor))
            holder.colorCircle.background = circle

            holder.tvName.text = theme.name
            holder.checkIcon.visibility = if (position == selectedIndex) View.VISIBLE else View.GONE

            holder.card.setCardBackgroundColor(
                if (position == selectedIndex)
                    Color.parseColor(theme.backgroundColor)
                else Color.WHITE
            )

            holder.card.setOnClickListener {
                selectedIndex = position
                notifyDataSetChanged()
                updatePreview()
            }
        }

        override fun getItemCount() = list.size
    }
}
