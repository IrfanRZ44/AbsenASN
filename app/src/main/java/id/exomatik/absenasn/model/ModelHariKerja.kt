
package id.exomatik.absenasn.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelHariKerja(
    var id: String = "",
    var jenisAbsen: String = "",
    var tanggal: String = "",
    var bulan: String = "",
    var tahun: String = "",
    var tanggalKerja: String = "",
    var jamMasuk: String = "",
    var jamPulang: String = "",
    var createdAt: String = "",
    var updatedAt: String = ""
) : Parcelable