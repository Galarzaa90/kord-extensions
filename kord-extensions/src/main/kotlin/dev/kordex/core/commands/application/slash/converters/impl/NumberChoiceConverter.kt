/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.slash.converters.impl

import dev.kord.core.entity.interaction.IntegerOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.IntegerOptionBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.application.slash.converters.ChoiceConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.utils.getIgnoringCase
import dev.kordex.parser.StringParser

private const val DEFAULT_RADIX = 10

/**
 * Choice converter for integer arguments. Supports mapping up to 25 choices to integers.
 *
 * Discord doesn't support longs or floating point types, so this is the only numeric type you can use directly.
 */
@Converter(
	"number",

	types = [ConverterType.CHOICE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.SINGLE],
	builderFields = ["public var radix: Int = $DEFAULT_RADIX"]
)
public class NumberChoiceConverter(
	private val radix: Int = DEFAULT_RADIX,
	choices: Map<String, Long>,
	override var validator: Validator<Long> = null,
) : ChoiceConverter<Long>(choices) {
	override val signatureTypeString: String = "converters.number.signatureType"

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false
		val choiceValue = choices.getIgnoringCase(arg, context.getLocale())

		if (choiceValue != null) {
			this.parsed = choiceValue

			return true
		}

		try {
			val result = arg.toLong(radix)

			if (result !in choices.values) {
				throw DiscordRelayedException(
					context.translate(
						"converters.choice.invalidChoice",

						replacements = arrayOf(
							arg,
							choices.entries.joinToString { "**${it.key}** -> `${it.value}`" }
						)
					)
				)
			}

			this.parsed = result
		} catch (e: NumberFormatException) {
			val errorString = if (radix == DEFAULT_RADIX) {
				context.translate("converters.number.error.invalid.defaultBase", replacements = arrayOf(arg))
			} else {
				context.translate("converters.number.error.invalid.otherBase", replacements = arrayOf(arg, radix))
			}

			throw DiscordRelayedException(errorString)
		}

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		IntegerOptionBuilder(arg.displayName, arg.description).apply {
			required = true

			this@NumberChoiceConverter.choices.forEach { choice(it.key, it.value) }
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? IntegerOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}
