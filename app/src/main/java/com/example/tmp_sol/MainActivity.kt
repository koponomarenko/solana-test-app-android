package com.example.tmp_sol

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.tmp_sol.ui.theme.TmpsolTheme
//import com.solana.rpc.SolanaRpcClient
//import com.solana.networking.KtorNetworkDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.solana.mobilewalletadapter.clientlib.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TmpsolTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Solana test app",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var recentBlockhashResult by remember { mutableStateOf("Not yet called") }
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        //========= Get Recent Blockhash ===============================================
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            onClick = {
                coroutineScope.launch {
                    val rpcUri = "https://api.devnet.solana.com".toUri()
                    recentBlockhashResult = RecentBlockhashUseCase(rpcUri = rpcUri).toString()
                }
            }
        ) {
            Text(
                text = "Get Recent Blockhash",
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = recentBlockhashResult,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        //========= Send SOL Transactions ===============================================
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            onClick = {
                coroutineScope.launch {
                    val rpcUri = "https://api.devnet.solana.com".toUri()
                    // TODO: return result?
                    SendTransactionsUseCase(
                        rpcUri = rpcUri,
                        transactions = listOf(byteArrayOf(1))
                    ).toString()
                }
            }
        ) {
            Text(
                text = "Send SOL Transactions",
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = "TODO: set result",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TmpsolTheme {
        Greeting("Android")
    }
}
