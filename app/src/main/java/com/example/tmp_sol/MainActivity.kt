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
import com.funkatronics.encoders.Base58
import com.solana.mobilewalletadapter.clientlib.*
import com.solana.networking.Rpc20Driver
import com.solana.publickey.SolanaPublicKey
import com.solana.rpccore.JsonRpc20Request
import com.solana.signer.Ed25519Signer
import com.solana.transaction.AccountMeta
import com.solana.transaction.Blockhash
import com.solana.transaction.Message
import com.solana.transaction.Transaction
import com.solana.transaction.TransactionInstruction
import diglol.crypto.Ed25519
import diglol.crypto.KeyPair
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray


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
    var sendTransactionRequestResult by remember { mutableStateOf("Not yet called") }
    val rpcUri = "https://api.devnet.solana.com".toUri()

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
                    try {
                        // Pass a lambda that updates the state
                        prepTransaction(coroutineScope) { result ->
                            sendTransactionRequestResult = result
                        }
                    } catch (e: Exception) {
                        sendTransactionRequestResult = "Error: ${e.message}"
                    }

//                    SendTransactionsUseCase(
//                        rpcUri = rpcUri,
//                        transactions = listOf(byteArrayOf(1))
//                    ).toString()
                }
            }
        ) {
            Text(
                text = "Send SOL Transaction",
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = sendTransactionRequestResult,
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

suspend fun prepTransaction(
    coroutineScope: CoroutineScope,
    onTransactionComplete: (String) -> Unit
) {
// Solana Memo Program
    val memoProgramId = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr" // TODO
    val memoProgramIdKey = SolanaPublicKey.from(memoProgramId)

    val address = SolanaPublicKey.from("H56PNhvLFohe3kVsWu4HQtseFYRGyfQ1yioVjhzmN9Bo")

// Construct the instruction
    val message = "Hello Solana!"
    val memoInstruction = TransactionInstruction(
        memoProgramIdKey,
        // Define the accounts in instruction
        listOf(AccountMeta(address, true, true)),
        // Pass in the instruction data as ByteArray
        message.encodeToByteArray()
    )

/// Create the Memo transaction

    // Build Message
    val rpcUri = "https://api.devnet.solana.com".toUri()

//    coroutineScope.launch {
    val recentBlockhashResult = RecentBlockhashUseCase(rpcUri = rpcUri)

    //val blockhash = Blockhash(RecentBlockhashUseCase(rpcUri = rpcUri))
    val solMessage = Message.Builder()
        .addInstruction(memoInstruction)
        .setRecentBlockhash(recentBlockhashResult)
        .build()
//    }

    // prepare signer
    val publicKey = "H56PNhvLFohe3kVsWu4HQtseFYRGyfQ1yioVjhzmN9Bo"
    val privateKey =
        "3h2U43gek9SQUfN1izTWXv7Gp7LzCUUFmBdXJFwzxGZmpS5nb4pJFge6umcvEGMBYFQHr6vYRitCLbToaWcCF7uT"
    val keyPair = KeyPair(publicKey.toByteArray(), privateKey.toByteArray())

    //val keyPair = Ed25519.generateKeyPair()
    val signer = object : Ed25519Signer() {
        override val publicKey: ByteArray get() = keyPair.publicKey
        override suspend fun signPayload(payload: ByteArray): ByteArray =
            Ed25519.sign(keyPair, payload)
    }

// Sign Message
    val signature = signer.signPayload(solMessage.serialize())

// Build Transaction
    val transaction = Transaction(listOf(signature), solMessage)
////////////==================================

    // serialize transaction
    val transactionBytes = transaction.serialize()
    val encodedTransaction = Base58.encodeToString(transactionBytes)

// setup RPC driver
    val rpcDriver = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())

    class SendTransactionRequest(encodedTransaction: String, requestId: String) : JsonRpc20Request(
        method = "sendTransaction",
        params = buildJsonArray {
            add(
                JsonPrimitive
                    (encodedTransaction)
            )
        },
        requestId
    )

// build rpc request
    val requestId = 1
    val rpcRequest = SendTransactionRequest(encodedTransaction, requestId.toString())

// send the request and get response
// using JsonElement.serializer() will return the JSON RPC response. you can use your own serializer to get back a specific object
    val rpcResponse = rpcDriver.makeRequest(rpcRequest, JsonElement.serializer())

    val result = rpcResponse.toString()
    onTransactionComplete(result)

    //Log.w("DDDD", "rpcResponse: ${rpcResponse.result.toString()}")
    Log.w("DDDD", "rpcResponse ... .")

    //SendTransactionsUseCase(rpcUri = rpcUri, transactions = listOf(transaction.serialize()))
}

