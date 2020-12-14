package ar.edu.unq.eperdemic.persistencia.dao.neo4j

import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.services.runner.Neo4JTransaction

class Neo4JDataDAO : DataDAO{
    override fun clear() {
        val tx = Neo4JTransaction.currentTransaction
        val query = """MATCH (n)
                        DETACH DELETE n"""
        tx.run(query)
    }
}