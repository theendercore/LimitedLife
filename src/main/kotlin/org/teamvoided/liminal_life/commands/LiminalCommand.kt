package org.teamvoided.liminal_life.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import org.teamvoided.liminal_life.LiminalLife.announce
import org.teamvoided.liminal_life.LiminalLife.tTxt
import org.teamvoided.liminal_life.commands.DisplayLocationArgumentType.displayLocationArg
import org.teamvoided.liminal_life.config.LiminalLifeConfig
import org.teamvoided.liminal_life.data.LifeData.addLife
import org.teamvoided.liminal_life.data.LifeData.getDisplayLoc
import org.teamvoided.liminal_life.data.LifeData.getLives
import org.teamvoided.liminal_life.data.LifeData.removeLives
import org.teamvoided.liminal_life.data.LifeData.setDisplayLoc
import org.teamvoided.liminal_life.data.LifeData.setLives

object LiminalCommand {
    fun init(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val liminalNode = literal("liminal_life").build()
        dispatcher.root.addChild(liminalNode)


        val lifeNode = literal("lives").executes(::getLives).build()
        liminalNode.addChild(lifeNode)

        lifeNode.addNode("set", ::setLives)

        lifeNode.addNode("add", ::addLives)

        lifeNode.addNode("remove", ::removeLives)

        val resetNode = literal("reset").requires { it.hasPermissionLevel(2) }.build()
        lifeNode.addChild(resetNode)
        val resetNodePlayerArg = argument("target", EntityArgumentType.players()).executes(::resetLives).build()
        resetNode.addChild(resetNodePlayerArg)


        val locNode = literal("display_location").executes(::getLoc).build()
        liminalNode.addChild(locNode)

        val getLocNode = literal("get").executes(::getLoc).build()
        locNode.addChild(getLocNode)

        val setLocNode = literal("set").build()
        locNode.addChild(setLocNode)
        val setLocNodeLocArg = displayLocationArg("loc").executes(::setLoc).build()
        setLocNode.addChild(setLocNodeLocArg)

        val testLocNode = literal("test").executes(::testLoc).build()
        locNode.addChild(testLocNode)


        val reloadConfig =
            literal("reload_config").requires { it.hasPermissionLevel(2) }.executes(::reloadConfig).build()
        liminalNode.addChild(reloadConfig)
    }

    private fun getLives(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val player = src.player ?: return 0
        player.sendSystemMessage(tTxt("You have %s lives!", player.getLives()))

        return 1
    }

    private fun setLives(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val players = EntityArgumentType.getOptionalPlayers(c, "target")
        val count = IntegerArgumentType.getInteger(c, "count")
        var pCount = 0

        players.forEach {
            val errors = it.setLives(count)
            if (errors.first == null) {
                src.sendError(tTxt(errors.second!!, pCount))
                return 0
            }
            pCount++
        }
        src.sendSystemMessage(tTxt("Changed life amount for %s players!", pCount))

        return pCount
    }

    private fun addLives(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val players = EntityArgumentType.getOptionalPlayers(c, "target")
        val count = IntegerArgumentType.getInteger(c, "count")
        var pCount = 0
        var pMaxCount = 0

        players.forEach {
            val lives = it.addLife(count)
            if (lives == null) pMaxCount++ else pCount++
        }
        src.sendSystemMessage(tTxt("Changed life amount for %s players!", pCount))
        if (pMaxCount > 0) src.sendError(tTxt("%s players where at max lives!", pMaxCount))

        return pCount
    }

    private fun removeLives(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val players = EntityArgumentType.getOptionalPlayers(c, "target")
        val count = IntegerArgumentType.getInteger(c, "count")
        var pCount = 0
        var pMaxCount = 0
        var pTo0Count = 0

        players.forEach {
            val lives = it.removeLives(count, true)
            when (lives) {
                null -> pMaxCount++
                0 -> pTo0Count++
                else -> pCount++
            }
        }
        src.sendSystemMessage(tTxt("Changed life amount for %s players!", pCount))
        if (pMaxCount > 0) src.sendError(tTxt("%s players where at 0 lives!", pMaxCount))
        if (pTo0Count > 0) src.sendError(tTxt("%s players set to 0 lives!", pMaxCount))

        return pCount
    }

    private fun resetLives(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val players = EntityArgumentType.getOptionalPlayers(c, "target")
        var pCount = 0

        players.forEach {
            val errors = it.setLives(LiminalLifeConfig.config.maxLifeCount)
            if (errors.first == null) {
                src.sendError(tTxt(errors.second!!, pCount))
                return 0
            }
            pCount++
        }
        src.sendSystemMessage(tTxt("Reset life amount for %s players!", pCount))

        return pCount
    }

    private fun setLoc(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val player = src.player ?: return 0
        val loc = DisplayLocationArgumentType.getDisplayLocation(c, "loc")
        player.setDisplayLoc(loc)
        src.sendSystemMessage(tTxt("Set display location to : %s", player.getDisplayLoc()))

        return 1
    }

    private fun getLoc(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val player = src.player ?: return 0
        src.sendSystemMessage(tTxt("Current display location : %s", player.getDisplayLoc()))

        return 1
    }

    private fun testLoc(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        val player = src.player ?: return 0
        player.announce(tTxt("Test Message"))

        return 1
    }

    private fun reloadConfig(c: CommandContext<ServerCommandSource>): Int {
        val src = c.source
        Thread {
            when (LiminalLifeConfig.load()) {
                1 -> src.sendSystemMessage(tTxt("Config reloaded!"))
                0 -> src.sendSystemMessage(tTxt("No config was found! Created a new one."))
                else -> src.sendError(tTxt("Could not load config file! Check the log for more info."))
            }
        }.start()
        return 1
    }

    private fun LiteralCommandNode<ServerCommandSource>.addNode(name: String, command: Command<ServerCommandSource>) {
        val root = literal(name).requires { it.hasPermissionLevel(2) }.build()
        this.addChild(root)
        val playerArg = argument("target", EntityArgumentType.players()).build()
        root.addChild(playerArg)
        val intArg =
            argument("count", IntegerArgumentType.integer(1, LiminalLifeConfig.config.maxLifeCount)).executes(command)
                .build()
        playerArg.addChild(intArg)
    }
}