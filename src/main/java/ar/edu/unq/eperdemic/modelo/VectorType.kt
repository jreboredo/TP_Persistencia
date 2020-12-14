package ar.edu.unq.eperdemic.modelo

enum class VectorType(val valor: String) {
    Animal("Animal"){
        override fun caminosValidos(): String {
            return "Aereo | Terrestre | Maritimo"
        }

    },
    Insecto("Insecto"){
        override fun caminosValidos(): String {
            return "Aereo | Terrestre"
        }

    },
    Humano("Humano"){
        override fun caminosValidos(): String {
            return "Terrestre | Maritimo"
        }

    };

    abstract fun caminosValidos(): String

}