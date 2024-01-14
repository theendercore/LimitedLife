package org.teamvoided.liminal_life.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.teamvoided.liminal_life.LiminalLife.tTxt
import org.teamvoided.liminal_life.data.LifeData.DisplayLocation
import java.util.concurrent.CompletableFuture

object DisplayLocationArgumentType {
    fun displayLocationArg(name: String): RequiredArgumentBuilder<ServerCommandSource, String> {
        return CommandManager.argument(name, StringArgumentType.string()).suggests(::listSuggestions)
    }

    @Throws(CommandSyntaxException::class)
    fun getDisplayLocation(context: CommandContext<ServerCommandSource>, name: String): DisplayLocation {
        val string = context.getArgument(name, String::class.java)
        try {
            return DisplayLocation.valueOf(string)
        } catch (e: IllegalArgumentException) {
            throw UNKNOWN_DISPLAY_LOCATION_TYPE_EXCEPTION.create(string)
        }
    }

    private fun <S> listSuggestions(
        commandContext: CommandContext<S>, suggestionsBuilder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        return if (commandContext.source is CommandSource) CommandSource.suggestMatching(
            DisplayLocation.entries.map { it.toString() }, suggestionsBuilder
        ) else Suggestions.empty()
    }

    private val UNKNOWN_DISPLAY_LOCATION_TYPE_EXCEPTION =
        DynamicCommandExceptionType { tTxt("Display Location %s not found!", it) }
}