package com.home.automation.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.home.automation.R
import com.home.automation.databinding.BluetoothDeviceItemLayoutBinding

class BluetoothDeviceListAdapter(
    private val deviceList: ArrayList<BluetoothDevice>,
    private val onClickListener: OnClickListener,
) :
    RecyclerView.Adapter<BluetoothDeviceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BluetoothDeviceListAdapter.ViewHolder {
        val rowViewBinding: BluetoothDeviceItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.bluetooth_device_item_layout,
            parent,
            false
        )
        return ViewHolder(rowViewBinding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(
        holder: BluetoothDeviceListAdapter.ViewHolder,
        position: Int,
    ) {
        val device = deviceList[position]
        holder.itemsListBinding.deviceTitle.text = deviceList[position].name
        holder.itemsListBinding.deviceAddress.text = deviceList[position].address
        holder.itemsListBinding.bluetoothImg.setImageResource(R.drawable.ic_bluetooth_24)
        holder.itemsListBinding.inFoImg.setImageResource(R.drawable.ic_outline_info_24)

        holder.itemView.setOnClickListener {
            onClickListener.onConnectClick(position, device)
        }
        holder.itemsListBinding.inFoImg.setOnClickListener {
            onClickListener.onUnPairClick(position, device, notifyItemRemoved(position))

        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }


    inner class ViewHolder(itemViewBinding: BluetoothDeviceItemLayoutBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val itemsListBinding = itemViewBinding

    }

    interface OnClickListener {
        fun onConnectClick(position: Int, device: BluetoothDevice)
        fun onUnPairClick(position: Int, device: BluetoothDevice, notifyItemRemoved: Unit)
    }

}
