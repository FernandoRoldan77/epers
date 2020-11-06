package ar.edu.unq.eperdemic.utils.hibernate

import ar.edu.unq.eperdemic.modelo.entities.ubicacion.Ubicacion
import ar.edu.unq.eperdemic.modelo.entities.especie.Especie
import ar.edu.unq.eperdemic.modelo.entities.mutacion.MutacionCombinada
import ar.edu.unq.eperdemic.modelo.entities.mutacion.MutacionParticular
import ar.edu.unq.eperdemic.modelo.entities.patogeno.Patogeno
import ar.edu.unq.eperdemic.modelo.entities.vector.Vector
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Animal
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Humano
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.Insecto
import ar.edu.unq.eperdemic.modelo.entities.vector.tipoBiologico.TipoBiologico
import ar.edu.unq.eperdemic.modelo.enums.TipoDeVector
import ar.edu.unq.eperdemic.services.interfaces.*
import ar.edu.unq.eperdemic.utils.DataService
import kotlin.collections.HashSet

class DataServiceHibernate(
        private val ubicacionServiceImpl: UbicacionService,
        private val vectorServiceImpl: VectorService,
        private val patogenoServiceImpl: PatogenoService,
        private val especieServiceImpl: EspecieService,
        private val mutacionServiceImpl: MutacionService
) : DataService {

    private val paises = arrayOf("Afganistán", "Albania", "Alemania", "Andorra", "Angola", "Antigua y Barbuda", "Arabia Saudita", "Argelia", "Argentina", "Armenia", "Australia", "Austria", "Azerbaiyán", "Bahamas", "Bangladés", "Barbados", "Baréin", "Bélgica", "Belice", "Benín", "Bielorrusia", "Birmania", "Bolivia", "Bosnia y Herzegovina", "Botsuana", "Brasil", "Brunéi", "Bulgaria", "Burkina Faso", "Burundi", "Bután", "Cabo Verde", "Camboya", "Camerún", "Canadá", "Catar", "Chad", "Chile", "China", "Chipre", "Ciudad del Vaticano", "Colombia", "Comoras", "Corea del Norte", "Corea del Sur", "Costa de Marfil", "Costa Rica", "Croacia", "Cuba", "Dinamarca", "Dominica", "Ecuador", "Egipto", "El Salvador", "Emiratos Árabes Unidos", "Eritrea", "Eslovaquia", "Eslovenia", "España", "Estados Unidos", "Estonia", "Etiopía", "Filipinas", "Finlandia", "Fiyi", "Francia", "Gabón", "Gambia", "Georgia", "Ghana", "Granada", "Grecia", "Guatemala", "Guyana", "Guinea", "Guinea ecuatorial", "Guinea-Bisáu", "Haití", "Honduras", "Hungría", "India", "Indonesia", "Irak", "Irán", "Irlanda", "Islandia", "Islas Marshall", "Islas Salomón", "Israel", "Italia", "Jamaica", "Japón", "Jordania", "Kazajistán", "Kenia", "Kirguistán", "Kiribati", "Kuwait", "Laos", "Lesoto", "Letonia", "Líbano", "Liberia", "Libia", "Liechtenstein", "Lituania", "Luxemburgo", "Madagascar", "Malasia", "Malaui", "Maldivas", "Malí", "Malta", "Marruecos", "Mauricio", "Mauritania", "México", "Micronesia", "Moldavia", "Mónaco", "Mongolia", "Montenegro", "Mozambique", "Namibia", "Nauru", "Nepal", "Nicaragua", "Níger", "Nigeria", "Noruega", "Nueva Zelanda", "Omán", "Países Bajos", "Pakistán", "Palaos", "Panamá", "Papúa Nueva Guinea", "Paraguay", "Perú", "Polonia", "Portugal", "Reino Unido", "República Centroafricana", "República Checa", "República de Macedonia", "República del Congo", "República Democrática del Congo", "República Dominicana", "República Sudafricana", "Ruanda", "Rumanía", "Rusia", "Samoa", "San Cristóbal y Nieves", "San Marino", "San Vicente y las Granadinas", "Santa Lucía", "Santo Tomé y Príncipe", "Senegal", "Serbia", "Seychelles", "Sierra Leona", "Singapur", "Siria", "Somalia", "Sri Lanka", "Suazilandia", "Sudán", "Sudán del Sur", "Suecia", "Suiza", "Surinam", "Tailandia", "Tanzania", "Tayikistán", "Timor Oriental", "Togo", "Tonga", "Trinidad y Tobago", "Túnez", "Turkmenistán", "Turquía", "Tuvalu", "Ucrania", "Uganda", "Uruguay", "Uzbekistán", "Vanuatu", "Venezuela", "Vietnam", "Yemen", "Yibuti", "Zambia", "Zimbabue")

    // patogenos
    private lateinit var patogenoVirus: Patogeno
    private lateinit var patogenoHongo: Patogeno
    private lateinit var patogenoBacteria: Patogeno
    private lateinit var patogenoProtozoos: Patogeno
    private lateinit var patogenoFueraDeRango: Patogeno

    // lista de patogones creados
    var patogenos = hashSetOf<Patogeno>()

    // ubicaciones
    private var ubicionesCaba = ubicacionServiceImpl.crear("Capital Federal")
    private var ubicionesMonteGrande = ubicacionServiceImpl.crear("Monte Grande")
    private var ubicionesLanus = ubicacionServiceImpl.crear("Lanus")
    private var ubicionesGlew = ubicacionServiceImpl.crear("Glew")
    private var ubicionesAvellaneda = ubicacionServiceImpl.crear("Avellaneda")
    private var ubicionesYrigoyen = ubicacionServiceImpl.crear("Yrigoyen")
    private var ubicionesBanField = ubicacionServiceImpl.crear("Banfield")

    private var ubicaciones = arrayOf(ubicionesCaba, ubicionesMonteGrande, ubicionesLanus, ubicionesGlew, ubicionesAvellaneda, ubicionesYrigoyen, ubicionesBanField)

    override fun crearSetDeDatosIniciales() {
        crearPatogenosYEspecies()
        crearVectores()
        crearMutaciones()
    }

    override fun eliminarTodo() {
        vectorServiceImpl.borrarTodasLasespecies() // elimina todas las especies asociadas a los vectores
        vectorServiceImpl.borrarTodos()
        ubicacionServiceImpl.eliminarTodasLasUbicaciones()
        especieServiceImpl.eliminarTodos()
        patogenoServiceImpl.eliminarTodosLosPatogenos()
        mutacionServiceImpl.eliminarTodaslasMutaciones()
    }

    private fun crearPatogenosYEspecies() {
        patogenoVirus = Patogeno("Virus")
        patogenoHongo = Patogeno("Hongo")
        patogenoBacteria = Patogeno("Bacterias")
        patogenoProtozoos = Patogeno("Protozoos")
        patogenoFueraDeRango = Patogeno("OutOfRange")

        patogenos = hashSetOf(patogenoVirus, patogenoHongo, patogenoBacteria, patogenoProtozoos, patogenoFueraDeRango)

        asignarValoresAleatoriosPatogenos(patogenos)

        agregarEspeciesAPatogeno(patogenoVirus, arrayOf("SARS-CoV-2", "Covid-19", "Ébola", "Hepatitis B", "Gripe A", "Norovirus"))
        agregarEspeciesAPatogeno(patogenoHongo, arrayOf("Fusarium", "Aspergillus", "Dermatofitos", "Ascomycota"))
        agregarEspeciesAPatogeno(patogenoBacteria, arrayOf("Bacilos", "Helicoidales"))
        agregarEspeciesAPatogeno(patogenoProtozoos, arrayOf("Rizópodos", "Ciliados", "Flagelados", "Esporozoos"))
        persistirPatogenoYEspecies(patogenos)
    }

    private fun asignarValoresAleatoriosPatogenos(patogenos: HashSet<Patogeno>) {
        for (patogeno in patogenos) {
            if (patogeno.tipo == "Protozoos") {
                patogeno.defensa = 99
                patogeno.setLetalidad(99)
            } else {
                patogeno.defensa = (1..100).random()
                patogeno.setLetalidad((1..100).random())
            }
        }
    }

    private fun agregarEspeciesAPatogeno(patogeno: Patogeno, especies: Array<String>) {
        for (especie in especies) {
            val paisRandom: String = paises[(paises.indices).random()]
            patogeno.especies.add(Especie(patogeno, especie, paisRandom))
        }
    }

    private fun persistirPatogenoYEspecies(patogenos: HashSet<Patogeno>) {
        patogenos.forEach { patogenoServiceImpl.crearPatogeno(it) }
    }

    private fun crearVectores() {
        // obtengo las especies de cada patogeno para luego pasarlas como argumentos
        val especiesVirus = patogenos.filter { it.tipo == "Virus" }.flatMap { it.especies }.toMutableList()
        val especiesBacterias = patogenos.filter { it.tipo == "Bacterias" }.flatMap { it.especies }.toMutableList()
        val especiesProtozoos = patogenos.filter { it.tipo == "Protozoos" }.flatMap { it.especies }.toMutableList()
        val especiesHongo = patogenos.filter { it.tipo == "Hongo" }.flatMap { it.especies }.toMutableList()

        // agrego
        agregarVectorAUbicacion(especiesVirus, ubicionesCaba, Animal(), false)
        agregarVectorAUbicacion(especiesBacterias, ubicionesCaba, Humano(), true)
        agregarVectorAUbicacion(especiesBacterias, ubicionesCaba, Insecto(), true)
        agregarVectorAUbicacion(especiesBacterias, ubicionesCaba, Animal(), true)
        agregarVectorAUbicacion(especiesVirus, ubicionesAvellaneda, Insecto(), false)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Animal(), true)
        agregarVectorAUbicacion(especiesBacterias, ubicionesAvellaneda, Animal(), true)
        agregarVectorAUbicacion(especiesBacterias, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesBacterias, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesAvellaneda, Insecto(), true)
        agregarVectorAUbicacion(especiesHongo, ubicionesAvellaneda, Humano(), false)
        agregarVectorAUbicacion(especiesVirus, ubicionesLanus, Insecto(), false)
        for (i in 0..25) agregarLanusHumanos(especiesProtozoos, ubicionesLanus)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesLanus, Animal(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesLanus, Humano(), true)
        agregarVectorAUbicacion(especiesProtozoos, ubicionesLanus, Insecto(), true)
        agregarVectorAUbicacion(especiesHongo, ubicionesLanus, Humano(), false)
        agregarVectorAUbicacion(especiesHongo, ubicionesMonteGrande, Humano(), false)
        agregarVectorAUbicacion(especiesHongo, ubicionesBanField, Humano(), false)
        agregarVectorAUbicacion(especiesHongo, ubicionesGlew, Insecto(), true)
        agregarVectorAUbicacion(especiesHongo, ubicionesGlew, Animal(), true)
        agregarVectorAUbicacion(especiesHongo, ubicionesGlew, Humano(), true)
        agregarVectorAUbicacion(especiesHongo, ubicionesYrigoyen, Humano(), false)
        persistirUbicacionYVector()
    }

    private fun agregarLanusHumanos(e: MutableList<Especie>, ubicionLanus: Ubicacion) {
        agregarVectorAUbicacion(e, ubicionLanus, Humano(), true)
    }

    private fun agregarVectorAUbicacion(especies: List<Especie>, ubicacion: Ubicacion, tipoBiologico: TipoBiologico, estadoContagiado: Boolean) {
        val especiesToVector = especies //.subList(0, (1 until especies.size).random())
        val vectorNew = Vector(tipoBiologico)
        vectorNew.ubicacion = ubicacion
        vectorNew.estaContagiado = estadoContagiado
        vectorNew.especies = especies.toMutableList()
        ubicacion.vectores.add(vectorNew)
        especies.minus(especiesToVector)
    }

    private fun persistirUbicacionYVector() {
        ubicaciones.forEach { ubicacionServiceImpl.actualizarUbicacion(it) }
    }


    private fun crearMutaciones() {
        val mutacionPadre = MutacionCombinada("tos", TipoDeVector.Insecto, 1)
        mutacionPadre.costoADN = 5

        val mutacionCombinadaHija = MutacionCombinada("sarampeon", TipoDeVector.Animal, 2)
        mutacionCombinadaHija.costoADN = 5

        mutacionCombinadaHija.parent = mutacionPadre

        mutacionPadre.children.add(mutacionCombinadaHija)

        val mutacionParticularHija = MutacionParticular("vomitar", TipoDeVector.Animal, 3)
        mutacionParticularHija.costoADN = 6

        val mutacionParticularHija2 = MutacionParticular("vomitar2", TipoDeVector.Insecto, 3)
        mutacionParticularHija2.costoADN = 6

        mutacionParticularHija.parent = mutacionPadre
        mutacionParticularHija2.parent = mutacionPadre
        mutacionPadre.children.add(mutacionParticularHija)
        mutacionPadre.children.add(mutacionParticularHija2)

        mutacionServiceImpl.crearMutacion(mutacionPadre)

        val mutacionMadre = MutacionCombinada("picadura", TipoDeVector.Insecto, 1)
        mutacionMadre.costoADN = 5

        val mutacionCombinadaHijo = MutacionCombinada("alergia", TipoDeVector.Animal, 4)
        mutacionCombinadaHijo.costoADN = 33

        mutacionCombinadaHijo.parent = mutacionMadre

        mutacionMadre.children.add(mutacionCombinadaHijo)

        val mutacionParticularHijo = MutacionParticular("acido", TipoDeVector.Humano, 2)
        mutacionParticularHijo.costoADN = 2

        mutacionParticularHijo.parent = mutacionMadre

        mutacionMadre.children.add(mutacionParticularHijo)

        val mutacionCombinadaHijo2 = MutacionCombinada("acido2", TipoDeVector.Humano, 2)
        mutacionCombinadaHijo2.costoADN = 2

        mutacionCombinadaHijo2.parent = mutacionMadre

        mutacionMadre.children.add(mutacionCombinadaHijo2)

        mutacionServiceImpl.crearMutacion(mutacionMadre)

        val mutacionParticularSola = MutacionParticular("propagacion", TipoDeVector.Insecto, 2)
        mutacionParticularSola.costoADN = 50
        mutacionParticularSola.agregarRequerimiento(mutacionMadre)
        mutacionServiceImpl.crearMutacion(mutacionParticularSola)

    }
}