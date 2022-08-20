package shakir.swalah.models.ipstack


import com.google.gson.annotations.SerializedName


data class Location(

	@field:SerializedName("capital")
	val capital: String? = null,

	@field:SerializedName("calling_code")
	val callingCode: String? = null,

	@field:SerializedName("languages")
	val languages: List<LanguagesItem?>? = null,

	@field:SerializedName("country_flag_emoji_unicode")
	val countryFlagEmojiUnicode: String? = null,

	@field:SerializedName("is_eu")
	val isEu: Boolean? = null,

	@field:SerializedName("country_flag_emoji")
	val countryFlagEmoji: String? = null,

	@field:SerializedName("country_flag")
	val countryFlag: String? = null,

	@field:SerializedName("geoname_id")
	val geonameId: Any? = null
)