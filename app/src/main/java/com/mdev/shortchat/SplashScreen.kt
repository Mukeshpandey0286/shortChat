//package com.mdev.shortchat
//
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.airbnb.lottie.compose.LottieAnimation
//import com.airbnb.lottie.compose.LottieCompositionSpec
//import com.airbnb.lottie.compose.LottieConstants
//import com.airbnb.lottie.compose.rememberLottieComposition
//import kotlinx.coroutines.delay
//
//@Composable
//fun SplashScreen(navController: NavController) {
//    // Animation states
//    val lottieAlpha = remember { Animatable(0f) }
//    val textAlpha = remember { Animatable(0f) }
//    var startTextAnimation by remember { mutableStateOf(false) }
//
//    // Load Lottie animation - replace R.raw.cred_animation with your actual Lottie file
//    val lottieComposition by rememberLottieComposition(
//        LottieCompositionSpec.RawRes(R.raw.cred_animation)
//    )
//
//    // Animation sequence
//    LaunchedEffect(key1 = true) {
//        // Fade in the Lottie animation
//        lottieAlpha.animateTo(
//            targetValue = 1f,
//            animationSpec = tween(durationMillis = 1000)
//        )
//
//        // Short delay before text appears
//        delay(500)
//        startTextAnimation = true
//
//        // Fade in the text
//        textAlpha.animateTo(
//            targetValue = 1f,
//            animationSpec = tween(durationMillis = 1000)
//        )
//
//        // Hold the splash screen for a moment
//        delay(2000)
//
//        // Navigate to main screen
//        navController.navigate("main_screen") {
//            popUpTo("splash_screen") { inclusive = true }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//    ) {
//        // Centered Lottie animation
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(400.dp)
//                .align(Alignment.Center)
//                .alpha(lottieAlpha.value),
//            contentAlignment = Alignment.Center
//        ) {
//            LottieAnimation(
//                composition = lottieComposition,
//                iterations = LottieConstants.IterateForever,
//                modifier = Modifier.size(300.dp)
//            )
//        }
//
//        // Bottom text content
//        if (startTextAnimation) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 64.dp)
//                    .alpha(textAlpha.value),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Bottom
//            ) {
//                Text(
//                    text = "WELCOME TO THE CLUB",
//                    color = Color(0xFF737373), // Light gray color
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    letterSpacing = 2.sp
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "make payments.",
//                    color = Color.White,
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Medium
//                )
//
//                Text(
//                    text = "earn rewards.",
//                    color = Color.White,
//                    fontSize = 28.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}