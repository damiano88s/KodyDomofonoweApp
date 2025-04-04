package com.example.kodydomofonowe

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.compose.ui.platform.LocalContext
import com.example.kodydomofonowe.ui.theme.MyTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TextField
import androidx.compose.animation.Crossfade


data class DomofonCode(val address: String, val code: String)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val sharedPreferences = getSharedPreferences("appPreferences", Context.MODE_PRIVATE)
            val isDarkTheme = sharedPreferences.getBoolean("isDarkTheme", false)
            var currentTheme by remember { mutableStateOf(isDarkTheme) }

            val onThemeToggle: (Boolean) -> Unit = { newTheme ->
                currentTheme = newTheme
                sharedPreferences.edit().putBoolean("isDarkTheme", newTheme).apply()
            }

            // 👉 Płynne przejście między trybami
            Crossfade(targetState = currentTheme) { isDark ->
                MyTheme(darkTheme = isDark) {
                    AppContent(
                        isDarkTheme = isDark,
                        onThemeToggle = onThemeToggle
                    )
                }
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

    LaunchedEffect(address.text) {
        if (address.text.isBlank()) {
            foundCode = emptyList()
        } else if (address.text.length >= 3) {
            val allCodes = readCodesFromExcelFile(context)
            foundCode = allCodes.filter {
                it.address.normalizePolish().contains(address.text.normalizePolish())
            }
        }
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

            TextField(
                value = address,
                onValueChange = { address = it },
                placeholder = {
                    Text("Wpisz nazwe ulicy", color = colorScheme.onSurface.copy(alpha = 0.5f))
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 20.sp,
                    color = colorScheme.onSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = colorScheme.primary,
                    unfocusedIndicatorColor = colorScheme.primary.copy(alpha = 0.5f),
                    cursorColor = colorScheme.primary,
                    focusedContainerColor = colorScheme.surface,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                ),
                modifier = Modifier
                    .width(250.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (foundCode.isNotEmpty()) {
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
                } else if (address.text.length >= 3) {
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
        modifier = Modifier.height(60.dp),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = colorScheme.surface,
            titleContentColor = colorScheme.onSurface
        ),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kody domofonowe",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    text = "MTBS / ZNT",
                    fontSize = 16.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        },
        actions = {
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Importuj plik Excel") },
                        onClick = {
                            onImportClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tryb jasny") },
                        onClick = {
                            onThemeToggle(false)
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
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
