/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("StringLiteralDuplication")

package dev.kordex.test.bot.extensions

import dev.kord.common.asJavaLocale
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.checks.types.CheckContextWithCache
import dev.kordex.core.checks.userFor
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.group
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.int
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.publicSlashCommand

public class I18nTestExtension : Extension() {
	override val name: String = "kordex.test-i18n"
	override val bundle: String = "test"

	override suspend fun setup() {
		publicSlashCommand {
			name = "command.banana-flat"
			description = "Translated banana"

			action {
				val commandLocale = getLocale()
				val interactionLocale = event.interaction.locale?.asJavaLocale()

				assert(commandLocale == interactionLocale) {
					"Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
				}

				respond { content = "Text: ${translate("command.banana")}" }
			}
		}

		publicSlashCommand {
			name = "command.banana-sub"
			description = "Translated banana subcommand"

			publicSubCommand {
				name = "command.banana"
				description = "Translated banana"

				action {
					val commandLocale = getLocale()
					val interactionLocale = event.interaction.locale?.asJavaLocale()

					assert(commandLocale == interactionLocale) {
						"Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
					}

					respond { content = "Text: ${translate("command.banana")}" }
				}
			}
		}

		publicSlashCommand {
			name = "command.banana-group"
			description = "Translated banana group"

			group("command.banana") {
				description = "Translated banana group"

				publicSubCommand {
					name = "command.banana"
					description = "Translated banana"

					action {
						val commandLocale = getLocale()
						val interactionLocale = event.interaction.locale?.asJavaLocale()

						assert(commandLocale == interactionLocale) {
							"Command locale (`$commandLocale`) does not match interaction locale (`$interactionLocale`)"
						}

						respond { content = "Text: ${translate("command.banana")}" }
					}
				}
			}
		}

		publicSlashCommand(::I18nTestArguments) {
			name = "command.fruit"
			description = "command.fruit"

			action {
				respond {
					content = translate("command.fruit.response", arrayOf(arguments.fruit))
				}
			}
		}

		publicSlashCommand(::I18nTestNamedArguments) {
			name = "command.apple"
			description = "command.apple"

			action {
				respond {
					content = translate(
						"command.apple.response",

						mapOf(
							"name" to arguments.name,
							"appleCount" to arguments.count
						)
					)
				}
			}
		}

		ephemeralSlashCommand {
			name = "test-translated-checks"
			description = "Command that always fails, to check CheckContext translations."

			check {
				translatedChecks()
			}

			action {
				// This command is expected to always fail, in order to test checks.
				respond {
					content = "It is impossible to get here."
				}
			}
		}

		ephemeralSlashCommand(::I18nTestValidations) {
			name = "test-translated-validations"
			description = "Command with arguments that always fail validations."

			action {
				// This command is expected to always fail, in order to test argument validations.
				respond {
					content = "It is impossible to get here."
				}
			}
		}
	}

	private suspend fun CheckContextWithCache<ChatInputCommandInteractionCreateEvent>.translatedChecks() {
		val user = userFor(event)
		this.defaultBundle = bundle

		if (user == null) {
			fail("Could not get user.")
			return
		}

		fail(
			buildList {
				// Translate, with default bundle
				add(translate("check.simple"))
				// Translate with a different bundle
				add(translate("check.simple", "custom"))
				// Translate with default bundle, and positional parameters
				add(translate("check.positionalParameters", arrayOf(user.mention, user.id)))
				// Translate with a different bundle, and positional parameters
				add(translate("check.positionalParameters", "custom", arrayOf(user.mention, user.id)))
				// Translate with default bundle, named parameters
				add(translate("check.namedParameters", replacements = mapOf("user" to user.mention, "id" to user.id)))
				// Translate with a different bundle, and named parameters
				add(translate("check.namedParameters", "custom", mapOf("user" to user.mention, "id" to user.id)))
			}.joinToString("\n")
		)
	}

	private inner class I18nTestValidations : Arguments() {
		val name by string {
			name = "name"
			description = "Will always fail to validate."
			validate {
				defaultBundle = bundle
				fail(
					buildList {
						// Translate, with default bundle
						add(translate("validation.simple"))
						// Translate with a different bundle
						add(translate("validation.simple", "custom"))
						// Translate with default bundle, and positional parameters
						add(translate("validation.positionalParameters", arrayOf(value)))
						// Translate with a different bundle, and positional parameters
						add(translate("validation.positionalParameters", "custom", arrayOf(value)))
						// Translate with default bundle, named parameters
						add(translate("validation.namedParameters", mapOf("value" to value)))
						// Translate with a different bundle, and named parameters
						add(translate("validation.namedParameters", "custom", mapOf("value" to value)))
					}.joinToString("\n")
				)
			}
		}
	}
}

internal class I18nTestArguments : Arguments() {
	val fruit by string {
		name = "command.fruit"
		description = "command.fruit"

		autoComplete {
			suggestString {
				listOf("Banana", "Apple", "Cherry").forEach { choice(it, it) }
			}
		}
	}
}

internal class I18nTestNamedArguments : Arguments() {
	val name by string {
		name = "command.apple.argument.name"
		description = "command.apple.argument.name"
	}

	val count by int {
		name = "command.apple.argument.count"
		description = "command.apple.argument.count"
	}
}
