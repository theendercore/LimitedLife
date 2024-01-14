package org.teamvoided.liminal_life.data

import eu.pb4.playerdata.api.PlayerDataApi
import eu.pb4.playerdata.api.storage.JsonDataStorage
import eu.pb4.playerdata.api.storage.PlayerDataStorage
import net.minecraft.server.network.ServerPlayerEntity
import org.teamvoided.liminal_life.LiminalLife.killPlayer
import org.teamvoided.liminal_life.config.LiminalLifeConfig

object LifeData {
    private val LIMINAL_LIFE: PlayerDataStorage<LiminalData> = JsonDataStorage("liminal_life", LiminalData::class.java)
    fun init() {
        PlayerDataApi.register(LIMINAL_LIFE)
    }

    fun ServerPlayerEntity.addLife(count: Int = 1): Int? {
        val data = this.getLives()
        if ((data + count) > LiminalLifeConfig.config.maxLifeCount) return null
        setLifeData(this, data + count)
        return data + count
    }

    fun ServerPlayerEntity.removeLives(count: Int = 1, shouldKill: Boolean = false): Int? {
        val data = this.getLives()
        if (data - count < 0) {
            if (shouldKill) {
                setLifeData(this, 0)
                this.killPlayer()
            } else return null
        }
        setLifeData(this, data - count)
        return data - count
    }

    fun ServerPlayerEntity.setLives(count: Int = LiminalLifeConfig.config.maxLifeCount): Pair<Int?, String?> {
        if (count > LiminalLifeConfig.config.maxLifeCount) return Pair(null, "more then max")
        if (count < 0) return Pair(null, "less then 0")
        setLifeData(this, count)
        return Pair(count, null)
    }

    fun ServerPlayerEntity.getLives(): Int {
        val data = getLifeData(this)
        return if (data == null) {
            setLifeData(this, LiminalLifeConfig.config.maxLifeCount)
            LiminalLifeConfig.config.maxLifeCount
        } else if (data < 0) 0
        else data
    }

    fun ServerPlayerEntity.setDisplayLoc(location: DisplayLocation) {
        setDLData(this, location)
    }

    fun ServerPlayerEntity.getDisplayLoc(): DisplayLocation {
        val data = getDLData(this)
        if (data != null) return data
        setDLData(this, DisplayLocation.CHAT)
        return DisplayLocation.CHAT
    }

    private fun getLifeData(player: ServerPlayerEntity): Int? =
        PlayerDataApi.getCustomDataFor(player, LIMINAL_LIFE)?.lives

    private fun setLifeData(player: ServerPlayerEntity, data: Int) {
        var iData = PlayerDataApi.getCustomDataFor(player, LIMINAL_LIFE)
        if (iData == null) iData = LiminalData()
        PlayerDataApi.setCustomDataFor(player, LIMINAL_LIFE, LiminalData(data, iData.displayLocation))
    }

    private fun getDLData(player: ServerPlayerEntity): DisplayLocation? =
        PlayerDataApi.getCustomDataFor(player, LIMINAL_LIFE)?.displayLocation


    private fun setDLData(player: ServerPlayerEntity, data: DisplayLocation) {
        var iData = PlayerDataApi.getCustomDataFor(player, LIMINAL_LIFE)
        if (iData == null) iData = LiminalData()
        PlayerDataApi.setCustomDataFor(player, LIMINAL_LIFE, LiminalData(iData.lives, data))
    }

    enum class DisplayLocation { TITLE, SUBTITLE, CHAT, HOTBAR }
    data class LiminalData(
        val lives: Int = LiminalLifeConfig.config.maxLifeCount,
        val displayLocation: DisplayLocation = DisplayLocation.CHAT
    )

}