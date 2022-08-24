package shakir.swalah

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_mont_view.*


class MonthViewActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mont_view)
        adjustWithSystemWindow(rootViewLL, topSpacer, true)
        val latitude = sp.getDouble("v2_latitude", INVALID_CORDINATE)
        val longitude = sp.getDouble("v2_longitude", INVALID_CORDINATE)
        var locality = sp.getString("v2_locality", null)

        if (latitude != INVALID_CORDINATE && longitude != INVALID_CORDINATE) {
            viewPager2.adapter = MonthAdapter(latitude,longitude,Util.timeFormat().replace(" a",""))
            viewPager2.setCurrentItem(50,false)
        }





    }



}