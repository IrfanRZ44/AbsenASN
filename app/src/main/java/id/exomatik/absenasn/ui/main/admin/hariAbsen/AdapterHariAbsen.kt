package id.exomatik.absenasn.ui.main.admin.hariAbsen

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.R
import kotlinx.android.synthetic.main.item_hari_kerja.view.*

class AdapterHariAbsen(
    private val listData: ArrayList<ModelHariKerja>,
    private val onClikDelete: (ModelHariKerja, Int) -> Unit,
) : RecyclerView.Adapter<AdapterHariAbsen.AfiliasiHolder>() {

    inner class AfiliasiHolder(private val item: View) :
        RecyclerView.ViewHolder(item) {
        @SuppressLint("SetTextI18n")
        fun bindAfiliasi(
            itemData: ModelHariKerja,
            position: Int
        ) {
            item.textJenisAbsen.text = "Absen ${itemData.jenisAbsen}"
            item.textTanggal.text = "Tanggal : ${itemData.tanggalKerja}"
            item.textWaktu.text = "Waktu : ${itemData.jamMasuk}-${itemData.jamPulang}"

            item.btnDelete.setOnClickListener {
                onClikDelete(itemData, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AfiliasiHolder {
        return AfiliasiHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_hari_kerja,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = listData.size
    override fun onBindViewHolder(holder: AfiliasiHolder, position: Int) {
        holder.bindAfiliasi(listData[position], position)
    }
}
