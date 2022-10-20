package com.home.automation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.home.automation.models.ComponentModel
import com.home.automation.models.DevicesModel
import com.home.automation.repository.DeviceRepository
import com.home.automation.utils.Resource

class DeviceViewModel : ViewModel() {
    private val repository: DeviceRepository = DeviceRepository()

    fun getDeviceList(userId: String): MutableLiveData<Resource<List<DevicesModel>>> {
        return repository.getDeviceList(userId)
    }


    fun addDevice(
        userId: String,
        devicesModel: DevicesModel,
        deviceId: String,
    ): MutableLiveData<Resource<Any>> {
        return repository.addDevice(userId, devicesModel, deviceId)
    }

    fun getComponent(userId: String): MutableLiveData<Resource<ComponentModel>> {
        return repository.getComponent(userId)
    }

    fun addComponents(
        userId: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        return repository.addComponents(userId, body)
    }

    fun updateComponents(
        userId: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        return repository.updateComponents(userId, body)
    }

    fun updateDevice(
        userId: String,
        deviceId: String,
        body: HashMap<String, Any>,
    ): MutableLiveData<Resource<Any>> {
        return repository.updateDevice(userId, deviceId, body)
    }

}