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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import com.example.kodydomofonowe.AppContent







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
fun AppContent(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var address by remember { mutableStateOf(TextFieldValue("")) }
    var foundCode by remember { mutableStateOf<List<DomofonCode>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchFinished by remember { mutableStateOf(false) }

    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var lastQuery by remember { mutableStateOf("") }


    LaunchedEffect(address.text) {
        val currentQuery = address.text.normalizePolish()

        if (currentQuery.length < 3) {
            foundCode = emptyList()
            isSearching = false
            hasSearched = false
            lastQuery = ""
            return@LaunchedEffect
        }

        delay(500) // ⏱️ zawsze czekamy przed przeszukiwaniem

        // ✅ Jeśli obecne wyniki nadal pasują – nie przeszukuj ponownie
        if (
            foundCode.isNotEmpty()
            && foundCode.all { it.address.normalizePolish().contains(currentQuery) }
        ) {
            lastQuery = currentQuery
            return@LaunchedEffect
        }

        // 📚 Dopiero teraz czytamy Excel
        isSearching = true
        hasSearched = false

        val allCodes = readCodesFromExcelFile(context)
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

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarWithMenu(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onImportClick = {
                    pickFileLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
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
            Spacer(modifier = Modifier.height(0.dp))

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithMenu(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onImportClick: () -> Unit
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
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                        },
                        text = { Text("Importuj plik Excel") },
                        onClick = {
                            onImportClick()
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
                }
            }
        }
    )
}









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
