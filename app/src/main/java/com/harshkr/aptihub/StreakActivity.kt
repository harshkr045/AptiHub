package com.harshkr.aptihub

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.util.HashSet

class StreakActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streak)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)
        calendarView.setDateTextAppearance(R.style.CalendarDateTextAppearance)

        val sharedPref = getSharedPreferences("streak_data", MODE_PRIVATE)
        val loginDates = sharedPref.getStringSet("login_dates", HashSet<String>()) ?: HashSet<String>()

        val dates = HashSet<CalendarDay>()

        for (date in loginDates) {
            val parts = date.split("-").map { it.toInt() }
            val year = parts[0]
            val month = parts[1]
            val day = parts[2]
            dates.add(CalendarDay.from(year, month, day))
        }

        val drawable = ContextCompat.getDrawable(this, R.drawable.fire)
        if (drawable != null) {
            calendarView.addDecorator(EventDecorator(drawable, dates))
        }
    }

    inner class EventDecorator(private val drawable: Drawable, private val dates: HashSet<CalendarDay>) : DayViewDecorator {

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(drawable)
            view.addSpan(ForegroundColorSpan(Color.TRANSPARENT))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
