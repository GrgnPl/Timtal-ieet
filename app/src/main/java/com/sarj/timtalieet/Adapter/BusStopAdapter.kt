package com.sarj.timtalieet.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sarj.timtalieet.Activity.BusInfoActivity
import com.sarj.timtalieet.Class.BusStop
import com.sarj.timtalieet.R

class BusStopAdapter(private var busStops: List<BusStop>) : RecyclerView.Adapter<BusStopAdapter.BusStopViewHolder>() {

    fun updateBusStops(newBusStops: List<BusStop>) {
        busStops = newBusStops
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusStopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bus_info_item, parent, false)
        return BusStopViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusStopViewHolder, position: Int) {
        val busStop = busStops[position]
        holder.bind(busStop)
        holder.button.setOnClickListener {
            val i = Intent(holder.itemView.context, BusInfoActivity::class.java)
            i.putExtra("deger",busStop.DURAK_DURAK_KODU)
            Log.e("den",busStop.DURAK_DURAK_KODU.toString())
            holder.itemView.context.startActivity(i)
        }

    }

    override fun getItemCount(): Int {
        return busStops.size
    }

    class BusStopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind the BusStop data to the item layout
        fun bind(busStop: BusStop) {
            itemView.findViewById<TextView>(R.id.kapi_no).text = "Durak Adı : ${busStop.DURAK_ADI}"
            itemView.findViewById<TextView>(R.id.guzergah).text = "Durak Kodu : ${busStop.DURAK_DURAK_KODU.toString()}"
            itemView.findViewById<TextView>(R.id.dak).text = "Yönü : ${busStop.DURAK_YON_BILGISI}"
            // Add other views and bind data accordingly
        }
        val button = itemView.findViewById<Button>(R.id.otobusGitButton)
    }

}