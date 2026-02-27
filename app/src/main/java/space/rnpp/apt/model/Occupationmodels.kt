package space.rnpp.apt.model

data class OccupationFormUiState(
    val input:              OccupationFormInput      = OccupationFormInput(),
    val errors:             OccupationFormErrors     = OccupationFormErrors(),
    val companySuggestions: List<CompanySuggestion>  = emptyList(),
    val showSuggestions:    Boolean                  = false,
    val isNextEnabled:      Boolean                  = false
)

data class CompanySuggestion(
    val id: Int,
    val name: String
)
data class OccupationFormInput(
    val companyName:    String = "",
    val companyAddress: String = "",
    val cityName:       String = "",
    val phoneNumber:    String = "",
    val npwp:           String = ""   // optional
)
data class OccupationFormErrors(//Decider if Next button enabled or not
    val companyNameError:    String? = null,
    val companyAddressError: String? = null,
    val cityNameError:       String? = null,
    val phoneNumberError:    String? = null,
    val npwpError:           String? = null
) {
    val hasErrors: Boolean
        get() = companyNameError    != null ||
                companyAddressError != null ||
                cityNameError       != null ||
                phoneNumberError    != null ||
                npwpError           != null
}