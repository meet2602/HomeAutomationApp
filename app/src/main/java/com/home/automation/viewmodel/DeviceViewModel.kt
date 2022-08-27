package com.home.automation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.home.automation.models.DeviceModel
import com.home.automation.models.DevicesModel
import com.home.automation.repository.DeviceRepository
import com.home.automation.utils.Resource

class DeviceViewModel : ViewModel() {
    private val repository: DeviceRepository = DeviceRepository()

    fun getDeviceList(userId: String): MutableLiveData<Resource<List<DevicesModel>>> {
        return repository.getDeviceList(userId)
    }

    fun getAllDeviceList(userId: String): MutableLiveData<Resource<DeviceModel>> {
        return repository.getAllDeviceList(userId)
    }

    fun addDevice(
        userId: String,
        devicesModel: DevicesModel,
        deviceId: String,
    ): MutableLiveData<Resource<Any>> {
        return repository.addDevice(userId, devicesModel, deviceId)
    }

    fun addAllDevice(
        userId: String,
        body: HashMap<String, Any>,
        deviceId: String,
    ): MutableLiveData<Resource<Any>> {
        return repository.addAllDevice(userId, body, deviceId)
    }

    fun updateDevice(
        userId: String,
        deviceId: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        return repository.updateDevice(userId, deviceId, body)
    }

    fun bulbStatusUpdate(
        userId: String,
        state: Boolean,
        deviceType: String,
        devicePos: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        return repository.bulbStatusUpdate(userId, state, deviceType, devicePos, body)
    }

}