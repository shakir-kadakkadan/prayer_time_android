package shakir.swalah.models.ipstack


import com.google.gson.annotations.SerializedName


data class IPStackResponse(

    @field:SerializedName("continent_name")
	val continentName: String? = null,

    @field:SerializedName("zip")
	val zip: String? = null,

    @field:SerializedName("city")
	val city: String? = null,

    @field:SerializedName("ip")
	val ip: String? = null,

    @field:SerializedName("latitude")
	val latitude: Double? = null,

    @field:SerializedName("continent_code")
	val continentCode: String? = null,

    @field:SerializedName("type")
	val type: String? = null,

    @field:SerializedName("country_code")
	val countryCode: String? = null,

    @field:SerializedName("country_name")
	val countryName: String? = null,

    @field:SerializedName("region_name")
	val regionName: String? = null,

    @field:SerializedName("locality")
	val location: Location? = null,

    @field:SerializedName("region_code")
	val regionCode: String? = null,

    @field:SerializedName("longitude")
	val longitude: Double? = null
)