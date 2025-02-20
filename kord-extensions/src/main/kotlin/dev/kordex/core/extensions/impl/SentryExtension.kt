/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.extensions.impl

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.extensions.SentryExtensionBuilder
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.coalescingString
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.chatCommand
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.sentry.SentryAdapter
import dev.kordex.core.sentry.sentryId
import dev.kordex.core.utils.respond
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId
import org.koin.core.component.inject

public const val SENTRY_EXTENSION_NAME: String = "kordex.sentry"

/**
 * Extension providing a feedback command for use with the Sentry integration.
 *
 * Even if you add this extension manually, it won't do anything unless you've set up the Sentry integration.
 */
public class SentryExtension : Extension() {
	override val name: String = SENTRY_EXTENSION_NAME

	/** Sentry adapter, for easy access to Sentry functions. **/
	public val sentryAdapter: SentryAdapter by inject()

	/** Bot settings. **/
	public val botSettings: ExtensibleBotBuilder by inject()

	/** Sentry extension settings, from the bot builder. **/
	public val sentrySettings: SentryExtensionBuilder =
		botSettings.extensionsBuilder.sentryExtensionBuilder

	@Suppress("StringLiteralDuplication")  // It's the command name
	override suspend fun setup() {
		if (sentryAdapter.enabled) {
			ephemeralSlashCommand(::FeedbackSlashArgs) {
				name = "extensions.sentry.commandName"
				description = "extensions.sentry.commandDescription.short"

				action {
					if (!sentry.adapter.hasEventId(arguments.id)) {
						respond {
							content = translate("extensions.sentry.error.invalidId")
						}

						return@action
					}

					val feedback = UserFeedback(
						arguments.id,
						member!!.asMember().tag,
						member!!.id.toString(),
						arguments.feedback
					)

					sentry.captureFeedback(feedback)
					sentry.adapter.removeEventId(arguments.id)

					respond {
						content = translate("extensions.sentry.thanks")
					}
				}
			}

			chatCommand(::FeedbackMessageArgs) {
				name = "extensions.sentry.commandName"
				description = "extensions.sentry.commandDescription.long"

				aliasKey = "extensions.sentry.commandAliases"

				action {
					if (!sentry.adapter.hasEventId(arguments.id)) {
						message.respond(
							translate("extensions.sentry.error.invalidId"),
							pingInReply = sentrySettings.pingInReply
						)

						return@action
					}

					val author = message.author!!

					sentry.adapter.sendFeedback(
						arguments.id,
						arguments.feedback,
						author.id.toString(),
					)

					message.respond(
						translate("extensions.sentry.thanks")
					)
				}
			}
		}
	}

	/** Arguments for the feedback command. **/
	public class FeedbackMessageArgs : Arguments() {
		/** Sentry event ID. **/
		public val id: SentryId by sentryId {
			name = "id"
			description = "extensions.sentry.arguments.id"
		}

		/** Feedback message to submit to Sentry. **/
		public val feedback: String by coalescingString {
			name = "feedback"
			description = "extensions.sentry.arguments.feedback"
		}
	}

	/** Arguments for the feedback command. **/
	public class FeedbackSlashArgs : Arguments() {
		/** Sentry event ID. **/
		public val id: SentryId by sentryId {
			name = "id"
			description = "extensions.sentry.arguments.id"
		}

		/** Feedback message to submit to Sentry. **/
		public val feedback: String by string {
			name = "feedback"
			description = "extensions.sentry.arguments.feedback"
		}
	}
}
