package org.teamvoided.liminal_life

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.GameMode
import net.minecraft.world.World
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.teamvoided.liminal_life.commands.LiminalCommand
import org.teamvoided.liminal_life.config.LiminalLifeConfig
import org.teamvoided.liminal_life.data.LifeData
import org.teamvoided.liminal_life.data.LifeData.getDisplayLoc
import org.teamvoided.liminal_life.data.LifeData.getLives
import org.teamvoided.liminal_life.data.LifeData.removeLives

@Suppress("unused", "MemberVisibilityCanBePrivate")
object LiminalLife {
    const val MODID = "liminal_life"

    @JvmField
    val log: Logger = LoggerFactory.getLogger(LiminalLife::class.simpleName)

    fun commonInit() {
        log.info("Limiting Liminal Lives ...")
        LifeData.init()
        LiminalLifeConfig.load()
        ServerPlayerEvents.AFTER_RESPAWN.register { old, player, _ ->
            if (!old.isInTeleportationState && World.END == old.world.registryKey) {
                when (val lives = player.removeLives()) {
                    0, null -> player.killPlayer()
                    in 1..LiminalLifeConfig.config.maxLifeCount -> player.sendSystemMessage(
                        tTxt(
                            "You have %s lives left!",
                            lives
                        )
                    )

                    else -> player.sendSystemMessage(tTxt("There was an error please message an admin!"))
                }
            }
        }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> LiminalCommand.init(dispatcher) }
        ServerPlayConnectionEvents.JOIN.register { networkHandler, _, _ ->
            val player = networkHandler.player
            val lives = player.getLives()
            if (lives <= 0) player.killPlayer()
        }
    }

    fun id(path: String) = Identifier(MODID, path)
    fun tTxt(text: String, vararg args: Any): MutableText = Text.translatable(text, *args)
    fun ServerPlayerEntity.announce(text: MutableText) {
        when (this.getDisplayLoc()) {
            LifeData.DisplayLocation.CHAT -> this.sendSystemMessage(text)
            LifeData.DisplayLocation.HOTBAR -> this.sendSystemMessage(text, true)
            LifeData.DisplayLocation.TITLE -> networkHandler.send(TitleS2CPacket(text))
            LifeData.DisplayLocation.SUBTITLE -> {
                networkHandler.send(SubtitleS2CPacket(text))
                networkHandler.send(TitleS2CPacket(Text.empty()))
            }
        }
    }

    fun ServerPlayerEntity.killPlayer() {
        this.changeGameMode(GameMode.SPECTATOR)
        this.announce(tTxt("You have ran out of lives!").formatted(Formatting.RED))
    }
}
