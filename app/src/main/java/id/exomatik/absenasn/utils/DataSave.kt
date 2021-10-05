package id.exomatik.absenasn.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.utils.Constant.reffUser
import com.google.gson.Gson
import id.exomatik.absenasn.model.ModelInfoApps

class DataSave(private val context: Context?) {
    private val preffs: SharedPreferences? = context?.getSharedPreferences("UserPref", 0)

    fun setDataObject(any: Any?, key: String) {
        try {
            val prefsEditor: SharedPreferences.Editor = preffs?.edit()
                ?: throw Exception("Preffs Belum Di Inisialisasikan")
            val gson = Gson()
            val json: String = gson.toJson(any)
            prefsEditor.putString(key, json)
            prefsEditor.apply()
        }catch (e: Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun getDataUser(): ModelUser? {
        return try {
            val gson = Gson()
            val json: String = preffs?.getString(reffUser, "")
                ?: throw Exception("Preffs Belum Di Inisialisasikan")
            gson.fromJson(json, ModelUser::class.java)
        }catch (e: Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            null
        }
    }

    fun getDataApps(): ModelInfoApps? {
        return try {
            val gson = Gson()
            val json: String = preffs?.getString(Constant.reffInfoApps, "")
                ?: throw Exception("Preffs Belum Di Inisialisasikan")
            gson.fromJson(json, ModelInfoApps::class.java)
        }catch (e: Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            null
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun setDataString(value: String?, key: String) {
        try {
            val prefsEditor: SharedPreferences.Editor =
                preffs?.edit() ?: throw Exception("Preffs Belum Di Inisialisasikan")
            prefsEditor.putString(key, value)
            prefsEditor.apply()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun getKeyString(key: String): String? {
        return try {
            preffs?.getString(key, "") ?: throw Exception("Data Kosong")
        }catch (e: Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            null
        }
    }
}