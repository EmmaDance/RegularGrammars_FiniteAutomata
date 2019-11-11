import java.io.File

fun main(args: Array<String>) {
    buildFA(readFileLineByLine("FA.txt"))
    start()
}

fun start(){
    val grammar = buildGrammar(readFileLineByLine("grammar.txt"))
    val FA = buildFA(readFileLineByLine("FA.txt"))
    var ctrl = -1
    while (ctrl != 0){
        printMenu()
        ctrl = readLine()?.toInt() ?: continue
        when (ctrl){
            0 -> println("Bye!")
            1 -> showNonTerminals(grammar)
            2 -> showTerminals(grammar)
            3 -> showAllProductions(grammar)
            4 -> {
                println("Enter the non-terminal:")
                val symbol = readLine()
                symbol?.let{
                    showProductionsOf(symbol, grammar)
                }
            }
            5 -> showStates(FA)
            6 -> showAlphabet(FA)
            7 -> showAllTransitions(FA)
            8 -> showFinalStates(FA)
            9 -> verify(grammar)
            10 -> buildFAFromGrammar(grammar)
            11 -> buildGrammarFromFA(FA)
        }
    }
}

fun buildGrammarFromFA(fa: FA) {
    val non_terminals: MutableSet<String> = fa.states
    val terminals: MutableSet<String> = fa.alphabet
    val productions: MutableMap<String, MutableSet<String>> = HashMap()
    val starting_symbol: String = fa.initial_state

    fa.transitions.forEach{
        val lhs = it.key.first
        val rhs1 = it.key.second
        val set: MutableSet<String> = HashSet()
        if (lhs == fa.initial_state && lhs in fa.final_states)
            set.add("eps")
        it.value.forEach{ s ->
            set.add(rhs1+s)
            if (s in fa.final_states)
                set.add(rhs1)
        }
        val existing = productions.putIfAbsent(lhs, set)
        if (existing!= null){
            existing.addAll(set)
            productions[lhs] = existing
        }
    }
    val grammar = Grammar(non_terminals, terminals, productions, starting_symbol)
    println("The grammar corresponding to the given FA is: ")
    showNonTerminals(grammar)
    showTerminals(grammar)
    showAllProductions(grammar)
    println("The starting Symbol is $starting_symbol")

}

fun buildFAFromGrammar(grammar: Grammar) {
    if (!grammar.isRegular())
        throw Error("Cannot build FA from irregular grammar")
    val states: MutableSet<String> = HashSet()
    val alphabet: MutableSet<String> = HashSet()
    val transitions: MutableMap<Pair<String, String>, MutableSet<String>> = HashMap()
    val initial_state: String
    val final_states: MutableSet<String> = HashSet()
    states.addAll(grammar.non_terminals)
    states.add("K")
    alphabet.addAll(grammar.terminals)
    initial_state = grammar.starting_symbol
    final_states.add("K")
    grammar.productions.forEach{
        val lhs = it.key
        val rhs = it.value
        rhs.forEach{ s ->
            val set: MutableSet<String> = HashSet()
            if (lhs == grammar.starting_symbol && s == ("eps"))
                final_states.add(grammar.starting_symbol)
            else{
                val key = Pair(lhs, s[0].toString())
                var value  = "K"
                if (s.length > 1 && s!="eps") value = s[1].toString()
                set.add(value)
                val oldSet = transitions.putIfAbsent(key, set)
                if (oldSet!=null)
                {
                    oldSet.add(value)
                    transitions.put(key, oldSet)
                }
            }
        }
    }
    val fa = FA(states, alphabet, transitions, initial_state, final_states)
    println("The FA corresponding to the given grammar is: ")
    showStates(fa)
    showAlphabet(fa)
    showFinalStates(fa)
    showAllTransitions(fa)
}

fun verify(grammar: Grammar) {
    if(grammar.isRegular())
        println("The grammar is regular")
    else
        println("The grammar is not regular")
}

fun showFinalStates(fa: FA) {
    println("Final States")
    fa.final_states.forEach(::println)
}

fun showAllTransitions(fa: FA) {
    println("Transitions")
    fa.transitions.forEach{
        println("delta(${it.key.first}, ${it.key.second}) = ${it.value}" )
    }
}

