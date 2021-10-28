package id.exomatik.absenasn.ui.main.account

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.request.CachePolicy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.activity_edit_profil.*
import java.text.SimpleDateFormat
import java.util.*

class EditProfilActivity : AppCompatActivity(){
    private lateinit var savedData : DataSave
    private var etFotoProfil : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_edit_profil)
        myCodeHere()
    }

    private fun myCodeHere() {
        savedData = DataSave(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Profil"
        supportActionBar?.show()

        val dataUser = savedData.getDataUser()
        if (dataUser != null){
            setDataUser(dataUser)
        }
        etTglLahir.editText?.keyListener = null
        if (savedData.getDataUser()?.jenisAkun == Constant.levelUser){
            etJabatan.visibility = View.VISIBLE
            etUnitKerja.visibility = View.VISIBLE
        }

        onClick()
    }

    private fun onClick() {
        cardFotoProfil.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(true)
                .setAllowRotation(true)
                .setAspectRatio(1, 1)
                .start(this)
        }

        btnSignUp.setOnClickListener {
            onClickSave()
        }

        btnGetTglLahir.setOnClickListener {
            getDateTglLahir()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            val username = savedData.getDataUser()?.username

            if (resultCode == Activity.RESULT_OK && !username.isNullOrEmpty()){
                val imageUri = result.uri
                etFotoProfil = imageUri

                imgFotoProfil.load(imageUri) {
                    crossfade(true)
                    placeholder(R.drawable.ic_camera_white)
                    error(R.drawable.ic_camera_white)
                    fallback(R.drawable.ic_camera_white)
                    memoryCachePolicy(CachePolicy.ENABLED)
                }
                saveFoto(imageUri, username)
            }
        }
    }

    private fun setDataUser(data: ModelUser){
        etNamaLengkap.editText?.setText(data.nama)
        etAlamat.editText?.setText(data.alamat)
        etTglLahir.editText?.setText(data.tanggalLahir)
        etTempatLahir.editText?.setText(data.tempatLahir)
        etFotoProfil = Uri.parse(data.fotoProfil)

        imgFotoProfil.load(etFotoProfil) {
            crossfade(true)
            placeholder(R.drawable.ic_camera_white)
            error(R.drawable.ic_camera_white)
            fallback(R.drawable.ic_camera_white)
            memoryCachePolicy(CachePolicy.ENABLED)
        }

        if (data.jk == "Perempuan"){
            rgJK.check(R.id.rbJk2)
        }
        else{
            rgJK.check(R.id.rbJk1)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickSave(){
        dismissKeyboard(this)
        setNullError()

        val radio = rgJK.checkedRadioButtonId
        val btn = findViewById<RadioButton?>(radio)
        val jenisKelamin = btn?.text.toString()
        val namaLengkap = etNamaLengkap.editText?.text.toString()
        val tglLahir = etTglLahir.editText?.text.toString()
        val tempatLahir = etTempatLahir.editText?.text.toString()
        val alamat = etAlamat.editText?.text.toString()
        val jabatan = etJabatan.editText?.text.toString()
        val unitKerja = etUnitKerja.editText?.text.toString()
        val fotoProfil = etFotoProfil?.path
        val tglSekarang = getDateNow(Constant.dateTimeFormat1)
        val dataOld = savedData.getDataUser()

        if (namaLengkap.isNotEmpty() && tglLahir.isNotEmpty() && tempatLahir.isNotEmpty()
            && !fotoProfil.isNullOrEmpty() && alamat.isNotEmpty()
            && radio > 0 && jenisKelamin.isNotEmpty() && dataOld != null
        ) {
            progress.visibility = View.VISIBLE

            val dataUser = ModelUser(
                dataOld.username, dataOld.password, dataOld.phone, dataOld.token, namaLengkap,
                jenisKelamin, dataOld.jenisAkun, tempatLahir, tglLahir, alamat, jabatan, unitKerja,
                Constant.statusActive, "", dataOld.fotoProfil, tglSekarang,
                tglSekarang, dataOld.createdAt
            )

            saveUser(dataUser)
        }
        else{
            when {
                fotoProfil.isNullOrEmpty() -> {
                    textStatus.text = "Mohon upload foto profil"
                }
                namaLengkap.isEmpty() -> {
                    setTextError("Error, Mohon masukkan nama lengkap", etNamaLengkap)
                }
                dataOld == null -> {
                    textStatus.text = "Error, terjadi kesalahan database"
                }
                jenisKelamin.isEmpty() -> {
                    etNamaLengkap.clearFocus()
                    textStatus.text = "Error, Mohon pilih jenis kelamin"
                }
                alamat.isEmpty() -> {
                    setTextError("Error, mohon masukkan alamat", etAlamat)
                }
                tempatLahir.isEmpty() -> {
                    setTextError("Error, Mohon masukkan tempat lahir", etTempatLahir)
                }
                tglLahir.isEmpty() -> {
                    textStatus.text = "Error, Mohon pilih tanggal lahir"
                    etNamaLengkap.clearFocus()
                    textStatus.text = "Error, Mohon pilih jenis kelamin"
                    etTglLahir.requestFocus()
                    etTglLahir.findFocus()
                    etTglLahir.error = "Error, Mohon pilih tanggal lahir"
                }
            }
        }
    }

    private fun getDateTglLahir() {
        dismissKeyboard(this)
        val datePickerDialog: DatePickerDialog
        val localCalendar = Calendar.getInstance()

        try {
            datePickerDialog = DatePickerDialog(this,
                { _, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3 ->
                    val dateSelected = Calendar.getInstance()
                    dateSelected[paramAnonymousInt1, paramAnonymousInt2] = paramAnonymousInt3
                    val dateFormatter = SimpleDateFormat(Constant.dateFormat1, Locale.US)
                    etTglLahir.editText?.setText(dateFormatter.format(dateSelected.time))
                },
                localCalendar[Calendar.YEAR],
                localCalendar[Calendar.MONTH],
                localCalendar[Calendar.DATE]
            )

            datePickerDialog.show()
        } catch (e: java.lang.Exception) {
            textStatus.text = e.message
        }
    }

    private fun setNullError(){
        etNamaLengkap.error = null
        etTglLahir.error = null
        etTempatLahir.error = null
        etAlamat.error = null
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        textStatus.text = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    private fun saveFoto(image: Uri, username: String){
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            getUrlFoto(it, username)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.simpanFoto(
            Constant.reffFotoUser, username
            , image, onSuccessListener, onFailureListener)
    }

    private fun getUrlFoto(uploadTask: UploadTask.TaskSnapshot, username: String) {
        val onSuccessListener = OnSuccessListener<Uri?>{
            saveUrlFotoUser(it.toString(), username)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.getUrlFoto(uploadTask, onSuccessListener, onFailureListener)
    }

    @SuppressLint("SetTextI18n")
    private fun saveUser(dataUser: ModelUser) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    savedData.setDataObject(
                        dataUser, Constant.reffUser
                    )

                    dismissKeyboard(this)
                    finish()
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal menyimpan data user"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }
        FirebaseUtils.setValueObject(
            Constant.reffUser, dataUser.username, dataUser, onCompleteListener, onFailureListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun saveUrlFotoUser(urlFoto: String, username: String) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    Toast.makeText(this, "Berhasil menyimpan foto", Toast.LENGTH_LONG).show()
                    val dataUser = savedData.getDataUser()
                    dataUser?.fotoProfil = urlFoto

                    savedData.setDataObject(
                        dataUser, Constant.reffUser
                    )
                } else {
                    progress.visibility = View.GONE
                    textStatus.text = "Gagal menyimpan data user"
                }
            }

        val onFailureListener = OnFailureListener { result ->
            progress.visibility = View.GONE
            textStatus.text = result.message
        }
        FirebaseUtils.setValueWith2ChildString(
            Constant.reffUser, username, Constant.fotoProfil, urlFoto, onCompleteListener, onFailureListener
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}