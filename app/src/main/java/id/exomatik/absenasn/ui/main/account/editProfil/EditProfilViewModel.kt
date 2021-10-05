@file:Suppress("DEPRECATION")

package id.exomatik.absenasn.ui.main.account.editProfil

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.UploadTask
import id.exomatik.absenasn.R
import id.exomatik.absenasn.base.BaseViewModel
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StaticFieldLeak")
class EditProfilViewModel(
    private val activity: Activity?,
    private val dataSave: DataSave?,
    private val navController: NavController,
    private val editNamaLengkap: TextInputLayout,
    private val editAlamat: TextInputLayout,
    private val editTempatLahir: TextInputLayout,
    private val editTanggalLahir: TextInputLayout,
    private val radioJK: RadioGroup
) : BaseViewModel() {
    val etNama = MutableLiveData<String>()
    val etFotoProfil = MutableLiveData<Uri>()
    val etAlamat = MutableLiveData<String>()
    val etTempatLahir = MutableLiveData<String>()
    val etTanggalLahir = MutableLiveData<String>()

    fun onClickLogin(){
        activity?.let { dismissKeyboard(it) }
        navController.navigate(R.id.loginFragment)
    }

    fun setDataUser(data: ModelUser){
        etNama.value = data.nama
        etAlamat.value = data.alamat
        etTanggalLahir.value = data.tanggalLahir
        etTempatLahir.value = data.tempatLahir
        etFotoProfil.value = Uri.parse(data.fotoProfil)

        if (data.jk == "Perempuan"){
            radioJK.check(R.id.rbJk2)
        }
        else{
            radioJK.check(R.id.rbJk1)
        }
    }

    fun onClickRegister(){
        activity?.let { dismissKeyboard(it) }
        setNullError()

        val radio = radioJK.checkedRadioButtonId
        val btn = activity?.findViewById<RadioButton?>(radio)
        val jenisKelamin = btn?.text.toString()
        val namaLengkap = etNama.value
        val tglLahir = etTanggalLahir.value
        val tempatLahir = etTempatLahir.value
        val alamat = etAlamat.value
        val fotoProfil = etFotoProfil.value?.path
        val tglSekarang = getDateNow(Constant.dateTimeFormat1)
        val dataOld = dataSave?.getDataUser()

        if (!namaLengkap.isNullOrEmpty() && !tglLahir.isNullOrEmpty() && !tempatLahir.isNullOrEmpty()
            && !fotoProfil.isNullOrEmpty() && !alamat.isNullOrEmpty()
            && radio > 0 && jenisKelamin.isNotEmpty() && dataOld != null
        ) {
            progress.visibility = View.VISIBLE

            val dataUser = ModelUser(
                dataOld.username, dataOld.password, dataOld.phone, dataOld.token, namaLengkap,
                jenisKelamin, tempatLahir, tglLahir, alamat, Constant.statusActive, dataOld.fotoProfil, tglSekarang,
                tglSekarang, dataOld.createdAt
            )

            saveUser(dataUser)
        }
        else{
            when {
                fotoProfil.isNullOrEmpty() -> {
                    textStatus.text = "Mohon upload foto profil"
                }
                namaLengkap.isNullOrEmpty() -> {
                    setTextError("Error, Mohon masukkan nama lengkap", editNamaLengkap)
                }
                dataOld == null -> {
                    textStatus.text = "Error, terjadi kesalahan database"
                }
                jenisKelamin.isEmpty() -> {
                    editNamaLengkap.clearFocus()
                    textStatus.text = "Error, Mohon pilih jenis kelamin"
                }
                alamat.isNullOrEmpty() -> {
                    setTextError("Error, mohon masukkan alamat", editAlamat)
                }
                tempatLahir.isNullOrEmpty() -> {
                    setTextError("Error, Mohon masukkan tempat lahir", editTempatLahir)
                }
                tglLahir.isNullOrEmpty() -> {
                    textStatus.text = "Error, Mohon pilih tanggal lahir"
                    editNamaLengkap.clearFocus()
                    textStatus.text = "Error, Mohon pilih jenis kelamin"
                    editTanggalLahir.requestFocus()
                    editTanggalLahir.findFocus()
                    editTanggalLahir.error = "Error, Mohon pilih tanggal lahir"
                }
            }
        }
    }

    fun getDateTglLahir() {
        activity?.let { dismissKeyboard(it) }
        val datePickerDialog: DatePickerDialog
        val localCalendar = Calendar.getInstance()

        try {
            datePickerDialog = DatePickerDialog(
                activity ?: throw Exception("Error, mohon mulai ulang aplikasi"),
                { _, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3 ->
                    val dateSelected = Calendar.getInstance()
                    dateSelected[paramAnonymousInt1, paramAnonymousInt2] = paramAnonymousInt3
                    val dateFormatter = SimpleDateFormat(Constant.dateFormat1, Locale.US)
                    etTanggalLahir.value = dateFormatter.format(dateSelected.time)
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
        editNamaLengkap.error = null
        editTanggalLahir.error = null
        editTempatLahir.error = null
        editAlamat.error = null
    }

    private fun setTextError(msg: String, editText: TextInputLayout){
        textStatus.text = msg
        editText.error = msg
        editText.requestFocus()
        editText.findFocus()
    }

    fun saveFoto(image: Uri, username: String){
        val onSuccessListener = OnSuccessListener<UploadTask.TaskSnapshot> {
            getUrlFoto(it, username)
        }

        val onFailureListener = OnFailureListener {
            textStatus.text = it.message
            progress.visibility = View.GONE
        }

        FirebaseUtils.simpanFoto(Constant.reffFotoUser, username
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

    private fun saveUser(dataUser: ModelUser) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    dataSave?.setDataObject(
                        dataUser, Constant.reffUser
                    )

                    navController.popBackStack()
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

    private fun saveUrlFotoUser(urlFoto: String, username: String) {
        val onCompleteListener =
            OnCompleteListener<Void> { result ->
                if (result.isSuccessful) {
                    textStatus.text = "Berhasil menyimpan foto"
                    val dataUser = dataSave?.getDataUser()
                    dataUser?.fotoProfil = urlFoto

                    dataSave?.setDataObject(
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
}