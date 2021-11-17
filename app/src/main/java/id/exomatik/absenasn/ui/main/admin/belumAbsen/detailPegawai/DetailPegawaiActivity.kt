package id.exomatik.absenasn.ui.main.admin.belumAbsen.detailPegawai

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.services.notification.model.Notification
import id.exomatik.absenasn.services.notification.model.Sender
import id.exomatik.absenasn.ui.main.MainActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_detail_pegawai.*

class DetailPegawaiActivity : AppCompatActivity() {
    private lateinit var savedData : DataSave
    private var dataUser : ModelUser? = null
    private var dataHariKerja : ModelHariKerja? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail_pegawai)

        savedData = DataSave(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Belum Absen"
        supportActionBar?.show()

        setData()
        onClick()
    }

    @SuppressLint("SetTextI18n")
    private fun setData(){
        dataUser = intent.getParcelableExtra(Constant.reffUser)
        dataHariKerja = intent.getParcelableExtra(Constant.reffHariAbsen)
        textUsernameNama.text = "Nama Pegawai ${dataUser?.nama}"
        textJabatanUnitOrganisasi.text = "Jabatan/Unit Kerja ${dataUser?.jabatan}/${dataUser?.unit_organisasi}"
        textPhone.text = dataUser?.phone

        imgFoto.load(dataUser?.fotoProfil) {
            crossfade(true)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_camera_white)
            error(R.drawable.ic_camera_white)
            fallback(R.drawable.ic_camera_white)
            memoryCachePolicy(CachePolicy.ENABLED)
        }
    }

    private fun onClick(){
        imgFoto.setOnClickListener {
            clickFoto()
        }

        btnWA.setOnClickListener {
            onClickWA()
        }

        btnAlpa.setOnClickListener {
            onClickAlpa()
        }

        btnCuti.setOnClickListener {
            onClickCuti()
        }

        btnSakit.setOnClickListener {
            onClickSakit()
        }

        btnIzin.setOnClickListener {
            onClickIzin()
        }
    }

    private fun clickFoto(){
        onClickFoto(dataUser?.fotoProfil?:"", this)
    }

    @SuppressLint("SetTextI18n")
    private fun onClickWA(){
        try {
            progress.visibility = View.VISIBLE
//            val text = "Salam Exomatik... Hari ini Anda belum melakukan absensi harian pegawai"
            val text = savedData.getDataApps()?.messageWhatsapp?:"Assalamualaikum Warrahmatullahi... Hari ini Anda belum melakukan absensi pegawai"

            val phoneNumber = dataUser?.phone
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=$text"
            packageManager?.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            progress.visibility = View.GONE
        } catch (e: ActivityNotFoundException){
            textStatus.text = "Error ${e.message}"
        } catch (e: PackageManager.NameNotFoundException){
            textStatus.text = "Error ${e.message}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickAlpa(){
        val dataHari = dataHariKerja
        val usernameUser = dataUser?.username
        val namaUser = dataUser?.nama

        if (dataHari != null && !usernameUser.isNullOrEmpty() && !namaUser.isNullOrEmpty()){
            validateData(Constant.absenAlpa, dataHari, usernameUser, namaUser)
        }
        else{
            textStatus.text = "Error, terjadi kesalahan database"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickCuti(){
        val dataHari = dataHariKerja
        val usernameUser = dataUser?.username
        val namaUser = dataUser?.nama

        if (dataHari != null && !usernameUser.isNullOrEmpty() && !namaUser.isNullOrEmpty()){
            validateData(Constant.absenCuti, dataHari, usernameUser, namaUser)
        }
        else{
            textStatus.text = "Error, terjadi kesalahan database"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickSakit(){
        val dataHari = dataHariKerja
        val usernameUser = dataUser?.username
        val namaUser = dataUser?.nama

        if (dataHari != null && !usernameUser.isNullOrEmpty() && !namaUser.isNullOrEmpty()){
            validateData(Constant.absenSakit, dataHari, usernameUser, namaUser)
        }
        else{
            textStatus.text = "Error, terjadi kesalahan database"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickIzin(){
        val dataHari = dataHariKerja
        val usernameUser = dataUser?.username
        val namaUser = dataUser?.nama

        if (dataHari != null && !usernameUser.isNullOrEmpty() && !namaUser.isNullOrEmpty()){
            validateData(Constant.absenIzin, dataHari, usernameUser, namaUser)
        }
        else{
            textStatus.text = "Error, terjadi kesalahan database"
        }
    }

    private fun validateData(jenis: String, dataHari: ModelHariKerja, usernameUser: String, namaUser: String){
        val dateCreated = getDateNow(Constant.dateFormat1)
        val dateTimeCreated = getDateNow(Constant.dateTimeFormat1)
        val timeCreated = getDateNow(Constant.timeFormat)

        val tglSplit = dateCreated.split("-")

        val resultAbsen = ModelAbsensi("", usernameUser,
            dataHari.id, namaUser, dataUser?.nip?:"-", dataUser?.pangkat?:"-",
            dataUser?.jabatan?:"-", dataUser?.unit_organisasi?:"-"
            , "0", "0", jenis, Constant.statusActive,
            tglSplit[0], tglSplit[1], tglSplit[2], dateCreated, timeCreated,
            "${dataHari.id}__${usernameUser}", dateTimeCreated, dateTimeCreated
        )

        createAbsen(resultAbsen)
    }

    @SuppressLint("SetTextI18n")
    private fun createAbsen(dataAbsen: ModelAbsensi){
        progress.visibility = View.VISIBLE

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                progress.visibility = View.GONE
                if (result.isSuccessful) {
                    textStatus.text = "Berhasil absensi"
                    Toast.makeText(this, "Berhasil absensi", Toast.LENGTH_LONG).show()
                    val notification = Notification(
                        "Absensi Anda ${dataAbsen.jenis}",
                        "Absenki"
                        , "id.exomatik.absenki.fcm_TARGET_NOTIFICATION_PEGAWAI"
                    )

                    val sender = Sender(notification, dataUser?.token)
                    FirebaseUtils.sendNotif(sender)
                    moveToMainActivity()
                } else {
                    textStatus.text = "Gagal absensi"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.saveAbsensiWithUnique1Child(
            Constant.reffAbsensi, dataAbsen, onCompleteListener, onFailureListener
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveToMainActivity()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                moveToMainActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun moveToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}