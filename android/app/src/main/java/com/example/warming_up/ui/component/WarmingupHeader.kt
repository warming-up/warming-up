package com.example.warming_up.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.warming_up.R
import com.example.warming_up.ui.theme.ResetIconColor
import com.example.warming_up.ui.theme.WarmingupTheme

private val HeaderLogoHeight = 40.dp

@Composable
fun WarmingupHeader(
    showReset: Boolean = true,
    onResetClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Warm Up",
            modifier = Modifier
                .width(124.dp)
                .height(HeaderLogoHeight),
            contentScale = ContentScale.Fit,
        )

        if (showReset) {
            IconButton(
                onClick = onResetClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(HeaderLogoHeight),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = "초기화",
                    modifier = Modifier.size(25.dp),
                    tint = ResetIconColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WarmingupHeaderPreview() {
    WarmingupTheme {
        WarmingupHeader()
    }
}
