package com.example.tmp_sol

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
import com.example.tmp_sol.ui.theme.TmpsolTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.funkatronics.encoders.Base58
import org.sol4k.Connection
import org.sol4k.Keypair
import org.sol4k.PublicKey
import org.sol4k.TransactionMessage
import org.sol4k.VersionedTransaction
import org.sol4k.instruction.SplTransferInstruction


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
    val coroutineScope = rememberCoroutineScope( )
    var recentBlockhashResult by remember { mutableStateOf("Not yet called") }
    var sendTransactionRequestResult by remember { mutableStateOf(buildAnnotatedString { append("Not yet called") }) }
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
        //========= Send SPL Transactions ===============================================
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            onClick = {
                coroutineScope.launch {
                    val rpcUri = "https://api.devnet.solana.com".toUri()
                    try {
                        // Pass a lambda that updates the state
                        prepTransaction(coroutineScope) { annotatedResult ->
                            sendTransactionRequestResult = annotatedResult
                        }
                    } catch (e: Exception) {
                        sendTransactionRequestResult = buildAnnotatedString { append("Error: ${e.message}") }
                    }

//                    SendTransactionsUseCase(
//                        rpcUri = rpcUri,
//                        transactions = listOf(byteArrayOf(1))
//                    ).toString()
                }
            }
        ) {
            Text(
                text = "Send SPL Transaction",
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

fun prepTransaction(
    coroutineScope: CoroutineScope,
    onTransactionComplete: (AnnotatedString) -> Unit
) {
    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val connection = Connection("https://api.devnet.solana.com")

            val sender = Keypair.fromSecretKey(Base58.decode("3h2U43gek9SQUfN1izTWXv7Gp7LzCUUFmBdXJFwzxGZmpS5nb4pJFge6umcvEGMBYFQHr6vYRitCLbToaWcCF7uT"))
            var dusdcToken = PublicKey("6jmx3aN9GHx7dSGp6iEW2CmKEFxBnmjFPKT8yRuuHQyT")
            // receiverAccount = "CCuSfDjoNXjcyRQ2AivHudqLTSa5rFeVoxnLSiszRo4v"
            var receiverAssociatedAccount = PublicKey("96mewfAN9mqVP65KRUYfai8QxU8mTXtYQwnoPwTi3RQH")
            var holderAssociatedAccount = PublicKey.findProgramDerivedAddress(sender.publicKey, dusdcToken);

            // build SOL instruction
            var splTransferInstruction = SplTransferInstruction(
                holderAssociatedAccount.publicKey,
                receiverAssociatedAccount,
                dusdcToken,
                sender.publicKey,
                100, // 0.000100
                6
            )

            // create message
            var message = TransactionMessage.newMessage(
                sender.publicKey,
                connection.getLatestBlockhash(),
                splTransferInstruction
            )
            var transaction = VersionedTransaction(message)
            transaction.sign(sender)

            var signature = connection.sendTransaction(transaction)

            Log.w("DDDD", "Transaction Signature: $signature")
            val resultText = buildAnnotatedString {
                append("Transaction Signature: ")
                pushLink(LinkAnnotation.Url("http://solscan.io/tx/$signature?cluster=devnet"))
                append(signature)
                pop() // Pop the link annotation
            }
            onTransactionComplete(resultText)
        } catch (e: Exception) {
            Log.e("DDDD", "Error in prepTransaction: ${e.message}", e)
            val errorText = buildAnnotatedString {
                append("Error: ${e.message}")
            }
            onTransactionComplete(errorText)
        }
    }
}

