package id.exomatik.absenasn.ui.main.beranda

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import id.exomatik.absenasn.base.BaseViewModel
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.getDateNow

@SuppressLint("StaticFieldLeak")
class BerandaViewModel : BaseViewModel() {
    val nama = MutableLiveData<String>()
    val deskripsi = MutableLiveData<String>()
    val kodeInstansi = MutableLiveData<String>()


}