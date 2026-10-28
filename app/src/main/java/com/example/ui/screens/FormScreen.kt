package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VerificationRecord
import com.example.ui.FormUiState
import com.example.ui.MainViewModel
import com.example.ui.components.BooleanToggleRow
import com.example.ui.components.DateTimePickerCard
import com.example.ui.components.FormInputField
import com.example.ui.components.FormSectionHeader
import com.example.ui.components.GpsLocationCard
import com.example.ui.components.MeterBoardPhotoCard
import com.example.ui.components.RemarksCard
import com.example.ui.components.ToggleSelectionRow
import com.example.ui.theme.KsebAmberSecondary
import com.example.ui.theme.KsebBluePrimary
import com.example.ui.theme.KsebGoodGreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FormScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (VerificationRecord) -> Unit,
    onNavigateToBlog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.validationError) {
        uiState.validationError?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.currentRecordId > 0) "Edit Field Verification" else "KSEB Field Verification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Electrical Estimate & Inspection Form",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToBlog,
                        modifier = Modifier.testTag("top_bar_my_blog_button")
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "My Blog", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.resetForm() },
                        modifier = Modifier.testTag("reset_form_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Reset Form", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.saveVerificationForm { record ->
                                onNavigateToDetail(record)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_and_preview_button")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Output")
                    }

                    Button(
                        onClick = {
                            viewModel.saveVerificationForm { record ->
                                onNavigateToDetail(record)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("save_and_download_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Export PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Official Notice Banner
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ElectricMeter, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Fill all field inspection parameters. Output data will be formatted according to KSEB standards and available for instant PDF download.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // SECTION 1: APPLICANT DETAILS
            item {
                FormSectionHeader(
                    number = "1",
                    title = "Applicant & Consumer Identity",
                    icon = Icons.Default.Badge
                )

                FormInputField(
                    value = uiState.applicantName,
                    onValueChange = viewModel::updateApplicantName,
                    label = "Applicant Name",
                    placeholder = "e.g. Manoj Kumar P.",
                    leadingIcon = Icons.Default.Badge,
                    isMandatory = true,
                    isError = uiState.validationError?.contains("Applicant Name", ignoreCase = true) == true,
                    errorMessage = "Applicant Name is required",
                    testTag = "input_applicant_name"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = uiState.applicationNo,
                    onValueChange = viewModel::updateApplicationNo,
                    label = "Application No",
                    placeholder = "e.g. KL-2024-EST-8842",
                    leadingIcon = Icons.Default.Tag,
                    testTag = "input_application_no"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = uiState.applicantContactNo,
                    onValueChange = viewModel::updateApplicantContactNo,
                    label = "Applicant Contact No",
                    placeholder = "e.g. 9847123456",
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    testTag = "input_contact_no"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // MANDATORY FIELD: Nearest Consumer Number
                FormInputField(
                    value = uiState.nearestConsumerNo,
                    onValueChange = viewModel::updateNearestConsumerNo,
                    label = "Nearest Consumer Number (Mandatory)",
                    placeholder = "e.g. 1155420087421 (13 digits)",
                    leadingIcon = Icons.Default.Bolt,
                    isMandatory = true,
                    isError = uiState.validationError?.contains("Nearest Consumer", ignoreCase = true) == true,
                    errorMessage = uiState.validationError ?: "Nearest Consumer Number is Mandatory",
                    keyboardType = KeyboardType.Number,
                    testTag = "input_nearest_consumer_no"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 2: STATUTORY CERTIFICATES
            item {
                FormSectionHeader(
                    number = "2",
                    title = "Statutory Certificates & Consents",
                    icon = Icons.Default.Assignment
                )

                BooleanToggleRow(
                    label = "Completion Certificate Submitted?",
                    value = uiState.completionCertificate,
                    onValueChange = viewModel::updateCompletionCertificate,
                    testTag = "toggle_completion_cert"
                )

                Spacer(modifier = Modifier.height(10.dp))

                BooleanToggleRow(
                    label = "Ownership Certificate Submitted?",
                    value = uiState.ownershipCertificate,
                    onValueChange = viewModel::updateOwnershipCertificate,
                    testTag = "toggle_ownership_cert"
                )

                Spacer(modifier = Modifier.height(10.dp))

                BooleanToggleRow(
                    label = "Is Any Other Consent Required?",
                    value = uiState.otherConsentRequired,
                    onValueChange = viewModel::updateOtherConsentRequired,
                    testTag = "toggle_other_consent"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 3: TECHNICAL & SAFETY VERIFICATION
            item {
                FormSectionHeader(
                    number = "3",
                    title = "Technical & Electrical Safety",
                    icon = Icons.Default.Security
                )

                ToggleSelectionRow(
                    label = "Internal Wiring Condition:",
                    option1 = "Good",
                    option2 = "Bad",
                    selectedOption = uiState.wiring,
                    onOptionSelected = viewModel::updateWiring,
                    testTag = "toggle_wiring"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ToggleSelectionRow(
                    label = "Earth Resistance:",
                    option1 = "Good",
                    option2 = "Bad",
                    selectedOption = uiState.earthResistance,
                    onOptionSelected = viewModel::updateEarthResistance,
                    testTag = "toggle_earth_resistance"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = uiState.rccbCompanyName,
                    onValueChange = viewModel::updateRccbCompanyName,
                    label = "RCCB Company Name / Specifications",
                    placeholder = "e.g. Legrand 30mA 40A / Havells / L&T",
                    leadingIcon = Icons.Default.Build,
                    testTag = "input_rccb_company"
                )

                Spacer(modifier = Modifier.height(10.dp))

                ToggleSelectionRow(
                    label = "Requirement Power:",
                    option1 = "Single Phase",
                    option2 = "Three Phase",
                    selectedOption = uiState.requirementPower,
                    onOptionSelected = viewModel::updateRequirementPower,
                    colorOption1 = MaterialTheme.colorScheme.primary,
                    colorOption2 = KsebAmberSecondary,
                    testTag = "toggle_requirement_power"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 4: NETWORK & CONNECTION SPECS
            item {
                FormSectionHeader(
                    number = "4",
                    title = "Network Parameters & Tariff",
                    icon = Icons.Default.Bolt
                )

                // Purpose of Connection with quick selection chips
                Column {
                    FormInputField(
                        value = uiState.purposeOfConnection,
                        onValueChange = viewModel::updatePurposeOfConnection,
                        label = "Purpose of the Electric Connection",
                        placeholder = "e.g. Domestic, Commercial, Agriculture",
                        leadingIcon = Icons.Default.Assignment,
                        testTag = "input_purpose"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Domestic", "Commercial", "Industrial", "Agriculture", "Construction", "General").forEach { purpose ->
                            val isSelected = uiState.purposeOfConnection.equals(purpose, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { viewModel.updatePurposeOfConnection(purpose) }
                            ) {
                                Text(
                                    text = purpose,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tariff with quick chips
                Column {
                    FormInputField(
                        value = uiState.tariff,
                        onValueChange = viewModel::updateTariff,
                        label = "Tariff",
                        placeholder = "e.g. LT-1A, LT-IV, LT-VIIA",
                        leadingIcon = Icons.Default.Tag,
                        testTag = "input_tariff"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("LT-1A (Domestic)", "LT-IV (Industrial)", "LT-VIIA (Commercial)", "LT-V (Agri)", "LT-VI (General)").forEach { tariffChip ->
                            val code = tariffChip.substringBefore(" ")
                            val isSelected = uiState.tariff.contains(code, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.clickable { viewModel.updateTariff(code) }
                            ) {
                                Text(
                                    text = tariffChip,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = uiState.poleNo,
                    onValueChange = viewModel::updatePoleNo,
                    label = "Pole Number",
                    placeholder = "e.g. KP-14/08",
                    leadingIcon = Icons.Default.Tag,
                    testTag = "input_pole_no"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = uiState.weatherproofWireLength,
                    onValueChange = viewModel::updateWeatherproofWireLength,
                    label = "Weatherproof Wire Length (in Meters)",
                    placeholder = "e.g. 28",
                    leadingIcon = Icons.Default.Build,
                    keyboardType = KeyboardType.Number,
                    testTag = "input_wire_length"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = uiState.transformerName,
                    onValueChange = viewModel::updateTransformerName,
                    label = "Transformer Name / Code",
                    placeholder = "e.g. Kadavanthra South 250kVA",
                    leadingIcon = Icons.Default.Bolt,
                    testTag = "input_transformer_name"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 5: FIELD EVIDENCE & GEOTAGGING
            item {
                FormSectionHeader(
                    number = "5",
                    title = "Field Verification Evidence",
                    icon = Icons.Default.LocationOn
                )

                // GPS Location
                GpsLocationCard(
                    gpsCoordinates = uiState.gpsCoordinates,
                    gpsAccuracy = uiState.gpsAccuracy,
                    isLoading = uiState.isGpsLoading,
                    onFetchGps = { viewModel.fetchGpsLocation(context) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Meter Board / Premises Photo
                MeterBoardPhotoCard(
                    photoUri = uiState.photoUri,
                    onPhotoSelected = viewModel::setPhotoUri
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date and Time
                DateTimePickerCard(
                    dateTime = uiState.dateTime,
                    onDateTimeChange = viewModel::updateDateTime
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // SECTION 6: OFFICER & STORAGE INTEGRATION
            item {
                FormSectionHeader(
                    number = "6",
                    title = "Estimate Officer & Google Drive",
                    icon = Icons.Default.CloudUpload
                )

                FormInputField(
                    value = uiState.estimatePreparedBy,
                    onValueChange = viewModel::updateEstimatePreparedBy,
                    label = "Estimate Prepared By",
                    placeholder = "e.g. Sudheesh Babu (Sub Engineer)",
                    leadingIcon = Icons.Default.Badge,
                    testTag = "input_prepared_by"
                )

                Spacer(modifier = Modifier.height(10.dp))

                FormInputField(
                    value = uiState.googleDriveLink,
                    onValueChange = viewModel::updateGoogleDriveLink,
                    label = "Google Drive Linking Option (URL/Folder)",
                    placeholder = "https://drive.google.com/drive/folders/...",
                    leadingIcon = Icons.Default.Cloud,
                    testTag = "input_drive_link"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Remarks with 500 word limit
                RemarksCard(
                    remarks = uiState.remarks,
                    onRemarksChange = viewModel::updateRemarks
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
