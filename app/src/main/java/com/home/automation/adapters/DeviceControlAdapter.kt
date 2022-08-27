package com.home.automation.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.home.automation.R
import com.home.automation.databinding.ViewDeviceLayoutBinding
import com.home.automation.models.DevicesModel

class DeviceControlAdapter(
    private val deviceList: List<DevicesModel>,
    private val onClickListener: OnClickListener,
) :
    RecyclerView.Adapter<DeviceControlAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DeviceControlAdapter.ViewHolder {
        val rowViewBinding: ViewDeviceLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.view_device_layout,
            parent,
            false
        )
        return ViewHolder(rowViewBinding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(
        holder: DeviceControlAdapter.ViewHolder,
        position: Int,
    ) {
        val device = deviceList[position]
        holder.itemsListBinding.deviceTitle.text = device.deviceName
        holder.itemsListBinding.switch1.isChecked = device.deviceStatus
        if (device.deviceStatus) {
            holder.itemsListBinding.bluetoothImg.setImageResource(R.drawable.ic_power_on)
        } else {
            holder.itemsListBinding.bluetoothImg.setImageResource(R.drawable.ic_power_off)
        }

        holder.itemsListBinding.switch1.setOnClickListener {
            onClickListener.onClick(device, position)
        }
        holder.itemView.setOnClickListener {
            onClickListener.onClick(device, position)
        }
        holder.itemView.setOnLongClickListener {
            onClickListener.onLongClick(device)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }


    inner class ViewHolder(itemViewBinding: ViewDeviceLayoutBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val itemsListBinding = itemViewBinding

    }

    interface OnClickListener {
        fun onClick(device: DevicesModel, position: Int)
        fun onLongClick(device: DevicesModel)
    }
}