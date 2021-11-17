package id.exomatik.absenasn.ui.main.admin.verifyPegawai

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.android.material.textfield.TextInputLayout
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.R
import id.exomatik.absenasn.utils.onClickFoto
import kotlinx.android.synthetic.main.item_verify_pegawai.view.*
import kotlin.collections.ArrayList

class AdapterVerifyPegawai(
    private val listData: ArrayList<ModelUser>,
    private val activity: Activity,
    private val onClikAccept: (ModelUser, Int) -> Unit,
    private val onClikRejected: (ModelUser, Int) -> Unit,
) : RecyclerView.Adapter<AdapterVerifyPegawai.AfiliasiHolder>() {

    inner class AfiliasiHolder(private val v: View) :
        RecyclerView.ViewHolder(v) {
        @SuppressLint("SetTextI18n")
        fun bindAfiliasi(itemData: ModelUser, position: Int) {
            v.textNama.text = itemData.nama
            v.textHp.text = "No Hp : ${itemData.phone}"
            v.textAlamat.text = "Alamat : ${itemData.alamat}"
            v.textJabatan.text = "NIP/NIDN/ID : ${itemData.nip}"

            v.imgFoto.load(itemData.fotoProfil) {
                crossfade(true)
                placeholder(R.drawable.ic_camera_white)
                error(R.drawable.ic_camera_white)
                fallback(R.drawable.ic_camera_white)
                memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            }

            v.imgFoto.setOnClickListener {
                onClickFoto(itemData.fotoProfil, activity)
            }

            v.setOnClickListener {
                showDialogDetail(itemData)
            }

            v.btnAccept.setOnClickListener {
                onClikAccept(itemData, position)
            }

            v.btnReject.setOnClickListener {
                onClikRejected(itemData, position)
            }
        }
    }

    private fun showDialogDetail(data: ModelUser){
        val alert = AlertDialog.Builder(activity).create()
        val inflater = LayoutInflater.from(activity)
        val dialogView = inflater.inflate(R.layout.dialog_detail_pegawai, null)
        alert.setView(dialogView)

        val imgFotoProfil = dialogView.findViewById<AppCompatImageView>(R.id.imgFotoProfil)
        val etNama = dialogView.findViewById<TextInputLayout>(R.id.etNama)
        val etNip = dialogView.findViewById<TextInputLayout>(R.id.etNip)
        val etJenisKelamin = dialogView.findViewById<TextInputLayout>(R.id.etJenisKelamin)
        val etAlamat = dialogView.findViewById<TextInputLayout>(R.id.etAlamat)
        val etPangkat = dialogView.findViewById<TextInputLayout>(R.id.etPangkat)
        val etJabatan = dialogView.findViewById<TextInputLayout>(R.id.etJabatan)
        val etUnitOrganisasi = dialogView.findViewById<TextInputLayout>(R.id.etUnitOrganisasi)
        val etTempatLahir = dialogView.findViewById<TextInputLayout>(R.id.etTempatLahir)
        val etTglLahir = dialogView.findViewById<TextInputLayout>(R.id.etTglLahir)
        val btnClose = dialogView.findViewById<AppCompatImageButton>(R.id.btnClose)

        etNama.editText?.setText(data.nama)
        etNip.editText?.setText(data.nip)
        etJenisKelamin.editText?.setText(data.jk)
        etAlamat.editText?.setText(data.alamat)
        etPangkat.editText?.setText(data.pangkat)
        etJabatan.editText?.setText(data.jabatan)
        etUnitOrganisasi.editText?.setText(data.unit_organisasi)
        etTempatLahir.editText?.setText(data.tempatLahir)
        etTglLahir.editText?.setText(data.tanggalLahir)

        imgFotoProfil.load(data.fotoProfil) {
            crossfade(true)
            placeholder(R.drawable.ic_camera_white)
            error(R.drawable.ic_camera_white)
            fallback(R.drawable.ic_camera_white)
            transformations(CircleCropTransformation())
            memoryCachePolicy(coil.request.CachePolicy.ENABLED)
        }

        imgFotoProfil.setOnClickListener {
            alert.dismiss()
            onClickFoto(data.fotoProfil, activity)
        }

        btnClose.setOnClickListener {
            alert.dismiss()
        }

        alert.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AfiliasiHolder {
        return AfiliasiHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_verify_pegawai,
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
