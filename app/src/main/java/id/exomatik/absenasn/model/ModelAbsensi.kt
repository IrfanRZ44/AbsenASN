package id.exomatik.absenasn.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelAbsensi(
    var id: String = "",
    var username_user: String = "",
    var id_hari: String = "",
    var foto_absensi: String = "",
    var nama: String = "",
    var jabatan: String = "",
    var latitude: String = "",
    var longitude: String = "",
    var jenis: String = "",
    var status: String = "",
    var tanggal: String = "",
    var bulan: String = "",
    var tahun: String = "",
    var tanggalKerja: String = "",
    var jam: String = "",
    var indexHariUsername: String = "",
    var created_at: String = "",
    var updated_at: String = ""
) : Parcelable