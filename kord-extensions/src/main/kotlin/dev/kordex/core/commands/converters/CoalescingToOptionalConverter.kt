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
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.parser.StringParser

/**
 * A special [OptionalConverter] that wraps a [SingleConverter], effectively turning it into an optional
 * converter with the same logic.
 *
 * The behaviours specified in [OptionalConverter] also apply to this converter, so it's worth reading about it.
 *
 * @param coalescingConverter The [SingleConverter] to wrap.
 *
 * @param newSignatureTypeString An optional signature type string to override the one set in [coalescingConverter].
 * @param newShowTypeInSignature An optional boolean to override the [showTypeInSignature] setting set in
 * [coalescingConverter].
 * @param newErrorTypeString An optional error type string to override the one set in [coalescingConverter].
 */
public class CoalescingToOptionalConverter<T : Any>(
	public val coalescingConverter: CoalescingConverter<T>,

	newSignatureTypeString: String? = null,
	newShowTypeInSignature: Boolean? = null,
	newErrorTypeString: String? = null,
	outputError: Boolean = false,

	override var validator: Validator<T?> = null,
) : OptionalCoalescingConverter<T>(outputError) {
	override val signatureTypeString: String = newSignatureTypeString ?: coalescingConverter.signatureTypeString
	override val showTypeInSignature: Boolean = newShowTypeInSignature ?: coalescingConverter.showTypeInSignature
	override val errorTypeString: String? = newErrorTypeString ?: coalescingConverter.errorTypeString

	private val dummyArgs = Arguments()

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
		val result = coalescingConverter.parse(parser, context, named)

		if (result > 0) {
			this.parsed = coalescingConverter.getValue(dummyArgs, coalescingConverter::parsed)
		}

		return result
	}

	override suspend fun handleError(
		t: Throwable,
		context: CommandContext,
	): String = coalescingConverter.handleError(t, context)

	override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder {
		val option = coalescingConverter.toSlashOption(arg)
		option.required = false

		return option
	}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val result = coalescingConverter.parseOption(context, option)

		if (result) {
			this.parsed = coalescingConverter.getValue(dummyArgs, coalescingConverter::parsed)
		}

		return result
	}
}
