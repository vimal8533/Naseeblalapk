package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

@Composable
fun MarketLogoMedallion(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showSubtext: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-fidelity Market Emblem Logo from user's custom design
        Image(
            painter = painterResource(id = R.drawable.market_logo),
            contentDescription = "Naseeb Lal Market - Market Rent Manager Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size)
                .shadow(elevation = 10.dp, shape = CircleShape)
                .clip(CircleShape)
        )

        if (showSubtext) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Naseeb Lal Market",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Market Rent Manager • Shops • Rent • Reliable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
