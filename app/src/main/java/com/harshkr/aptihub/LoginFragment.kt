package com.harshkr.aptihub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private var isLoginMode = true
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        auth = FirebaseAuth.getInstance()

        val emailField = view.findViewById<TextInputEditText>(R.id.email_field)
        val passwordField = view.findViewById<TextInputEditText>(R.id.password_field)
        val primaryButton = view.findViewById<Button>(R.id.primary_button)
        val toggleText = view.findViewById<TextView>(R.id.toggle_text)
        val headerText = view.findViewById<TextView>(R.id.header_text)
        val forgotPasswordText = view.findViewById<TextView>(R.id.forgot_password_text)
        progressBar = view.findViewById(R.id.progress_bar)
        val googleSignInButton = view.findViewById<SignInButton>(R.id.google_sign_in_button)

        primaryButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                if (isLoginMode) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            progressBar.visibility = View.GONE
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(activity, TopicSelectionActivity::class.java))
                                activity?.finish()
                                dismiss()
                            } else {
                                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            progressBar.visibility = View.GONE
                            if (task.isSuccessful) {
                                val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                                if (isNewUser) {
                                    startActivity(Intent(activity, UserDetailsActivity::class.java))
                                } else {
                                    startActivity(Intent(activity, TopicSelectionActivity::class.java))
                                }
                                activity?.finish()
                                dismiss()
                            } else {
                                Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        toggleText.setOnClickListener {
            isLoginMode = !isLoginMode
            if (isLoginMode) {
                headerText.text = "Welcome Back"
                primaryButton.text = "Log In"
                toggleText.text = "Don't have an account? Sign Up"
                forgotPasswordText.visibility = View.VISIBLE

            } else {
                headerText.text = "Create an Account"
                primaryButton.text = "Sign Up"
                toggleText.text = "Already have an account? Log In"
                forgotPasswordText.visibility = View.GONE
            }
        }

        forgotPasswordText.setOnClickListener {
            val email = emailField.text.toString()
            if (email.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter your email to reset password.", Toast.LENGTH_SHORT).show()
            }
        }

        googleSignInButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val credentialManager = CredentialManager.create(requireContext())
                    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .build()

                    val request: GetCredentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(requireActivity(), request)
                    val credential = result.credential
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val googleIdToken = googleIdTokenCredential.idToken
                        withContext(Dispatchers.Main) {
                            firebaseAuthWithGoogle(googleIdToken)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            val error = "Unexpected credential type: ${credential.type}"
                            Log.e("LoginFragment", error)
                            Toast.makeText(context, "Google Sign-In failed. $error", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Log.e("LoginFragment", "Google Sign-in failed with exception", e)
                        Toast.makeText(context, "Google Sign-in failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        startActivity(Intent(activity, UserDetailsActivity::class.java))
                    } else {
                        startActivity(Intent(activity, TopicSelectionActivity::class.java))
                    }
                    activity?.finish()
                    dismiss()
                } else {
                    Log.e("LoginFragment", "FirebaseAuth with Google failed.", task.exception)
                    Toast.makeText(context, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
