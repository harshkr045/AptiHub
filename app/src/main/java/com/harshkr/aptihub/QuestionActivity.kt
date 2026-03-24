package com.harshkr.aptihub

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class QuestionActivity : AppCompatActivity() {

    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var wrongAnswers = 0
    private lateinit var levelTimer: CountDownTimer

    private lateinit var questions: List<Question>
    private val userAnswers = mutableMapOf<Int, String>()

    private lateinit var questionText: TextView
    private lateinit var option1: Button
    private lateinit var option2: Button
    private lateinit var option3: Button
    private lateinit var option4: Button
    private lateinit var correctAnswersText: TextView
    private lateinit var wrongAnswersText: TextView
    private lateinit var timerText: TextView
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var skipButton: Button
    private var defaultButtonTint: ColorStateList? = null

    private var shuffledOptions: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        questionText = findViewById(R.id.question_text)
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        correctAnswersText = findViewById(R.id.correct_answers)
        wrongAnswersText = findViewById(R.id.wrong_answers)
        timerText = findViewById(R.id.timer)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)
        skipButton = findViewById(R.id.skip_button)

        defaultButtonTint = ContextCompat.getColorStateList(this, R.color.option_color)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val difficulty = intent.getStringExtra("DIFFICULTY")
        val topicId = intent.getStringExtra("TOPIC_ID")

        if (difficulty != null && topicId != null) {
            loadQuestionsFromFirestore(difficulty, topicId)
        } else {
            Toast.makeText(this, "Topic or difficulty not selected.", Toast.LENGTH_LONG).show()
            finish()
        }

        val optionButtons = listOf(option1, option2, option3, option4)
        optionButtons.forEach { button ->
            button.setOnClickListener {
                checkAnswer(button)
            }
        }

        nextButton.setOnClickListener {
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion()
            } else {
                finishQuiz()
            }
        }

        previousButton.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                loadQuestion()
            }
        }

        skipButton.setOnClickListener {
            if (userAnswers.containsKey(currentQuestionIndex)) return@setOnClickListener
            userAnswers[currentQuestionIndex] = "skipped"
            if (currentQuestionIndex == questions.size - 1) {
                nextButton.isEnabled = true // Enable submit button
            } else {
                currentQuestionIndex++
                loadQuestion()
            }
        }
    }

    private fun loadQuestionsFromFirestore(difficulty: String, topicId: String) {
        val collectionName = when (difficulty) {
            "Easy" -> "easy_questions"
            "Medium" -> "medium_questions"
            "Hard" -> "hard_questions"
            else -> {
                Toast.makeText(this, "Invalid difficulty level.", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }

        val db = Firebase.firestore
        db.collection(collectionName)
            .whereEqualTo("topic", topicId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    AlertDialog.Builder(this)
                        .setTitle("No Questions Found")
                        .setMessage("No questions found for this topic and difficulty. Please select another topic or difficulty.")
                        .setPositiveButton("OK") { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                } else {
                    questions = result.toObjects(Question::class.java)
                    if (questions.isNotEmpty()) {
                        questions = questions.shuffled()
                        startLevelTimer()
                        loadQuestion()
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("No Questions Found")
                            .setMessage("No questions found for this topic and difficulty. Please select another topic or difficulty.")
                            .setPositiveButton("OK") { _, _ -> finish() }
                            .setCancelable(false)
                            .show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("QuestionActivity", "Error getting documents.", exception)
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Failed to load questions. Please check your internet connection and try again.")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::levelTimer.isInitialized) {
            levelTimer.cancel()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadQuestion() {
        resetButtons()
        val question = questions[currentQuestionIndex]
        questionText.text = question.question

        if (question.options.size < 4) {
            AlertDialog.Builder(this)
                .setTitle("Question Error")
                .setMessage("A question could not be loaded correctly. The quiz will now end.")
                .setPositiveButton("OK") { _, _ -> finishQuiz() }
                .setCancelable(false)
                .show()
            return
        }

        val options = listOf(option1, option2, option3, option4)
        shuffledOptions = question.options.shuffled()
        options.forEachIndexed { index, button ->
            button.text = shuffledOptions[index]
        }

        previousButton.isEnabled = currentQuestionIndex > 0
        nextButton.isEnabled = false

        if (currentQuestionIndex == questions.size - 1) {
            nextButton.text = "Submit"
            (nextButton as? com.google.android.material.button.MaterialButton)?.icon = null
            nextButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        } else {
            nextButton.text = "Next"
            (nextButton as? com.google.android.material.button.MaterialButton)?.icon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_forward)
            nextButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }

        if (userAnswers.containsKey(currentQuestionIndex)) {
            val selectedAnswer = userAnswers[currentQuestionIndex]
            if (selectedAnswer != "skipped") {
                val selectedButton = options.find { it.text.toString() == selectedAnswer }
                if (selectedButton != null) {
                    highlightAnswer(selectedButton, false)
                }
            } else {
                nextButton.isEnabled = true
            }
        }
    }

    private fun startLevelTimer() {
        val totalTimeInMillis = questions.size * 60 * 1000L
        levelTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            @SuppressLint("DefaultLocale")
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                Toast.makeText(this@QuestionActivity, "Time's up!", Toast.LENGTH_SHORT).show()
                finishQuiz()
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    private fun checkAnswer(selectedOption: Button) {
        if (userAnswers.containsKey(currentQuestionIndex)) return // Already answered

        val selectedAnswer = selectedOption.text.toString()
        userAnswers[currentQuestionIndex] = selectedAnswer
        highlightAnswer(selectedOption, true)
    }

    @SuppressLint("SetTextI18n")
    private fun highlightAnswer(selectedOption: Button, updateScore: Boolean) {
        val question = questions[currentQuestionIndex]
        val selectedAnswer = selectedOption.text.toString()

        if (selectedAnswer.trim() == question.correctAnswer.trim()) {
            if(updateScore) correctAnswers++
            selectedOption.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.correct_answer))
        } else {
            if(updateScore) wrongAnswers++
            selectedOption.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.wrong_answer))

            val correctButton = findCorrectButton(question.correctAnswer)
            correctButton?.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.correct_answer))
        }
        correctAnswersText.text = "Correct: $correctAnswers"
        wrongAnswersText.text = "Wrong: $wrongAnswers"

        disableOptions()
        nextButton.isEnabled = true
    }

    private fun findCorrectButton(correctAnswer: String): Button? {
        val options = listOf(option1, option2, option3, option4)
        return options.find { it.text.toString().trim() == correctAnswer.trim() }
    }

    private fun disableOptions() {
        listOf(option1, option2, option3, option4).forEach { it.isEnabled = false }
    }

    private fun resetButtons() {
        val optionButtons = listOf(option1, option2, option3, option4)
        optionButtons.forEach { button ->
            button.isEnabled = true
            button.backgroundTintList = defaultButtonTint
        }
    }

    private fun finishQuiz() {
        if (::levelTimer.isInitialized) {
            levelTimer.cancel()
        }
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("CORRECT_ANSWERS", correctAnswers)
        intent.putExtra("WRONG_ANSWERS", wrongAnswers)
        intent.putExtra("TOTAL_QUESTIONS", questions.size)
        startActivity(intent)
        finish()
    }
}
