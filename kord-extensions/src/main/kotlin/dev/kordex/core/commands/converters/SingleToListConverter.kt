/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters

import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.parser.StringParser

/**
 * A special [ListConverter] that wraps a [SingleConverter], effectively turning it into a list-handling converter
 * with the same logic.
 *
 * The behaviours specified in [ListConverter] also apply to this converter, so it's worth reading about it.
 *
 * @param singleConverter The [SingleConverter] to wrap.
 *
 * @param newSignatureTypeString An optional signature type string to override the one set in [singleConverter].
 * @param newShowTypeInSignature An optional boolean to override the [showTypeInSignature] setting set in
 * [singleConverter].
 * @param newErrorTypeString An optional error type string to override the one set in [singleConverter].
 */
public class SingleToListConverter<T : Any>(
	required: Boolean = true,
	public val singleConverter: SingleConverter<T>,

	newSignatureTypeString: String? = null,
	newShowTypeInSignature: Boolean? = null,
	newErrorTypeString: String? = null,

	override var validator: Validator<List<T>> = null,
) : ListConverter<T>(required) {
	override val signatureTypeString: String = newSignatureTypeString ?: singleConverter.signatureTypeString
	override val showTypeInSignature: Boolean = newShowTypeInSignature ?: singleConverter.showTypeInSignature
	override val errorTypeString: String? = newErrorTypeString ?: singleConverter.errorTypeString

	private val dummyArgs = Arguments()

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
		val values = mutableListOf<T>()

		if (named == null) {
			while (true) {
				val arg = parser?.peekNext()?.data

				try {
					val result = singleConverter.parse(null, context, arg)

					if (!result) {
						break
					}

					val value = singleConverter.getValue(dummyArgs, singleConverter::parsed)

					values.add(value)

					parser?.parseNext()  // Move the cursor ahead
				} catch (e: DiscordRelayedException) {
					break
				}
			}
		} else {
			for (arg in named) {
				try {
					val result = singleConverter.parse(null, context, arg)

					if (!result) {
						break
					}

					val value = singleConverter.getValue(dummyArgs, singleConverter::parsed)

					values.add(value)
				} catch (e: DiscordRelayedException) {
					break
				}
			}
		}

		parsed = values

		return parsed.size
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
		singleConverter.toSlashOption(arg)

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val result = singleConverter.parseOption(context, option)

		if (result) {
			parsed = listOf(singleConverter.getValue(dummyArgs, singleConverter::parsed))
		}

		return result
	}

	override suspend fun handleError(
		t: Throwable,
		context: CommandContext,
	): String = singleConverter.handleError(t, context)
}
