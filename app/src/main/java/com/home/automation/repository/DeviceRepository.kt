package com.home.automation.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.home.automation.models.ComponentModel
import com.home.automation.models.DevicesModel
import com.home.automation.utils.Resource
import com.home.automation.utils.Resource.*


class DeviceRepository {
    private val rootRef = FirebaseDatabase.getInstance().reference
    private val deviceRef =
        rootRef.child("Device")

    fun getDeviceList(userId: String): MutableLiveData<Resource<List<DevicesModel>>> {
        val mutableLiveData: MutableLiveData<Resource<List<DevicesModel>>> = MutableLiveData()
        mutableLiveData.postValue(Loading())
        deviceRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val responseModel = dataSnapshot.children.map { snapShot ->
//                    snapShot.getValue(DeviceModel::class.java)!!
//                }
                Log.d("meetData", dataSnapshot.children.map {
                    it
                }.toString())
                val devicesModel = listOf<DevicesModel>()
                mutableLiveData.postValue(Success(devicesModel))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                mutableLiveData.postValue(
                    Error(
                        databaseError.toException().message.toString(),
                        null
                    )
                )
            }
        })
        return mutableLiveData
    }

    fun addDevice(
        userId: String,
        devicesModel: DevicesModel,
        deviceId: String,
    ): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Loading())

        deviceRef.child(userId).child(deviceId).setValue(devicesModel)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    mutableLiveData.postValue(Success(result))
                } else {
                    mutableLiveData.postValue(Error(task.exception!!.message.toString(), null))
                }
            }

        return mutableLiveData
    }


    fun getComponent(userId: String): MutableLiveData<Resource<ComponentModel>> {
        val rootRef = FirebaseDatabase.getInstance().reference
        val deviceRef =
            rootRef.child("Components")
        val mutableLiveData: MutableLiveData<Resource<ComponentModel>> = MutableLiveData()
        mutableLiveData.postValue(Loading())
        deviceRef.child(userId).child("Devices").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("suc", dataSnapshot.value.toString())
                val responseModel = dataSnapshot.getValue(ComponentModel::class.java)
                mutableLiveData.postValue(Success(responseModel))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                mutableLiveData.postValue(
                    Error(
                        databaseError.toException().message.toString(),
                        null
                    )
                )
            }
        })
        return mutableLiveData
    }

    fun addComponents(
        userId: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        val rootRef = FirebaseDatabase.getInstance().reference
        val deviceRef =
            rootRef.child("Components")
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Loading())

        deviceRef.child(userId).child("Devices").setValue(body).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                mutableLiveData.postValue(Success(result))
            } else {
                mutableLiveData.postValue(Error(task.exception!!.message.toString(), null))
            }
        }

        return mutableLiveData
    }

    fun updateComponents(
        userId: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        val rootRef = FirebaseDatabase.getInstance().reference
        val deviceRef =
            rootRef.child("Components")
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Loading())
        deviceRef.child(userId).child("Devices").updateChildren(body)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    mutableLiveData.postValue(Success(result))
                } else {
                    mutableLiveData.postValue(Error(task.exception!!.message.toString(), null))
                }
            }
        return mutableLiveData
    }

    fun updateDevice(
        userId: String,
        deviceId: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Loading())
        deviceRef.child(userId).child(deviceId).updateChildren(body).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                mutableLiveData.postValue(Success(result))
            } else {
                mutableLiveData.postValue(Error(task.exception!!.message.toString(), null))
            }
        }
        return mutableLiveData
    }


}