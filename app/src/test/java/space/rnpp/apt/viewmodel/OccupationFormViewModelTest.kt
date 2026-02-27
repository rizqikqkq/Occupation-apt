package space.rnpp.apt.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import space.rnpp.apt.model.CompanySuggestion
import space.rnpp.apt.model.OccupationFormInput
import space.rnpp.apt.model.OccupationRepository
import space.rnpp.apt.util.OccupationValidator


@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("OccupationFormViewModel Tests")
class OccupationFormViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: OccupationRepository = mock()
    private val validator = OccupationValidator()
    private lateinit var viewModel: OccupationFormViewModel

    private val fakeSuggestions = listOf(
        CompanySuggestion(1, "PT Bank Nasional"),
        CompanySuggestion(2, "PT Digital Solusi")
    )

    // @BeforeEach runs before EVERY single @Test method.
    // This ensures each test starts from a clean, known state.
    @BeforeEach
    fun setUp() {
        // Replace Dispatchers.Main with test dispatcher.=
        Dispatchers.setMain(testDispatcher)

        // Set the default mock behaviour before each test
        whenever(mockRepository.getCompanySuggestions(any()))
            .thenReturn(fakeSuggestions)

        viewModel = OccupationFormViewModel(
            repository = mockRepository,
            validator  = validator
        )
    }

    // @AfterEach runs after EVERY @Test to clean up.
    // Resetting Main prevents the test dispatcher from leaking into other tests.
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -- NESTED TEST CLASSES (JUnit5 feature) --------------------------------
    @Nested
    @DisplayName("Initial State")
    inner class InitialStateTests {

        @Test
        @DisplayName("All fields should be empty on start")
        fun startEmptyField() = runTest {
            val state = viewModel.uiState.value
            assertEquals("", state.input.companyName)
            assertEquals("", state.input.companyAddress)
            assertEquals("", state.input.cityName)
            assertEquals("", state.input.phoneNumber)
            assertEquals("", state.input.npwp)
        }

        @Test
        @DisplayName("Next button should be disabled on start")
        fun disableNextButton() {
            assertFalse(viewModel.uiState.value.isNextEnabled)
        }

        @Test
        @DisplayName("No validation errors should exist on start")
        fun initNoError() {
            assertFalse(viewModel.uiState.value.errors.hasErrors)
        }

        @Test
        @DisplayName("Suggestions dropdown should be hidden on start")
        fun initHideSuggestion() {
            assertFalse(viewModel.uiState.value.showSuggestions)
            assertTrue(viewModel.uiState.value.companySuggestions.isEmpty())
        }
    }


    @Nested
    @DisplayName("Company Name – Auto Suggest")
    inner class AutoSuggestTests {

        @Test
        @DisplayName("SUCCESS: typing >3 chars shows suggestions after 500ms")
        fun attemptSuggestingWithMinChar() = runTest {
            viewModel.onCompanyNameChanged("PT B")

            // Before 500ms - mock should NOT have been called yet
            advanceTimeBy(400)
            verify(mockRepository, never()).getCompanySuggestions(any())
            assertFalse(viewModel.uiState.value.showSuggestions)

            // After 500ms - mock IS called, suggestions appear
            advanceTimeBy(200) // total = 600ms
            verify(mockRepository, times(1)).getCompanySuggestions("PT B")
            assertTrue(viewModel.uiState.value.showSuggestions)
            assertEquals(2, viewModel.uiState.value.companySuggestions.size)
        }

        @Test
        @DisplayName("FAILURE: typing ≤3 chars never triggers suggestions")
        fun attemptSuggestingWithLessThanMinChar() = runTest {
            viewModel.onCompanyNameChanged("PT")
            advanceTimeBy(600)

            verify(mockRepository, never()).getCompanySuggestions(any())
            assertFalse(viewModel.uiState.value.showSuggestions)
        }

        @Test
        @DisplayName("SUCCESS: rapid typing only fires one repository call")
        fun rapidTypingTest() = runTest {
            // Simulate user fast typingh
            viewModel.onCompanyNameChanged("P")
            viewModel.onCompanyNameChanged("PT")
            viewModel.onCompanyNameChanged("PT ")
            viewModel.onCompanyNameChanged("PT B")
            viewModel.onCompanyNameChanged("PT Ba")  

            advanceTimeBy(600)

            // Despite 5 keystrokes, repository called exactly ONCE with the last value
            verify(mockRepository, times(1)).getCompanySuggestions(any())
            verify(mockRepository, times(1)).getCompanySuggestions("PT Ba")
        }

        @Test
        @DisplayName("SUCCESS: selecting suggestion fills field and hides dropdown")
        fun userSelectSuggestion() = runTest {
            viewModel.onCompanyNameChanged("PT B")
            advanceTimeBy(600)
            assertTrue(viewModel.uiState.value.showSuggestions)

            val selected = fakeSuggestions[0] // PT Bank Nasional
            viewModel.onCompanySuggestionSelected(selected)

            val state = viewModel.uiState.value
            assertEquals("PT Bank Nasional", state.input.companyName)
            assertFalse(state.showSuggestions)
            assertTrue(state.companySuggestions.isEmpty())
            assertNull(state.errors.companyNameError)
        }

        @Test
        @DisplayName("FAILURE: empty result from repository hides dropdown")
        fun noSuggestion() = runTest {
            whenever(mockRepository.getCompanySuggestions(any())).thenReturn(emptyList())

            viewModel.onCompanyNameChanged("ZZZZ")
            advanceTimeBy(600)

            assertFalse(viewModel.uiState.value.showSuggestions)
        }

        @Test
        @DisplayName("SUCCESS: dismiss hides dropdown without changing input")
        fun userNotSelectingSuggestion() = runTest {
            viewModel.onCompanyNameChanged("PT B")
            advanceTimeBy(600)
            assertTrue(viewModel.uiState.value.showSuggestions)

            viewModel.onDismissSuggestions()

            assertFalse(viewModel.uiState.value.showSuggestions)
            assertEquals("PT B", viewModel.uiState.value.input.companyName)
        }
    }


    @Nested
    @DisplayName("Field Validation – Debounce (300ms)")
    inner class FieldValidationTests {

        @Test
        @DisplayName("SUCCESS: valid company name clears error after debounce")
        fun companyTypeTest() = runTest {
            viewModel.onCompanyNameChanged("A") // too short - triggers error
            advanceTimeBy(400)
            assertNotNull(viewModel.uiState.value.errors.companyNameError)

            viewModel.onCompanyNameChanged("PT Bank Nasional") // valid
            advanceTimeBy(400)
            assertNull(viewModel.uiState.value.errors.companyNameError)
        }

        @Test
        @DisplayName("FAILURE: blank company name shows required error")
        fun deleteAllLetterFromCompany() = runTest {
            viewModel.onCompanyNameChanged("") // blank
            advanceTimeBy(400)
            assertEquals("Company name is required",
                viewModel.uiState.value.errors.companyNameError)
        }

        @Test
        @DisplayName("FAILURE: short company name shows length error")
        fun companyWithLessThanMinChar() = runTest {
            viewModel.onCompanyNameChanged("A") // only 1 char, minimum is 2
            advanceTimeBy(400)
            assertNotNull(viewModel.uiState.value.errors.companyNameError)
        }

        @Test
        @DisplayName("SUCCESS: error does NOT appear before debounce fires")
        fun companySuggestionShortDelay() = runTest {
            viewModel.onCompanyNameChanged("A") // invalid
            advanceTimeBy(200) // before debounce
            assertNull(viewModel.uiState.value.errors.companyNameError)
        }

        @Test
        @DisplayName("SUCCESS: phone strips non-digit characters")
        fun phoneNumberWithSymbolOrChar() = runTest {
            viewModel.onPhoneNumberChanged("0812-345-678")
            assertEquals("0812345678", viewModel.uiState.value.input.phoneNumber)
        }

        @Test
        @DisplayName("FAILURE: phone too short shows error")
        fun phoneNumberTooShort() = runTest {
            viewModel.onPhoneNumberChanged("1234567") // 7 digits, min is 8
            advanceTimeBy(400)
            assertNotNull(viewModel.uiState.value.errors.phoneNumberError)
        }

        @Test
        @DisplayName("SUCCESS: city with numbers shows error")
        fun cityHasDigits() = runTest {
            viewModel.onCityNameChanged("Jakarta1")
            advanceTimeBy(400)
            assertEquals("City name must contain only letters",
                viewModel.uiState.value.errors.cityNameError)
        }

        @Test
        @DisplayName("SUCCESS: NPWP strips non-digit characters")
        fun npwpHasFormatedString() = runTest {
            viewModel.onNpwpChanged("12.345.678.9-012.300") // If done Append NIK with NPWP, the digits it's use is NIK instead of NPWP
            assertEquals("123456789012300".take(16),
                viewModel.uiState.value.input.npwp.take(16))
        }

        @Test
        @DisplayName("FAILURE: NPWP with wrong digit count shows error")
        fun npwpHasLessThanMinDigits() = runTest {
            viewModel.onNpwpChanged("1234567890") // 10 digits, needs 16
            advanceTimeBy(400)
            assertEquals(
                "NPWP must be exactly ${OccupationValidator.NPWP_LENGTH} digits",
                viewModel.uiState.value.errors.npwpError
            )
        }

        @Test
        @DisplayName("SUCCESS: blank NPWP has no error (optional field)")
        fun npwpIsBlank() = runTest {
            viewModel.onNpwpChanged("")
            advanceTimeBy(400)
            assertNull(viewModel.uiState.value.errors.npwpError)
        }

        @Test
        @DisplayName("SUCCESS: NPWP with exactly 16 digits has no error")
        fun npwpIsValid() = runTest {
            viewModel.onNpwpChanged("1234567890123456") // exactly 16
            advanceTimeBy(400)
            assertNull(viewModel.uiState.value.errors.npwpError)
        }
    }


    @Nested
    @DisplayName("Next Button – isNextEnabled Logic")
    inner class NextButtonTests {

        @Test
        @DisplayName("FAILURE: Next disabled when all fields empty")
        fun allFieldEmpty() {
            assertFalse(viewModel.uiState.value.isNextEnabled)
        }

        @Test
        @DisplayName("FAILURE: Next disabled when only some fields filled")
        fun someFieldIsFilled() = runTest {
            viewModel.onCompanyNameChanged("PT Bank Nasional")
            viewModel.onCityNameChanged("Jakarta")
            advanceTimeBy(400)
            // companyAddress and phoneNumber still empty
            assertFalse(viewModel.uiState.value.isNextEnabled)
        }

        @Test
        @DisplayName("SUCCESS: Next enabled when all mandatory fields are valid")
        fun allFieldValid() = runTest {
            fillAllValidFields()
            advanceTimeBy(400)
            assertTrue(viewModel.uiState.value.isNextEnabled)
        }

        @Test
        @DisplayName("FAILURE: Next disabled when any error exists")
        fun someFieldValid() = runTest {
            fillAllValidFields()
            advanceTimeBy(400)
            assertTrue(viewModel.uiState.value.isNextEnabled)

            // Now introduce an error
            viewModel.onCityNameChanged("Jakarta123") // invalid - contains digits
            advanceTimeBy(400)
            assertFalse(viewModel.uiState.value.isNextEnabled)
        }

        @Test
        @DisplayName("SUCCESS: Next enabled even when optional NPWP is blank")
        fun allFieldValidWithBlankNPWP() = runTest {
            fillAllValidFields(npwp = "") // no NPWP
            advanceTimeBy(400)
            assertTrue(viewModel.uiState.value.isNextEnabled)
        }

        @Test
        @DisplayName("FAILURE: Next disabled when optional NPWP has wrong length")
        fun allFieldValidExceptNPWP() = runTest {
            fillAllValidFields(npwp = "12345") // too short
            advanceTimeBy(400)
            assertFalse(viewModel.uiState.value.isNextEnabled)
        }

        @Test
        @DisplayName("SUCCESS: Next re-enables after fixing an error")
        fun redoFillingFieldAfterFailed() = runTest {
            fillAllValidFields()
            advanceTimeBy(400)
            assertTrue(viewModel.uiState.value.isNextEnabled)

            viewModel.onPhoneNumberChanged("123") // break it
            advanceTimeBy(400)
            assertFalse(viewModel.uiState.value.isNextEnabled)

            viewModel.onPhoneNumberChanged("08123456789") // fix it
            advanceTimeBy(400)
            assertTrue(viewModel.uiState.value.isNextEnabled)
        }
    }

    // -- HELPER -----------------------------------------------------------------------------------------------
    private fun fillAllValidFields(npwp: String = "") {
        viewModel.onCompanyNameChanged("PT Bank Nasional")
        viewModel.onCompanyAddressChanged("Jl. Sudirman No. 1, Jakarta Pusat")
        viewModel.onCityNameChanged("Jakarta")
        viewModel.onPhoneNumberChanged("08123456789")
        if (npwp.isNotEmpty()) viewModel.onNpwpChanged(npwp)
    }
}

