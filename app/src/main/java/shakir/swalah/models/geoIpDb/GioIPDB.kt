package shakir.swalah.models.geoIpDb


import com.google.gson.annotations.SerializedName


data class GioIPDB(

	@field:SerializedName("country_code")
	val countryCode: String? = null,

	@field:SerializedName("city")
	val city: String? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("IPv4")
	val iPv4: String? = null,

	@field:SerializedName("country_name")
	val countryName: String? = null,

	@field:SerializedName("postal")
	val postal: String? = null,

	@field:SerializedName("state")
	val state: String? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null
)