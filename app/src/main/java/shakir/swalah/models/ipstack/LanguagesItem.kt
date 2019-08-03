package shakir.swalah.models.ipstack


import com.google.gson.annotations.SerializedName


data class LanguagesItem(

	@field:SerializedName("code")
	val code: String? = null,

	@field:SerializedName("native")
	val jsonMemberNative: String? = null,

	@field:SerializedName("name")
	val name: String? = null
)