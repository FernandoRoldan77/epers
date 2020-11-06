package ar.edu.unq.eperdemic.mongoDB

import ar.edu.unq.eperdemic.dao.hibernate.impl.EspecieHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.PatogenoHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.UbicacionHibernateDAO
import ar.edu.unq.eperdemic.dao.hibernate.impl.VectorDAOImpl
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.probabilidad.ProbabilidadDeContagio
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.enums.TipoCamino
import ar.edu.unq.eperdemic.modelo.enums.TipoEvento
import ar.edu.unq.eperdemic.services.impl.EspecieServiceImpl
import ar.edu.unq.eperdemic.services.impl.FeedServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class UbicacionFeedTest {

    private val ubicacionHibernateDAO = UbicacionHibernateDAO()
    private val ubicacionNeo4jDAO = UbicacionNeo4jDAO()
    private val feedServiceImpl = FeedServiceImpl(FeedMongoImplDAO(), UbicacionNeo4jDAO())
    private val vectorDAOImpl = VectorDAOImpl()
    private val vectorServiceImpl = VectorServiceImpl(vectorDAOImpl, FeedMongoImplDAO())
    private val ubicacionServiceImpl = UbicacionServiceImpl(ubicacionHibernateDAO, ubicacionNeo4jDAO, vectorDAOImpl, FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())
    val estadoMock: ProbabilidadDeContagio = Mockito.spy(ProbabilidadDeContagio::class.java)

    var vectorHumano = Vector(Humano())
    var vectorAnimal = Vector(Animal())
    var vectorInsecto = Vector(Insecto())
    var vectorInsectoContagio = Vector(Insecto())

    val ubicacionWilde = ubicacionServiceImpl.crear("Wilde")
    val ubicacionQuilmes = ubicacionServiceImpl.crear("Quilmes")


    val vectorHumano2 = Vector(Humano())
    val donBosco = ubicacionServiceImpl.crear("Don Bosco")
    val bernal = ubicacionServiceImpl.crear("Bernal")
    val dominico = ubicacionServiceImpl.crear("Dominico")
    val sarandi = ubicacionServiceImpl.crear("Sarandi")
    val espeleta = ubicacionServiceImpl.crear("Espeleta")


    val bosques = ubicacionServiceImpl.crear("Bosques")
    val zeballos = ubicacionServiceImpl.crear("Zeballos")
    val varela = ubicacionServiceImpl.crear("Varela")
    val covid = Especie(Patogeno("Virus"), "Covid", "china")
    val sars = Especie(Patogeno("Virus"), "Sars", "china")


    @Before
    fun setUp() {


        vectorHumano.moverAUbicacion(ubicacionQuilmes)
        vectorServiceImpl.crear(vectorHumano)
        ubicacionServiceImpl.conectar("Quilmes", "Wilde", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.mover(vectorHumano.id!!, ubicacionWilde.nombreUbicacion)

        especieServiceImpl.crear(sars)

        ubicacionServiceImpl.conectar("Don Bosco", "Bernal", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Bernal", "Don Bosco", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Bernal", "Dominico", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Dominico", "Sarandi", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Bernal", "Sarandi", TipoCamino.Terrestre.name)

        ubicacionServiceImpl.conectar("Sarandi", "Bernal", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Sarandi", "Dominico", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Espeleta", "Sarandi", TipoCamino.Terrestre.name)

        ubicacionServiceImpl.conectar("Sarandi", "Espeleta", TipoCamino.Terrestre.name)

        vectorHumano2.moverAUbicacion((bernal))
        vectorServiceImpl.crear(vectorHumano2)
        vectorHumano2.moverAUbicacion(dominico)
        ubicacionServiceImpl.mover(vectorHumano2.id!!, dominico.nombreUbicacion)
        vectorHumano2.moverAUbicacion(sarandi)
        ubicacionServiceImpl.mover(vectorHumano2.id!!, sarandi.nombreUbicacion)

        vectorInsecto.moverAUbicacion(espeleta)

        vectorAnimal.moverAUbicacion(espeleta)
        vectorServiceImpl.crear(vectorAnimal)
        ubicacionServiceImpl.conectar("Bosques", "Zeballos", TipoCamino.Terrestre.name)
        ubicacionServiceImpl.conectar("Bosques", "Varela", TipoCamino.Terrestre.name)

        especieServiceImpl.crear(covid)
        setDesignadorDeEstadoMock(vectorAnimal)
        setDesignadorDeEstadoMock(vectorHumano)
        setDesignadorDeEstadoMock(vectorInsecto)
        setDesignadorDeEstadoMock(vectorInsectoContagio)

        vectorInsectoContagio.moverAUbicacion(ubicacionWilde)
        vectorServiceImpl.crear(vectorInsectoContagio)
        vectorServiceImpl.infectar(vectorInsectoContagio, covid)

        vectorServiceImpl.crear(vectorAnimal)
        vectorServiceImpl.crear(vectorInsecto)
        vectorServiceImpl.infectar(vectorAnimal, covid)
        vectorServiceImpl.contagiar(vectorAnimal, listOf(vectorHumano, vectorInsecto))

        vectorServiceImpl.infectar(vectorHumano, covid)

        vectorServiceImpl.contagiar(vectorInsectoContagio, listOf(vectorAnimal))
    }

    private fun setDesignadorDeEstadoMock(aVector: Vector) {
        Mockito.`when`(estadoMock.calcularFactorContagio(aVector.getTipoBiologico(), aVector.especies)).thenReturn(100)
        aVector.setProbabilidad(estadoMock)
    }

    @Test
    fun moverVectorAUbicacionWildeYRegistrarElArribo() {
        val eventosUbicacion = feedServiceImpl.feedUbicacion(vectorHumano.ubicacion!!.nombreUbicacion).filter {
            it.tipoEvento == TipoEvento.Arribo.name
        }

        Assert.assertEquals(ubicacionWilde.nombreUbicacion,
                eventosUbicacion.first().obtenerUbicacion()
        )
    }

    @Test
    fun seRegistranTodasLasUbicacionesConectadas() {
        val ubicacionDondeEstaVectorHumano2 = vectorHumano2.ubicacion!!.nombreUbicacion
        val eventosUbicacion = feedServiceImpl.feedUbicacion(ubicacionDondeEstaVectorHumano2).filter {
            it.tipoEvento == TipoEvento.Arribo.name && it.obtenerVector() == 0
        }
        assert(
                listOf(sarandi.nombreUbicacion, dominico.nombreUbicacion).all { ubicacion ->
                    eventosUbicacion.any {
                        it.obtenerUbicacion() == ubicacion
                    }
                }
        )
    }

    @Test
    fun todosLosViajesQueSeHicieronALocacionSarandi() {
        val vectorBernal = Vector(Humano())
        vectorBernal.especies.add(sars)
        vectorBernal.estaContagiado = true
        vectorBernal.moverAUbicacion(donBosco)
        vectorServiceImpl.crear(vectorBernal)
        ubicacionServiceImpl.moverMasCorto(vectorBernal.id!!, bernal.nombreUbicacion)

        val eventosUbicacion = feedServiceImpl.feedUbicacion(sarandi.nombreUbicacion).filter {
            it.tipoEvento == TipoEvento.Arribo.name && it.obtenerVector() != 0
        }

        assert(listOf(vectorBernal.id, vectorHumano2.id).all {
            eventosUbicacion.any { e -> e.obtenerVector() == it }
        })
    }

    @Test
    fun todosLosViajesQueSeHicieronALocacionBernalYUbicacionesConectadas() {
        val vectorBernal = Vector(Humano())
        vectorBernal.especies.add(sars)
        vectorBernal.estaContagiado = true
        vectorBernal.moverAUbicacion(donBosco)
        vectorServiceImpl.crear(vectorBernal)
        ubicacionServiceImpl.mover(vectorBernal.id!!, bernal.nombreUbicacion)
        ubicacionServiceImpl.moverMasCorto(vectorBernal.id!!, sarandi.nombreUbicacion)

        val vectorEspeleta = Vector(Humano())
        vectorEspeleta.especies.add(sars)
        vectorEspeleta.estaContagiado = true
        vectorEspeleta.moverAUbicacion(sarandi)
        vectorServiceImpl.crear(vectorEspeleta)
        ubicacionServiceImpl.mover(vectorEspeleta.id!!, espeleta.nombreUbicacion)


        val eventosUbicacion = feedServiceImpl.feedUbicacion(bernal.nombreUbicacion).filter {
            it.tipoEvento == TipoEvento.Arribo.name && it.obtenerVector() != 0
        }

        assert(eventosUbicacion.filter { it.obtenerUbicacion() == bernal.nombreUbicacion }.any {
            it.obtenerVector() == vectorBernal.id
        })
        assert(eventosUbicacion.filter {
            it.obtenerUbicacion() == sarandi.nombreUbicacion
        }.any {
            it.obtenerVector() == vectorBernal.id || it.obtenerVector() == vectorHumano2.id
        })
        assert(eventosUbicacion.filter {
            it.obtenerUbicacion() == dominico.nombreUbicacion
        }.any {
            it.obtenerVector() == vectorHumano2.id
        })
    }

    @Test
    fun todosLosViajesQueSeHicieronALocacionSarandiYUbicacionesConectadas() {
        val vectorBernal = Vector(Humano())
        vectorBernal.especies.add(sars)
        vectorBernal.estaContagiado = true
        vectorBernal.moverAUbicacion(donBosco)
        vectorServiceImpl.crear(vectorBernal)
        ubicacionServiceImpl.mover(vectorBernal.id!!, bernal.nombreUbicacion)
        ubicacionServiceImpl.moverMasCorto(vectorBernal.id!!, sarandi.nombreUbicacion)

        val vectorEspeleta = Vector(Humano())
        vectorEspeleta.especies.add(sars)
        vectorEspeleta.estaContagiado = true
        vectorEspeleta.moverAUbicacion(sarandi)
        vectorServiceImpl.crear(vectorEspeleta)
        ubicacionServiceImpl.mover(vectorEspeleta.id!!, espeleta.nombreUbicacion)


        val eventosUbicacion = feedServiceImpl.feedUbicacion(sarandi.nombreUbicacion).filter {
            it.tipoEvento == TipoEvento.Arribo.name && it.obtenerVector() != 0
        }

        assert(eventosUbicacion.filter {
            it.obtenerUbicacion() == bernal.nombreUbicacion
        }.any {
            it.obtenerVector() == vectorBernal.id
        })
        assert(eventosUbicacion.filter {
            it.obtenerUbicacion() == espeleta.nombreUbicacion
        }.any {
            it.obtenerVector() == vectorEspeleta.id
        })
        assert(eventosUbicacion.filter {
            it.obtenerUbicacion() == dominico.nombreUbicacion
        }.any {
            it.obtenerVector() == vectorHumano2.id
        })
    }

    @Test
    fun seContagian2VectoresEnUbicacionBernal() {
        val vector1 = Vector(Humano())
        val vector2 = Vector(Humano())

        vector1.moverAUbicacion(bernal)
        vector2.moverAUbicacion(bernal)
        vectorAnimal.moverAUbicacion(bernal)
        vectorServiceImpl.crear(vector1)
        vectorServiceImpl.crear(vector2)
        vectorServiceImpl.crear(vectorAnimal)

        setDesignadorDeEstadoMock(vector1)
        setDesignadorDeEstadoMock(vector2)

        val listaVectoresAInfectar = listOf(vector1, vector2)

        vectorServiceImpl.contagiar(vectorAnimal, listaVectoresAInfectar)

        val eventosBernal = feedServiceImpl.feedUbicacion(bernal.nombreUbicacion)

        assert(listaVectoresAInfectar.all {
            eventosBernal.any { e ->
                (e.obtenerVectorContagiado() == vector1.id ||
                        e.obtenerVectorContagiado() == vector2.id)
                        && vectorServiceImpl.recuperar(e.obtenerVectorContagiado()).estaContagiado
            }
        })
    }

    @Test
    fun contagiosEnUbicacionBernalYUbicacionesLindantes() {
        val vector1 = Vector(Humano())
        val vector2 = Vector(Humano())
        val vector3 = Vector(Humano())
        val vector4 = Vector(Humano())

        vector1.moverAUbicacion(bernal)
        vector2.moverAUbicacion(bernal)
        vector3.moverAUbicacion(donBosco)
        vector4.moverAUbicacion(dominico)

        vectorAnimal.moverAUbicacion(bernal)
        vectorServiceImpl.crear(vector1)
        vectorServiceImpl.crear(vector2)
        vectorServiceImpl.crear(vector3)
        vectorServiceImpl.crear(vector4)
        vectorServiceImpl.crear(vectorAnimal)

        setDesignadorDeEstadoMock(vector1)
        setDesignadorDeEstadoMock(vector2)
        setDesignadorDeEstadoMock(vector3)
        setDesignadorDeEstadoMock(vector4)

        val listaVectoresAInfectar = listOf(vector1, vector2, vector3, vector4)

        vectorServiceImpl.contagiar(vectorAnimal, listaVectoresAInfectar.subList(0, 2))

        vectorAnimal.moverAUbicacion(donBosco)
        vectorServiceImpl.actualizar(vectorAnimal)
        vectorServiceImpl.contagiar(vectorAnimal, listaVectoresAInfectar.subList(2, 3))

        vectorAnimal.moverAUbicacion(dominico)
        vectorServiceImpl.actualizar(vectorAnimal)
        vectorServiceImpl.contagiar(vectorAnimal, listaVectoresAInfectar.subList(3, 4))

        val eventosBernalYUbicacionesLindantes = feedServiceImpl.feedUbicacion(bernal.nombreUbicacion)

        assert(listaVectoresAInfectar.all {
            eventosBernalYUbicacionesLindantes.any { e ->
                (e.obtenerVectorContagiado() == vector1.id ||
                        e.obtenerVectorContagiado() == vector2.id)
                        && vectorServiceImpl.recuperar(e.obtenerVectorContagiado()).estaContagiado
            }
        })

        val eventosDonBosco = eventosBernalYUbicacionesLindantes.filter {
            it.obtenerUbicacion() == donBosco.nombreUbicacion
        }

        assert(listaVectoresAInfectar.all {
            eventosDonBosco.any { e ->
                (e.obtenerVectorContagiado() == vector3.id)
                        && vectorServiceImpl.recuperar(e.obtenerVectorContagiado()).estaContagiado
            }
        })

        val eventosDominico = eventosBernalYUbicacionesLindantes.filter {
            it.obtenerUbicacion() == dominico.nombreUbicacion
        }

        assert(listaVectoresAInfectar.all {
            eventosDominico.any { e ->
                (e.obtenerVectorContagiado() == vector4.id) &&
                        vectorServiceImpl.recuperar(e.obtenerVectorContagiado()).estaContagiado
            }
        })
    }

    @Test
    fun `contagio que se hizo en Wilde`() {
        val contagiosEnWilde = feedServiceImpl.feedUbicacion(ubicacionWilde.nombreUbicacion).filter {
            it.tipoEvento == TipoEvento.Contagio.name && it.obtenerVector() == 0 && it.obtenerPatogeno() == ""
                    && it.obtenerUbicacion() == "Wilde"
        }
        val nombreUbicacion = contagiosEnWilde.distinctBy { it.obtenerUbicacion() }.first().obtenerUbicacion()
        Assert.assertEquals(nombreUbicacion, "Wilde")
        assert(contagiosEnWilde.size == 1)
    }

    @Test
    fun `infeccion que se hizo en Quilmes`() {
        val contagiosEnQuilmes = feedServiceImpl.feedUbicacion(ubicacionQuilmes.nombreUbicacion).filter {
            it.tipoEvento == TipoEvento.Contagio.name && it.obtenerVector() == 0 && it.obtenerPatogeno() == ""
                    && it.obtenerUbicacion() == "Quilmes"
        }
        val nombreUbicacion = contagiosEnQuilmes.distinctBy { it.obtenerUbicacion() }.first().obtenerUbicacion()
        Assert.assertEquals(nombreUbicacion, "Quilmes")
        assert(contagiosEnQuilmes.size == 1)
    }

    @Test
    fun `contagio e infeccion que se hicieron en espeleta`() {
        val contagiosEnEspeleta = feedServiceImpl.feedUbicacion(espeleta.nombreUbicacion).filter {
            it.tipoEvento == TipoEvento.Contagio.name && it.obtenerVector() == 0 && it.obtenerPatogeno() == ""
                    && it.obtenerUbicacion() == "Espeleta"
        }
        val nombreUbicacion = contagiosEnEspeleta.distinctBy { it.obtenerUbicacion() }.first().obtenerUbicacion()
        Assert.assertEquals(nombreUbicacion, "Espeleta")
        assert(contagiosEnEspeleta.size == 2)
    }


    @After
    fun eliminarTodo() {
        vectorServiceImpl.borrarTodasLasespecies() // elimina todas las especies asociadas a los vectores
        vectorServiceImpl.borrarTodos()
        ubicacionServiceImpl.eliminarTodasLasUbicaciones()
        feedServiceImpl.eliminarTodo()
    }
}