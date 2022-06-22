package one.devsky.extensions

import one.devsky.PrivateChannels
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun <T> T.applyIf(condition: Boolean, block: T.() -> Unit): T {
    if (condition) block()
    return this
}

fun Properties.set(key: String, value: String) {
    setProperty(key, value)
    saveProperties()
}

fun Properties.getList(key: String): List<String> {
    return getProperty(key)?.split(",") ?: listOf()
}

fun Properties.set(key: String, value: List<String>) {
    setProperty(key, value.joinToString(","))
    saveProperties()
}

fun Properties.addToList(key: String, value: String) {
    var list = getList(key)
    if (!list.contains(value)) {
        list = list.plus(value)
        set(key, list)
    }
}

fun Properties.removeFromList(key: String, value: String) {
    var list = getList(key)
    if (list.contains(value)) {
        list = list.minus(value)
        set(key, list)
    }
}

fun saveProperties() {
    val file = File("data.properties")
    val fileOutputStream = FileOutputStream(file)
    PrivateChannels.instance.properties.store(fileOutputStream, "Einstellungsdatei der DiscordVerification")
}