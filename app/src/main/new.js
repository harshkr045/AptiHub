import { initializeApp } from "https://www.gstatic.com/firebasejs/9.15.0/firebase-app.js";import { getAuth, GoogleAuthProvider, signInWithPopup, createUserWithEmailAndPassword, signInWithEmailAndPassword, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/9.15.0/firebase-auth.js";

// --- PASTE YOUR FIREBASE CONFIGURATION HERE ---
const firebaseConfig = { /* ... YOUR CONFIG ... */ };

// --- INITIALIZE FIREBASE AND AUTH ---
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();

// --- GET HTML ELEMENTS ---
const loginForm = document.getElementById('login-form');
const signupForm = document.getElementById('signup-form');
const googleSigninBtn = document.getElementById('google-signin-btn');

// --- REDIRECT IF ALREADY LOGGED IN ---
onAuthStateChanged(auth, user => {
    if (user) {
        console.log("User already logged in, redirecting...");
        window.location.href = '/2-quiz.html';
    }
});

// --- EVENT LISTENERS ---
// Email/Password Login
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    try {
        await signInWithEmailAndPassword(auth, email, password);
    } catch (error) {
        alert(error.message);
    }
});

// Email/Password Signup
signupForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('signup-email').value;
    const password = document.getElementById('signup-password').value;
    try {
        await createUserWithEmailAndPassword(auth, email, password);
    } catch (error) {
        alert(error.message);
    }
});

// Google Sign-In
googleSigninBtn.addEventListener('click', async () => {
    try {
        await signInWithPopup(auth, provider);
    } catch (error) {
        alert(error.message);
    }
});