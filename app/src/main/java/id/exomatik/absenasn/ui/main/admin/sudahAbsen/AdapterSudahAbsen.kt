package id.exomatik.absenasn.ui.main.admin.sudahAbsen

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
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

class AdapterSudahAbsen(
    private val listAfiliasi: ArrayList<ModelAbsensi>,
    private val activity: Activity?,
    private val onClik: (ModelAbsensi, ModelUser?) -> Unit
) : RecyclerView.Adapter<AdapterSudahAbsen.AfiliasiHolder>() {

    inner class AfiliasiHolder(private val itemV: View) :
        RecyclerView.ViewHolder(itemV) {
        @SuppressLint("SetTextI18n")
        fun bindAfiliasi(itemData: ModelAbsensi) {
            getDataUser(itemData, itemV.textNama, itemV.textJabatan, itemV.imgFoto, itemV.btnKonfirmasi)
            itemV.textTanggal.text = "${itemData.jam} / ${itemData.tanggalKerja}"
            itemV.textJenis.text = "Absen : ${itemData.jenis}"

            when (itemData.status) {
                Constant.statusActive -> {
                    itemV.btnKonfirmasi.visibility = View.GONE
                    itemV.textStatus.text = "Status : Dikonfirmasi"
                    itemV.textStatus.setTextColor(Color.GREEN)
                }
                Constant.statusRejected -> {
                    itemV.btnKonfirmasi.visibility = View.GONE
                    itemV.textStatus.text = "Status : Ditolak"
                    itemV.textStatus.setTextColor(Color.RED)
                }
                Constant.statusRequest -> {
                    itemV.btnKonfirmasi.visibility = View.VISIBLE
                    itemV.textStatus.text = "Status : Belum dikonfirmasi"
                    itemV.textStatus.setTextColor(Color.YELLOW)
                }
                else -> {
                    itemV.btnKonfirmasi.visibility = View.GONE
                    itemV.textStatus.text = "Status : -"
                    itemV.textStatus.setTextColor(Color.BLACK)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDataUser(itemData: ModelAbsensi, textNama: AppCompatTextView, textJabatan: AppCompatTextView,
                            imgFoto: AppCompatImageView, btnKonfirmasi: AppCompatButton){
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                textNama.text = itemData.username_user
                textJabatan.text = "-"
                btnKonfirmasi.setOnClickListener {
                    onClik(itemData, null)
                }
            }

            override fun onDataChange(result: DataSnapshot) {
                if (result.exists()) {
                    val data = result.getValue(ModelUser::class.java)

                    if (data != null){
                        textNama.text = data.nama
                        textJabatan.text = "${data.jabatan}/${data.unit_kerja}"

                        imgFoto.load(data.fotoProfil) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                            placeholder(R.drawable.ic_camera_white)
                            error(R.drawable.ic_camera_white)
                            fallback(R.drawable.ic_camera_white)
                            memoryCachePolicy(CachePolicy.ENABLED)
                        }

                        imgFoto.setOnClickListener {
                            activity?.let { it1 -> onClickFoto(data.fotoProfil, it1) }
                        }

                        btnKonfirmasi.setOnClickListener {
                            onClik(itemData, data)
                        }
                    }
                    else{
                        textNama.text = itemData.username_user
                        textJabatan.text = "-"
                        btnKonfirmasi.setOnClickListener {
                            onClik(itemData, null)
                        }
                    }
                }
                else{
                    textNama.text = itemData.username_user
                    textJabatan.text = "-"
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
