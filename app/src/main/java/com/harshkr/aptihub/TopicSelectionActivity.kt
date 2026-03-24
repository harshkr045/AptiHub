package com.harshkr.aptihub

// Import the Topic data class
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.HashSet

class TopicSelectionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var topicAdapter: TopicAdapter
    private lateinit var welcomeText: TextView
    private var streakCount = 0
    private lateinit var streakCountText: TextView
    private val allTopics = mutableListOf<Topic>()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topic_selection)

        auth = FirebaseAuth.getInstance()

        welcomeText = findViewById(R.id.welcome_text)
        recyclerView = findViewById(R.id.topics_recycler_view)
        val searchView = findViewById<SearchView>(R.id.search_view)
        progressBar = findViewById(R.id.progress_bar)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        topicAdapter = TopicAdapter(allTopics) { topic ->
            val intent = Intent(this, QuizLevelActivity::class.java)
            intent.putExtra("TOPIC_ID", topic.id)
            intent.putExtra("TOPIC_TITLE", topic.title)
            startActivity(intent)
        }

        recyclerView.adapter = topicAdapter
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.layoutManager = GridLayoutManager(this, 2)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredTopics = allTopics.filter {
                    it.title.contains(newText ?: "", ignoreCase = true)
                }
                topicAdapter.updateTopics(filteredTopics)
                return true
            }
        })
        fetchTopicsFromFirestore()
    }

    private fun fetchTopicsFromFirestore() {
        progressBar.visibility = View.VISIBLE
        val db = Firebase.firestore
        val topics = mutableSetOf<String>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val collections = listOf("easy_questions", "medium_questions", "hard_questions")
                for (collectionName in collections) {
                    val snapshot = db.collection(collectionName).get().await()
                    val collectionTopics = snapshot.documents.mapNotNull { it.getString("topic") }.filter { it.isNotEmpty() }
                    topics.addAll(collectionTopics)
                }

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (topics.isEmpty()) {
                        Toast.makeText(this@TopicSelectionActivity, "No topics found.", Toast.LENGTH_SHORT).show()
                    } else {
                        allTopics.clear()
                        topics.forEach { topicId ->
                            val title = topicId.replace('_', ' ').split(' ').joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                            allTopics.add(Topic(topicId, title, getIconForTopic(topicId)))
                        }
                        topicAdapter.updateTopics(allTopics)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@TopicSelectionActivity, "Error fetching topics: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @DrawableRes
    private fun getIconForTopic(topicId: String): Int {
        return when (topicId) {
            "profit_loss" -> R.drawable.profit_loss_icon
            "time_and_work" -> R.drawable.time_work_icon
            "ratio_and_proportion" -> R.drawable.ratio_and_proportion_icon
            "simple_interest" -> R.drawable.simple_interest_icon
            else -> R.drawable.ic_launcher_foreground
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        // Refresh user data every time the activity is resumed
        auth.currentUser?.reload()?.addOnCompleteListener {
            val user = auth.currentUser
            val userName = user?.displayName ?: user?.email?.split("@")?.get(0) ?: "User"
            welcomeText.text = "Welcome, $userName! \nSelect the question field you want to access..."
        }
        updateStreak()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_topic_selection, menu)
        val streakItem = menu?.findItem(R.id.action_streak)
        val actionView = streakItem?.actionView
        streakCountText = actionView?.findViewById(R.id.streak_count_text)!!
        streakCountText.text = streakCount.toString()
        actionView.setOnClickListener {
            onOptionsItemSelected(streakItem)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_streak -> {
                startActivity(Intent(this, StreakActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateStreak() {
        val sharedPref = getSharedPreferences("streak_data", MODE_PRIVATE)
        val lastLogin = sharedPref.getLong("last_login", 0)
        val streak = sharedPref.getInt("streak_count", 0)
        val loginDates = sharedPref.getStringSet("login_dates", HashSet<String>()) ?: HashSet<String>()

        val today = Calendar.getInstance()
        val lastLoginCal = Calendar.getInstance()
        lastLoginCal.timeInMillis = lastLogin

        val isSameDay = today.get(Calendar.YEAR) == lastLoginCal.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == lastLoginCal.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) {
            streakCount = streak
        } else {
            lastLoginCal.add(Calendar.DAY_OF_YEAR, 1)
            val isConsecutiveDay = today.get(Calendar.YEAR) == lastLoginCal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == lastLoginCal.get(Calendar.DAY_OF_YEAR)

            streakCount = if (isConsecutiveDay) streak + 1 else 1

            val todayDateString = "${today.get(Calendar.YEAR)}-${today.get(Calendar.MONTH) + 1}-${today.get(Calendar.DAY_OF_MONTH)}"
            val newLoginDates = HashSet(loginDates)
            newLoginDates.add(todayDateString)

            sharedPref.edit {
                putLong("last_login", today.timeInMillis)
                putInt("streak_count", streakCount)
                putStringSet("login_dates", newLoginDates)
            }
        }

        invalidateOptionsMenu()
    }
}