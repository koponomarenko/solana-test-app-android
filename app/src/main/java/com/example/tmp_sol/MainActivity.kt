package com.example.tmp_sol

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TmpsolTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    //val result by remember { mutableStateOf(callRecentBlockhashUseCase()) }
    val coroutineScope = rememberCoroutineScope()
    var result by remember { mutableStateOf("Not yet called") }
    Column {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
        Button(
            modifier = Modifier.fillMaxWidth()
                .padding(10.dp), // should expand horizontally and have padding
            onClick = {
                coroutineScope.launch {
                    val rpcUriStr: String = "https://api.devnet.solana.com"
                    val rpcUri = rpcUriStr.toUri()
                    result = RecentBlockhashUseCase(rpcUri = rpcUri).toString()
                    Log.i("callRecentBlockhashUseCase", "result=$result")
                }
            }
        ) {
            Text(
                text = "callRecentBlockhashUseCase",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }
//        Text(
//            text = result,
//            modifier = modifier
//        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TmpsolTheme {
        Greeting("Android")
    }
}

//fun callRecentBlockhashUseCase() {
//    CoroutineScope(Dispatchers.Main).launch {
//        val rpcUriStr: String = "https://api.devnet.solana.com"
//        val rpcUri = rpcUriStr.toUri()
//        val blockhash = RecentBlockhashUseCase(rpcUri)
//    }
//}