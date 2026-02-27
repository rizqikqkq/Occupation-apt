package space.rnpp.apt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import space.rnpp.apt.model.CompanySuggestion
import space.rnpp.apt.model.DummyOccupationRepository
import space.rnpp.apt.model.FieldValidState
import space.rnpp.apt.model.OccupationFormUiState
import space.rnpp.apt.model.OccupationRepository
import space.rnpp.apt.util.OccupationValidator

class OccupationFormViewModel(
    private val repository: OccupationRepository = DummyOccupationRepository(),
    private val validator:  OccupationValidator   = OccupationValidator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OccupationFormUiState())
    val uiState: StateFlow<OccupationFormUiState> = _uiState.asStateFlow()

    private var companyNameValidationJob: Job? = null
    private var companySuggestJob:        Job? = null   // 500 ms suggest debounce
    private var addressDebounceJob:       Job? = null
    private var cityDebounceJob:          Job? = null
    private var phoneDebounceJob:         Job? = null
    private var npwpDebounceJob:          Job? = null

    //region : Fungsi Rekomendasi Company
    fun onCompanyNameChanged(value: String) {
        _uiState.update { it.copy(input = it.input.copy(companyName = value)) }

        companyNameValidationJob?.cancel()
        companyNameValidationJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateCompanyName(value)
            _uiState.update { state ->
                state.copy(errors = state.errors.copy(companyNameError = error))
            }
            recompute()
        }

        companySuggestJob?.cancel()
        if (value.length > 3) {
            companySuggestJob = viewModelScope.launch {
                delay(SUGGEST_DEBOUNCE_MS)
                val suggestions = repository.getCompanySuggestions(value)
                _uiState.update { state ->
                    state.copy(
                        companySuggestions = suggestions,
                        showSuggestions    = suggestions.isNotEmpty()
                    )
                }
            }
        } else {
            _uiState.update { state ->
                state.copy(companySuggestions = emptyList(), showSuggestions = false)
            }
        }
    }
    fun onCompanySuggestionSelected(suggestion: CompanySuggestion) {
        _uiState.update { state ->
            state.copy(
                input              = state.input.copy(companyName = suggestion.name),
                companySuggestions = emptyList(),
                showSuggestions    = false,
                errors             = state.errors.copy(companyNameError = null)
            )
        }
        recompute()
    }

    fun onDismissSuggestions() {
        _uiState.update { state -> state.copy(showSuggestions = false) }
    }
    //endregion

    fun onCompanyAddressChanged(value: String) {
        _uiState.update { state ->
            state.copy(input = state.input.copy(companyAddress = value))
        }
        addressDebounceJob?.cancel()
        addressDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateCompanyAddress(value)
            _uiState.update { state ->
                state.copy(errors = state.errors.copy(companyAddressError = error))
            }
            recompute()
        }
    }
    fun onCityNameChanged(value: String) {
        _uiState.update { state ->
            state.copy(input = state.input.copy(cityName = value))
        }
        cityDebounceJob?.cancel()
        cityDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateCityName(value)
            _uiState.update { state ->
                state.copy(errors = state.errors.copy(cityNameError = error))
            }
            recompute()
        }
    }
    fun onPhoneNumberChanged(value: String) {
        val digitsOnly = value.filter { char -> char.isDigit() }
        _uiState.update { state ->
            state.copy(input = state.input.copy(phoneNumber = digitsOnly))
        }
        phoneDebounceJob?.cancel()
        phoneDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validatePhoneNumber(digitsOnly)
            _uiState.update { state ->
                state.copy(errors = state.errors.copy(phoneNumberError = error))
            }
            recompute()
        }
    }
    fun onNpwpChanged(value: String) {
        val digitsOnly = value.filter { char -> char.isDigit() }
        // Enforce the max-length cap here — not in the Composable
        val capped = digitsOnly.take(OccupationValidator.NPWP_LENGTH)
        _uiState.update { state ->
            state.copy(input = state.input.copy(npwp = capped))
        }
        npwpDebounceJob?.cancel()
        npwpDebounceJob = viewModelScope.launch {
            delay(VALIDATION_DEBOUNCE_MS)
            val error = validator.validateNpwp(capped)
            _uiState.update { state ->
                state.copy(errors = state.errors.copy(npwpError = error))
            }
            recompute()
        }
    }
    fun onNext() {
        val fullErrors = validator.validateAll(_uiState.value.input)
        if (fullErrors.hasErrors) {
            _uiState.update { state -> state.copy(errors = fullErrors) }
            recompute()
            return
        }
        // TODO: do next action
    }

    private fun recompute() {
        val input  = _uiState.value.input
        val errors = _uiState.value.errors

        val mandatoryFilled =
            input.companyName.isNotBlank()    &&
            input.companyAddress.isNotBlank() &&
            input.cityName.isNotBlank()       &&
            input.phoneNumber.isNotBlank()

        val fieldValid = FieldValidState(
            companyNameValid    = input.companyName.isNotBlank()    && errors.companyNameError    == null,
            companyAddressValid = input.companyAddress.isNotBlank() && errors.companyAddressError == null,
            cityNameValid       = input.cityName.isNotBlank()       && errors.cityNameError       == null,
            phoneNumberValid    = input.phoneNumber.isNotBlank()    && errors.phoneNumberError    == null,
            npwpValid           = input.npwp.isNotBlank()           && errors.npwpError           == null
        )

        _uiState.update { state ->
            state.copy(
                isNextEnabled = mandatoryFilled && !errors.hasErrors,
                fieldValid    = fieldValid
            )
        }
    }

    companion object {
        private const val VALIDATION_DEBOUNCE_MS = 300L
        private const val SUGGEST_DEBOUNCE_MS    = 500L
    }
}