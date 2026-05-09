package com.example.project.data

import com.example.project.models.User
import com.example.project.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val db = FirebaseDatabase.getInstance().getReference("activities")

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun signup(name: String, email: String, password: String, role: UserRole): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Signup failed"))
            
            val user = User(
                id = firebaseUser.uid,
                name = name,
                email = email,
                role = role
            )
            
            firestore.collection("users").document(user.id).set(user).await()
            _currentUser.value = user
            
            logActivity(user.id, "ACCOUNT_CREATED", "User signed up as ${role.name}")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed"))
            
            val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = userDoc.toObject(User::class.java)
            
            if (user != null) {
                _currentUser.value = user
                logActivity(user.id, "LOGIN", "User logged in")
                Result.success(Unit)
            } else {
                Result.failure(Exception("User data not found in Firestore"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String, bio: String): Result<Unit> {
        val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(user.id).update(
                mapOf(
                    "name" to name,
                    "bio" to bio
                )
            ).await()
            _currentUser.value = user.copy(name = name, bio = bio)
            logActivity(user.id, "PROFILE_UPDATED", "User updated their name and bio")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _currentUser.value?.let { logActivity(it.id, "LOGOUT", "User logged out") }
        auth.signOut()
        _currentUser.value = null
    }

    suspend fun deleteAccount(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        val userId = user.uid
        return try {
            logActivity(userId, "ACCOUNT_DELETED", "User deleted their account")
            firestore.collection("users").document(userId).delete().await()
            user.delete().await()
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun checkAuthState() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            firestore.collection("users").document(firebaseUser.uid).get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        _currentUser.value = user
                    }
                }
        }
    }

    private fun logActivity(userId: String, action: String, details: String) {
        val timestamp = System.currentTimeMillis()
        val logEntry = mapOf(
            "userId" to userId,
            "action" to action,
            "details" to details,
            "timestamp" to timestamp
        )
        db.push().setValue(logEntry)
    }
}
