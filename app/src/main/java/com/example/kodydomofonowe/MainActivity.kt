package com.example.kodydomofonowe

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kodydomofonowe.theme.ui.AppTheme
import com.example.kodydomofonowe.ui.theme.MyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream

import androidx.compose.material.icons.filled.FileDownload
import android.os.Environment
import java.io.File
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import com.example.kodydomofonowe.ShowExportDialog
import org.apache.poi.ss.usermodel.Sheet




import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.io.FileOutputStream
import com.example.kodydomofonowe.AppContent


import androidx.compose.ui.platform.LocalContext


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch



fun String.normalizePolish(): String {
    return this.lowercase()
        .replace("ą", "a")
        .replace("ć", "c")
        .replace("ę", "e")
        .replace("ł", "l")
        .replace("ń", "n")
        .replace("ó", "o")
        .replace("ś", "s")
        .replace("ź", "z")
        .replace("ż", "z")
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyTheme(darkTheme = isSystemInDarkTheme()) {
                MainAppWithTheme() // <- to używamy!
            }
        }
    }
}




        data class DomofonCode(val address: String, val code: String)

@Composable
fun MainAppWithTheme() {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("appPreferences", Context.MODE_PRIVATE) }

    var isDarkTheme by remember {
        mutableStateOf(sharedPreferences.getBoolean("isDarkTheme", false))
    }

    val onThemeToggle: (Boolean) -> Unit = { newTheme ->
        isDarkTheme = newTheme
        sharedPreferences.edit().putBoolean("isDarkTheme", newTheme).apply()
    }

    Crossfade(targetState = isDarkTheme, label = "theme") { isDark ->
        AppTheme(darkTheme = isDark) {
            Surface(color = MaterialTheme.colorScheme.background) {
                AppContent(
                    isDarkTheme = isDark,
                    onThemeToggle = onThemeToggle
                )
            }
        }
    }
}



@Composable
fun ResponsiveText(
    text: String,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight? = null,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}




fun importExcelFile(uri: Uri, context: Context, onImportFinished: () -> Unit) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val destinationFile = File(context.filesDir, "kody_domofonowe.xlsx")
        if (inputStream == null) {
            Log.e("IMPORT_EXCEL", "InputStream jest nullem")
            return
        }
        destinationFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        Log.d("IMPORT_EXCEL", "Plik został zapisany do: ${destinationFile.absolutePath}")
        onImportFinished()
    } catch (e: Exception) {
        Log.e("IMPORT_EXCEL", "Błąd podczas importu: ${e.message}")
    }
}

fun saveNewCodeToExcel(context: Context, address: String, code: String): Boolean {
    return try {
        val file = File(context.filesDir, "nowe_kody.xlsx")
        val workbook: XSSFWorkbook
        val sheet: Sheet

        if (file.exists()) {
            val inputStream = FileInputStream(file)
            workbook = XSSFWorkbook(inputStream)
            sheet = workbook.getSheetAt(0)
            inputStream.close()
        } else {
            workbook = XSSFWorkbook()
            sheet = workbook.createSheet("Kody")

            // Dodaj nagłówek tylko raz
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Adres")
            headerRow.createCell(1).setCellValue("Kod")
        }

        // Dodaj nowy wiersz
        val newRow = sheet.createRow(sheet.lastRowNum + 1)
        newRow.createCell(0).setCellValue(address)
        newRow.createCell(1).setCellValue(code)

        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)
        outputStream.close()
        workbook.close()

        true
    } catch (e: Exception) {
        Log.e("SAVE_NEW_CODE", "Błąd zapisu: ${e.message}")
        false
    }
}



fun copyFileToDownloads(context: Context, sourceFile: File): Boolean {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val targetFile = File(downloadsDir, sourceFile.name)
        sourceFile.copyTo(targetFile, overwrite = true)
        true
    } catch (e: Exception) {
        Log.e("EXPORT_EXCEL", "Błąd podczas kopiowania pliku: ${e.message}")
        false
    }
}










