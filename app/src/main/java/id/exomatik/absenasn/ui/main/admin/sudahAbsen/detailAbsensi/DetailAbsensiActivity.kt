package id.exomatik.absenasn.ui.main.admin.sudahAbsen.detailAbsensi

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.services.notification.model.Notification
import id.exomatik.absenasn.services.notification.model.Sender
import id.exomatik.absenasn.ui.main.MainActivity
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.FirebaseUtils
import id.exomatik.absenasn.utils.onClickFoto
import kotlinx.android.synthetic.main.activity_detail_absensi.*

class DetailAbsensiActivity : AppCompatActivity(), OnMapReadyCallback {
    private val mapBundelKey = "MapViewBundleKey"
    private var gmap: GoogleMap? = null
    private var marker: Marker? = null
    private var dataAbsensi : ModelAbsensi? = null
    private var dataUser : ModelUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail_absensi)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Absensi"
        supportActionBar?.show()
        onClick()
        setData()
        setMap(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    private fun setData(){
        dataAbsensi = intent.getParcelableExtra(Constant.reffAbsensi)
        dataUser = intent.getParcelableExtra(Constant.reffUser)
        val requestOpen = intent.getBooleanExtra(Constant.request, false)

        textNama.text = "Pegawai ${dataUser?.nama}"
        textJabatan.text = "Jabatan/UnitKerja ${dataUser?.jabatan}/${dataUser?.unit_kerja}"
        textUsernamePhone.text = dataUser?.phone
        textJenisAbsenJam.text = "Telah Absen pada pukul ${dataAbsensi?.jam}"
        imgFoto.load(dataUser?.fotoProfil) {
            crossfade(true)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_camera_white)
            error(R.drawable.ic_camera_white)
            fallback(R.drawable.ic_camera_white)
            memoryCachePolicy(CachePolicy.ENABLED)
        }

        if (requestOpen){
            btnKonfirmasi.visibility = View.VISIBLE
            btnTolak.visibility = View.VISIBLE
        }
        else{
            btnKonfirmasi.visibility = View.GONE
            btnTolak.visibility = View.GONE
        }

        when (dataAbsensi?.status) {
            Constant.statusActive -> {
                textStatusAbsen.text = "Status : Dikonfirmasi"
                textStatusAbsen.setTextColor(Color.GREEN)
            }
            Constant.statusRejected -> {
                textStatusAbsen.text = "Status : Ditolak"
                textStatusAbsen.setTextColor(Color.RED)
            }
            else -> {
                textStatusAbsen.text = "Status : Belum dikonfirmasi"
                textStatusAbsen.setTextColor(Color.BLUE)
            }
        }
    }

    private fun onClick(){
        imgFoto.setOnClickListener {
            clickFoto()
        }

        btnTolak.setOnClickListener {
            onClickSend(2)
        }

        btnKonfirmasi.setOnClickListener {
            onClickSend(1)
        }
    }

    private fun setMap(savedInstanceState: Bundle?) {
        progress.visibility = View.VISIBLE
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(mapBundelKey)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(mapBundelKey)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(mapBundelKey, mapViewBundle)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    @SuppressLint("SetTextI18n")
    override fun onMapReady(googleMap: GoogleMap) {
        gmap = googleMap
        gmap?.setMinZoomPreference(15f)
        progress.visibility = View.GONE

        val latitude = dataAbsensi?.latitude
        val longitude = dataAbsensi?.longitude

        if (!latitude.isNullOrEmpty() && !longitude.isNullOrEmpty()){
            val lat = latitude.toDouble()
            val longit = longitude.toDouble()
            val myLocation = LatLng(lat, longit)
            gmap?.moveCamera(CameraUpdateFactory.newLatLng(myLocation))
            gmap?.setMinZoomPreference(15f)
            val place = MarkerOptions().position(myLocation).title("Lokasi Absen")
            marker = gmap?.addMarker(place)
        }
        else{
            textStatus.text = "Error, gagal mengambil lokasi absen"
        }
    }

    private fun clickFoto(){
        onClickFoto(dataUser?.fotoProfil?:"", this)
    }

    @SuppressLint("SetTextI18n")
    private fun onClickSend(status: Int){
        val resultAbsen = dataAbsensi

        if (resultAbsen != null && (status == 1 || status == 2)){
            if (status == 1){
                resultAbsen.status = Constant.statusActive
            }
            else{
                resultAbsen.status = Constant.rejected
            }

            updateAbsensiStatus(resultAbsen)
        }
        else{
            textStatus.text = "Error, terjadi kesalahan database"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAbsensiStatus(resultAbsen: ModelAbsensi){
        progress.visibility = View.VISIBLE

        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                progress.visibility = View.GONE
                if (result.isSuccessful) {
                    val notification: Notification

                    if (resultAbsen.status == Constant.rejected){
                        textStatus.text = "Berhasil menolak absen"

                        notification = Notification(
                            "Absensi Anda ditolak",
                            "Absenki"
                            , "id.exomatik.absenasn.fcm_TARGET_NOTIFICATION_PEGAWAI"
                        )
                    }
                    else{
                        textStatus.text = "Berhasil mengkonfirmasi absen"

                        notification = Notification(
                            "Absensi Anda sudah dikonfirmasi",
                            "Absenki"
                            , "id.exomatik.absenasn.fcm_TARGET_NOTIFICATION_PEGAWAI"
                        )
                    }

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

        FirebaseUtils.setValueWith1ChildObject(
            Constant.reffAbsensi, resultAbsen.id, resultAbsen, onCompleteListener, onFailureListener
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

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}