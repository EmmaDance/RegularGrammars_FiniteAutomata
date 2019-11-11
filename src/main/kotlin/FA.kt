data class FA (
    val states: MutableSet<String>,
    val alphabet: MutableSet<String>,
    val transitions: MutableMap<Pair<String, String>, MutableSet<String>>,
    val initial_state: String,
    val final_states: MutableSet<String>)