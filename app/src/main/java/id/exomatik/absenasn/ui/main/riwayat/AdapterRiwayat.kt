package id.exomatik.absenasn.ui.main.riwayat

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.FirebaseUtils
import id.exomatik.absenasn.utils.onClickFoto
import kotlinx.android.synthetic.main.item_sudah_absen.view.*

class AdapterRiwayat(
    private val listAfiliasi: ArrayList<ModelAbsensi>,
    private val onClik: (ModelAbsensi, ModelUser?) -> Unit,
    private val activity: Activity?
) : RecyclerView.Adapter<AdapterRiwayat.AfiliasiHolder>() {

    inner class AfiliasiHolder(private val itemV: View) :
        RecyclerView.ViewHolder(itemV) {
        @SuppressLint("SetTextI18n")
        fun bindAfiliasi(itemData: ModelAbsensi) {
            getDataUser(itemData, itemV.textNama, itemV.btnKonfirmasi)
            itemV.textTanggal.text = "${itemData.jam} / ${itemData.tanggalKerja}"
            itemV.textJenis.text = "Absen : ${itemData.jenis}"

            if (itemData.jenis == Constant.absenAlpa || itemData.jenis == Constant.absenIzin ||
                itemData.jenis == Constant.absenCuti || itemData.jenis == Constant.absenSakit){
                itemV.btnKonfirmasi.visibility = View.GONE
            }
            else{
                itemV.btnKonfirmasi.visibility = View.VISIBLE
            }

            when (itemData.status) {
                Constant.statusActive -> {
                    itemV.textStatus.text = "Status : Dikonfirmasi"
                    itemV.textStatus.setTextColor(Color.GREEN)
                }
                Constant.statusRejected -> {
                    itemV.textStatus.text = "Status : Ditolak"
                    itemV.textStatus.setTextColor(Color.RED)
                }
                Constant.statusRequest -> {
                    itemV.textStatus.text = "Status : Belum dikonfirmasi"
                    itemV.textStatus.setTextColor(Color.YELLOW)
                }
                else -> {
                    itemV.btnKonfirmasi.visibility = View.GONE
                    itemV.textStatus.text = "Status : -"
                    itemV.textStatus.setTextColor(Color.BLACK)
                }
            }
            itemV.imgFoto.load(itemData.foto_absensi) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_camera_white)
                error(R.drawable.ic_camera_white)
                fallback(R.drawable.ic_camera_white)
                memoryCachePolicy(CachePolicy.ENABLED)
            }

            itemV.imgFoto.setOnClickListener {
                activity?.let { it1 -> onClickFoto(itemData.foto_absensi, it1) }
            }
        }
    }

    private fun getDataUser(itemData: ModelAbsensi, textNama: AppCompatTextView, btnKonfirmasi: AppCompatButton){
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                textNama.text = itemData.username_user
                btnKonfirmasi.setOnClickListener {
                    onClik(itemData, null)
                }
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    val data = result.getValue(ModelUser::class.java)

                    if (data != null){
                        textNama.text = data.nama
                        btnKonfirmasi.setOnClickListener {
                            onClik(itemData, data)
                        }
                    }
                    else{
                        textNama.text = itemData.username_user
                        btnKonfirmasi.setOnClickListener {
                            onClik(itemData, null)
                        }
                    }
                }
                else{
                    textNama.text = itemData.username_user
                    btnKonfirmasi.setOnClickListener {
                        onClik(itemData, null)
                    }
                }
            }
        }

        FirebaseUtils.getData1Child(
            Constant.reffUser, itemData.username_user, valueEventListener
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AfiliasiHolder {
        return AfiliasiHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_sudah_absen,
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
