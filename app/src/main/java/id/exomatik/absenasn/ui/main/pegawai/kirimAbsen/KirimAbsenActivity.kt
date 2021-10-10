package id.exomatik.absenasn.ui.main.pegawai.kirimAbsen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.UploadTask
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.services.notification.model.Notification
import id.exomatik.absenasn.services.notification.model.Sender
import id.exomatik.absenasn.ui.main.MainActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_kirim_absen.*

class KirimAbsenActivity : AppCompatActivity() {
    private lateinit var savedData: DataSave
    private var foto : Uri? = null
    private var jenisAbsensi : String? = ""
    private var idHari : String? = ""
    private var idAbsensi : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_kirim_absen)
        savedData = DataSave(this)

        init()
        onClick()
    }

    private fun init() {
        btnAbsen.isEnabled = false
        idHari = intent.getStringExtra(Constant.idHari)
        idAbsensi = intent.getStringExtra(Constant.idAbsen)
        jenisAbsensi = intent.getStringExtra(Constant.jenis)

        val fotoOld = intent.getStringExtra(Constant.reffFotoUser)
        val fotoFromCamera = intent.getParcelableExtra<Uri>(Constant.reffFotoUser2)

        if (fotoFromCamera != null){
            Glide.with(this).load(fotoFromCamera.path).into(imgFoto)
            foto = fotoFromCamera
            btnAbsen.isEnabled = true
        }
        else if (!fotoOld.isNullOrEmpty()){
            Glide.with(this).load(fotoOld).into(imgFoto)
        }

        if (!idAbsensi.isNullOrEmpty()){
            supportActionBar?.title = "Absensi Datang Ulang"
        }
        else{
            supportActionBar?.title = "Absensi Datang"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.show()
        etUsername.editText?.setText(savedData.getDataUser()?.username)
        textJenisAbsen.text = jenisAbsensi
    }

    private fun onClick() {
        btnAbsen.setOnClickListener {
            onClickAbsen()
        }

        cardFoto.setOnClickListener {
            showLog("Camera")
//            val bundle = Bundle()
//            val fragmentTujuan = CameraFragment()
//            bundle.putParcelable(Constant.reffInstansi, dataInstansi.value)
//            bundle.putString(Constant.idHari, idHari)
//            bundle.putString(Constant.idAbsen, idAbsensi)
//            bundle.putString(Constant.jenis, jenisAbsensi)
//            bundle.putString(Constant.kodeInstansi, kodeInstansi)
//            bundle.putString(Constant.desc, etDesc.value)
//            bundle.putString(Constant.kategori, etKategori)
//            fragmentTujuan.arguments = bundle
//            val navOption = NavOptions.Builder().setPopUpTo(R.id.cameraFragment, true).build()
//            findNavController().navigate(R.id.cameraFragment,bundle, navOption)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocation(act: Activity){
        if (ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                act, Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            textStatus.text = "Anda belum mengizinkan akses lokasi aplikasi ini"

            ActivityCompat.requestPermissions(
                act,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                Constant.codeRequestLocation
            )
        } else {
            progress.visibility = View.VISIBLE
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(act)

            fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                progress.visibility = View.GONE
                val lat = location?.latitude
                val longit = location?.longitude

                if (lat != null && longit != null){
                    validateData(lat.toString(), longit.toString())
                }
                else{
                    textStatus.text = "Error, gagal mengambil lokasi terkini"
                }
            }

            fusedLocationClient.lastLocation.addOnFailureListener {
                progress.visibility = View.GONE
                textStatus.text = it.message
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun validateData(latitude: String, longitude: String){
        progress.visibility = View.VISIBLE

        val dateCreated = getDateNow(Constant.dateFormat1)
        val dateTimeCreated = getDateNow(Constant.dateTimeFormat1)
        val timeCreated = getDateNow(Constant.timeFormat)
        val usernameUser = savedData.getDataUser()?.username
        val hariId = idHari
        val urlFoto = foto
        val jenis = jenisAbsensi

        if (!usernameUser.isNullOrEmpty() && !hariId.isNullOrEmpty() && urlFoto != null
            && !jenis.isNullOrEmpty()){
            val tglSplit = dateCreated.split("-")

            val resultAbsen = ModelAbsensi("", usernameUser, hariId, "",
                latitude, longitude, jenis, Constant.statusRequest,
                tglSplit[0], tglSplit[1], tglSplit[2], dateCreated, timeCreated,
                "${hariId}__${usernameUser}", dateTimeCreated, dateTimeCreated
            )

            saveFoto(urlFoto, resultAbsen)
        }
        else{
            when {
                urlFoto == null -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, Anda harus mengupload foto"
                }
                usernameUser.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, terjadi kesalahan sistem database user"
                }
                jenis.isNullOrEmpty() -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, terjadi kesalahan sistem database jenis"
                }
                else -> {
                    progress.visibility = View.GONE
                    textStatus.text = "Error, mohon login ulang"
                }
            }
        }
    }

    private fun onClickAbsen(){
        progress.visibility = View.VISIBLE
        getCurrentLocation(this)
    }

    private fun saveFoto(image: Uri, dataAbsen: ModelAbsensi){
        progress.visibility = View.VISIBLE

        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            progress.visibility = View.GONE
            getUrlFoto(it, dataAbsen)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.simpanFoto(Constant.reffFotoAbsensi, "${savedData.getDataUser()?.username}__${getTimeStamp()}"
            , image, onSuccessListener, onFailureListener)
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot, dataAbsen: ModelAbsensi) {
        progress.visibility = View.VISIBLE
        val onSuccessListener = OnSuccessListener<Uri?>{
            progress.visibility = View.GONE
            dataAbsen.foto_absensi = it.toString()

            val id = idAbsensi
            if (!id.isNullOrEmpty()){
                dataAbsen.id = id
                updateAbsensi(dataAbsen)
            }
            else{
                createAbsen(dataAbsen)
            }
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.getUrlFoto(uploadTask, onSuccessListener, onFailureListener)
    }

    @SuppressLint("SetTextI18n")
    private fun createAbsen(dataAbsen: ModelAbsensi){
        progress.visibility = View.VISIBLE

        val onCompleteListener = OnCompleteListener<Void> { result ->
                progress.visibility = View.GONE
                if (result.isSuccessful) {
                    textStatus.text = "Berhasil absensi"
                    Toast.makeText(this, "Berhasil absensi", Toast.LENGTH_LONG).show()

                    sendNotification()
                    moveBack()
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

    @SuppressLint("SetTextI18n")
    private fun updateAbsensi(dataAbsen: ModelAbsensi){
        progress.visibility = View.VISIBLE

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                progress.visibility = View.GONE
                if (result.isSuccessful) {
                    textStatus.text = "Berhasil melakukan absensi ulang"
                    Toast.makeText(this, "Berhasil melakukan absensi ulang", Toast.LENGTH_LONG).show()

                    sendNotification()
                    moveBack()
                } else {
                    textStatus.text = "Gagal absensi"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }

        FirebaseUtils.setValueWith1ChildObject(
            Constant.reffAbsensi, dataAbsen.id, dataAbsen, onCompleteListener, onFailureListener
        )
    }

    private fun sendNotification(){
        val notification = Notification(
            "${savedData.getDataUser()?.nama} sudah melakukan absensi",
            "Absen ASN"
            , "id.exomatik.absenasn.fcm_TARGET_NOTIFICATION_ADMIN"
        )

        getDataAdmin(notification)
    }

    private fun getDataAdmin(notification: Notification){
        progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                textStatus.text = result.message
                progress.visibility = View.GONE
            }

            override fun onDataChange(result: DataSnapshot) {
                progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelUser::class.java)

                        val sender = Sender(notification, data?.token)
                        FirebaseUtils.sendNotif(sender)
                    }
                }
                else{
                    textStatus.text = Constant.noData
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffUser, Constant.reffJenisAkun, Constant.levelAdmin, valueEventListener
        )
    }

    private fun moveBack(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                moveBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}