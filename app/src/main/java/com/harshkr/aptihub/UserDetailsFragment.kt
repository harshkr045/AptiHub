package com.harshkr.aptihub

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.util.Calendar
import java.util.Locale

class UserDetailsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val usernameField = view.findViewById<TextInputEditText>(R.id.username_field)
        val dobField = view.findViewById<TextInputEditText>(R.id.dob_field)
        val saveButton = view.findViewById<Button>(R.id.save_button)

        dobField.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val ddmmyyyy = "DDMMYYYY"
            private val cal = Calendar.getInstance()

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() == current) {
                    return
                }

                var clean = s.toString().replace("[^\\d.]".toRegex(), "")
                val cleanC = current.replace("[^\\d.]".toRegex(), "")

                val cl = clean.length
                var sel = cl
                if (cl > 2) sel++
                if (cl > 4) sel++

                if (clean == cleanC) sel--

                if (clean.length < 8) {
                    clean += ddmmyyyy.substring(clean.length)
                } else {
                    var day = Integer.parseInt(clean.take(2))
                    var mon = Integer.parseInt(clean.substring(2, 4))
                    var year = Integer.parseInt(clean.substring(4, 8))

                    mon = if (mon < 1) 1 else if (mon > 12) 12 else mon
                    cal.set(Calendar.MONTH, mon - 1)
                    year = if (year < 1900) 1900 else if (year > 2100) 2100 else year
                    cal.set(Calendar.YEAR, year)

                    day = if (day > cal.getActualMaximum(Calendar.DATE)) cal.getActualMaximum(Calendar.DATE) else day
                    clean = String.format(Locale.US, "%02d%02d%04d", day, mon, year)
                }

                clean = String.format(
                    Locale.US,
                    "%s/%s/%s",
                    clean.take(2),
                    clean.substring(2, 4),
                    clean.substring(4, 8)
                )

                sel = if (sel < 0) 0 else sel
                current = clean
                dobField.setText(current)
                dobField.setSelection(if (sel < current.length) sel else current.length)
            }
        })


        saveButton.setOnClickListener {
            val username = usernameField.text.toString().trim()
            val dob = dobField.text.toString().trim()

            if (username.isNotEmpty() && dob.length == 10 && user != null) { // Check if dob is fully entered
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            // Force a reload of the user's profile from the server
                            user.reload().addOnCompleteListener { reloadTask ->
                                if (reloadTask.isSuccessful) {
                                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(activity, TopicSelectionActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                } else {
                                    // Fallback if reload fails, but still navigate
                                    Toast.makeText(context, "Could not refresh profile, but navigating...", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(activity, TopicSelectionActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Failed to update profile: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter all details correctly.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
