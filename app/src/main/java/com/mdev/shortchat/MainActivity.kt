package com.mdev.shortchat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mdev.shortchat.ui.theme.ShortChatTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var contactList: List<String> = listOf()
    private var currentPhoneNumber: String? = null

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request multiple permissions
        val requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }

            if (allGranted) {
                contactList = getPhoneNumbers()
                currentPhoneNumber = getCurrentPhoneNumber()
                loadUI()
            } else {
                // Show UI with empty data
                loadUI()
            }
        }

        // Check if permissions are already granted
        val allPermissionsGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            contactList = getPhoneNumbers()
            currentPhoneNumber = getCurrentPhoneNumber()
            loadUI()
        } else {
            requestPermissionsLauncher.launch(requiredPermissions)
        }
    }

    private fun loadUI() {
        setContent {
            ShortChatTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "splash_screen") {
                    composable("splash_screen") {
                        SplashScreen(navController = navController)
                    }
                    composable("main_screen") {
                        MembershipApplicationScreen(
                            currentPhoneNumber = currentPhoneNumber,
                            contactList = contactList,
                            onNumberSelected = { number ->
                                // You can show a toast or log the selected number
                                Toast.makeText(this@MainActivity, "Selected: $number", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun getPhoneNumbers(): List<String> {
        val phoneNumbers = mutableListOf<String>()
        val contentResolver = contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null, null, null
        )

        cursor?.use {
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex)?.replace("\\s".toRegex(), "")
                number?.let { phoneNumbers.add(it) }
            }
        }
        return phoneNumbers.distinct()
    }

    private fun getCurrentPhoneNumber(): String? {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Try to get phone number using SubscriptionManager first (more reliable on dual SIM)
                val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                val activeSubscriptionInfoList = subscriptionManager?.activeSubscriptionInfoList

                if (!activeSubscriptionInfoList.isNullOrEmpty()) {
                    // Get the first active subscription number
                    val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    for (subscriptionInfo in activeSubscriptionInfoList) {
                        val phoneNumber = telephonyManager.createForSubscriptionId(subscriptionInfo.subscriptionId).line1Number
                        if (!phoneNumber.isNullOrEmpty()) {
                            return phoneNumber
                        }
                    }
                }

                // Fallback to default TelephonyManager
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                return telephonyManager.line1Number
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    // Animation states
    val lottieAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    var startTextAnimation by remember { mutableStateOf(false) }

    // Load Lottie animation - replace R.raw.cred_animation with your actual Lottie file
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.shorchat_splash)
    )

    // Animation sequence
    LaunchedEffect(key1 = true) {
        // Fade in the Lottie animation
        lottieAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Short delay before text appears
        delay(500)
        startTextAnimation = true

        // Fade in the text
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Hold the splash screen for a moment
        delay(2000)

        // Navigate to main screen
        navController.navigate("main_screen") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 8.dp)
    ) {
        // Centered Lottie animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.Center)
                .alpha(lottieAlpha.value),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = lottieComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(400.dp)
            )
        }

        // Bottom text content
        if (startTextAnimation) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
                    .alpha(textAlpha.value),
//                horizontalAlignment = Alignment.,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "WELCOME TO THE CLUB",
                    color = Color(0xFF737373), // Light gray color
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "make payments.",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "earn rewards.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipApplicationScreen(
    currentPhoneNumber: String?,
    contactList: List<String>,
    onNumberSelected: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top section
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "MEMBERSHIP APPLICATION",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "tell us your mobile number",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Mobile number input field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "MOBILE NUMBER",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(5.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        // Only accept numeric input
                        if (it.all { char -> char.isDigit() } && it.length <= 10) {
                            text = it
                            showSuggestions = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedTextColor = Color.White,
                        containerColor = Color.Transparent,
                        unfocusedBorderColor = Color.Gray,
                        focusedBorderColor = Color.White
                    ),
                    placeholder = { Text("Enter your mobile number", color = Color.Gray, style = MaterialTheme.typography.labelSmall) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Checkbox for credit information access
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                var checked by remember { mutableStateOf(false) }

                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.White,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.Black
                    )
                )

                Text(
                    text = "allow CRED to access your credit information from RBI approved bureaus, this does not impact your credit score.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Agree & Continue button
            Button(
                onClick = { /* Handle continue */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    if(text.length == 10) Color.White else Color.Gray,
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                if(text.length == 10) {
                    Text(
                        text = "Agree & Continue",
                        fontSize = 16.sp,
                        color = Color.Black

                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "→",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }else{
                    Text(
                        text = "Agree & Continue",
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "→",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Suggestions Dialog
        if (showSuggestions && contactList.isNotEmpty() && currentPhoneNumber != null) {
            Dialog(onDismissRequest = { showSuggestions = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Continue with",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Current SIM number
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    text = currentPhoneNumber
                                    onNumberSelected(currentPhoneNumber)
                                    showSuggestions = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.LightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📱",
                                    fontSize = 20.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = currentPhoneNumber,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // "None of the above" option
                        Text(
                            text = "NONE OF THE ABOVE",
                            fontSize = 16.sp,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier
                                .clickable {
                                    showSuggestions = false
                                }
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}