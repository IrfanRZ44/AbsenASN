package id.exomatik.absenki.ui.main.beranda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import id.exomatik.absenki.R
import id.exomatik.absenki.model.ModelBarang
import id.exomatik.absenki.utils.Constant.defaultTempFoto
import id.exomatik.absenki.utils.onClickFoto
import kotlinx.android.synthetic.main.item_kontak.view.*

class AdapterDataBeranda(
    private val listAfiliasi: ArrayList<ModelBarang>,
    private val onClik: (ModelBarang) -> Unit,
    private val navController: NavController
) : RecyclerView.Adapter<AdapterDataBeranda.AfiliasiHolder>() {

    inner class AfiliasiHolder(private val item: View) :
        RecyclerView.ViewHolder(item) {
        fun bindAfiliasi(
            itemData: ModelBarang,
            onClik: (ModelBarang) -> Unit) {

            item.textNama.text = itemData.nama
            item.textDesc.text = itemData.jenis
            item.textLokasi.text = itemData.harga.toString()

            item.imgFoto.load(defaultTempFoto) {
                crossfade(true)
                placeholder(R.drawable.ic_camera_white)
                error(R.drawable.ic_camera_white)
                fallback(R.drawable.ic_camera_white)
                memoryCachePolicy(CachePolicy.ENABLED)
            }

            item.imgFoto.setOnClickListener {
                onClickFoto(defaultTempFoto,
                    navController)
            }

            item.setOnClickListener {
                onClik(itemData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AfiliasiHolder {
        return AfiliasiHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_kontak,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = listAfiliasi.size
    override fun onBindViewHolder(holder: AfiliasiHolder, position: Int) {
        holder.bindAfiliasi(listAfiliasi[position], onClik)
    }
}
