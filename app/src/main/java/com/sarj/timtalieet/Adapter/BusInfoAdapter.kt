package com.sarj.timtalieet.Adapter

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.sarj.timtalieet.Activity.BusDetailActivity
import com.sarj.timtalieet.Class.BusDetailInfo
import com.sarj.timtalieet.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class BusInfoAdapter(private var busStops: List<BusDetailInfo>) : RecyclerView.Adapter<BusInfoAdapter.BusInfoViewHolder>() {

    fun updateBusStops(newBusStops: List<BusDetailInfo>) {
        busStops = newBusStops
        notifyDataSetChanged() // Yeni verilerle güncelleme
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusInfoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bus_stop_item, parent, false)
        return BusInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusInfoViewHolder, position: Int) {
        val busStop = busStops[position]
        holder.bind(busStop)
        holder.button.setOnClickListener {
            val i = Intent(holder.itemView.context, BusDetailActivity::class.java)
            val yazi = busStop.kapino
            i.putExtra("gonderilecek_deger", yazi)
            holder.itemView.context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return busStops.size
    }

    class BusInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
        private lateinit var geocoder: Geocoder

        fun bind(busInfo: BusDetailInfo) {
            itemView.findViewById<TextView>(R.id.kapi_no).text =
                "Hat Kodu - Kapı No : ${busInfo.hatkodu} ${busInfo.kapino}"
            itemView.findViewById<TextView>(R.id.guzergah).text =
                "Hat Adı : ${busInfo.hatadi.toString()}"
            itemView.findViewById<TextView>(R.id.dak).text = "Dakika : ${busInfo.dakika}"

            val latitude = busInfo.son_konum?.split(",")?.getOrNull(0)?.toDouble() ?: 0.0
            val longitude = busInfo.son_konum?.split(",")?.getOrNull(1)?.toDouble() ?: 0.0

            CoroutineScope(Dispatchers.Main).launch {
                val address = getAddressFromLatLng(itemView.context, longitude, latitude)
                itemView.findViewById<TextView>(R.id.dak2).text = "Son Konum : $address"
            }
        }

        val button = itemView.findViewById<Button>(R.id.otobusGitButton)

        private suspend fun getAddressFromLatLng(
            context: Context,
            latitude: Double,
            longitude: Double
        ): String {
            geocoder = Geocoder(context, Locale.getDefault())

            return try {
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                if (addresses!!.isNotEmpty()) {
                    val address = addresses[0]
                    address.thoroughfare ?: "Unknown Street"
                } else {
                    "Address Not Found"
                }
            } catch (e: Exception) {
                "Address Not Found"
            }
        }
    }
}