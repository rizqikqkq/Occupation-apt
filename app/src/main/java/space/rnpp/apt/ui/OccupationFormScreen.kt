package space.rnpp.apt.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import space.rnpp.apt.model.CompanySuggestion
import space.rnpp.apt.model.OccupationFormUiState
import space.rnpp.apt.ui.theme.*
import space.rnpp.apt.util.OccupationValidator
import space.rnpp.apt.viewmodel.OccupationFormViewModel


private val FieldCorner  = RoundedCornerShape(12.dp)

@Composable
fun OccupationFormScreen(
    vm: OccupationFormViewModel = viewModel(),
    onBackPressed: () -> Unit = {},
    systemPadding: PaddingValues = PaddingValues(0.dp)
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    OccupationFormContent(
        uiState                  = uiState,
        systemPadding            = systemPadding,
        onBackPressed            = onBackPressed,
        onCompanyNameChanged     = { value      -> vm.onCompanyNameChanged(value) },
        onCompanyNameCleared     = {               vm.onCompanyNameChanged("") },
        onSuggestionSelected     = { suggestion -> vm.onCompanySuggestionSelected(suggestion) },
        onDismissSuggestions     = {               vm.onDismissSuggestions() },
        onCompanyAddressChanged  = { value      -> vm.onCompanyAddressChanged(value) },
        onCityNameChanged        = { value      -> vm.onCityNameChanged(value) },
        onPhoneNumberChanged     = { value      -> vm.onPhoneNumberChanged(value) },
        onNpwpChanged            = { value      -> vm.onNpwpChanged(value) },
        onNext                   = {               vm.onNext() }
    )
}

@Composable
private fun OccupationFormContent(
    uiState:                 OccupationFormUiState,
    systemPadding:           PaddingValues,
    onBackPressed:           () -> Unit,
    onCompanyNameChanged:    (String) -> Unit,
    onCompanyNameCleared:    () -> Unit,
    onSuggestionSelected:    (CompanySuggestion) -> Unit,
    onDismissSuggestions:    () -> Unit,
    onCompanyAddressChanged: (String) -> Unit,
    onCityNameChanged:       (String) -> Unit,
    onPhoneNumberChanged:    (String) -> Unit,
    onNpwpChanged:           (String) -> Unit,
    onNext:                  () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = systemPadding.calculateTopPadding())
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint               = TextBlack,
                    modifier           = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text       = "",
                fontSize   = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextBlack
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = "Di mana kamu bekerja?",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = TextBlack,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text       = "Isi detail tempat kerja kamu, termasuk nama resmi, " +
                             "alamat, dan nomor teleponnya.",
                fontSize   = 13.sp,
                color      = SubtitleGray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            CompanyNameField(
                value                = uiState.input.companyName,
                onValueChange        = onCompanyNameChanged,
                onCleared            = onCompanyNameCleared,
                error                = uiState.errors.companyNameError,
                isValid              = uiState.fieldValid.companyNameValid,
                suggestions          = uiState.companySuggestions,
                showSuggestions      = uiState.showSuggestions,
                onSuggestionSelected = onSuggestionSelected,
                onDismiss            = onDismissSuggestions
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomInputField(
                value         = uiState.input.companyAddress,
                onValueChange = onCompanyAddressChanged,
                placeholder   = "Alamat",
                hint          = "Contoh: Grha BNI Jl. Jend Sudirman No.1",
                error         = uiState.errors.companyAddressError,
                isValid       = uiState.fieldValid.companyAddressValid,
                singleLine    = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomInputField(
                value         = uiState.input.cityName,
                onValueChange = onCityNameChanged,
                placeholder   = "Kota/kabupaten",
                hint          = null,
                error         = uiState.errors.cityNameError,
                isValid       = uiState.fieldValid.cityNameValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            PhoneField(
                value         = uiState.input.phoneNumber,
                onValueChange = onPhoneNumberChanged,
                error         = uiState.errors.phoneNumberError,
                isValid       = uiState.fieldValid.phoneNumberValid
            )

            Spacer(modifier = Modifier.height(16.dp))

            NpwpField(
                value         = uiState.input.npwp,
                onValueChange = onNpwpChanged,
                error         = uiState.errors.npwpError,
                isValid       = uiState.fieldValid.npwpValid
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = systemPadding.calculateBottomPadding())
        ) {
            Button(
                onClick  = onNext,
                enabled  = uiState.isNextEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = Purple10,
                    disabledContainerColor = Color(0xFFE0E0E0)
                )
            ) {
                Text(
                    text       = "Lanjut",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (uiState.isNextEnabled) Color.White else HintGray
                )
            }
        }
    }
}

