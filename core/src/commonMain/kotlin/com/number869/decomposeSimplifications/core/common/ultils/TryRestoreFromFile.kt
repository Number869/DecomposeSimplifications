package com.number869.decomposeSimplifications.core.common.ultils

import com.arkivanov.essenty.statekeeper.SerializableContainer
import java.io.File
import java.io.ObjectInputStream

fun tryRestoreStateFromFile(): SerializableContainer? {
    return File("states.dat").takeIf(File::exists)?.let { file ->
        try {
            ObjectInputStream(file.inputStream())
                .use(ObjectInputStream::readObject) as SerializableContainer
        } catch (e: Exception) {
            null
        } finally {
            file.delete()
        }
    }
}