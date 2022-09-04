package com.home.automation.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.home.automation.models.DeviceModel
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

    fun getAllDeviceList(userId: String): MutableLiveData<Resource<DeviceModel>> {
        val mutableLiveData: MutableLiveData<Resource<DeviceModel>> = MutableLiveData()
        mutableLiveData.postValue(Loading())
        deviceRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("suc", dataSnapshot.value.toString())
                val responseModel = dataSnapshot.getValue(DeviceModel::class.java)
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

    fun bulbStatusUpdate(
        userId: String,
        state: Boolean,
        deviceType: String,
        devicePos: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Loading())
        val updateDeviceRef = if (state) {
            deviceRef.child(userId).child("$deviceType/$devicePos")
        } else {
            deviceRef.child(userId).child(deviceType)
        }
        updateDeviceRef.updateChildren(body)
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

    fun addAllDevice(
        userId: String,
        body: HashMap<String, Any>,
        deviceId: String,
    ): MutableLiveData<Resource<Any>> {
        val mutableLiveData: MutableLiveData<Resource<Any>> = MutableLiveData()
        mutableLiveData.postValue(Loading())

        deviceRef.child(userId).child(deviceId).setValue(body).addOnCompleteListener { task ->
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