package id.exomatik.absenasn.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import id.exomatik.absenasn.R
import id.exomatik.absenasn.ui.main.admin.belumAbsen.BelumAbsenFragment
import id.exomatik.absenasn.ui.main.admin.daftarPegawai.DaftarPegawaiFragment
import id.exomatik.absenasn.ui.main.admin.hariAbsen.HariAbsenFragment
import id.exomatik.absenasn.ui.main.admin.sudahAbsen.SudahAbsenFragment
import id.exomatik.absenasn.ui.main.pegawai.absensi.AbsensiFragment
import id.exomatik.absenasn.ui.main.riwayat.RiwayatFragment
import id.exomatik.absenasn.utils.Constant
import id.exomatik.absenasn.utils.DataSave
import id.exomatik.absenasn.utils.adapter.SectionsPagerAdapter
import id.exomatik.absenasn.utils.getDateNow
import kotlinx.android.synthetic.main.fragment_admin.*
import kotlinx.android.synthetic.main.header_class.*
import kotlinx.android.synthetic.main.header_class.view.*

class BerandaFragment : Fragment() {
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

    @Suppress("DEPRECATION")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupViewPager(viewPager)
        tabs.setupWithViewPager(viewPager)
    }

    private fun setupViewPager(pager: ViewPager) {
        val adapter = SectionsPagerAdapter(childFragmentManager)

        if (savedData.getDataUser()?.jenisAkun == Constant.levelAdmin){
            adapter.addFragment(SudahAbsenFragment(), "Sudah Absen")
            adapter.addFragment(BelumAbsenFragment(), "Belum Absen")
            adapter.addFragment(HariAbsenFragment(), "Hari Absen")
            adapter.addFragment(RiwayatFragment(), "Riwayat")
            adapter.addFragment(DaftarPegawaiFragment(), "Pegawai")
        }
        else{
            adapter.addFragment(AbsensiFragment(), "Absensi")
            adapter.addFragment(RiwayatFragment(), "Riwayat")
        }

        pager.adapter = adapter
    }
}