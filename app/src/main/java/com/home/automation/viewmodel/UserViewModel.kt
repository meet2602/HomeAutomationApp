package com.home.automation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.home.automation.models.UserModel
import com.home.automation.repository.UserRepository
import com.home.automation.utils.Resource
import com.home.automation.utils.SessionManager

class UserViewModel : ViewModel() {
    private val repository: UserRepository = UserRepository()

    fun signIn(email: String, password: String): MutableLiveData<Resource<Any>> {
        return repository.signIn(email, password)
    }

    fun createAccount(email: String, password: String): MutableLiveData<Resource<Any>> {
        return repository.createAccount(email, password)
    }

    fun registerUser(userId: String, userModel: UserModel): MutableLiveData<Resource<Any>> {
        return repository.registerUser(userId, userModel)
    }

    fun getUserDetail(userId: String): MutableLiveData<Resource<UserModel>> {
        return repository.getUserDetail(userId)
    }

    fun splashGetUserDetail(userId: String): MutableLiveData<Resource<UserModel>> {
        return repository.splashGetUserDetail(userId)
    }

    fun updateUser(userId: String, body: HashMap<String, Any>): MutableLiveData<Resource<Any>> {
        return repository.updateUser(userId, body)
    }

    fun signOut(sessionManager: SessionManager): MutableLiveData<Resource<Any>> {
        return repository.signOut(sessionManager)
    }


}