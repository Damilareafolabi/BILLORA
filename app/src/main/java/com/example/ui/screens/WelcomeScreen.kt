package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppCurrency
import com.example.ui.theme.BilloraBlack
import com.example.ui.theme.BilloraCrimson
import com.example.ui.theme.BilloraCrimsonSoft
import com.example.viewmodel.BilloraViewModel

@Composable
fun WelcomeScreen(
    viewModel: BilloraViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCurrency by remember { mutableStateOf(AppCurrency.DEFAULT.code) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BilloraBlack,
                        Color(0xFF1A1A1E),
                        BilloraBlack
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            // Adaptive 2-Column Layout for Tablets & Large Screens
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Hero Art & Branding
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WelcomeHeroCard()
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Billora",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Invoice & Receipt Maker by SimeonJr Studios",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFAAAAAA)
                    )
                }

                // Right Column: Value Points, Currency & Actions
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    WelcomeContent(
                        selectedCurrency = selectedCurrency,
                        onCurrencySelected = { selectedCurrency = it },
                        onGetStarted = { viewModel.completeWelcome(selectedCurrency, setupProfileNow = false) },
                        onSetupProfile = { viewModel.completeWelcome(selectedCurrency, setupProfileNow = true) }
                    )
                }
            }
        } else {
            // Mobile Vertical Flow (Responsive phone layout)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Brand Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BilloraCrimsonSoft.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BilloraCrimson.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(BilloraCrimson)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIMEONJR STUDIOS PRESENTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = BilloraCrimsonSoft
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // App Hero Title
                Text(
                    text = "Smart Invoicing.\nInstant Receipts.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Professional offline-first invoicing and receipt generator. Create, calculate, and share clean PDF documents in seconds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 480.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Hero Illustration
                WelcomeHeroCard()

                Spacer(modifier = Modifier.height(20.dp))

                // Feature Highlights & Currency & Call to Action
                WelcomeContent(
                    selectedCurrency = selectedCurrency,
                    onCurrencySelected = { selectedCurrency = it },
                    onGetStarted = { viewModel.completeWelcome(selectedCurrency, setupProfileNow = false) },
                    onSetupProfile = { viewModel.completeWelcome(selectedCurrency, setupProfileNow = true) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun WelcomeHeroCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 440.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181C)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2A2A32))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_welcome_hero),
            contentDescription = "Billora Invoices and Receipts Preview",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
            contentScale = ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeContent(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onGetStarted: () -> Unit,
    onSetupProfile: () -> Unit
) {
    Column(
        modifier = Modifier.widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Value Pillars
        ValueItem(
            icon = Icons.Default.ReceiptLong,
            title = "Fast Invoice & Receipt Creation",
            description = "Line items, quantities, automated VAT/tax, discounts, and instant balances."
        )

        ValueItem(
            icon = Icons.Default.PictureAsPdf,
            title = "Crisp Vector PDF Export",
            description = "High-definition documents ready to print or share directly via WhatsApp & Email."
        )

        ValueItem(
            icon = Icons.Default.Lock,
            title = "100% Offline & Private",
            description = "Zero cloud tracking. All financial records and customer data stay on your device."
        )

        // Currency Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E38))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEFAULT CURRENCY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = BilloraCrimson
                    )
                    Text(
                        text = "Nigerian Naira Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFAAAAAA)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppCurrency.entries.forEach { currency ->
                        val isSelected = currency.code.equals(selectedCurrency, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onCurrencySelected(currency.code) }
                                .background(if (isSelected) BilloraCrimson else Color(0xFF2A2A34))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "${currency.symbol} ${currency.code}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Primary Actions
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("get_started_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BilloraCrimson),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Text(
                text = "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }

        OutlinedButton(
            onClick = onSetupProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("setup_business_profile_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444450))
        ) {
            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp), tint = BilloraCrimson)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set Up Business Profile First", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ValueItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF24242C)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BilloraCrimson,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFAAAAAA)
            )
        }
    }
}
