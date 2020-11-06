package ar.edu.unq.eperdemic.hibernateTP2.mutacion

import ar.edu.unq.eperdemic.dao.hibernate.impl.*
import ar.edu.unq.eperdemic.dao.mongoDB.impl.FeedMongoImplDAO
import ar.edu.unq.eperdemic.dao.neo4j.impl.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.mutacion.MutacionCombinada
import ar.edu.unq.eperdemic.modelo.entities.mutacion.MutacionParticular
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.enums.TipoDeVector
import ar.edu.unq.eperdemic.modelo.exception.ADNInsuficienteException
import ar.edu.unq.eperdemic.modelo.exception.RequerimientoException
import ar.edu.unq.eperdemic.services.impl.*
import ar.edu.unq.eperdemic.utils.hibernate.DataServiceHibernate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*


class MutacionTest {

    private val ubicacionServiceImpl = UbicacionServiceImpl(UbicacionHibernateDAO(), UbicacionNeo4jDAO(), VectorDAOImpl(), FeedMongoImplDAO(), PatogenoHibernateDAO())
    private val vectorServiceImpl = VectorServiceImpl(VectorDAOImpl(), FeedMongoImplDAO())
    private val patogenoServiceImpl = PatogenoServiceImpl(PatogenoHibernateDAO(), FeedMongoImplDAO())
    private val especieServiceImpl = EspecieServiceImpl(EspecieHibernateDAO())
    private val mutacionService = MutacionServiceImpl(MutacionHibernateDAO(), EspecieHibernateDAO(),FeedMongoImplDAO())
    private val dataServiceHibernate = DataServiceHibernate(ubicacionServiceImpl, vectorServiceImpl, patogenoServiceImpl, especieServiceImpl, mutacionService)

    @Before
    fun preparar() {
        dataServiceHibernate.crearSetDeDatosIniciales()
    }

    @Test
    fun calcularAdnDeUnaMutacionParticular() {
        val mutacionP = MutacionParticular("tos", TipoDeVector.Insecto, 0)
        mutacionP.costoADN = 50
        assertEquals(mutacionP.costoADN, 50)
    }

    @Test
    fun calcularAdnDeUnaMutacionCombinadaCon2Mutaciones() {

        val mutacionC = MutacionCombinada("tos", TipoDeVector.Insecto, 0)
        val mutacionP25adn = MutacionParticular("tos", TipoDeVector.Insecto, 0)
        mutacionP25adn.costoADN = 25

        mutacionC.agregarMutacion(mutacionP25adn)

        val mutacionC50adn = MutacionCombinada("tos", TipoDeVector.Insecto, 0)
        mutacionC50adn.costoADN = 50

        mutacionC.agregarMutacion(mutacionC50adn)

        val sumaDeMutaciones = mutacionC.costoADN +  mutacionP25adn.costoADN + mutacionC50adn.costoADN
        assertEquals(sumaDeMutaciones, 75)  //Ver porque no funciona con combinada

    }

