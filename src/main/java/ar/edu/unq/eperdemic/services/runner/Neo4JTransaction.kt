package ar.edu.unq.eperdemic.services.runner

import org.neo4j.driver.*

class Neo4JTransaction: Transaction{
    private val driver: Driver

    companion object {
        private var transaction: org.neo4j.driver.Transaction? = null
        val currentTransaction: org.neo4j.driver.Transaction
            get() {
                if (transaction == null) {
                    throw RuntimeException("No hay ninguna tx en el contexto")
                }
                return transaction!!
            }
    }

    init {
        val env = System.getenv()
        val url = env.getOrDefault("NEO4J_URL", "bolt://localhost:7687")
        val username = env.getOrDefault("NEO4J_USER", "neo4j")
        val password = env.getOrDefault("NEO4J_PASSWORD", "root")
        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password),
                Config.builder().withLogging(Logging.slf4j()).build()
        )
    }

    override fun start() {
        transaction = driver.session().beginTransaction()
    }

    override fun commit() {
        transaction?.commit()
    }

    override fun rollback() {
        transaction?.rollback()
    }

}