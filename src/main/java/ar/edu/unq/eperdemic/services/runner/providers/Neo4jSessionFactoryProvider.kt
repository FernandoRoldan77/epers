package ar.edu.unq.eperdemic.services.runner.providers

import org.neo4j.driver.*

class Neo4jSessionFactoryProvider private constructor() {

    private val sessionFactory: Driver?

    init {
        val env = System.getenv()
        val uri = env.getOrDefault("URL", "bolt://localhost:7687")
        val username = env.getOrDefault("USER", "neo4j")
        val password = env.getOrDefault("PASSWORD", "root")

        val config = Config.builder()
                .withLogging(Logging.slf4j())
                .withTrustStrategy(Config.TrustStrategy.trustSystemCertificates())
                .build()

        this.sessionFactory = GraphDatabase.driver(uri, AuthTokens.basic(username, password), config)
    }

    fun createSession(): Session {
        return this.sessionFactory!!.session()
    }

    companion object {

        private var INSTANCE: Neo4jSessionFactoryProvider? = null

        val instance: Neo4jSessionFactoryProvider
            get() {
                if (INSTANCE == null) {
                    INSTANCE = Neo4jSessionFactoryProvider()
                }
                return INSTANCE!!
            }

        fun destroy() {
            if (INSTANCE != null && INSTANCE!!.sessionFactory != null) {
                INSTANCE!!.sessionFactory!!.close()
            }
            INSTANCE = null
        }
    }


}