    @Test
    fun `agregar requerimiento a mutacion`() {
        val mutacion = MutacionCombinada("tos", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento = MutacionCombinada("vomito", TipoDeVector.Insecto, 0)

        mutacion.agregarRequerimiento(mutacionRequerimiento)

        assert(mutacion.mutacionesRequeridas.isNotEmpty())
        assert(mutacion.mutacionesRequeridas.any { it.nombre == mutacionRequerimiento.nombre })
    }

    @Test
    fun `agregar 2 requerimientos a mutacion`() {
        val mutacion = MutacionCombinada("tos", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento = MutacionCombinada("vomito", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento2 = MutacionCombinada("estornudo", TipoDeVector.Insecto, 0)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        mutacion.agregarRequerimiento(mutacionRequerimiento2)

        assert(mutacion.mutacionesRequeridas.isNotEmpty())
        assert(mutacion.mutacionesRequeridas.any { it.nombre == mutacionRequerimiento.nombre })
        assert(mutacion.mutacionesRequeridas.any { it.nombre == mutacionRequerimiento2.nombre })
    }

    @Test(expected = RequerimientoException::class)
    fun `no puede mutar especie porque no presenta el requerimiento`() {
        val mutacion = MutacionCombinada("tos", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento = MutacionCombinada("vomito", TipoDeVector.Insecto, 0)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        mutacion.mutar(Especie(mock(Patogeno::class.java), "covid19", "China"))
    }

    @Test(expected = RequerimientoException::class)
    fun `no puede mutar especie porque no presenta 1 de 2 requerimientos`() {
        val mutacion = MutacionCombinada("tos", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento = MutacionCombinada("vomito", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento2 = MutacionCombinada("estornudo", TipoDeVector.Insecto, 0)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        mutacion.agregarRequerimiento(mutacionRequerimiento2)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        val especie = Especie(mock(Patogeno::class.java), "covid19", "China")
        especie.mutaciones.add(mutacionRequerimiento)

        mutacion.mutar(especie)
    }

    @Test(expected = ADNInsuficienteException::class)
    fun `puede mutar porque tiene el requerimiento pero no el adn suficiente`() {
        val mutacion = MutacionCombinada("tos", TipoDeVector.Insecto, 1)
        mutacion.costoADN = 10
        val mutacionRequerimiento = MutacionCombinada("vomito", TipoDeVector.Insecto, 0)

        val especie = Especie(mock(Patogeno::class.java), "covid19", "China")
        especie.mutaciones.add(mutacionRequerimiento)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        mutacion.mutar(especie)
    }

    @Test(expected = ADNInsuficienteException::class)
    fun `puede mutar porque tiene los requerimientos pero no el adn suficiente`() {
        val mutacion = MutacionCombinada("tos", TipoDeVector.Insecto, 1)
        mutacion.costoADN = 10
        val mutacionRequerimiento = MutacionCombinada("vomito", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento2 = MutacionCombinada("estornudo", TipoDeVector.Insecto, 0)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        mutacion.agregarRequerimiento(mutacionRequerimiento2)

        val especie = Especie(mock(Patogeno::class.java), "covid19", "China")
        especie.mutaciones.add(mutacionRequerimiento)
        especie.mutaciones.add(mutacionRequerimiento2)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        mutacion.agregarRequerimiento(mutacionRequerimiento2)

        mutacion.mutar(especie)
    }

    @Test
    fun `agregar requerimiento a mutacion y persistirlo`() {
        val mutacion = MutacionCombinada("tos3", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento = MutacionCombinada("vomito2", TipoDeVector.Insecto, 0)

        mutacion.agregarRequerimiento(mutacionRequerimiento)

        mutacionService.crearMutacion(mutacion)

        val mutacionRecuperada = mutacionService.recuperarMutacion(mutacion.id!!)

        assert(mutacionRecuperada.mutacionesRequeridas.isNotEmpty())
        assert(mutacionRecuperada.mutacionesRequeridas.any { it.nombre == mutacionRequerimiento.nombre })
    }

    @Test
    fun `agregar requerimientos a mutacion y persistirlos`() {
        val mutacion = MutacionCombinada("tos3", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento = MutacionCombinada("vomito2", TipoDeVector.Insecto, 0)
        val mutacionRequerimiento2 = MutacionCombinada("estornudo", TipoDeVector.Insecto, 0)

        mutacion.agregarRequerimiento(mutacionRequerimiento)
        mutacion.agregarRequerimiento(mutacionRequerimiento2)

        mutacionService.crearMutacion(mutacion)

        val mutacionRecuperada = mutacionService.recuperarMutacion(mutacion.id!!)

        assert(mutacionRecuperada.mutacionesRequeridas.isNotEmpty())
        assert(mutacionRecuperada.mutacionesRequeridas.any { it.nombre == mutacionRequerimiento.nombre })
        assert(mutacionRecuperada.mutacionesRequeridas.any { it.nombre == mutacionRequerimiento2.nombre })
    }

    @Test
    fun `listar nombres de mutaciones`() {
        val mutacion = MutacionCombinada("tos3", TipoDeVector.Insecto, 0)
        mutacion.agregarMutacion(MutacionCombinada("vomito2", TipoDeVector.Insecto, 0))
        mutacion.agregarMutacion(MutacionCombinada("estornudo", TipoDeVector.Insecto, 0))
        mutacion.agregarMutacion(MutacionParticular("tos4", TipoDeVector.Insecto, 0))
        assertEquals(mutacion.identificadoresMutaciones(), listOf("tos3", "vomito2", "estornudo", "tos4"))
    }

    @Test
    fun `mutar especie con mutacion particular`() {
        val mutacion = MutacionParticular("tos", TipoDeVector.Insecto, 10)
        val especieAMutar = mock(Especie::class.java)
        `when`(especieAMutar.puntosDeADN).thenReturn(100)
        mutacion.mutar(especieAMutar)
        verify(especieAMutar).agregarMutacion(mutacion)
    }

    @Test
    fun `mutar especie con mutacion particular a factor contagio insecto`() {
        val especieAMutar = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.infectaATipoVector == TipoDeVector.Insecto &&
                    it.costoADN == 6
        }
        val mutacionAmutar = mutaciones.random()
        mutacionAmutar.mutacionesRequeridas.clear()
        mutacionAmutar.mutar(especieAMutar)
        assert(especieAMutar.contagioContraInsectos == mutacionAmutar.valorDeContagio)
    }

    @Test
    fun `mutar especie con mutacion particular a factor contagio humano`() {
        val especieAMutar = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutacionAmutar = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.infectaATipoVector == TipoDeVector.Humano
        }.minBy { it.costoADN }!!
        mutacionAmutar.mutacionesRequeridas.clear()
        mutacionAmutar.mutar(especieAMutar)
        assert(especieAMutar.contagioContraHumanos == mutacionAmutar.valorDeContagio)
    }

    @Test
    fun `mutar especie con mutacion particular a factor contagio animal`() {
        val especieAMutar = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutacionAmutar = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.infectaATipoVector == TipoDeVector.Animal
        }.minBy { it.costoADN }!!
        mutacionAmutar.mutacionesRequeridas.clear()
        mutacionAmutar.mutar(especieAMutar)
        assert(especieAMutar.contagioContraAnimales == mutacionAmutar.valorDeContagio)
    }

    //

    @Test
    fun `mutar especie con mutacion combinada a factor contagio insecto`() {
        val especieAMutar = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.infectaATipoVector == TipoDeVector.Insecto
        }
        val mutacionAmutar = mutaciones.random()
        mutacionAmutar.mutacionesRequeridas.clear()
        mutacionAmutar.mutar(especieAMutar)
        assert(especieAMutar.contagioContraInsectos == mutacionAmutar.valorDeContagio)
    }

    @Test
    fun `mutar especie con mutacion combinada a factor contagio humano`() {
        val especieAMutar = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutacionAmutar = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.infectaATipoVector == TipoDeVector.Humano
        }.minBy { it.costoADN }!!
        mutacionAmutar.mutacionesRequeridas.clear()
        mutacionAmutar.mutar(especieAMutar)
        assert(especieAMutar.contagioContraHumanos == mutacionAmutar.valorDeContagio)
    }

    @Test
    fun `mutar especie con mutacion combinada a factor contagio animal`() {
        val especieAMutar = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutacionAmutar = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.infectaATipoVector == TipoDeVector.Animal
        }.minBy { it.costoADN }!!
        mutacionAmutar.mutacionesRequeridas.clear()
        mutacionAmutar.mutar(especieAMutar)
        assert(especieAMutar.contagioContraAnimales == mutacionAmutar.valorDeContagio)
    }


    @Test
    fun actualizarMutacionCombinada() {
        val mutaciones = mutacionService.recuperarMutaciones().filter { it::class.java == MutacionCombinada::class.java }
        val mutacionRandom = mutaciones.random() as MutacionCombinada
        val randomValue = (0..100).random()
        mutacionRandom.costoADN = randomValue
        mutacionRandom.children.forEach { it.costoADN = randomValue }
        mutacionService.actualizarMutacion(mutacionRandom)
        val recuperarMutacion = mutacionService.recuperarMutacion(mutacionRandom.id!!) as MutacionCombinada
        assertTrue(recuperarMutacion.children.all { it.costoADN == randomValue })
    }


    @Test
    fun actualizarMutacionParticular() {
        val mutaciones = mutacionService.recuperarMutaciones().filter { it::class.java == MutacionParticular::class.java }
        val mutacionRandom = mutaciones.random() as MutacionParticular
        val randomValue = (0..100).random()
        mutacionRandom.costoADN = randomValue
        mutacionService.actualizarMutacion(mutacionRandom)
        val recuperarMutacion = mutacionService.recuperarMutacion(mutacionRandom.id!!)
        assertEquals(recuperarMutacion.costoADN, randomValue)
    }


    @Test
    fun mutarEspecieConMutacionSingular() {
        val especieRandom = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.costoADN <= 5
        } // filtros los vectores de tipo humano contagiados y con adn <= 5
        val mutacionRandom = mutaciones.random()
        mutacionRandom.mutar(especieRandom!!)
        assert(especieRandom.mutaciones.contains(mutacionRandom))
    }

    @Test
    fun `mutar especie con mutacion combinada y persistirlo`() {
        val especieASerMutada = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)

        val especieRecuperada = especieServiceImpl.recuperar(especieASerMutada.id)

        assert(especieRecuperada.contieneMutacionesRequeridas(listOf(mutacionAMutar)))
    }

    @Test
    fun `mutar especie con mutacion particular y persistirlo`() {
        val especieASerMutada = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)

        val especieRecuperada = especieServiceImpl.recuperar(especieASerMutada.id)

        assert(especieRecuperada.contieneMutacionesRequeridas(listOf(mutacionAMutar)))
    }

