package com.exomatik.ballighadmin.ui.main.requestLD

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.request.CachePolicy
import com.exomatik.ballighadmin.R
import com.exomatik.ballighadmin.model.ModelDataLembaga
import kotlinx.android.synthetic.main.item_cari_afiliasi.view.*

class AdapterRequestLD (private val listAfiliasi : ArrayList<ModelDataLembaga?>,
                        private val onClik : (ModelDataLembaga) -> Unit,
                        private val context: Context?,
                        private val requestAccept: (ModelDataLembaga) -> Unit,
                        private val requestReject: (ModelDataLembaga) -> Unit
) : RecyclerView.Adapter<AdapterRequestLD.AfiliasiHolder>(){

    inner class AfiliasiHolder(private val itemAfiliasi : View) : RecyclerView.ViewHolder(itemAfiliasi){
        @SuppressLint("SetTextI18n")
        @Suppress("DEPRECATION")
        fun bindAfiliasi(data: ModelDataLembaga,
                         onClik: (ModelDataLembaga) -> Unit,
                         requestAccept: (ModelDataLembaga) -> Unit,
                         requestReject: (ModelDataLembaga) -> Unit){
            itemAfiliasi.namaLd.text = data.namaPanjangLembaga
            itemAfiliasi.kabLd.text = data.kotaLembaga
            itemAfiliasi.provLd.text = data.provinsiLembaga
            itemAfiliasi.imgLd.setBackgroundResource(android.R.color.transparent)

            itemAfiliasi.imgLd.load(data.fotoLembaga) {
                crossfade(true)
                placeholder(R.drawable.ic_camera_white)
                error(R.drawable.ic_camera_white)
                fallback(R.drawable.ic_camera_white)
                memoryCachePolicy(CachePolicy.ENABLED)
            }

            itemAfiliasi.setOnClickListener {
                onClik(data)
            }

            itemAfiliasi.statusReqLd.visibility = View.VISIBLE
            itemAfiliasi.statusTolak.visibility = View.VISIBLE
            context?.resources?.getColor(R.color.black)?.let {
                itemAfiliasi.statusTolak.setTextColor(
                    it
                )
                itemAfiliasi.statusReqLd.setTextColor(
                    it
                )
            }
            itemAfiliasi.statusReqLd.text = "Terima"
            itemAfiliasi.statusTolak.text = "Tolak"

            itemAfiliasi.statusReqLd.setOnClickListener {
                requestAccept(data)
            }

            itemAfiliasi.statusTolak.setOnClickListener {
                requestReject(data)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AfiliasiHolder {
        return AfiliasiHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_cari_afiliasi, parent, false))
    }
    override fun getItemCount(): Int = listAfiliasi.size
    override fun onBindViewHolder(holder: AfiliasiHolder, position: Int) {
        listAfiliasi[position]?.let { holder.bindAfiliasi(it, onClik, requestAccept, requestReject) }
    }
}