package id.exomatik.absenki.model.response

import android.os.Parcelable
import id.exomatik.absenki.model.ModelBarang
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ModelResponseBarang(
    var data: List<ModelBarang> = emptyList(),
    var message: String = ""
) : Parcelable