    @Test(expected = ADNInsuficienteException::class)
    fun `intentar mutar especie con mutacion particular y persistirlo pero no tiene suficiente adn`() {
        val especieASerMutada = Especie(mock(Patogeno::class.java), "covid19", "china")
        especieServiceImpl.crear(especieASerMutada)
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)
    }

    @Test(expected = ADNInsuficienteException::class)
    fun `intentar mutar especie con mutacion combinada y persistirlo pero no tiene suficiente adn`() {
        val especieASerMutada = Especie(mock(Patogeno::class.java), "covid19", "china")
        especieServiceImpl.crear(especieASerMutada)
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)
    }

    @Test(expected = RequerimientoException::class)
    fun `intentar mutar especie con mutacion particular y persistirlo pero no tiene requerimiento`() {
        val especieASerMutada = Especie(mock(Patogeno::class.java), "covid19", "china")
        especieServiceImpl.crear(especieASerMutada)
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()
        mutacionAMutar.agregarRequerimiento(MutacionParticular("mutacionPrueba", TipoDeVector.Animal, 1))
        mutacionService.actualizarMutacion(mutacionAMutar)

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)
    }

    @Test(expected = RequerimientoException::class)
    fun `intentar mutar especie con mutacion combinada y persistirlo pero no tiene requerimiento`() {
        val especieASerMutada = Especie(mock(Patogeno::class.java), "covid19", "china")
        especieServiceImpl.crear(especieASerMutada)
        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()
        mutacionAMutar.agregarRequerimiento(MutacionParticular("mutacionPrueba", TipoDeVector.Animal, 1))
        mutacionService.actualizarMutacion(mutacionAMutar)

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)
    }

    @Test
    fun `mutar especie con mutacion particular que contiene requerimiento de mutacion y persistirlo`() {
        val especieASerMutada = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!

        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        val requerimiento = MutacionParticular("mutacionPrueba", TipoDeVector.Animal, 1)

        especieASerMutada.agregarMutacion(requerimiento)
        especieServiceImpl.actualizar(especieASerMutada)

        mutacionAMutar.agregarRequerimiento(requerimiento)
        mutacionService.actualizarMutacion(mutacionAMutar)

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)

        val especieRecuperada = especieServiceImpl.recuperar(especieASerMutada.id)

        assert(especieRecuperada.contieneMutacionesRequeridas(listOf(mutacionAMutar, requerimiento)))
    }

    @Test
    fun `mutar especie con mutacion combinada que contiene requerimiento de mutacion y persistirlo`() {
        val especieASerMutada = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!

        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        val requerimiento = MutacionParticular("mutacionPrueba", TipoDeVector.Animal, 1)

        especieASerMutada.agregarMutacion(requerimiento)
        especieServiceImpl.actualizar(especieASerMutada)

        mutacionAMutar.agregarRequerimiento(requerimiento)
        mutacionService.actualizarMutacion(mutacionAMutar)

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)

        val especieRecuperada = especieServiceImpl.recuperar(especieASerMutada.id)

        assert(especieRecuperada.contieneMutacionesRequeridas(listOf(mutacionAMutar, requerimiento)))
    }

    @Test
    fun `mutar especie con mutacion combinada que contiene requerimientos de mutaciones y persistirlo`() {
        val especieASerMutada = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!

        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionCombinada::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        val requerimiento = MutacionParticular("mutacionPrueba", TipoDeVector.Animal, 1)
        val requerimiento2 = MutacionCombinada("mutacionPrueba2", TipoDeVector.Animal, 1)

        especieASerMutada.agregarMutacion(requerimiento)
        especieASerMutada.agregarMutacion(requerimiento2)
        especieServiceImpl.actualizar(especieASerMutada)

        mutacionAMutar.agregarRequerimiento(requerimiento)
        mutacionAMutar.agregarRequerimiento(requerimiento2)
        mutacionService.actualizarMutacion(mutacionAMutar)

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)

        val especieRecuperada = especieServiceImpl.recuperar(especieASerMutada.id)

        assert(especieRecuperada.contieneMutacionesRequeridas(listOf(mutacionAMutar, requerimiento)))
    }

    @Test
    fun `mutar especie con mutacion particular que contiene requerimientos de mutaciones y persistirlo`() {
        val especieASerMutada = especieServiceImpl.recuperarTodos().maxBy { it.vectores.size }!!

        val mutaciones = mutacionService.recuperarMutaciones().filter {
            it::class.java == MutacionParticular::class.java && it.costoADN <= 5
        }
        val mutacionAMutar = mutaciones.random()

        val requerimiento = MutacionParticular("mutacionPrueba", TipoDeVector.Animal, 1)
        val requerimiento2 = MutacionCombinada("mutacionPrueba2", TipoDeVector.Animal, 1)

        especieASerMutada.agregarMutacion(requerimiento)
        especieASerMutada.agregarMutacion(requerimiento2)
        especieServiceImpl.actualizar(especieASerMutada)

        mutacionAMutar.agregarRequerimiento(requerimiento)
        mutacionAMutar.agregarRequerimiento(requerimiento2)
        mutacionService.actualizarMutacion(mutacionAMutar)

        mutacionService.mutar(especieASerMutada.id!!, mutacionAMutar.id!!)

        val especieRecuperada = especieServiceImpl.recuperar(especieASerMutada.id)

        assert(especieRecuperada.contieneMutacionesRequeridas(listOf(mutacionAMutar, requerimiento)))
    }

    @After
    fun eliminarTodaslasMutaciones() {
       dataServiceHibernate.eliminarTodo()
    }
}