@DisplayName("OccupationValidator Tests")
class OccupationValidatorTest {

    private val validator = OccupationValidator()

    // -- COMPANY NAME -------------------------------------------------------------------------------------------
    @Nested
    @DisplayName("Company Name Validation")
    inner class CompanyNameValidation {

        @Test @DisplayName("SUCCESS: valid name returns null")
        fun companyValid() =
            assertNull(validator.validateCompanyName("PT Bank Nasional"))

        @Test @DisplayName("FAILURE: blank returns required error")
        fun companyBlank() =
            assertEquals("Company name is required", validator.validateCompanyName(""))

        @Test @DisplayName("FAILURE: single char returns too short error")
        fun companyTooShort() =
            assertNotNull(validator.validateCompanyName("A"))

        @Test @DisplayName("FAILURE: 101 chars returns too long error")
        fun companyTooLong() =
            assertNotNull(validator.validateCompanyName("A".repeat(101)))

        @Test @DisplayName("SUCCESS: exactly 2 chars is the minimum valid")
        fun companyHasEnough() =
            assertNull(validator.validateCompanyName("PT"))
    }

    // -- COMPANY ADDRESS ------------------------------------------------------─
    @Nested
    @DisplayName("Company Address Validation")
    inner class CompanyAddressValidation {

        @Test @DisplayName("SUCCESS: valid address returns null")
        fun addressValid() =
            assertNull(validator.validateCompanyAddress("Jl. Sudirman No. 1"))

        @Test @DisplayName("FAILURE: blank returns required error")
        fun addressInvalid() =
            assertNotNull(validator.validateCompanyAddress(""))

        @Test @DisplayName("FAILURE: less than 5 chars returns short error")
        fun addressToShort() =
            assertNotNull(validator.validateCompanyAddress("Jl."))
    }

