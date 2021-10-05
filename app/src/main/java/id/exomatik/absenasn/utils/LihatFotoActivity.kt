package id.exomatik.absenasn.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import coil.request.CachePolicy
import id.exomatik.absenasn.R
import kotlinx.android.synthetic.main.activity_lihat_foto.*

class LihatFotoActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_lihat_foto)
        myCodeHere()
    }

    private fun myCodeHere() {
        supportActionBar?.show()
        supportActionBar?.title = "Foto"

        val urlFoto= intent.getStringExtra("urlFoto")

        imgFoto.load(urlFoto) {
            crossfade(true)
            placeholder(R.drawable.ic_camera_white)
            error(R.drawable.ic_camera_white)
            fallback(R.drawable.ic_camera_white)
            memoryCachePolicy(CachePolicy.ENABLED)
        }
    }
}