fun readCodesFromExcelFile(context: Context): List<DomofonCode> {
    val result = mutableListOf<DomofonCode>()
    try {
        val file = File(context.filesDir, "kody_domofonowe.xlsx")
        if (!file.exists()) return result
        val inputStream = FileInputStream(file)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)


        for (row in sheet) {
            if (row.rowNum == 0) continue


            val address = row.getCell(0)?.stringCellValue ?: continue
            val code = row.getCell(1)?.stringCellValue ?: continue
            Log.d("EXCEL", "Wczytano: $address -> $code")


            result.add(DomofonCode(address, code))
        }
        workbook.close()
    } catch (e: Exception) {
        Log.e("READ_EXCEL", "Błąd podczas odczytu: ${e.message}")
    }
    return result
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCodeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme
    var isSaving by remember { mutableStateOf(false) }

    var address by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj nowy kod") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface,
                    navigationIconContentColor = colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Adres") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Kod domofonu") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            var isSaving by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    isSaving = true
                    coroutineScope.launch {
                        val success = saveNewCodeToExcel(context, address, code)
                        if (success) {
                            snackbarHostState.showSnackbar("Kod został zapisany")
                            address = ""
                            code = ""
                        } else {
                            snackbarHostState.showSnackbar("Błąd przy zapisie")
                        }
                        isSaving = false
                    }
                },
                enabled = address.isNotBlank() && code.isNotBlank() && !isSaving,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Zapisz") // nie zmieniamy napisu
            }










        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    Log.d("SPRAWDZ_PLIK", "Plik istnieje: ${File(context.filesDir, "kody_domofonowe.xlsx").exists()}")

    var address by remember { mutableStateOf(TextFieldValue("")) }
    var foundCode by remember { mutableStateOf<List<DomofonCode>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchFinished by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var lastQuery by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var isAddCodeScreenVisible by remember { mutableStateOf(false) }

    val pickFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                importExcelFile(it, context) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Plik został dodany")
                    }
                }
            }
        }
    LaunchedEffect(address.text) {
        val currentQuery = address.text.normalizePolish()

        if (currentQuery.length < 3) {
            foundCode = emptyList()
            isSearching = false
            hasSearched = false
            lastQuery = ""
            return@LaunchedEffect
        }

        delay(300)

        isSearching = true
        hasSearched = false

        val allCodes = readCodesFromExcelFile(context)
        Log.d("EXCEL", "Wczytano ${allCodes.size} kodów")

        foundCode = allCodes.filter { domofonCode ->
            domofonCode.address
                .normalizePolish()
                .split(" ")
                .any { it.startsWith(currentQuery) }
        }

        lastQuery = currentQuery
        isSearching = false
        hasSearched = true
    }


    val colorScheme = MaterialTheme.colorScheme

    if (isAddCodeScreenVisible) {
        AddCodeScreen(onBack = { isAddCodeScreenVisible = false })
    } else {
        Scaffold(
            containerColor = colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBarWithMenu(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    onImportClick = {
                        pickFileLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    },
                    onExportClick = {
                        showExportDialog = true
                    },
                    onAddCodeClick = {
                        isAddCodeScreenVisible = true
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                val searchUnderlineColor = if (isDarkTheme) Color(0xFFFF9800) else colorScheme.primary


                TextField(
                    value = address,
                    onValueChange = { address = it },
                    placeholder = {
                        Text(
                            "Wpisz nazwę ulicy",
                            color = colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        color = colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = searchUnderlineColor,
                        unfocusedIndicatorColor = searchUnderlineColor.copy(alpha = 0.5f),
                        cursorColor = searchUnderlineColor,
                        focusedContainerColor = colorScheme.background,
                        unfocusedContainerColor = colorScheme.background,
                        focusedTextColor = colorScheme.onSurface,
                        unfocusedTextColor = colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isSearching) {
                        // nic nie pokazujemy podczas ładowania
                    } else if (foundCode.isNotEmpty()) {
                        foundCode.forEach { item ->
                            Text(
                                text = item.address,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "kod: ${item.code}",
                                fontSize = 22.sp,
                                color = colorScheme.onSurface.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else if (hasSearched && !isSearching) {
                        Text(
                            "Brak wyników",
                            modifier = Modifier.padding(top = 20.dp),
                            color = colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ShowExportDialog(
            context = context,
            onDismiss = { showExportDialog = false },
            onFileSelected = { selectedFile ->
                val success = copyFileToDownloads(context, selectedFile)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (success)
                            "Plik ${selectedFile.name} zapisany w folderze Pobrane"
                        else
                            "Błąd podczas eksportu pliku"
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithMenu(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onAddCodeClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    CenterAlignedTopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorScheme.background,
            titleContentColor = colorScheme.onBackground
        ),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 35.dp)
            ) {
                Text(
                    text = "Kody domofonowe",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)) {
                            append("MTBS")
                        }
                        append(" / ")
                        withStyle(style = SpanStyle(color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)) {
                            append("ZNT")
                        }
                    },
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            Box(
                modifier = Modifier
                    .padding(top = 30.dp, end = 4.dp)
            ) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.surface)
                        .padding(vertical = 4.dp)
                        .shadow(4.dp)
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        text = { Text("Dodaj kod") },
                        onClick = {
                            onAddCodeClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.WbSunny, contentDescription = null)
                        },
                        text = { Text("Tryb jasny") },
                        onClick = {
                            onThemeToggle(false)
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.DarkMode, contentDescription = null)
                        },
                        text = { Text("Tryb ciemny") },
                        onClick = {
                            onThemeToggle(true)
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                        },
                        text = { Text("Importuj plik Excel") },
                        onClick = {
                            onImportClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                        },
                        text = { Text("Eksportuj plik Excel") },
                        onClick = {
                            onExportClick()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun ShowExportDialog(
    context: Context,
    onDismiss: () -> Unit,
    onFileSelected: (File) -> Unit
) {
    val excelFiles = remember {
        context.filesDir
            .listFiles()
            ?.filter { it.name.endsWith(".xlsx") }
            ?.sortedBy { it.name }
            ?: emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Wybierz plik do eksportu") },
        text = {
            Column {
                excelFiles.forEach { file ->
                    TextButton(onClick = {
                        onFileSelected(file)
                        onDismiss()
                    }) {
                        Text(file.name)
                    }
                }
                if (excelFiles.isEmpty()) {
                    Text("Brak plików .xlsx do eksportu")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

