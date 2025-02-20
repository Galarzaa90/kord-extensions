/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.RoleOptionValue
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.RoleBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.DEFAULT_KORDEX_BUNDLE
import dev.kordex.parser.StringParser
import kotlinx.coroutines.flow.firstOrNull

/**
 * Argument converter for discord [Role] arguments.
 *
 * This converter supports specifying roles by supplying:
 * * A role mention
 * * A role ID
 * * A message name - the first matching role from the given guild will be used.
 *
 * @param requiredGuild Lambda returning a specific guild to require the role to be in. If omitted, defaults to the
 * guild the command was invoked in.
 */
@Converter(
	"role",

	types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
	imports = ["dev.kord.common.entity.Snowflake"],
	builderFields = ["public var requiredGuild: (suspend () -> Snowflake)? = null"]
)
public class RoleConverter(
	private var requiredGuild: (suspend () -> Snowflake)? = null,
	override var validator: Validator<Role> = null,
) : SingleConverter<Role>() {
	override val signatureTypeString: String = "converters.role.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false

		parsed = findRole(arg, context)
			?: throw DiscordRelayedException(
				context.translate("converters.role.error.missing", replacements = arrayOf(arg))
			)

		return true
	}

	private suspend fun findRole(arg: String, context: CommandContext): Role? {
		val guildId: Snowflake = if (requiredGuild != null) {
			requiredGuild!!.invoke()
		} else {
			context.getGuild()?.id
		} ?: return null

		val guild: Guild = kord.getGuildOrNull(guildId) ?: return null

		@Suppress("MagicNumber")
		return if (arg.startsWith("<@&") && arg.endsWith(">")) { // It's a mention
			val id: String = arg.substring(3, arg.length - 1)

			try {
				guild.getRole(Snowflake(id))
			} catch (e: NumberFormatException) {
				throw DiscordRelayedException(
					context.translate("converters.role.error.invalid", replacements = arrayOf(id))
				)
			}
		} else {
			try { // Try for a role ID first
				guild.getRole(Snowflake(arg))
			} catch (e: NumberFormatException) { // It's not an ID, let's try the name
				guild.roles.firstOrNull { role ->
					role.name.equals(arg, true)
				}
			}
		}
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		RoleBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = if (context.eventObj is AutoCompleteInteractionCreateEvent) {
			val id = (option as? RoleOptionValue)?.value ?: return false
			val guild = context.getGuild() ?: return false

			guild.getRoleOrNull(id) ?: return false
		} else {
			(option as? RoleOptionValue)?.resolvedObject ?: return false
		}

		this.parsed = optionValue

		return true
	}
}
