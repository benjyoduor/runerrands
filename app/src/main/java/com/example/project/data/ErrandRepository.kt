package com.example.project.data

import com.example.project.models.Errand
import com.example.project.models.ErrandCategory
import com.example.project.models.ErrandStatus
import com.example.project.models.User
import com.example.project.models.UserRole
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object ErrandRepository {
    private val database = FirebaseDatabase.getInstance().getReference("errands")
    private val activityLog = FirebaseDatabase.getInstance().getReference("activities")
    private val firestore = FirebaseFirestore.getInstance()

    private val _errands = MutableStateFlow<List<Errand>>(emptyList())
    val errands: StateFlow<List<Errand>> = _errands.asStateFlow()

    init {
        listenForErrands()
    }

    private fun listenForErrands() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Errand>()
                snapshot.children.forEach { child ->
                    val errand = child.getValue(Errand::class.java)
                    if (errand != null) {
                        list.add(errand)
                    }
                }
                _errands.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun postErrand(title: String, description: String, budget: Double, location: String, category: ErrandCategory) {
        val user = AuthRepository.currentUser.value ?: return
        val newErrand = Errand(
            title = title,
            description = description,
            budget = budget,
            location = location,
            category = category,
            requestorId = user.id
        )
        database.child(newErrand.id).setValue(newErrand)
        
        logActivity(user.id, "POSTED_ERRAND", "User posted a new errand: $title", newErrand.id)
    }

    fun updateErrand(errandId: String, title: String, description: String, budget: Double, location: String, category: ErrandCategory) {
        val user = AuthRepository.currentUser.value ?: return
        val updates = mapOf(
            "title" to title,
            "description" to description,
            "budget" to budget,
            "location" to location,
            "category" to category
        )
        database.child(errandId).updateChildren(updates)
        logActivity(user.id, "UPDATED_ERRAND", "User updated errand: $title", errandId)
    }

    fun deleteErrand(errandId: String) {
        val user = AuthRepository.currentUser.value ?: return
        database.child(errandId).removeValue()
        logActivity(user.id, "DELETED_ERRAND", "User deleted an errand", errandId)
    }

    fun applyForErrand(errandId: String) {
        val user = AuthRepository.currentUser.value ?: return
        if (user.role != UserRole.RUNNER) return

        val errand = _errands.value.find { it.id == errandId } ?: return
        if (!errand.applicants.contains(user.id)) {
            val updatedApplicants = errand.applicants + user.id
            database.child(errandId).child("applicants").setValue(updatedApplicants)
            
            logActivity(user.id, "APPLIED_FOR_ERRAND", "User applied for errand: ${errand.title}", errandId)
        }
    }

    fun hireRunner(errandId: String, runnerId: String) {
        val user = AuthRepository.currentUser.value ?: return
        val errand = _errands.value.find { it.id == errandId } ?: return
        
        val updates = mapOf(
            "status" to ErrandStatus.IN_PROGRESS,
            "runnerId" to runnerId
        )
        database.child(errandId).updateChildren(updates)
        
        logActivity(user.id, "HIRED_RUNNER", "Requestor hired a runner for errand: ${errand.title}", errandId)
        logActivity(runnerId, "GOT_HIRED", "Runner was hired for errand: ${errand.title}", errandId)
    }

    fun updateErrandStatus(errandId: String, status: ErrandStatus) {
        val user = AuthRepository.currentUser.value ?: return
        val errand = _errands.value.find { it.id == errandId } ?: return
        
        database.child(errandId).child("status").setValue(status)
        
        logActivity(user.id, "STATUS_UPDATE", "Errand '${errand.title}' status changed to ${status.name}", errandId)
        
        if (status == ErrandStatus.COMPLETED && errand.runnerId != null) {
            logActivity(errand.runnerId, "COMPLETED_TASK", "Task '${errand.title}' marked as completed", errandId)
        }
    }

    suspend fun completeAndRateErrand(errandId: String, rating: Int, review: String) {
        val user = AuthRepository.currentUser.value ?: return
        val errand = _errands.value.find { it.id == errandId } ?: return
        val runnerId = errand.runnerId ?: return

        val updates = mapOf(
            "status" to ErrandStatus.COMPLETED,
            "runnerRating" to rating,
            "runnerReview" to review
        )
        database.child(errandId).updateChildren(updates).await()

        // Update Runner's average rating in Firestore
        val runnerDoc = firestore.collection("users").document(runnerId).get().await()
        val runner = runnerDoc.toObject(User::class.java)
        if (runner != null) {
            val newTotalRatings = runner.totalRatings + 1
            val newAverageRating = ((runner.rating * runner.totalRatings) + rating) / newTotalRatings
            firestore.collection("users").document(runnerId).update(
                mapOf(
                    "rating" to newAverageRating,
                    "totalRatings" to newTotalRatings
                )
            ).await()
        }

        logActivity(user.id, "COMPLETED_AND_RATED", "Errand completed and runner rated $rating stars", errandId)
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            firestore.collection("users").document(userId).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun logActivity(userId: String, action: String, details: String, errandId: String? = null) {
        val timestamp = System.currentTimeMillis()
        val logEntry = mutableMapOf<String, Any>(
            "userId" to userId,
            "action" to action,
            "details" to details,
            "timestamp" to timestamp
        )
        errandId?.let { logEntry["errandId"] = it }
        activityLog.push().setValue(logEntry)
    }
}
