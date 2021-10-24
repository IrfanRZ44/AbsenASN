package id.exomatik.absenasn.ui.main.riwayat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList
import id.exomatik.absenasn.R
import id.exomatik.absenasn.model.ModelAbsensi
import id.exomatik.absenasn.model.ModelHariKerja
import id.exomatik.absenasn.model.ModelUser
import id.exomatik.absenasn.ui.main.admin.sudahAbsen.detailAbsensi.DetailAbsensiActivity
import id.exomatik.absenasn.utils.*
import kotlinx.android.synthetic.main.fragment_riwayat.view.*
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RiwayatFragment : Fragment() {
    private lateinit var savedData: DataSave
    private lateinit var v : View
    private val listData = ArrayList<ModelAbsensi>()
    private var adapter: AdapterRiwayat? = null
    private var dataHariKerja: ModelHariKerja? = null

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View {
        v = paramLayoutInflater.inflate(R.layout.fragment_riwayat, paramViewGroup, false)

        savedData = DataSave(context)
        init()
        onClick()
        return v
    }

    private fun init() {
        v.etPickDate.editText?.keyListener = null

        initAdapter()

        if (savedData.getDataUser()?.jenisAkun == Constant.levelAdmin){
            v.rfaLayout.visibility = View.VISIBLE
            floatingAction()
        }
        else{
            v.rfaLayout.visibility = View.GONE
        }

        val tglSelected = getDateNow(Constant.dateFormat1)
        v.etPickDate.editText?.setText(tglSelected)
        validateDataSearch(tglSelected)
    }

    private fun onClick() {
        v.btnPickDate.setOnClickListener {
            getPickDate()
        }

        v.swipeRefresh.setOnRefreshListener {
            v.swipeRefresh.isRefreshing = false
            listData.clear()
            adapter?.notifyDataSetChanged()
            val tglSelected = getDateNow(Constant.dateFormat1)
            v.etPickDate.editText?.setText(tglSelected)
            validateDataSearch(tglSelected)
        }
    }

    private fun floatingAction() {
        val rfaContent = RapidFloatingActionContentLabelList(context)
        val item = listOf(
            RFACLabelItem<Int>()
                .setLabel("Download Absensi")
                .setResId(R.drawable.ic_download_white)
                .setIconNormalColor(0xffd84315.toInt())
                .setIconPressedColor(0xffbf360c.toInt())
                .setWrapper(0)
        )

        rfaContent.setItems(item).setIconShadowColor(0xff888888.toInt())

        val rfabHelper = RapidFloatingActionHelper(
            context,
            v.rfaLayout,
            v.rfaBtn,
            rfaContent
        ).build()

        rfaContent.setOnRapidFloatingActionContentLabelListListener(object :
            RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener<Any> {
            override fun onRFACItemLabelClick(position: Int, item: RFACLabelItem<Any>?) {
                when(position) {
                    0 -> {
                        onClickDownload()
                    }
                }
                rfabHelper.toggleContent()
            }

            override fun onRFACItemIconClick(position: Int, item: RFACLabelItem<Any>?) {
                when(position) {
                    0 -> {
                        onClickDownload()
                    }
                }
                rfabHelper.toggleContent()
            }
        })
    }

    private fun initAdapter() {
        adapter = AdapterRiwayat(
            listData,
            { dataAbsen: ModelAbsensi, dataUser: ModelUser? -> onClickItem(dataAbsen, dataUser) },
            activity
        )
        v.rvData.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        v.rvData.adapter = adapter
        v.rvData.isNestedScrollingEnabled = true
    }

    private fun getPickDate() {
        activity?.let { dismissKeyboard(it) }
        val datePickerDialog: DatePickerDialog
        val localCalendar = Calendar.getInstance()

        try {
            datePickerDialog = DatePickerDialog(
                activity ?: throw Exception("Error, mohon mulai ulang aplikasi"),
                { _, paramAnonymousInt1, paramAnonymousInt2, paramAnonymousInt3 ->
                    val dateSelected = Calendar.getInstance()
                    dateSelected[paramAnonymousInt1, paramAnonymousInt2] = paramAnonymousInt3
                    val dateFormatter = SimpleDateFormat(Constant.dateFormat1, Locale.US)
                    val tglSelected = dateFormatter.format(dateSelected.time)

                    v.etPickDate.editText?.setText(tglSelected)
                    listData.clear()
                    adapter?.notifyDataSetChanged()
                    validateDataSearch(tglSelected)
                },
                localCalendar[Calendar.YEAR],
                localCalendar[Calendar.MONTH],
                localCalendar[Calendar.DATE]
            )

            datePickerDialog.show()
        } catch (e: java.lang.Exception) {
            v.textStatus.text = e.message
        }
    }

    @SuppressLint("SetTextI18n")
    private fun validateDataSearch(tglSelected: String){
        val usernameUser = savedData.getDataUser()?.username

        if (savedData.getDataUser()?.jenisAkun == Constant.levelAdmin){
            getHariKerjaOwner(tglSelected)
        }
        else if (savedData.getDataUser()?.jenisAkun == Constant.levelUser && !usernameUser.isNullOrEmpty()){
            getHariKerjaUser(tglSelected, usernameUser)
        }
        else{
            v.textStatus.text = "Error, terjadi kesalahan database, mohon login ulang"
        }
    }

    private fun getHariKerjaOwner(tanggal: String) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
                dataHariKerja = null
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelHariKerja::class.java)

                        if (data != null && data.tanggalKerja == tanggal){
                            dataHariKerja = data
                        }
                    }

                    val hariKerja = dataHariKerja
                    if (hariKerja != null && hariKerja.tanggalKerja == tanggal){
                        v.textStatus.text = ""
                        getAbsensiOwner(hariKerja)
                    }
                    else{
                        v.textStatus.text = "$tanggal bukan hari absen"
                    }
                }
                else{
                    dataHariKerja = null
                    v.textStatus.text = "$tanggal bukan hari absen"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffHariAbsen, Constant.indexTanggalKerja, tanggal, valueEventListener
        )
    }

    private fun getHariKerjaUser(tanggal: String, username: String) {
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
                dataHariKerja = null
            }

            @SuppressLint("SetTextI18n")
            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE
                if (result.exists()) {
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelHariKerja::class.java)

                        if (data != null && data.tanggalKerja == tanggal){
                            dataHariKerja = data
                        }
                    }

                    val hariKerja = dataHariKerja
                    if (hariKerja != null && hariKerja.tanggalKerja == tanggal){
                        v.textStatus.text = ""
                        getAbsensiUser(hariKerja, username)
                    }
                    else{
                        v.textStatus.text = "$tanggal bukan hari absen"
                    }
                }
                else{
                    dataHariKerja = null
                    v.textStatus.text = "$tanggal bukan hari absen"
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffHariAbsen, Constant.indexTanggalKerja, tanggal, valueEventListener
        )
    }

    private fun getAbsensiOwner(hariKerja: ModelHariKerja) {
        val indexKodeHari = hariKerja.id
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE

                if (result.exists()) {
                    v.textStatus.text = ""
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelAbsensi::class.java)

                        if (data != null && data.id_hari == indexKodeHari){
                            listData.add(data)
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
                else{
                    v.textStatus.text = Constant.noDataAbsen
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffAbsensi, Constant.indexIDHari, indexKodeHari, valueEventListener
        )
    }

    private fun getAbsensiUser(hariKerja: ModelHariKerja, username: String) {
        val indexKodeHariUsername = "${hariKerja.id}__$username"
        v.progress.visibility = View.VISIBLE

        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(result: DatabaseError) {
                v.textStatus.text = result.message
                v.progress.visibility = View.GONE
            }

            override fun onDataChange(result: DataSnapshot) {
                v.progress.visibility = View.GONE

                if (result.exists()) {
                    v.textStatus.text = ""
                    for (snapshot in result.children) {
                        val data = snapshot.getValue(ModelAbsensi::class.java)

                        if (data != null && data.indexHariUsername == indexKodeHariUsername){
                            listData.add(data)
                            adapter?.notifyDataSetChanged()
                        }
                    }
                }
                else{
                    v.textStatus.text = Constant.noDataAbsen
                }
            }
        }

        FirebaseUtils.searchDataWith1ChildObject(
            Constant.reffAbsensi, Constant.indexHariUsername, indexKodeHariUsername, valueEventListener
        )
    }

    @SuppressLint("SetTextI18n")
    private fun checkPermissionStorage(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                } == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                v.textStatus.text = "Izin menyimpan file pada penyimpanan telepon ditolak"
                activity?.let {
                    ActivityCompat.requestPermissions(
                        it,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickItem(dataAbsen: ModelAbsensi, dataUser: ModelUser?) {
        if (dataUser == null){
            v.textStatus.text = "Error, gagal mendapatkan data pegawai"
        }
        else{
            val intent = Intent(activity, DetailAbsensiActivity::class.java)
            intent.putExtra(Constant.reffAbsensi, dataAbsen)
            intent.putExtra(Constant.reffUser, dataUser)
            intent.putExtra(Constant.request, false)
            activity?.startActivity(intent)
            activity?.finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickDownload(){
        if (dataHariKerja != null){
            createExcel()
        }
        else{
            Toast.makeText(activity, "Maaf, tanggal ${v.etPickDate.editText?.text} bukan hari absen", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    private fun createExcel(){
        if (checkPermissionStorage()){
            val wb = HSSFWorkbook()
            val cellStyle = wb.createCellStyle()

            cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
            cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle.alignment = CellStyle.ALIGN_CENTER
            cellStyle.fillForegroundColor = HSSFColor.WHITE.index
            cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle.borderBottom = CellStyle.BORDER_THIN
            cellStyle.borderTop = CellStyle.BORDER_THIN
            cellStyle.borderLeft = CellStyle.BORDER_THIN
            cellStyle.borderRight = CellStyle.BORDER_THIN

            val cellStyle2 = wb.createCellStyle()

            cellStyle2.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
            cellStyle2.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle2.alignment = CellStyle.ALIGN_CENTER
            cellStyle2.fillForegroundColor = HSSFColor.WHITE.index
            cellStyle2.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle2.borderTop = CellStyle.BORDER_THIN
            cellStyle2.borderLeft = CellStyle.BORDER_THIN
            cellStyle2.borderRight = CellStyle.BORDER_THIN
            cellStyle2.borderBottom = CellStyle.BORDER_NONE

            val cellStyle3 = wb.createCellStyle()
            cellStyle3.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
            cellStyle3.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle3.alignment = CellStyle.ALIGN_CENTER
            cellStyle3.fillForegroundColor = HSSFColor.WHITE.index
            cellStyle3.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle3.borderLeft = CellStyle.BORDER_THIN
            cellStyle3.borderRight = CellStyle.BORDER_THIN
            cellStyle3.borderTop = CellStyle.BORDER_NONE
            cellStyle3.borderBottom = CellStyle.BORDER_NONE

            val cellStyle4 = wb.createCellStyle()

            cellStyle4.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
            cellStyle4.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle4.alignment = CellStyle.ALIGN_CENTER
            cellStyle4.fillForegroundColor = HSSFColor.WHITE.index
            cellStyle4.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            cellStyle4.borderLeft = CellStyle.BORDER_THIN
            cellStyle4.borderRight = CellStyle.BORDER_THIN
            cellStyle4.borderBottom = CellStyle.BORDER_THIN
            cellStyle4.borderTop = CellStyle.BORDER_NONE

            //Now we are creating sheet
            val sheet = wb.createSheet(Constant.appName)

            sheet.setColumnWidth(0, (15 * 75))
            sheet.setColumnWidth(1, (15 * 350))
            sheet.setColumnWidth(2, (15 * 220))

            var i = 3
            while(i < 3) {
                sheet.setColumnWidth(i++, (15 * 170))
            }

            excelRow0(sheet, wb)
            excelRow1(sheet, wb)
            excelRow2(sheet, wb)
            excelRow3(sheet, wb)
            excelRow4(sheet, wb)
            excelRow5(sheet, wb)
            excelRow6(sheet, wb)
            excelRow7(sheet, wb)

            excelRowAbsensi(sheet, cellStyle, listData)

            saveFile(wb)
        }
        else{
            v.textStatus.text = "Mohon berikan izin penyimpanan telepon terlebih dulu"
        }
    }

    private fun excelRowAbsensi(sheet: Sheet, cellStyle: CellStyle, listAbsensi: List<ModelAbsensi>){
        var indexRow = 8
        for (i in listAbsensi.indices){
            val row = sheet.createRow(indexRow++)

            makeCell(0, "$i.", row, cellStyle)
            makeCell(1, listAbsensi[i].nama, row, cellStyle)
            makeCell(2, listAbsensi[i].jabatan, row, cellStyle)
        }
    }

    @SuppressLint("SetTextI18n")
    @Suppress("DEPRECATION")
    private fun saveFile(excelData: Workbook){
        val file = File( "" + Environment.getExternalStorageDirectory() + File.separator +
                Environment.DIRECTORY_DOWNLOADS, "Daftar Hadir ${dataHariKerja?.jenisAbsen} - ${dataHariKerja?.tanggalKerja}.xls")

        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(file)
            excelData.write(outputStream)
            activity?.let { dialogSucces("File excel tersimpan pada ${file.path}", it) }

//            activity?.let { openFile(it, file) }
        } catch (e: IOException) {
            e.printStackTrace()
            v.textStatus.text = "Gagal menyimpan file excel + ${e.message}"

            try {
                outputStream?.close()
            } catch (ex: IOException) {
                v.textStatus.text = ex.message
            }
        }
    }

    private fun dialogSucces(msg: String, act: Activity) {
        val alert = AlertDialog.Builder(act)
        alert.setTitle("Berhasil Mendownload File")
        alert.setMessage(msg)
        alert.setPositiveButton(
            Constant.iya
        ) { dialog, _ ->
            dialog.dismiss()
        }

        alert.show()
    }

    private fun openFile(act: Activity, filePath: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        showLog(filePath.path)
        val fileProvider = FileProvider.getUriForFile(act, act.packageName, filePath)

        intent.setDataAndType(fileProvider, "application/vnd.ms-excel")
        activity?.startActivity(Intent.createChooser(intent, "Open file"))
    }

    private fun makeCell(index: Int, text: String, row: Row, cellStyle: CellStyle) : Cell {
        val cell = row.createCell(index)
        cell.setCellValue(text)
        cell.cellStyle = cellStyle

        return cell
    }

    @Suppress("DEPRECATION")
    private fun excelRow0(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_NONE
        cellStyle.borderTop = CellStyle.BORDER_THIN
        cellStyle.borderLeft = CellStyle.BORDER_THIN
        cellStyle.borderRight = CellStyle.BORDER_THIN

        val row = sheet.createRow(0)

        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 5))
        makeCell(0, "", row, cellStyle)
    }

    @Suppress("DEPRECATION")
    private fun excelRow1(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_NONE
        cellStyle.borderTop = CellStyle.BORDER_NONE
        cellStyle.borderLeft = CellStyle.BORDER_NONE
        cellStyle.borderRight = CellStyle.BORDER_NONE

        val row = sheet.createRow(1)

        sheet.addMergedRegion(CellRangeAddress(1, 1, 0, 5))
        makeCell(0, "Daftar Hadir", row, cellStyle)
    }

    @Suppress("DEPRECATION")
    private fun excelRow2(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_NONE
        cellStyle.borderTop = CellStyle.BORDER_NONE
        cellStyle.borderLeft = CellStyle.BORDER_NONE
        cellStyle.borderRight = CellStyle.BORDER_NONE

        val row = sheet.createRow(2)

        sheet.addMergedRegion(CellRangeAddress(2, 2, 0, 5))
        makeCell(0, "", row, cellStyle)
    }

    @Suppress("DEPRECATION")
    private fun excelRow3(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_LEFT
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_NONE
        cellStyle.borderTop = CellStyle.BORDER_NONE
        cellStyle.borderLeft = CellStyle.BORDER_NONE
        cellStyle.borderRight = CellStyle.BORDER_NONE

        val row = sheet.createRow(3)

        sheet.addMergedRegion(CellRangeAddress(3, 3, 1, 5))
        makeCell(1, "Jenis Kegiatan : ${dataHariKerja?.jenisAbsen?:'-'}", row, cellStyle)
    }

    @Suppress("DEPRECATION")
    private fun excelRow4(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_LEFT
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_NONE
        cellStyle.borderTop = CellStyle.BORDER_NONE
        cellStyle.borderLeft = CellStyle.BORDER_NONE
        cellStyle.borderRight = CellStyle.BORDER_NONE

        val row = sheet.createRow(4)

        sheet.addMergedRegion(CellRangeAddress(4, 4, 1, 5))
        makeCell(1, "Tanggal : ${dataHariKerja?.tanggalKerja?:'-'}", row, cellStyle)
    }

    @Suppress("DEPRECATION")
    private fun excelRow5(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_LEFT
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_NONE
        cellStyle.borderTop = CellStyle.BORDER_NONE
        cellStyle.borderLeft = CellStyle.BORDER_NONE
        cellStyle.borderRight = CellStyle.BORDER_NONE

        val row = sheet.createRow(5)

        sheet.addMergedRegion(CellRangeAddress(5, 5, 1, 5))
        makeCell(1, "Pukul : ${dataHariKerja?.jamMasuk?:'-'} Wita s.d. ${dataHariKerja?.jamPulang?:'-'} Wita", row, cellStyle)
    }

    @Suppress("DEPRECATION")
    private fun excelRow6(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_NONE
        cellStyle.borderTop = CellStyle.BORDER_NONE
        cellStyle.borderLeft = CellStyle.BORDER_NONE
        cellStyle.borderRight = CellStyle.BORDER_NONE

        val row = sheet.createRow(6)

        sheet.addMergedRegion(CellRangeAddress(6, 6, 0, 5))
        makeCell(0, "", row, cellStyle)
    }

    @Suppress("DEPRECATION")
    private fun excelRow7(sheet: Sheet, workbook: Workbook){
        val cellStyle = workbook.createCellStyle()

        cellStyle.fillForegroundColor = HSSFColor.LIGHT_BLUE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.alignment = CellStyle.ALIGN_CENTER
        cellStyle.fillForegroundColor = HSSFColor.WHITE.index
        cellStyle.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
        cellStyle.borderBottom = CellStyle.BORDER_THICK
        cellStyle.borderTop = CellStyle.BORDER_THICK
        cellStyle.borderLeft = CellStyle.BORDER_THICK
        cellStyle.borderRight = CellStyle.BORDER_THICK

        val row = sheet.createRow(7)

        makeCell(0, "No.", row, cellStyle)
        makeCell(1, "Nama", row, cellStyle)
        makeCell(2, "Jabatan", row, cellStyle)
    }
}