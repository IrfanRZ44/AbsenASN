package id.exomatik.absenasn.ui.main.admin.belumAbsen

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.onClickFoto
import kotlinx.android.synthetic.main.item_belum_absen.view.*

class AdapterBelumAbsen(
    private val listAfiliasi: ArrayList<ModelUser>,
    private val activity: Activity?,
    private val onClik: (ModelUser) -> Unit
) : RecyclerView.Adapter<AdapterBelumAbsen.AfiliasiHolder>() {

    inner class AfiliasiHolder(private val itemV: View) :
        RecyclerView.ViewHolder(itemV) {
        @SuppressLint("SetTextI18n")
        fun bindAfiliasi(itemData: ModelUser) {
            itemV.textNama.text = itemData.nama
            itemV.textJabatan.text = "${itemData.jabatan}/${itemData.unit_kerja}"
            itemV.textTglLahir.text = "${itemData.tempatLahir}, ${itemData.tanggalLahir}"
            itemV.textJenisKelamin.text = itemData.jk
            itemV.textAlamat.text = itemData.alamat

            itemV.imgFoto.load(itemData.fotoProfil) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_camera_white)
                error(R.drawable.ic_camera_white)
                fallback(R.drawable.ic_camera_white)
                memoryCachePolicy(CachePolicy.ENABLED)
            }

            itemV.imgFoto.setOnClickListener {
                activity?.let { it1 -> onClickFoto(itemData.fotoProfil, it1) }
            }

            itemV.btnKonfirmasi.setOnClickListener {
                onClik(itemData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AfiliasiHolder {
        return AfiliasiHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_belum_absen,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = listAfiliasi.size
    override fun onBindViewHolder(holder: AfiliasiHolder, position: Int) {
        holder.bindAfiliasi(listAfiliasi[position])
    }
}
