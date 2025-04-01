package com.example.kodydomofonowe

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import kotlinx.coroutines.launch
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.activity.compose.setContent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp



// DomofonCode - prosta klasa danych bez zależności Room
data class DomofonCode(val address: String, val code: String)

@OptIn(ExperimentalMaterial3Api::class)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @OptIn(ExperimentalMaterial3Api::class)


        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val coroutineScope = rememberCoroutineScope()
            val context = this

            var address by remember { mutableStateOf(TextFieldValue("")) }
            var code by remember { mutableStateOf(TextFieldValue("")) }
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

                    // Podtytuł sekcji
                    Text(
                        text = "Wpisz adres:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp, bottom = 4.dp)
                    )


                    Spacer(modifier = Modifier.height(8.dp))

                    // Pole tekstowe – adres
                    TextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Ulica i numer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Kod domofonu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { /* Dodaj kod */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dodaj kod")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = { /* Szukaj kodu */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Szukaj kodu")
                        }
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

    // Funkcja importująca plik Excel
    private fun importExcelFile(uri: Uri, context: Context, onImportFinished: () -> Unit) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val destinationFile = File(context.filesDir, "kody_domofonowe.xlsx")
            destinationFile.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            onImportFinished()
        } catch (e: Exception) {
            Log.e("IMPORT_EXCEL", "Błąd podczas importu: ${e.message}")
        }
    }

    // Funkcja odczytująca dane z pliku Excel
    private fun readCodesFromExcelFile(context: Context): List<DomofonCode> {
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
        Log.d("READ", "Wczytano ${result.size} rekordów")
        return result
    }

    // Funkcja zapisująca dane do pliku Excel
    private fun saveCodeToExcel(context: Context, address: String, code: String) {
        try {
            val file = File(context.filesDir, "kody_domofonowe.xlsx")
            val workbook = if (file.exists()) {
                XSSFWorkbook(FileInputStream(file))
            } else {
                XSSFWorkbook().apply {
                    createSheet("Sheet1").createRow(0).apply {
                        createCell(0).setCellValue("Adres")
                        createCell(1).setCellValue("Kod")
                    }
                }
            }

            val sheet = workbook.getSheetAt(0)
            val lastRowNum = sheet.lastRowNum
            val newRow: Row = sheet.createRow(lastRowNum + 1)
            newRow.createCell(0).setCellValue(address)
            newRow.createCell(1).setCellValue(code)

            val outputStream = FileOutputStream(file)
            workbook.write(outputStream)
            workbook.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("WRITE_EXCEL", "Błąd podczas zapisu: ${e.message}")
        }
    }
}
