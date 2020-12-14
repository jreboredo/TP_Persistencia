package ar.edu.unq.eperdemic.services.runner


object TransactionRunner {
    private var transactions:List<Transaction> = listOf(HibernateTransaction(), Neo4JTransaction()/*, MongoTransaction()*/)

    fun <T> runTrx(bloque: ()->T): T {
        try{
            transactions.forEach { it.start() }
            val result = bloque()
            transactions.forEach { it.commit() }
            return result
        } catch (exception:Throwable){
            transactions.forEach { it.rollback() }
            throw exception
        }
    }
}