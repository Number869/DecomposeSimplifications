package com.number869.decomposeSimplifications.ui.screens.category1Option1

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.number869.decomposeSimplifications.common.BasicBottomSheet
import com.number869.decomposeSimplifications.core.common.DecomposeNavControllerFlex
import com.number869.decomposeSimplifications.core.common.decomposeViewModel
import com.number869.decomposeSimplifications.ui.navigation.Screens
import com.number869.decomposeSimplifications.ui.screens.category2default.Category2DefaultScreen

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Category1Option1Screen(id: String, navController: DecomposeNavControllerFlex) {
    val vm = decomposeViewModel(Category1Option1ViewModel())
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Category 1 Option 1 $id")
        Text("random uuid from vm that destroys itself after exit ${vm.randomUuid}")
        FilledTonalButton(
            onClick = {
                navController.openAsScreen(Screens.Category2.Default.destinationName) {
                    Category2DefaultScreen(navController)
                }
            }
        ) {
            Text("Open category 2")
        }

        FilledTonalButton(
            onClick = {
                val meowDestinationKey = "meoooow"

                // bottom sheets don't need fade animation (that's set by default)
                navController.openInOverlay(key = meowDestinationKey, animation = null) {
                    BasicBottomSheet(
                        onDismiss = { navController.closeOverlay(meowDestinationKey) }
                    ) { MeowBottomsheetContent(navController) }
                }
            }
        ) {
            Text("Open bottom sheet")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeowBottomsheetContent(navController: DecomposeNavControllerFlex) {
    Column(
        Modifier
            .clip(RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    top = 36.dp, // padding for the handle pill
                )
            ) {
                repeat(6) {
                    item {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (it != 0) Spacer(Modifier.height(64.dp))

                            Text(
                                "BasicBottomSheet example",
                                style = MaterialTheme.typography.headlineSmall,
                            )

                            Text(
                                "from optional-extensions artifact",
                                style = MaterialTheme.typography.titleSmall,
                            )

                            Spacer(Modifier.height(32.dp))

                            Button(
                                onClick = {
                                    navController.openInOverlay("meow dialog") { MeowDialog(navController) }
                                }
                            ) {
                                Text("Open a dialog")
                            }
                        }
                    }
                }

                item {
                    // system bars padding because both status bar and nav bar paddings
                    // are needed because the sheet size is equal to the window size while
                    // also not being displayed under the status bar
                    Spacer(Modifier.systemBarsPadding())
                }
            }

            // the pill/handle
            Box(
                Modifier
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    .width(38.dp)
                    .height(4.dp)
                    .align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun MeowDialog(navController: DecomposeNavControllerFlex) {
    AlertDialog(
        onDismissRequest = navController::navigateBack,
        text = { Text("another dialooooog :3333") },
        confirmButton = {
            Button(onClick = navController::navigateBack) {
                Text("Close")
            }
        }
    )
}