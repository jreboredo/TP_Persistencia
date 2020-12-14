package ar.edu.unq.eperdemic.persistencia.dao.neo4j

import ar.edu.unq.eperdemic.excepciones.ImposibleMoverseException
import ar.edu.unq.eperdemic.excepciones.UbicacionNoAlcanzableException
import ar.edu.unq.eperdemic.modelo.TipoDeCamino
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.runner.Neo4JTransaction
import org.neo4j.driver.Record
import org.neo4j.driver.Transaction
import org.neo4j.driver.Value
import org.neo4j.driver.Values

class Neo4JUbicacionDAO : UbicacionDAO {

    private fun consumeRun(tx : Transaction, query : String, parameters: Value){
        tx.run(query, parameters).consume()
    }

    override fun guardar(ubicacion : Ubicacion) {
        val tx = Neo4JTransaction.currentTransaction
        val query = "MERGE (ubicacion: Ubicacion {nombre: ${'$'}nombre})"
        val params = Values.parameters("nombre", ubicacion.nombre)

        consumeRun(tx, query, params)
    }

    override fun recuperar(nombreDeUbicacion: String): Ubicacion {
        val tx = Neo4JTransaction.currentTransaction
        val query = "MATCH (ubicacion:Ubicacion {nombre: ${'$'}nombre}) RETURN ubicacion"
        val result = tx.run(query, Values.parameters("nombre", nombreDeUbicacion))
        return Ubicacion(result.single()[0]["nombre"].asString())
    }

    override fun recuperarATodos(): List<Ubicacion> {
        val tx = Neo4JTransaction.currentTransaction
        val query = "MATCH (ubicacion:Ubicacion) RETURN ubicacion"
        val result = tx.run(query)
        return result.list { record: Record ->
            val nombre = record[0]["nombre"].asString()
            Ubicacion(nombre)
        }
    }

    fun conectar(ubicacion1: String, ubicacion2: String, tipoCamino: TipoDeCamino) {
        val tx = Neo4JTransaction.currentTransaction
        val query = """MATCH (ubicacion1:Ubicacion {nombre: ${'$'}nombreUbic1})
                       MATCH (ubicacion2:Ubicacion {nombre: ${'$'}nombreUbic2})
                       MERGE (ubicacion1)-[:${tipoCamino}]->(ubicacion2)
                    """
        val params = Values.parameters("nombreUbic1", ubicacion1, "nombreUbic2", ubicacion2)
        consumeRun(tx, query, params)
    }

    fun conectados(nombreDeUbicacion:String): List<Ubicacion>{
        val tx = Neo4JTransaction.currentTransaction
        val query = """
                        MATCH (ubicacion:Ubicacion {nombre: ${'$'}nombreDeUbicacion})
                        MATCH (ubicacion)-[]->(conectadas)
                        RETURN conectadas
                    """
        var result = tx.run(query, Values.parameters("nombreDeUbicacion", nombreDeUbicacion))
        return result.list { record: Record ->
            val nombre = record[0]["nombre"].asString()
            Ubicacion(nombre)
        }
    }

    fun puedeMover(vector: Vector, nuevaUbicacion: Ubicacion): Boolean {
        try {
            val tx = Neo4JTransaction.currentTransaction
            var caminosValidos = vector.tipo!!.caminosValidos()
            val query = """
                        MATCH (:Ubicacion {nombre: ${'$'}ubicacionActual})-[relacion: $caminosValidos]->(anexo:Ubicacion{nombre: ${'$'}nuevaUbicacion})
                        RETURN anexo.nombre
                    """
            val result = tx.run(query, Values.parameters("ubicacionActual", vector.ubicacionActual!!.nombre, "nuevaUbicacion", nuevaUbicacion.nombre))
            var nextResult = result.single()
            nextResult[0].asString()

            return true
        }catch(e:Exception){
            throw ImposibleMoverseException("No es posible moverse a esa ubicacion")
        }
    }

    fun moverMasCorto(vector:Vector, nombreDeUbicacion: String): Vector{
        try{
        val tx = Neo4JTransaction.currentTransaction
        var caminosValidos = vector.tipo!!.caminosValidos()
        val query = """
                        MATCH (actual:Ubicacion {nombre: ${'$'}ubicacionActual}),(tgt:Ubicacion{nombre: ${'$'}nuevaUbicacion}), 
                            p = shortestPath((actual)-[: $caminosValidos*]-(tgt))
                        RETURN length(p)
                    """
        val result = tx.run(query, Values.parameters("ubicacionActual", vector.ubicacionActual!!.nombre,
                "nuevaUbicacion", nombreDeUbicacion))
        result.single().get(0).asInt()
            vector.ubicacionActual = Ubicacion(nombreDeUbicacion)
            return vector
        }catch(e:Exception){
            throw UbicacionNoAlcanzableException("La Ubicacion es Inalcanzable")
        }
    }

    fun capacidadDeExpansion(vector: Vector, movimientos: Int): Int {
        val tx = Neo4JTransaction.currentTransaction
        var caminosValidos = vector.tipo!!.caminosValidos()
        val query = """
                        MATCH (root:Ubicacion { nombre: ${'$'}ubicacionActual })-[: $caminosValidos*0..$movimientos]-(ub:Ubicacion)
                        RETURN DISTINCT ub.nombre
                    """
        val result = tx.run(query,
                Values.parameters("ubicacionActual", vector.ubicacionActual!!.nombre))
        var ret = result.list()
        return ret.size - 1 // Le resto el propio nodo a la lista obtenida
    }
}