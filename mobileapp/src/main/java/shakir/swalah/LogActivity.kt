/*
package shakir.swalah

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_log.*
import kotlin.concurrent.thread

class LogActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        thread {
            val all = appDatabase.LogDao().getAll()
            runOnUiThread {
                recyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager=LinearLayoutManager(this@LogActivity)
                    adapter=LogAdapter(all)
                }
            }
        }





    }
}
*/
