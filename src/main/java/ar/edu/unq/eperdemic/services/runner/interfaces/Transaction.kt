package ar.edu.unq.eperdemic.services.runner.interfaces

interface Transaction {
    fun start()
    fun commit()
    fun rollback()
}