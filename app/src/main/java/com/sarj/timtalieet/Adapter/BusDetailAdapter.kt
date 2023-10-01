package com.sarj.timtalieet.Adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sarj.timtalieet.Activity.BusDetailActivity
import com.sarj.timtalieet.Class.BusDetailInfo
import com.sarj.timtalieet.Class.BusInfo
import com.sarj.timtalieet.R

class BusDetailAdapter(private var busStops: List<BusInfo>) : RecyclerView.Adapter<BusDetailAdapter.BusStopViewHolder>() {

    fun updateBusStops(newBusStops: List<BusInfo>) {
        busStops = newBusStops
        notifyDataSetChanged() // Yeni verilerle güncelleme
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusStopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bus_detail_item, parent, false)
        return BusStopViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusStopViewHolder, position: Int) {
        val busStop = busStops[position]
        holder.bind(busStop)
        holder.button.setOnClickListener {
            val i = Intent(holder.itemView.context, BusDetailActivity::class.java)
            holder.itemView.context.startActivity(i)
        }
    }


    override fun getItemCount(): Int {
        return busStops.size
    }

    class BusStopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind the BusStop data to the item layout
        fun bind(busInfo: BusInfo) {
            itemView.findViewById<TextView>(R.id.kapi_no).text = "Otobüs Plakası :  ${busInfo.K_ARAC_PLAKA}"
            itemView.findViewById<TextView>(R.id.guzergah).text = "Otobüs Hızı : ${busInfo.H_OTOBUSKONUM_HIZ}"
            itemView.findViewById<TextView>(R.id.dak).text = "Kapı Numarası : ${busInfo.K_ARAC_KAPINUMARASI}"
            // Add other views and bind data accordingly
        }
        val button = itemView.findViewById<Button>(R.id.otobusGitButton)
    }

}