fun showAlphabet(fa: FA) {
    println("Alphabet")
    fa.alphabet.forEach(::println)
}

fun showStates(fa: FA) {
    println("States")
    fa.states.forEach(::println)
}

fun showProductionsOf(symbol: String, grammar: Grammar) {
    println("Productions of $symbol")
    grammar.productions.filter { it.key == symbol }.forEach(::println)
}

fun showAllProductions(grammar: Grammar) {
    println("Productions")
    grammar.productions.forEach(::println)
}

fun showTerminals(grammar: Grammar) {
    println("Terminals")
    grammar.terminals.forEach(::println)
}

fun showNonTerminals(grammar: Grammar) {
    println("Non-terminals")
    grammar.non_terminals.forEach(::println)
}

private fun readFromKeyboard(): ArrayList<String> {
    val lines: ArrayList<String> = ArrayList()
    println("Enter the set of non-terminals, separated by space: ")
    var line = readLine()!!
    lines.add(line)
    println("Enter the set of terminals, separated by space: ")
    line = readLine()!!
    lines.add(line)
    println("Enter the starting symbol (must be from set of non-terminals: ")
    line = readLine()!!
    lines.add(line)
    println("Enter the set of productions in the followong format: ")
    println("S a aA (for the productions S->a|aA)")
    println("When you want to finish entering productions, enter 0 on a new line.")

    while (line != "0") {
        line = readLine()!!
        lines.add(line)
    }
    return lines
}

fun readFileLineByLine(fileName: String):ArrayList<String> {
    val lines: ArrayList<String> = ArrayList()
    File(fileName).forEachLine {
        lines.add(it)
    }
    return lines
}

fun buildGrammar(lines: ArrayList<String>): Grammar{
    var non_terminals: MutableSet<String> = HashSet()
    var terminals: MutableSet<String> = HashSet()
    var starting_symbol: String = ""
    var productions: MutableMap<String, MutableSet<String>> = HashMap()
    for (i in 0 until lines.size){
        when(i){
            0-> non_terminals = lines[i].split(" ").toMutableSet()
            1-> terminals = lines[i].split(" ").toMutableSet()
            2-> starting_symbol = lines[i]
            else -> {
                val line = lines[i] .split(" ")
                val lhs = line[0]
                val rhs: MutableSet<String> = HashSet()
                line.forEach {
                    if (lhs != it)
                        rhs.add(it)
                }
                productions.putIfAbsent(lhs, rhs)
            }
        }
    }
    val grammar = Grammar(non_terminals, terminals, productions, starting_symbol)
    return grammar
}

fun buildFA(lines: ArrayList<String>): FA {
    var states: MutableSet<String> = HashSet()
    var alphabet: MutableSet<String> = HashSet()
    var initial_state: String = ""
    var final_states: MutableSet<String> = HashSet()
    var transitions: MutableMap<Pair<String, String>, MutableSet<String>> = HashMap()
    for (i in 0 until lines.size) {
        when (i) {
            0 -> states = lines[i].split(" ").toMutableSet()
            1 -> alphabet = lines[i].split(" ").toMutableSet()
            2 -> initial_state = lines[i]
            3 -> final_states = lines[i].split(" ").toMutableSet()
            else -> {
                val line = lines[i].split(" ")
                val first = line[0]
                val second = line[1]
                val result = line[2]
                val existing = transitions.putIfAbsent(Pair(first, second), mutableSetOf<String>(result))
                if (existing != null){
                    existing.add(result)
                    transitions.put(Pair(first, second), existing)
                }
            }
        }
    }
    return FA(states, alphabet, transitions, initial_state, final_states)

}

fun printMenu (){
    println("MENU")
    println("1 - Show the set of non-terminals.")
    println("2 - Show the set of terminals")
    println("3 - Show the set of productions")
    println("4 - Show the productions of a certain non-terminal symbol")
    println("5 - Show the set of states.")
    println("6 - Show the alphabet")
    println("7 - Show the set of transitions")
    println("8 - Show the set of final states")
    println("9 - Verify if the grammar is regular.")
    println("10 - Given a regular grammar, construct the FA")
    println("11 - Given a FA, construct a regular grammar")
    println("0 - Exit")
}