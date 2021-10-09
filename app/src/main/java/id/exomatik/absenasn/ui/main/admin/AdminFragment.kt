package id.exomatik.absenasn.ui.main.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.exomatik.absenasn.R
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.getDateNow
import kotlinx.android.synthetic.main.header_class.view.*

class AdminFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_admin, paramViewGroup, false)

        savedData = DataSave(context)
        myCodeHere()

        return v
    }

    private fun myCodeHere() {
        v.textTanggal.text = getDateNow(Constant.dateFormat2)
    }
}