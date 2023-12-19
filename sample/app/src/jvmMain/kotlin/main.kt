import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.number869.decomposeSimplifications.App
import com.number869.decomposeSimplifications.core.common.navigation.alt.decomposeAltNavController
import com.number869.decomposeSimplifications.ui.navigation.Destinations
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.awt.Dimension

fun main() = application {
    startKoin {
        modules(
            module {
                single {
                    decomposeAltNavController(Destinations.Empty, Destinations.serializer())
                }
            }
        )
    }

    Window(
        title = "Decompose Simplifications",
        state = rememberWindowState(width = 800.dp, height = 600.dp),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(350, 600)
        App()
    }
}