package com.example.warming_up.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.R
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun WarmingupBottomBar(
    selectedTab: BottomTab,
    onTabClick: (BottomTab) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 4.dp,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(top = 10.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomBarItem(
                    label = "지금",
                    iconRes = R.drawable.ic_schedule,
                    selected = selectedTab == BottomTab.Now,
                    onClick = { onTabClick(BottomTab.Now) },
                )
                BottomBarItem(
                    label = "준비물",
                    iconRes = R.drawable.ic_playlist,
                    selected = selectedTab == BottomTab.Supplies,
                    onClick = { onTabClick(BottomTab.Supplies) },
                )
            }
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    @DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            modifier = Modifier.size(25.dp),
            tint = if (selected) WarmBlue else WarmSubText,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (selected) WarmBlue else WarmSubText,
            fontSize = 10.5.sp,
            lineHeight = 15.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WarmingupBottomBarPreview() {
    WarmingupTheme {
        WarmingupBottomBar(
            selectedTab = BottomTab.Now,
            onTabClick = {},
        )
    }
}
