package id.exomatik.absenki.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelInfoApps(
    var informasi: String? = "",
    var statusApps: String? = "",
    var versionApps: String? = "",
    ) : Parcelable