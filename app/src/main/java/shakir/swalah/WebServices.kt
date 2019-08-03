package shakir.swalah

import io.reactivex.Observable
import retrofit2.http.GET
import shakir.swalah.models.geoIpDb.GioIPDB
import shakir.swalah.models.ipstack.IPStackResponse

interface WebServices {

    @GET("http://api.ipstack.com/134.201.250.155?access_key=03b12180213533d575cc021e50c57207")
    fun ipStack(): Observable<IPStackResponse>


    @GET("http://geoip-db.com/json/")
    fun gioIpDB(): Observable<GioIPDB>


}