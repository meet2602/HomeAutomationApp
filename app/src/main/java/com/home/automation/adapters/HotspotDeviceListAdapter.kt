package com.home.automation.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.home.automation.R
import com.home.automation.databinding.WifiDeviceItemLayoutBinding
import com.home.automation.models.HotspotModel

class HotspotDeviceListAdapter(
    private val deviceList: ArrayList<HotspotModel>,
) :
    RecyclerView.Adapter<HotspotDeviceListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HotspotDeviceListAdapter.ViewHolder {
        val rowViewBinding: WifiDeviceItemLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.wifi_device_item_layout,
            parent,
            false
        )
        return ViewHolder(rowViewBinding)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(
        holder: HotspotDeviceListAdapter.ViewHolder,
        position: Int,
    ) {
        val device = deviceList[position]
        holder.itemsListBinding.deviceTitle.text = device.ipAddress
        holder.itemsListBinding.deviceAddress.text = device.hwAddress
        holder.itemsListBinding.bluetoothImg.setImageResource(R.drawable.ic_baseline_wifi_24)
        holder.itemsListBinding.inFoImg.setImageResource(R.drawable.ic_outline_info_24)

        holder.itemView.setOnClickListener {
        }
        holder.itemsListBinding.inFoImg.setOnClickListener {
//            onClickListener.onUnPairClick(position, device,notifyItemRemoved(position))
        }
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }


    inner class ViewHolder(itemViewBinding: WifiDeviceItemLayoutBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        val itemsListBinding = itemViewBinding

    }
}