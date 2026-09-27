package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.model.TitleStatusCategory
import com.example.data.pdf.PdfReportGenerator
import com.example.data.remote.NhtsaApiService
import com.example.data.remote.NhtsaRecallsApiService
import com.example.data.remote.VinHistoryRepository
import com.example.ui.VinViewModel
import com.example.ui.components.*
import com.example.ui.history.SearchHistorySheet
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val nhtsaApi = NhtsaApiService.create()
        val recallsApi = NhtsaRecallsApiService.create()
        val repository = VinHistoryRepository(nhtsaApi, recallsApi, database.vinSearchDao())
        val viewModelFactory = VinViewModel.Factory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: VinViewModel = viewModel(factory = viewModelFactory)
                VinCheckApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VinCheckApp(
    viewModel: VinViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Handle snackbar messages
    LaunchedEffect(uiState.showSuccessSnackbar) {
        uiState.showSuccessSnackbar?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("vin_check_main_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_car_vin_logo_1790486654721),
                            contentDescription = "لوگوی برنامه",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VIN Check Pro",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "استعلام جامع اصالت و سوابق خودرو",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // History button with badge
                    BadgedBox(
                        badge = {
                            if (searchHistory.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("${searchHistory.size}")
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.setHistorySheetOpen(true) },
                            modifier = Modifier.testTag("top_bar_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "سوابق جستجو"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // VIN Input & Sample Buttons Card
                VinInputCard(
                    vin = uiState.queryVin,
                    isLoading = uiState.isLoading,
                    onVinChange = { viewModel.onVinChanged(it) },
                    onSearch = { viewModel.searchVin() }
                )
            }

            // Error Message Card
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("error_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Loading status bar
            if (uiState.isLoading) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "در حال تجمیع اطلاعات از مراجع رسمی و حراجی‌ها...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "NHTSA VPIC • Copart Salvage • IAAI Archive • NMVTIS Title Brands",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Vehicle Report Content
            uiState.currentReport?.let { report ->
                // 1. Vehicle Summary & PDF Export Toolbar Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_summary_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Title Status Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (report.titleStatusCategory) {
                                        TitleStatusCategory.CLEAN -> Color(0xFFD1FAE5)
                                        TitleStatusCategory.SALVAGE -> MaterialTheme.colorScheme.errorContainer
                                        TitleStatusCategory.WATER_FLOOD -> Color(0xFFE0F2FE)
                                        TitleStatusCategory.REBUILT -> Color(0xFFEDE9FE)
                                        TitleStatusCategory.JUNK_SCRAP -> Color(0xFFFEE2E2)
                                    }
                                ) {
                                    Text(
                                        text = report.titleDisplayBadge,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (report.titleStatusCategory) {
                                            TitleStatusCategory.CLEAN -> Color(0xFF065F46)
                                            TitleStatusCategory.SALVAGE -> MaterialTheme.colorScheme.onErrorContainer
                                            TitleStatusCategory.WATER_FLOOD -> Color(0xFF0369A1)
                                            TitleStatusCategory.REBUILT -> Color(0xFF6D28D9)
                                            TitleStatusCategory.JUNK_SCRAP -> Color(0xFF991B1B)
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VIN: ${report.vin}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(report.vin))
                                            Toast.makeText(context, "شماره VIN کپی شد", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "کپی VIN",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${report.year} ${report.make} ${report.model}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = report.titleSummary,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // PDF Action Buttons Row
                            Text(
                                text = "گزارش چاپی و اسناد رسمی (PDF Export):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Open / View PDF
                                Button(
                                    onClick = {
                                        viewModel.generatePdf(context) { file ->
                                            PdfReportGenerator.openPdf(context, file)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_pdf_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !uiState.isGeneratingPdf
                                ) {
                                    if (uiState.isGeneratingPdf) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("مشاهده PDF", fontSize = 12.sp)
                                }

                                // 2. Print Directly
                                FilledTonalButton(
                                    onClick = {
                                        viewModel.generatePdf(context) { file ->
                                            PdfReportGenerator.printPdf(context, file, "VIN_Report_${report.vin}")
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("print_pdf_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("چاپ", fontSize = 12.sp)
                                }

                                // 3. Share PDF
                                FilledTonalButton(
                                    onClick = {
                                        viewModel.generatePdf(context) { file ->
                                            PdfReportGenerator.sharePdf(context, file, report.vin)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("share_pdf_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("اشتراک", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 2. Accident & Salvage Photos Gallery
                item {
                    AuctionPhotosGallery(
                        photos = report.damagePhotos,
                        auctionRecord = report.auctionRecords.firstOrNull(),
                        titleStatus = report.titleStatusCategory,
                        onPhotoClick = { photo -> viewModel.setPhotoPreview(photo) }
                    )
                }

                // 3. Copart / IAAI Auction Damage Record (if available)
                if (report.auctionRecords.isNotEmpty()) {
                    item {
                        SalvageHistoryCard(auctionRecord = report.auctionRecords.first())
                    }
                }

                // 4. NMVTIS Title Brands Audit
                item {
                    TitleBrandsCard(brands = report.titleBrands)
                }

                // 5. Official NHTSA Technical Specs
                item {
                    VehicleSpecsCard(report = report)
                }

                // 6. Active Safety Recalls
                item {
                    RecallsCard(recalls = report.recalls)
                }

                // 7. Multi-Source Cross-Check Directory (Copart, IAAI, BidFax, stat.vin, CARFAX, etc.)
                item {
                    ExternalSourcesCard(sources = report.externalSources)
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Full-screen photo zoom dialog
    uiState.selectedPhotoForPreview?.let { photo ->
        FullScreenPhotoDialog(
            photo = photo,
            onDismiss = { viewModel.setPhotoPreview(null) }
        )
    }

    // Search History Bottom Sheet
    if (uiState.isHistorySheetOpen) {
        SearchHistorySheet(
            historyList = searchHistory,
            onSelect = { entity -> viewModel.loadFromHistory(entity) },
            onToggleFavorite = { vin, isFav -> viewModel.toggleFavorite(vin, isFav) },
            onDelete = { vin -> viewModel.deleteHistoryItem(vin) },
            onClearAll = { viewModel.clearAllHistory() },
            onDismiss = { viewModel.setHistorySheetOpen(false) }
        )
    }
}
