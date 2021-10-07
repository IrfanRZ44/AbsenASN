package id.exomatik.absenasn.ui.main.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import id.exomatik.absenasn.R
import id.exomatik.absenasn.utils.DataSave

class AdminFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_admin, paramViewGroup, false)

        savedData = DataSave(context)
        setHasOptionsMenu(true)
        myCodeHere()

        return v
    }

    private fun myCodeHere() {
        val supportActionBar = (activity as AppCompatActivity).supportActionBar
        supportActionBar?.show()
    }
}