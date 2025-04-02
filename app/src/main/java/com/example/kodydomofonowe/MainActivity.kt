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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class DomofonCode(val address: String, val code: String)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @OptIn(ExperimentalMaterial3Api::class)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val context = this

            var address by remember { mutableStateOf(TextFieldValue("")) }
            var foundCode by remember { mutableStateOf<List<DomofonCode>>(emptyList()) }

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

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Kody domofonowe",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "MTBS / ZNT",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 22.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Wpisz adres:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, bottom = 4.dp)
                    )

                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Ulica i numer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val allCodes = readCodesFromExcelFile(context)
                            foundCode = allCodes.filter {
                                it.address.normalizePolish().contains(address.text.normalizePolish())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        Text("Szukaj kodu")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { pickFileLauncher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    ) {
                        Text("Importuj plik Excel")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (foundCode.isNotEmpty()) {
                        Column {
                            foundCode.forEach { item ->
                                Text(
                                    text = item.address,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text("kod: ${item.code}", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        Text("Brak wyników")
                    }
                }
            }
        }
    }
}

// 🔄 Normalizacja znaków dla wyszukiwania
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

// 📥 Import pliku Excel
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

// 📖 Czytanie danych z pliku Excel
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
            Log.d("READ_EXCEL", "Wczytano: $address - $code")
            result.add(DomofonCode(address, code))
        }
        workbook.close()
    } catch (e: Exception) {
        Log.e("READ_EXCEL", "Błąd podczas odczytu: ${e.message}")
    }
    return result
}
