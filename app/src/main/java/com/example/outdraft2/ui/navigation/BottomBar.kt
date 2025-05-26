package com.example.outdraft2.ui.navigation

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.outdraft2.R
import com.example.outdraft2.ui.pages.builds.BuildPage
import com.example.outdraft2.ui.pages.counter.CounterPage
import com.example.outdraft2.ui.pages.searchplayer.SearchPlayerPage
import kotlinx.coroutines.launch

@Composable
fun BottomApp(pagerState: PagerState) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(R.drawable.ahri_icon),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
        )
        Image(
            painter = painterResource(R.drawable.player_icon),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
        )
        Image(
            painter = painterResource(R.drawable.build_icon),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                }
        )
    }
}

@Composable
fun AppNavigation(activity: Activity) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        when (page) {
            0 -> CounterPage()
            1 -> SearchPlayerPage(activity)
            2 -> BuildPage()
        }
    }
}

@Composable
fun AppWithBottomNavigation(activity: Activity) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> CounterPage()
                1 -> SearchPlayerPage(activity)
                2 -> BuildPage()
            }
        }

        BottomAppBar(
            containerColor = Color(0xFF2B3A4B)
        ) {
            BottomApp(pagerState = pagerState)
        }
    }
}