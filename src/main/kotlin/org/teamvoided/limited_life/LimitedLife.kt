package org.teamvoided.limited_life

import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

@Suppress("unused")
object LimitedLife {
    const val MODID = "limited_life"

    @JvmField
    val LOGGER = LoggerFactory.getLogger(LimitedLife::class.simpleName)

    fun commonInit() {
        LOGGER.info("Hello from Common")
    }

    fun id(path: String) = Identifier(MODID, path)
}
