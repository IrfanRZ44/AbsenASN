package id.exomatik.absenasn.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelUser(
    var username: String = "",
    var password: String = "",
    var phone: String = "",
    var token: String = "",
    var nama: String = "",
    var jk: String = "",
    var jenisAkun: String = "",
    var tempatLahir: String = "",
    var tanggalLahir: String = "",
    var alamat: String = "",
    var jabatan: String = "",
    var unit_kerja: String = "",
    var status: String = "",
    var comment: String = "",
    var fotoProfil: String = "",
    var lastLogin: String = "",
    var updatedAt: String = "",
    var createdAt: String = "",
    ) : Parcelable