package com.example.data.repository

enum class AgentTurnPhase {
    REQUESTING, STREAMING, CHECKING_TOOL, AWAITING_APPROVAL, RUNNING_TOOL, RESUMING,
}

data class AgentActivity(
    val phase: AgentTurnPhase,
    val toolName: String? = null,
    val action: AgentActionLog? = null,
)

/** Only locally observed events — never the model's private reasoning or raw tool arguments. */
data class AgentTurnProgress internal constructor(
    val messageId: String,
    val startedAtMillis: Long,
    internal val activity: AgentActivity = AgentActivity(AgentTurnPhase.REQUESTING),
    val tools: List<AgentActionLog> = emptyList(),
) {
    internal fun record(event: AgentActivity): AgentTurnProgress = copy(
        activity = event,
        tools = event.action?.let { tools + it } ?: tools,
    )
}

/** Late events can update only the turn still owned by this conversation. */
internal fun AgentTurnProgress?.belongsTo(id: String): Boolean = this?.messageId == id

/** Accumulates display-safe text deltas and calls even if the last chunk carries neither. */
internal class AgentReplyBuffer<Call>(private val callKey: (Call) -> Any) {
    private val text = StringBuilder()
    private val calls = linkedMapOf<Any, Call>()
    var received: Boolean = false
        private set
    val reply: String get() = text.toString()
    val requestedCalls: List<Call> get() = calls.values.toList()

    fun append(delta: String?, toolCalls: List<Call>) {
        received = true
        if (delta != null) text.append(delta)
        for (call in toolCalls) calls[callKey(call)] = call
    }
}
