/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.core.entity.interaction.BooleanOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.BooleanBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.i18n.DEFAULT_KORDEX_BUNDLE
import dev.kordex.core.utils.parseBoolean
import dev.kordex.parser.StringParser

/**
 * Argument converter for [Boolean] arguments.
 *
 * Truthiness is determined by the [parseBoolean] function.
 */
@Converter(
	"boolean",

	types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
public class BooleanConverter(
	override var validator: Validator<Boolean> = null,
) : SingleConverter<Boolean>() {
	public override val signatureTypeString: String = "converters.boolean.signatureType"
	public override val errorTypeString: String = "converters.boolean.errorType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
		val arg: String = named ?: parser?.parseNext()?.data ?: return false
		val bool: Boolean = arg.parseBoolean(context) ?: return false

		this.parsed = bool

		return true
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		BooleanBuilder(arg.displayName, arg.description).apply { required = true }

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? BooleanOptionValue)?.value ?: return false
		this.parsed = optionValue

		return true
	}
}
