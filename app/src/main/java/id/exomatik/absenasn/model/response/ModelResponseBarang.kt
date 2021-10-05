package id.exomatik.absenasn.model.response

import android.os.Parcelable
import id.exomatik.absenasn.model.ModelBarang
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelResponseBarang(
    var data: List<ModelBarang> = emptyList(),
    var message: String = ""
) : Parcelable