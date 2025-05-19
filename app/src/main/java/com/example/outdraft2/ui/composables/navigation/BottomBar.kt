package com.example.outdraft2.ui.composables.navigation

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.outdraft2.R
import com.example.outdraft2.ui.composables.pages.builds.BuildPage
import com.example.outdraft2.ui.composables.pages.counter.CounterPage
import com.example.outdraft2.ui.composables.pages.searchplayer.SearchPlayerPage


@Composable
fun BottomApp(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),

        horizontalArrangement = Arrangement.SpaceBetween,

        ) {
        Image(
            painter = painterResource(R.drawable.player_icon),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable { navController.navigate("search") }
        )
        Image(
            painter = painterResource(R.drawable.ahri_icon),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable { navController.navigate("counter") }

        )
        Image(
            painter = painterResource(R.drawable.build_icon),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .clickable { navController.navigate("build") }

        )
    }
}

@Composable
fun AppNavigation(navController: NavHostController, activity: Activity) {
    NavHost(navController = navController, startDestination = "counter") {
        composable("search") { SearchPlayerPage(activity) }
        composable("counter") { CounterPage() }
        composable("build") { BuildPage() }
    }
}

