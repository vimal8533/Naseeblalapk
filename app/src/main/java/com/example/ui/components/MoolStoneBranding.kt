package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GoldAccent

/**
 * Professional WhatsApp / Corporate style footer displaying "from" with MoolStone Logo & Name.
 */
@Composable
fun MoolStoneBrandingFooter(
    modifier: Modifier = Modifier,
    logoSize: Dp = 22.dp,
    nameColor: Color = Color.White,
    fromColor: Color = Color.White.copy(alpha = 0.5f),
    testTagPrefix: String = "moolstone"
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "from",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = fromColor,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_moolstone_logo),
                contentDescription = "MoolStone Company Logo",
                modifier = Modifier
                    .size(logoSize)
                    .testTag("${testTagPrefix}_logo")
            )

            Text(
                text = "MoolStone",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = nameColor,
                letterSpacing = 1.2.sp,
                modifier = Modifier.testTag("${testTagPrefix}_text")
            )
        }
    }
}