    // -- CITY NAME ---------------------------------------------------------------------------------------------
    @Nested
    @DisplayName("City Name Validation")
    inner class CityNameValidation {

        @Test @DisplayName("SUCCESS: valid city returns null")
        fun cityValid() =
            assertNull(validator.validateCityName("Jakarta"))

        @Test @DisplayName("FAILURE: blank returns required error")
        fun cityBlank() =
            assertNotNull(validator.validateCityName(""))

        @Test @DisplayName("FAILURE: city with digits returns letters-only error")
        fun cityHasDigits() =
            assertEquals("City name must contain only letters",
                validator.validateCityName("Jakarta1"))

        @Test @DisplayName("SUCCESS: city with spaces is valid")
        fun cityHasWS() =
            assertNull(validator.validateCityName("Kota Bogor"))
    }

    // -- PHONE NUMBER -------------------------------------------------------------------------------------------
    @Nested
    @DisplayName("Phone Number Validation")
    inner class PhoneNumberValidation {

        @Test @DisplayName("SUCCESS: valid phone returns null")
        fun phoneValid() =
            assertNull(validator.validatePhoneNumber("08123456789"))

        @Test @DisplayName("FAILURE: blank returns required error")
        fun phoneBlank() =
            assertNotNull(validator.validatePhoneNumber(""))

        @Test @DisplayName("FAILURE: 7 digits is too short")
        fun phoneNotEnoughDigits() =
            assertNotNull(validator.validatePhoneNumber("1234567"))

        @Test @DisplayName("FAILURE: 16 digits is too long")
        fun phoneHaveTooManyDigits() =
            assertNotNull(validator.validatePhoneNumber("1234567890123456"))

        @Test @DisplayName("FAILURE: phone with letters returns digit-only error")
        fun phoneHaveNotNumber() =
            assertEquals("Phone number must contain digits only",
                validator.validatePhoneNumber("0812abc456"))

        @Test @DisplayName("SUCCESS: 8 digits is the minimum valid")
        fun phoneHasMinDigits() =
            assertNull(validator.validatePhoneNumber("12345678"))
    }

