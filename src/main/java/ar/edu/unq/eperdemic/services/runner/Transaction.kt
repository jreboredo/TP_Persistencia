package ar.edu.unq.eperdemic.services.runner

interface Transaction {
    fun start()
    fun commit()
    fun rollback()
}