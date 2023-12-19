package com.number869.decomposeSimplifications.ui.screens.category1Option1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.number869.decomposeSimplifications.core.common.navigation.alt.ContentTypes
import com.number869.decomposeSimplifications.core.common.navigation.alt.DecomposeAltNavController
import com.number869.decomposeSimplifications.core.common.navigation.alt.LocalContentType
import com.number869.decomposeSimplifications.core.common.ultils.decomposeViewModel
import com.number869.decomposeSimplifications.ui.navigation.Destinations

@Composable
fun Category1Option1Screen(
    id: String,
    navController: DecomposeAltNavController<Destinations>,
) {
    val isOverlay = LocalContentType.current == ContentTypes.Overlay
    val vm = decomposeViewModel(Category1Option1ViewModel())

    LaunchedEffect(null) {
        if (isOverlay) {
            navController.openInSnack { ToastMessageUi() }
        }
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Category 1 Option 1 $id")
        Text("random uuid from vm that destroys itself after exit ${vm.randomUuid}")
        FilledTonalButton(
            onClick = { navController.navigateToScreen(Destinations.Category2.Default) }
        ) {
            Text("Open category 2")
        }

        FilledTonalButton(
            onClick = {
                navController.navigateToOverlay(Destinations.Overlay.ExampleBottomSheet)
            }
        ) {
            Text("Open bottom sheet")
        }

        FilledTonalButton(
            onClick = {
                navController.navigateToOverlay(Destinations.Category2.Default)
            }
        ) {
            Text("Open category 2 as overlay")
        }
    }
}

@Composable
fun MeowBottomsheetContent(navController: DecomposeAltNavController<Destinations>) {
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
                                    navController.navigateToOverlay(Destinations.Overlay.DialogFromBottomSheet)
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
fun MeowDialog(navController: DecomposeAltNavController<Destinations>) {
    AlertDialog(
        onDismissRequest = navController::navigateBack,
        text = { Text("another dialooooog :3333") },
        confirmButton = {
            Button(onClick = { navController.closeOverlay(Destinations.Overlay.DialogFromBottomSheet) }) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BoxScope.ToastMessageUi() {
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .padding(64.dp)
            .clip(CircleShape)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "This was opened in an overlay",
            Modifier.padding(vertical = 16.dp),
            style = MaterialTheme.typography.titleSmall
        )
    }
}