    // -- NPWP ---------------------------------------------------------------------------------------------------
    @Nested
    @DisplayName("NPWP Validation")
    inner class NpwpValidation {

        @Test @DisplayName("SUCCESS: blank NPWP returns null (optional)")
        fun npwpBlank() =
            assertNull(validator.validateNpwp(""))

        @Test @DisplayName("SUCCESS: exactly 16 digits returns null")
        fun npwpValid() =
            assertNull(validator.validateNpwp("1234567890123456"))

        @Test @DisplayName("FAILURE: 15 digits is too short")
        fun npwpHaveNotEnoughDigits() =
            assertEquals(
                "NPWP must be exactly ${OccupationValidator.NPWP_LENGTH} digits",
                validator.validateNpwp("123456789012345")
            )

        @Test @DisplayName("FAILURE: 17 digits is too long")
        fun npwpHaveTooManyDigits() =
            assertNotNull(validator.validateNpwp("12345678901234567"))

        @Test @DisplayName("FAILURE: non-digit NPWP returns digit-only error")
        fun npwpHaveNotNumber() =
            assertEquals("NPWP must contain digits only",
                validator.validateNpwp("123456789012345A"))
    }

    // -- VALIDATE ALL -------------------------------------------------------------------------------------------
    @Nested
    @DisplayName("validateAll – Full Form")
    inner class ValidateAllTests {

        @Test @DisplayName("SUCCESS: complete valid input has no errors")
        fun inputValid() {
            val input = OccupationFormInput(
                companyName    = "PT Bank Nasional",
                companyAddress = "Jl. Sudirman No. 1, Jakarta",
                cityName       = "Jakarta",
                phoneNumber    = "08123456789",
                npwp           = ""   // optional and blank = valid
            )
            val errors = validator.validateAll(input)
            assertFalse(errors.hasErrors)
            assertNull(errors.companyNameError)
            assertNull(errors.companyAddressError)
            assertNull(errors.cityNameError)
            assertNull(errors.phoneNumberError)
            assertNull(errors.npwpError)
        }

        @Test @DisplayName("FAILURE: empty input produces errors on all mandatory fields")
        fun inputNotValid() {
            val errors = validator.validateAll(OccupationFormInput())
            assertTrue(errors.hasErrors)
            assertNotNull(errors.companyNameError)
            assertNotNull(errors.companyAddressError)
            assertNotNull(errors.cityNameError)
            assertNotNull(errors.phoneNumberError)
            assertNull(errors.npwpError) // optional + blank = still no error
        }

        @Test @DisplayName("FAILURE: valid mandatory fields + invalid NPWP produces one error")
        fun inputHasError() {
            val input = OccupationFormInput(
                companyName    = "PT Bank Nasional",
                companyAddress = "Jl. Sudirman No. 1",
                cityName       = "Jakarta",
                phoneNumber    = "08123456789",
                npwp           = "12345" // too short
            )
            val errors = validator.validateAll(input)
            assertTrue(errors.hasErrors)
            assertNull(errors.companyNameError)
            assertNull(errors.companyAddressError)
            assertNull(errors.cityNameError)
            assertNull(errors.phoneNumberError)
            assertNotNull(errors.npwpError) // only NPWP has an error
        }
    }
}