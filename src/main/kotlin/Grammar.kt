
data class Grammar(
    val non_terminals: MutableSet<String>,
    val terminals: MutableSet<String>,
    val productions: Map<String, MutableSet<String>>,
    val starting_symbol: String){

    fun isRegular():Boolean {

        if (!this.isRightLinear())
            return false

        // verify epsilon productions
        var hasEpsilon = false
        productions.forEach{
            if (it.value.contains("eps")){
                hasEpsilon = true
                if (it.key != starting_symbol)
                    return false
            }
            if (it.value.contains(starting_symbol) && hasEpsilon)
                return false
        }
        return true
    }

    private fun isRightLinear(): Boolean {
        productions.values.forEach {
            // wrong length
            it.forEach{s->
                if (s.length > 2 && s!= "eps")
                return false
            }
            val simple = it.filter { s -> s.length == 1 }
            // if any production has a single non_terminal
            if( simple.intersect(non_terminals.asIterable()).isNotEmpty())
                return false
            val double = it.filter { s -> s.length == 2 }
            double.forEach{ s ->
                val first: String = s[0].toString()
                val second: String = s[1].toString()
                if (first in non_terminals || second in terminals)
                    return false
            }
        }

        return true
    }
}