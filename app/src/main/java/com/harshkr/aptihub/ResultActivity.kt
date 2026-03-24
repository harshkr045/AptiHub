package com.harshkr.aptihub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashSet
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar_result)
        setSupportActionBar(toolbar)

        // Save the streak data
        val sharedPref = getSharedPreferences("streak_data", MODE_PRIVATE)
        val editor = sharedPref.edit()
        val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val currentDate = sdf.format(Date())
        val loginDates = sharedPref.getStringSet("login_dates", HashSet<String>()) ?: HashSet<String>()
        loginDates.add(currentDate)
        editor.putStringSet("login_dates", loginDates)
        editor.apply()

        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)
        // Get the total from the intent, with a default fallback.
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 10)
        val score = correctAnswers // Each question is worth 1 point

        val scoreText = findViewById<TextView>(R.id.score_text)
        val remarkText = findViewById<TextView>(R.id.remark_text)

        scoreText.text = "Score: $score / $totalQuestions"

        // Calculate percentage for a more flexible remark system
        val percentage = if (totalQuestions > 0) (score.toDouble() / totalQuestions) * 100 else 0.0

        val remark = when {
            percentage >= 95 -> "Excellent mastery demonstrated."
            percentage >= 80 -> "Strong understanding shown."
            percentage >= 60 -> "Solid, good performance."
            percentage >= 40 -> "Needs more focus."
            percentage >= 20 -> "Below basic standard."
            else -> "Urgent improvement needed."
        }
        remarkText.text = remark

        val exitButton = findViewById<Button>(R.id.exit_button)
        val homeButton = findViewById<Button>(R.id.home_button)

        exitButton.setOnClickListener {
            finishAffinity()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, TopicSelectionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
