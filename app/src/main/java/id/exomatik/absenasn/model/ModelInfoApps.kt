package id.exomatik.absenasn.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelInfoApps(
    var aboutApps: String? = "",
    var informasi: String? = "",
    var statusApps: String? = "",
    var versionApps: String? = "",
    var messageWhatsapp: String? = "",
    ) : Parcelable