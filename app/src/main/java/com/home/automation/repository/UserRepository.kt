package com.home.automation.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.home.automation.models.UserModel
import com.home.automation.utils.Resource
import com.home.automation.utils.SessionManager

class UserRepository {
    private val auth = Firebase.auth
    private val rootRef = FirebaseDatabase.getInstance().reference
    private val userRef =
        rootRef.child("User")

    fun signIn(email: String, password: String): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Resource.Loading())
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    mutableLiveData.postValue(Resource.Success(result))
                } else {
                    mutableLiveData.postValue(
                        Resource.Error(
                            task.exception!!.message.toString(),
                            null
                        )
                    )
                }
            }
        return mutableLiveData
    }

    fun createAccount(email: String, password: String): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Resource.Loading())
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    mutableLiveData.postValue(Resource.Success(result))
                } else {
                    mutableLiveData.postValue(
                        Resource.Error(
                            task.exception!!.message.toString(),
                            null
                        )
                    )
                }
            }
        return mutableLiveData
    }

    fun registerUser(userId: String, userModel: UserModel): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Resource.Loading())

        userRef.child(userId).setValue(userModel).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                mutableLiveData.postValue(Resource.Success(result))
            } else {
                mutableLiveData.postValue(Resource.Error(task.exception!!.message.toString(), null))
            }
        }

        return mutableLiveData
    }

    fun getUserDetail(userId: String): MutableLiveData<Resource<UserModel>> {
        val mutableLiveData: MutableLiveData<Resource<UserModel>> = MutableLiveData()
        mutableLiveData.postValue(Resource.Loading())
        userRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val responseModel = dataSnapshot.getValue(UserModel::class.java)!!
                mutableLiveData.postValue(Resource.Success(responseModel))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                mutableLiveData.postValue(
                    Resource.Error(
                        databaseError.toException().message.toString(),
                        null
                    )
                )
            }
        })
        return mutableLiveData
    }

    fun splashGetUserDetail(userId: String): MutableLiveData<Resource<UserModel>> {
        val mutableLiveData: MutableLiveData<Resource<UserModel>> = MutableLiveData()
        mutableLiveData.postValue(Resource.Loading())

        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val responseModel = dataSnapshot.getValue(UserModel::class.java)!!
                mutableLiveData.postValue(Resource.Success(responseModel))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                mutableLiveData.postValue(
                    Resource.Error(
                        databaseError.toException().message.toString(),
                        null
                    )
                )
            }
        })
        return mutableLiveData
    }

    fun updateUser(userId: String, body: HashMap<String, Any>): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Resource.Loading())
        userRef.child(userId).updateChildren(body).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                mutableLiveData.postValue(Resource.Success(result))
            } else {
                mutableLiveData.postValue(Resource.Error(task.exception!!.message.toString(), null))
            }
        }
        return mutableLiveData
    }

    fun signOut(sessionManager: SessionManager): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Resource.Loading())

        Firebase.auth.signOut()
//        sessionManager.clearData()
        mutableLiveData.postValue(Resource.Success(null))
        return mutableLiveData
    }
}