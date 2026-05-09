package com.example.project.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.AuthRepository
import com.example.project.data.ErrandRepository
import com.example.project.models.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.example.project.models.ErrandStatus
import com.example.project.models.ErrandCategory
import com.example.project.models.User
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ErrandViewModel : ViewModel() {
    private val authRepository = AuthRepository
    private val errandRepository = ErrandRepository
    
    val errands = errandRepository.errands
    val currentUser = authRepository.currentUser

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val firestore = FirebaseFirestore.getInstance()

    init {
        authRepository.checkAuthState()
    }

    fun signup(name: String, email: String, password: String, role: UserRole, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signup(name, email, password, role)
            _isLoading.value = false
            if (result.isSuccess) {
                onResult(true)
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Signup failed"
                onResult(false)
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.login(email, password)
            _isLoading.value = false
            if (result.isSuccess) {
                onResult(true)
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Login failed"
                onResult(false)
            }
        }
    }

    fun updateProfile(name: String, bio: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.updateProfile(name, bio)
            _isLoading.value = false
            onResult(result.isSuccess)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.deleteAccount()
            _isLoading.value = false
            onResult(result.isSuccess)
        }
    }

    fun postErrand(title: String, description: String, budget: Double, location: String, category: ErrandCategory) {
        errandRepository.postErrand(title, description, budget, location, category)
    }

    fun updateErrand(errandId: String, title: String, description: String, budget: Double, location: String, category: ErrandCategory) {
        errandRepository.updateErrand(errandId, title, description, budget, location, category)
    }

    fun deleteErrand(errandId: String) {
        errandRepository.deleteErrand(errandId)
    }

    fun applyForErrand(errandId: String) {
        errandRepository.applyForErrand(errandId)
    }
    
    fun hireRunner(errandId: String, runnerId: String) {
        errandRepository.hireRunner(errandId, runnerId)
    }
    
    fun updateErrandStatus(errandId: String, status: ErrandStatus) {
        errandRepository.updateErrandStatus(errandId, status)
    }

    fun completeAndRateErrand(errandId: String, rating: Int, review: String) {
        viewModelScope.launch {
            _isLoading.value = true
            errandRepository.completeAndRateErrand(errandId, rating, review)
            _isLoading.value = false
        }
    }
    
    fun fetchUser(uid: String, onResult: (User?) -> Unit) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { onResult(it.toObject(User::class.java)) }
    }

    suspend fun getUserById(id: String) = errandRepository.getUserById(id)
}