//region Components
@Composable
private fun NpwpField(
    value:         String,
    onValueChange: (String) -> Unit,
    error:         String?,
    isValid:       Boolean
) {
    val maxLength   = OccupationValidator.NPWP_LENGTH
    val currentLen  = value.length
    val strokeColor = when {
        error != null -> ErrorRed
        isValid       -> ValidGreen
        else          -> BorderGray
    }

    Column {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,   // plain passthrough — ViewModel enforces the cap
            placeholder     = { Text("NPWP (opsional)", color = HintGray, fontSize = 14.sp) },
            isError         = error != null,
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier        = Modifier.fillMaxWidth(),
            shape           = FieldCorner,
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = if (isValid) ValidGreen else Purple10,
                unfocusedBorderColor    = strokeColor,
                errorBorderColor        = ErrorRed,
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor     = Color.White,
                cursorColor             = Purple10
            )
        )

        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (error != null) {
                    Text(text = error, fontSize = 12.sp, color = ErrorRed)
                } else {
                    Text(
                        text       = "Sesuai aturan pemerintah, NIK dipakai untuk NPWP. " +
                                     "Kalau belum dipadanin, lakukan segera setelah registrasi ya!",
                        fontSize   = 12.sp,
                        color      = HintGray,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text       = "$currentLen/$maxLength",
                fontSize   = 12.sp,
                color      = when {
                    error != null           -> ErrorRed
                    currentLen == maxLength -> ValidGreen
                    else                    -> HintGray
                },
                fontWeight = if (currentLen == maxLength) FontWeight.SemiBold
                             else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun CompanyNameField(
    value:                String,
    onValueChange:        (String) -> Unit,
    onCleared:            () -> Unit,
    error:                String?,
    isValid:              Boolean,
    suggestions:          List<CompanySuggestion>,
    showSuggestions:      Boolean,
    onSuggestionSelected: (CompanySuggestion) -> Unit,
    onDismiss:            () -> Unit
) {
    val strokeColor = when {
        error != null -> ErrorRed
        isValid       -> ValidGreen
        else          -> BorderGray
    }

    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                placeholder   = { Text("Nama tempat kerja", color = HintGray, fontSize = 14.sp) },
                trailingIcon  = if (value.isNotEmpty()) {
                    {
                        IconButton(onClick = onCleared) {
                            Icon(
                                imageVector        = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint               = HintGray,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                    }
                } else null,
                isError       = error != null,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = FieldCorner,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = if (isValid) ValidGreen else Purple10,
                    unfocusedBorderColor    = strokeColor,
                    errorBorderColor        = ErrorRed,
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorContainerColor     = Color.White,
                    cursorColor             = Purple10
                )
            )

            // DropdownMenu anchored to the Box
            DropdownMenu(
                expanded         = showSuggestions && suggestions.isNotEmpty(),
                onDismissRequest = onDismiss,
                modifier         = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text    = { Text(suggestion.name, fontSize = 14.sp, color = TextBlack) },
                        onClick = { onSuggestionSelected(suggestion) }
                    )
                }
            }
        }

        Text(
            text     = "Contoh: Bank Negara Indonesia",
            fontSize = 12.sp,
            color    = HintGray,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )

        AnimatedVisibility(visible = error != null) {
            Text(
                text     = error ?: "",
                fontSize = 12.sp,
                color    = ErrorRed,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun PhoneField(
    value:         String,
    onValueChange: (String) -> Unit,
    error:         String?,
    isValid:       Boolean
) {
    var expanded     by remember { mutableStateOf(false) }
    var selectedCode by remember { mutableStateOf("+62") }
    val countryCodes = listOf("+62", "+1", "+44", "+81", "+65")

    val strokeColor = when {
        error != null -> ErrorRed
        isValid       -> ValidGreen
        else          -> BorderGray
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.5.dp, color = strokeColor, shape = FieldCorner)
                .background(Color.White, FieldCorner),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "($selectedCode)",
                        fontSize   = 14.sp,
                        color      = TextBlack,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector        = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint               = TextBlack,
                        modifier           = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false },
                    modifier         = Modifier.background(Color.White)
                ) {
                    countryCodes.forEach { code ->
                        DropdownMenuItem(
                            text    = { Text(code, fontSize = 14.sp) },
                            onClick = { selectedCode = code; expanded = false }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(BorderGray)
            )

            OutlinedTextField(
                value           = value,
                onValueChange   = onValueChange,
                placeholder     = { Text("Nomor telepon", color = HintGray, fontSize = 14.sp) },
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.weight(1f),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = Color.Transparent,
                    unfocusedBorderColor    = Color.Transparent,
                    errorBorderColor        = Color.Transparent,
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor     = Color.Transparent,
                    cursorColor             = Purple10
                )
            )
        }

        AnimatedVisibility(visible = error != null) {
            Text(
                text     = error ?: "",
                fontSize = 12.sp,
                color    = ErrorRed,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
@Composable
private fun CustomInputField(
    value:         String,
    onValueChange: (String) -> Unit,
    placeholder:   String,
    hint:          String?,
    error:         String?      = null,
    isValid:       Boolean      = false,
    keyboardType:  KeyboardType = KeyboardType.Text,
    singleLine:    Boolean      = true
) {
    val strokeColor = when {
        error != null -> ErrorRed
        isValid       -> ValidGreen
        else          -> BorderGray
    }

    Column {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            placeholder     = { Text(placeholder, color = HintGray, fontSize = 14.sp) },
            isError         = error != null,
            singleLine      = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier        = Modifier.fillMaxWidth(),
            shape           = FieldCorner,
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = if (isValid) ValidGreen else Purple10,
                unfocusedBorderColor    = strokeColor,
                errorBorderColor        = ErrorRed,
                focusedContainerColor   = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor     = Color.White,
                cursorColor             = Purple10
            )
        )

        if (hint != null) {
            Text(
                text       = hint,
                fontSize   = 12.sp,
                color      = HintGray,
                lineHeight = 16.sp,
                modifier   = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }

        AnimatedVisibility(visible = error != null) {
            Text(
                text     = error ?: "",
                fontSize = 12.sp,
                color    = ErrorRed,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}

//endregion