/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.dev.unsafe.contexts

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kordex.core.commands.application.message.MessageCommand
import dev.kordex.core.commands.application.message.MessageCommandContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeCommandInteractionContext
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm

/** Command context for an unsafe message command. **/
@UnsafeAPI
public class UnsafeCommandMessageCommandContext<M : UnsafeModalForm>(
	override val event: MessageCommandInteractionCreateEvent,
	override val command: MessageCommand<UnsafeCommandMessageCommandContext<M>, M>,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : MessageCommandContext<UnsafeCommandMessageCommandContext<M>, M>(event, command, cache),
    UnsafeCommandInteractionContext
