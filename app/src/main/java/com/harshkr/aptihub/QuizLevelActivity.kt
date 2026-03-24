package com.harshkr.aptihub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class QuizLevelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_level)

        val topicId = intent.getStringExtra("TOPIC_ID")
        val topicTitle = intent.getStringExtra("TOPIC_TITLE")

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = topicTitle
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val easyButton = findViewById<Button>(R.id.easy_button)
        val mediumButton = findViewById<Button>(R.id.medium_button)
        val hardButton = findViewById<Button>(R.id.hard_button)

        easyButton.setOnClickListener {
            startQuiz("Easy", topicId)
        }

        mediumButton.setOnClickListener {
            startQuiz("Medium", topicId)
        }

        hardButton.setOnClickListener {
            startQuiz("Hard", topicId)
        }
    }

    private fun startQuiz(difficulty: String, topicId: String?) {
        val intent = Intent(this, QuestionActivity::class.java)
        intent.putExtra("DIFFICULTY", difficulty)
        intent.putExtra("TOPIC_ID", topicId)
        startActivity(intent)
    }
}
