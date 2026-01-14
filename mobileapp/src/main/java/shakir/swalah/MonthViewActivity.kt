package shakir.swalah

import android.os.Bundle
import shakir.swalah.databinding.ActivityMainBinding
import shakir.swalah.databinding.ActivityMontViewBinding


class MonthViewActivity : BaseActivity() {

    private lateinit var binding: ActivityMontViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMontViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustWithSystemWindow(binding.rootViewLL, binding.topSpacer, true)
        binding.backButton.setOnClickListener { finish() }
        val latitude = sp.getDouble("v2_latitude", INVALID_CORDINATE)
        val longitude = sp.getDouble("v2_longitude", INVALID_CORDINATE)
        var locality = sp.getString("v2_locality", null)

        if (latitude != INVALID_CORDINATE && longitude != INVALID_CORDINATE) {
            binding.viewPager2.adapter = MonthAdapter(latitude,longitude,Util.timeFormat().replace(" a",""))
            binding.viewPager2.setCurrentItem(50,false)
        }





    }



}