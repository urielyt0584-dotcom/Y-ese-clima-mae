package com.example.domain

import com.example.data.model.WeatherCategory
import kotlin.random.Random

object WeatherCommentEngine {

    private val dynamicRemoteInsults = mutableListOf<String>()
    private val dynamicSeasonalInsults = mutableListOf<com.example.data.model.SeasonalInsult>()

    @Synchronized
    fun updateDynamicInsults(insults: List<String>) {
        if (insults.isNotEmpty()) {
            dynamicRemoteInsults.clear()
            dynamicRemoteInsults.addAll(insults)
        }
    }

    @Synchronized
    fun updateSeasonalInsults(insults: List<com.example.data.model.SeasonalInsult>) {
        dynamicSeasonalInsults.clear()
        dynamicSeasonalInsults.addAll(insults.filter { it.isActive && it.text.isNotBlank() })
    }

    @Synchronized
    fun getSeasonalInsultsCount(): Int = dynamicSeasonalInsults.size

    @Synchronized
    fun getSeasonalInsultsFor(category: WeatherCategory, isFamilyFriendlyMode: Boolean): List<String> {
        val matches = dynamicSeasonalInsults.filter { insult ->
            val matchesCategory = insult.category.equals("ALL", ignoreCase = true) ||
                    insult.category.equals(category.name, ignoreCase = true)
            val matchesFamily = if (isFamilyFriendlyMode) insult.isFamilyFriendly else true
            matchesCategory && matchesFamily
        }.map { it.text }

        return if (!isFamilyFriendlyMode) {
            matches + dynamicRemoteInsults
        } else {
            matches
        }
    }

    fun determineCategory(
        temperature: Double,
        feelsLike: Double,
        humidity: Int,
        precipitationProbability: Int,
        windSpeed: Double,
        uvIndex: Double,
        weatherCode: Int,
        isDay: Boolean
    ): WeatherCategory {
        return when {
            weatherCode in 95..99 -> WeatherCategory.THUNDERSTORM
            weatherCode in listOf(65, 67, 82) || (precipitationProbability >= 80 && weatherCode in listOf(61, 63, 80, 81)) -> WeatherCategory.HEAVY_RAIN
            weatherCode in listOf(51, 53, 55, 56, 57, 61, 63, 66, 80, 81) || precipitationProbability >= 65 -> WeatherCategory.RAIN
            temperature >= 31.0 || feelsLike >= 34.0 -> WeatherCategory.EXTREME_HEAT
            temperature <= 17.5 -> WeatherCategory.COLD
            windSpeed >= 30.0 -> WeatherCategory.STRONG_WIND
            !isDay -> WeatherCategory.NIGHT
            weatherCode in 0..1 -> WeatherCategory.CLEAR_SKIES
            weatherCode in 2..3 -> WeatherCategory.CLOUDS
            uvIndex >= 7.5 -> WeatherCategory.HIGH_UV
            humidity >= 85 && temperature >= 22.0 -> WeatherCategory.HIGH_HUMIDITY
            else -> WeatherCategory.CLOUDS
        }
    }

    private fun getForeignPrefix(country: String, isFamilyFriendlyMode: Boolean, rand: Random): String {
        return WeatherForeignInsults.getForeignPrefix(country, isFamilyFriendlyMode, rand)
    }

    fun generateComment(
        isFamilyFriendlyMode: Boolean = true,
        temperature: Double,
        feelsLike: Double,
        humidity: Int,
        precipitationProbability: Int,
        windSpeed: Double,
        uvIndex: Double,
        weatherCode: Int,
        isDay: Boolean = true,
        checkCount: Int = 1,
        randomSeed: Int? = null,
        excludeComment: String? = null,
        country: String = "Costa Rica",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        locationName: String = ""
    ): String {
        val rand = if (randomSeed != null) Random(randomSeed) else Random.Default
        val isForeign = WeatherForeignInsults.isForeignLocation(country, latitude, longitude)
        val cleanCountry = WeatherForeignInsults.cleanForeignCountryName(country)
        val foreignPrefix = if (isForeign) WeatherForeignInsults.getForeignPrefix(cleanCountry, isFamilyFriendlyMode, rand) else ""
        
        if (checkCount >= 4) {
            if (isForeign) {
                val foreignRepeats = WeatherForeignInsults.getForeignRepeatedChecks(cleanCountry, isFamilyFriendlyMode)
                val filtered = if (excludeComment != null && foreignRepeats.size > 1) {
                    foreignRepeats.filter { it != excludeComment }
                } else foreignRepeats
                return com.example.util.SmartInsultSelector.getUnique(filtered, randomSeed)
            }

            val repeatedComments = if (isFamilyFriendlyMode) listOf(
                "¿Otra vez revisando el clima? El tiempo no va a cambiar tan rápido.",
                "Uy, ¿espera que la pantalla le tire un rayo de sol? ¡Tranquilo!",
                "Si sigue actualizando el clima va a gastar la batería antes de salir.",
                "Cuarta vez que revisa... asómese a la ventana mejor.",
                "Paciencia, que el aguacero no se va a quitar mágicamente.",
                "Ni el meteorólogo del canal 7 revisa el clima tantas veces seguidas. Relájese un toque.",
                "Tranquilo, mae, que el aguacero no se va a quitar por verlo tanto.",
                "¿Esperando un arcoíris en la pantalla? Asómese a ver afuera mejor."
            ) else listOf(
                "¿Otra vez revisando el clima? Mae, el tiempo no va a cambiar porque usted lo mire cada dos segundos, carepicha.",
                "Diay mae, ¿espera que la pantalla le tire un rayo de sol o qué? ¡Deje de tocar el botón, pedazo de malparido!",
                "Mae, si sigue actualizando el clima va a gastar la batería antes de que empiece a llover, idiota.",
                "Cuarta vez que revisa... mae, asómese a la ventana mejor, no sea tan vago y carebarro.",
                "Tranquilo mae, que el aguacero no se va a quitar por su linda cara, pedazo de bestia.",
                "Mae, ni el meteorólogo del canal 7 revisa el clima tantas veces seguidas. Relájese un toque o tómese un té de tilo.",
                "¡Deje de estar jodiendo con el botón, carepicha! El clima no va a cambiar porque le dé como un mono con hambre.",
                "¿Otra vez actualizando esta mierda? Asómese a la calle, los ojos no los tiene solo de adorno.",
                "Mae, me cago en la puta, ¿usted cree que yo controlo las nubes o qué? Deje de espamear la pantalla.",
                "Cuarta vez que revisa... ¿Qué espera ver, careverga? ¿Nieve en Chepe? ¡Suelte el cel!",
                "Diay soberano imbécil, el sol no va a salir por su linda jupa. Vaya bretee o haga algo productivo.",
                "Mae, si sigue tocando el botón le voy a cobrar por consulta, pedazo de mamapichas.",
                "Relájese un pichazo, mae. El clima está igual de despichado que hace 30 segundos.",
                "¿Qué putas le pasa en los dedos? Pare de actualizar o le va a explotar la batería, playo.",
                "Ni su mamá le pone tanta atención como usted a este clima hijueputa. ¡Ya pare, mae!"
            )
            return com.example.util.SmartInsultSelector.getUnique(repeatedComments, randomSeed)
        }

        val category = determineCategory(
            temperature = temperature,
            feelsLike = feelsLike,
            humidity = humidity,
            precipitationProbability = precipitationProbability,
            windSpeed = windSpeed,
            uvIndex = uvIndex,
            weatherCode = weatherCode,
            isDay = isDay
        )

        val combo = detectWeatherCombo(
            temperature = temperature,
            feelsLike = feelsLike,
            humidity = humidity,
            precipitationProbability = precipitationProbability,
            windSpeed = windSpeed,
            uvIndex = uvIndex,
            weatherCode = weatherCode,
            isDay = isDay
        )

        val specialLoc = SpecialLocationInsults.detectSpecialLocation(locationName, latitude, longitude)
        if (specialLoc != null && !isForeign) {
            val specialInsults = SpecialLocationInsults.getSpecialLocationInsults(
                location = specialLoc,
                category = category,
                combo = combo,
                isFamilyFriendly = isFamilyFriendlyMode,
                temperature = temperature,
                feelsLike = feelsLike,
                humidity = humidity,
                windSpeed = windSpeed,
                precipitationProbability = precipitationProbability
            )
            val filtered = if (excludeComment != null && specialInsults.size > 1) {
                specialInsults.filter { it != excludeComment }
            } else specialInsults
            if (filtered.isNotEmpty()) {
                return com.example.util.SmartInsultSelector.getUnique(filtered, randomSeed)
            }
        }

        if (combo != null) {
            if (isForeign) {
                val foreignComboList = WeatherForeignInsults.getForeignComboInsults(
                    combo = combo,
                    country = cleanCountry,
                    isFamilyFriendly = isFamilyFriendlyMode,
                    temp = temperature,
                    wind = windSpeed
                )
                if (foreignComboList.isNotEmpty() && rand.nextInt(100) < 80) {
                    val filtered = if (excludeComment != null && foreignComboList.size > 1) {
                        foreignComboList.filter { it != excludeComment }
                    } else foreignComboList
                    return com.example.util.SmartInsultSelector.getUnique(filtered, randomSeed)
                }
            }

            val comboInsults = WeatherComboInsults.getComboInsults(
                combo = combo,
                isFamilyFriendly = isFamilyFriendlyMode,
                temperature = temperature,
                feelsLike = feelsLike,
                humidity = humidity,
                windSpeed = windSpeed,
                uvIndex = uvIndex
            )
            val filteredComboInsults = if (excludeComment != null && comboInsults.size > 1) {
                comboInsults.filter { it != excludeComment }
            } else {
                comboInsults
            }
            if (filteredComboInsults.isNotEmpty()) {
                val chosenCombo = com.example.util.SmartInsultSelector.getUnique(filteredComboInsults, randomSeed)
                return if (isForeign && !chosenCombo.contains(cleanCountry, ignoreCase = true)) {
                    foreignPrefix + chosenCombo
                } else {
                    chosenCombo
                }
            }
        }

        if (isForeign) {
            // When outside Costa Rica, dedicated foreign insults have an 85% probability:
            // "haz que cuando uno esté afuera del país, los insultos sean más probables de aparecer, y añade muchos más insultos para cuando uno esté afuera de Costa Rica"
            val foreignCatInsults = WeatherForeignInsults.getForeignCategoryInsults(
                category = category,
                country = cleanCountry,
                isFamilyFriendlyMode = isFamilyFriendlyMode,
                temperature = temperature,
                feelsLike = feelsLike,
                windSpeed = windSpeed
            )
            val foreignGeneralInsults = WeatherForeignInsults.getGeneralForeignRoasts(cleanCountry, isFamilyFriendlyMode)
            val foreignPool = foreignCatInsults + foreignGeneralInsults

            if (foreignPool.isNotEmpty() && rand.nextInt(100) < 85) {
                val filtered = if (excludeComment != null && foreignPool.size > 1) {
                    foreignPool.filter { it != excludeComment }
                } else foreignPool
                return com.example.util.SmartInsultSelector.getUnique(filtered, randomSeed)
            }
        }

        return getCommentForCategory(
            isFamilyFriendlyMode = isFamilyFriendlyMode,
            category = category,
            temperature = temperature,
            humidity = humidity,
            windSpeed = windSpeed,
            uvIndex = uvIndex,
            randomSeed = randomSeed,
            excludeComment = excludeComment,
            country = cleanCountry,
            isForeign = isForeign,
            specialLocation = specialLoc
        )
    }

    private fun getCommentForCategory(
        isFamilyFriendlyMode: Boolean,
        category: WeatherCategory,
        temperature: Double,
        humidity: Int,
        windSpeed: Double,
        uvIndex: Double,
        randomSeed: Int?,
        excludeComment: String? = null,
        country: String = "Costa Rica",
        isForeign: Boolean = false,
        specialLocation: SpecialLocation? = null
    ): String {
        val rand = if (randomSeed != null) Random(randomSeed) else Random.Default
        val foreignPrefix = if (isForeign) WeatherForeignInsults.getForeignPrefix(country, isFamilyFriendlyMode, rand) else ""

        val comments = if (isFamilyFriendlyMode) {
            when (category) {
                WeatherCategory.EXTREME_HEAT -> listOf(
                    "Mae, hace demasiado calor. Tómese un fresquito.",
                    "El sol pica bastante. Mejor busque sombra, pura vida.",
                    "Qué calorón, mae. Póngase algo bien fresco.",
                    "Demasiado calor hoy, tómese algo bien frío, al suave.",
                    "Busque sombra, que el sol no perdona hoy.",
                    "Un granizado de la Sabana caería apenas con este calor.",
                    "Si sale, lleve botellita de agua porque está bravo el sol.",
                    "Qué calorcito más bravo, mae. Mejor quedarse fresquito en casa.",
                    "Hace un calor de locos, puro para estar en pantaloneta todo el día.",
                    "Este sol está como para freír un huevo en la acera. Cuídese mucho.",
                    "Mae, qué bochorno. Busque un buen arbolito para refrescarse.",
                    "Tómese un fresco de cas bien frío, porque el calor está rajado.",
                    "Qué calor, mae. Ponga a ventilar la casa para no asarse.",
                    "Un clima apenas para ir al Puerto, lástima estar trabajando, pura vida.",
                    "El asfalto echa humo, mae. Trate de no andar mucho asoleándose.",
                    "Un granizado bien cargado con leche pinito y condensada es lo único que salva hoy, pura vida.",
                    "Bájese el calor con una sandía bien fría o un vasito de agua de pipa, mae.",
                    "El sol está bravo, póngase bloqueador y no se le olvide la gorrita si va a salir a hacer mandados."
                )
                WeatherCategory.COLD -> listOf(
                    "Saque el abrigo, mae, hace bastante frío.",
                    "Qué frío está haciendo. Tápese bien, pura vida.",
                    "Clima ideal para un buen café chorreado con pancito.",
                    "El viento está helado hoy, póngase jacket.",
                    "Día para no soltar la cobija, pura vida.",
                    "Qué friíto más rico para echarse a ver tele.",
                    "Hace un frío como para dormir con medias.",
                    "Saque el abrigo del fondo del clóset, mae.",
                    "Un aguadulce calentito cae apenas con este clima frío.",
                    "Parece clima del Zurquí, póngase bufanda si va a salir.",
                    "Qué frío más sabroso para hacer pereza todo el domingo.",
                    "Tápese bien que las madrugadas están congeladas, pura vida.",
                    "Mae, el viento le congela la nariz. Ande bien abrigado.",
                    "Clima para caldito de pollo y meterse tempranito a la cama.",
                    "Hace tanto frío que hasta las cobijas se sienten heladas, mae.",
                    "Qué friíto más rico para echarse un tamalito caliente con café recién chorreado.",
                    "Póngase jacket gruesa y medias de lana, que el aire viene bajando del volcán bien helado.",
                    "El clima está ideal para pasar la tarde en familia bien abrigaditos con aguadulce."
                )
                WeatherCategory.HEAVY_RAIN -> listOf(
                    "Se vino el baldazo, mae. Lleve sombrilla sí o sí.",
                    "Típico aguacero de invierno, mejor quedarse en la choza.",
                    "Las calles deben estar hechas un río, tenga cuidado.",
                    "Cae un polaco. Salga solo si es muy necesario, mae.",
                    "Qué manera de llover, pura vida pero desde adentro.",
                    "Cuidado en la calle que no se ve nada con este baldazo.",
                    "Un aguacero de esos que dan ganas de puro gallo pinto.",
                    "Esto no es lluvia, es un diluvio. Mejor no salir.",
                    "El cielo se abrió, mae. Ni se le ocurra salir sin capa.",
                    "Lluvia fuerte y pareja, asegure bien las ventanas en la choza.",
                    "Está cayendo un verdadero temporal, cuidado con las presas.",
                    "Mejor espérese a que escampe, salir ahorita es mojada segura.",
                    "Qué clase de pinto, mae. La calle está imposible con tanta agua.",
                    "Paciencia en carretera, con este baldazo todo el mundo maneja lento.",
                    "Un buen cafecito mientras pasa este aguacero tan bárbaro.",
                    "Qué baldazo más parejo, mae. Mejor espere a que aclare antes de cruzar la calle.",
                    "Típica tarde de invierno tico: agua a cántaros y cafecito con pan casero en la mesa.",
                    "Asegure las canoas y cierre bien las ventanas para que no se le pase el agua, pura vida.",
                    "Baldazo de los buenos en todo el país, tómese el tiempo con calma y evite cruzar pasos inundados.",
                    "Agua a mares cayendo sobre el techo, aproveche para descansar calientito en familia."
                )
                WeatherCategory.RAIN -> listOf(
                    "Está lloviendo. No olvide la sombrilla, mae.",
                    "Clima lluvioso. Perfecto para un cafecito, pura vida.",
                    "Agüita constante hoy, salga preparado.",
                    "Lleve algo para protegerse del agua, no se vaya a resfriar.",
                    "Agüita suave pero mojadora, cuidado se resfría.",
                    "Clima para andar despacito, la calle está resbalosa.",
                    "Una tarde lluviosa típica, nada que un buen café no arregle.",
                    "Con esta llovizna mejor ande paraguas en mano.",
                    "Llueve un poquito, pero lo suficiente para arruinar el peinado.",
                    "Agüita necia que no para, abrigue bien a los chiquitos.",
                    "Un día pasado por agua, tómelo con calma y no se estrese.",
                    "Clima de pereza total. Si tiene que salir, ande con cuidado.",
                    "Lluvia tica, mae. Mejor salir con tiempo por si hay presa.",
                    "Lleve sombrilla que esta agüita callada empapa de verdad.",
                    "Está cayendo pelo de gato, abrigue el pecho para evitar gripes.",
                    "Lluvia de esas tranquilas para descansar, nada más ande con paraguas en mano por si arrecia.",
                    "Agüita constante para regar las matas del patio, salga con saquito impermeable.",
                    "Día lluvioso muy acogedor, tómese el tiempo con calma y maneje despacio.",
                    "Llovizna tica de media tarde, guarde la ropa del patio a tiempo y disfrute un cafecito.",
                    "Agüita mansa pero constante, use calzado cerrado para no mojarse las medias, pura vida."
                )
                WeatherCategory.THUNDERSTORM -> listOf(
                    "Hay rayería, mae. Quédese en un lugar seguro.",
                    "Truenos y relámpagos. Mejor desconecte las varas.",
                    "Tormenta fuerte. No salga si no es indispensable, pura vida.",
                    "Cuidado con los rayos, el clima está bastante feo hoy.",
                    "Qué clase de rayería, mejor no ande descalzo.",
                    "Los truenos están fuertes, pura vida pero de larguito.",
                    "Mejor espere a que pase la tormenta para salir, mae.",
                    "Cielo bravo hoy, no se arriesgue con esta rayería.",
                    "Ese trueno asustó a medio barrio, mae. Métase a la casa.",
                    "Desconecte los electrodomésticos por aquello de las moscas.",
                    "Una tormenta bien ruidosa, tranquilice a los perritos en casa.",
                    "La rayería está pesada, espérese a que el cielo se calme un poco.",
                    "Pura fiesta de truenos allá arriba, mejor ni asomarse a la ventana.",
                    "Cielo eléctrico hoy. Aléjese de los árboles altos, pura vida.",
                    "Truenos que hacen temblar los vidrios. Agárrese de la silla, mae.",
                    "Tormenta con buena rayería, mejor espere adentro y cuide que los perritos no se asusten.",
                    "Los truenos están sonando fuerte hoy, manténgase seguro bajo techo y desconecte aparatos.",
                    "Rayería brava sobre el cielo tico, manténgase en un lugar techado y seguro hasta que pase la tormenta.",
                    "Relámpagos iluminando la tarde, cuide a los animalitos y espere con paciencia que escampe."
                )
                WeatherCategory.CLOUDS -> listOf(
                    "El sol se escondió hoy. Día muy nublado, mae.",
                    "Cielo gris, tiene pinta de que va a llover.",
                    "Pura nube. Día tranquilo pero opaco, pura vida.",
                    "Está tapado el cielo, un día bastante tranquilo.",
                    "Bien nublado, especial para no hacer mucho.",
                    "Todo gris hoy, pero tranquilo el ambiente.",
                    "Las nubes no dejan ver ni un pedacito de sol.",
                    "Clima opaco, puro para descansar en la choza.",
                    "Cielo de pura panza de burro, ni una estrellita se va a ver.",
                    "El clima amaneció con pereza, todo gris y apagadito.",
                    "Día nublado, ideal para los que no aguantan el calor.",
                    "Está oscurito hoy. Ni ocupe poner bloqueador, pura vida.",
                    "Un toldo de nubes tapa todo, a lo mejor escampa, a lo mejor no.",
                    "Día pálido, mae. Acomódese y tómese algo calientito.",
                    "Las nubes andan paseando bajito hoy. Disfrute lo fresco.",
                    "Cielo encapotado pero fresquito, perfecto para salir a caminar sin que pegue el sol.",
                    "Día de pura nube y calma, ideal para leer un buen libro o adelantar cosas en la casa.",
                    "Frescura de nubes bajas hoy, disfrute la brisa suave y el ambiente tranquilo."
                )
                WeatherCategory.STRONG_WIND -> listOf(
                    "Hace mucho viento, cuidado con las ramas sueltas.",
                    "Hay un chiflón muy fuerte, agarre bien las cosas.",
                    "Qué ventolero, mae. Cuídese si sale a caminar.",
                    "Mucho viento hoy, póngase abrigo ligero.",
                    "Ese ventolero está como para volar papalote.",
                    "Cuidado y el viento se le lleva la ropa del tendedero.",
                    "Agárrese el sombrero que el chiflón anda jugando rudo.",
                    "Los vientos alisios andan haciendo de las suyas hoy, mae.",
                    "Qué manera de soplar, asegure bien el techo de la casa.",
                    "Viento fuerte, perfecto para despejar un poco las nubes.",
                    "El viento chifla en las ventanas. Tápese los oídos, pura vida.",
                    "Camine despacito que este ventolón lo empuja a uno.",
                    "Los alisios soplan con fuerza hoy, sostenga bien el paraguas para que no se le abra al revés.",
                    "Qué buen ventolero para secar la ropa en un dos por tres, solo asegúrela bien con prensas.",
                    "Brisa navideña o alisios de temporada soplando con vigor, cuídese de los cambios bruscos de temperatura."
                )
                WeatherCategory.HIGH_UV -> listOf(
                    "El sol está que quema. Use bloqueador, mae.",
                    "Índice UV muy alto, cuídese mucho la piel.",
                    "Clima ideal para no exponerse mucho al sol directo.",
                    "No salga sin gorra ni bloqueador, pura vida.",
                    "El sol está picando fuerte, ande sombrillita para taparse.",
                    "Los rayos UV andan sin compasión. Cuide a los chiquitos.",
                    "Sol de esos que dejan marca. Evite las quemadas, mae.",
                    "Póngase cremita protectora porque el solazo no perdona hoy.",
                    "Radiación solar fuerte hoy, busque la sombra y tome bastante agüita fresca."
                )
                WeatherCategory.PERFECT -> listOf(
                    "Un día sencillamente hermoso, disfrute mucho.",
                    "El clima está pura vida. Ni calorón ni fríazo, al punto.",
                    "Día espectacular para salir a dar una vuelta por el parque.",
                    "Pocas veces tenemos un clima tan bonito, aproveche el rato.",
                    "Cielo bonito y brisa fresca, mae. Un diez de diez.",
                    "Clima de postal en Costa Rica hoy, aproveche para compartir al aire libre."
                )
                WeatherCategory.TROPICAL -> listOf(
                    "Climita cálido sabroso, perfecto para ir pensando en la playa.",
                    "Hace un frescor tropical muy tuanis. Tómese un fresquito.",
                    "Calorcito del bueno. Póngase cómodo y disfrute el día.",
                    "El clima andan vacilón, tibio y relajante, pura vida.",
                    "Día de playita pero en la ciudad, póngase ropita fresca.",
                    "Ambiente tropical delicioso, apenas para una pipa bien fría o un refresco natural."
                )
                WeatherCategory.CLEAR_SKIES -> listOf(
                    "Cielo limpiecito, sin una sola nube. ¡Qué belleza!",
                    "Hoy sí salió el sol con todo, pura vida. ¡A lavar la ropa!",
                    "Despejado total, mae. Salga a respirar aire puro un ratito.",
                    "Un sol resplandeciente, que le alegre el día bastante.",
                    "Está tan despejado que parece cuadro de pintura. Disfrute.",
                    "Día completamente soleado y despejado, ideal para secar las cobijas y pasear."
                )
                WeatherCategory.HIGH_HUMIDITY -> listOf(
                    "Se siente pegajoso el aire por tanta humedad, tome mucha agua.",
                    "Bochorno fuerte hoy, la humedad está altísima, pura vida.",
                    "Suda uno hasta sentado, mae. Ponga el ventilador un ratito.",
                    "Se siente el aire pesado, tómelo con calma y vístase fresquito.",
                    "Humedad al máximo, parece que andamos caminando en un sauna.",
                    "El bochorno se siente pesado hoy, tómese un buen vaso de agua con limón para refrescar el cuerpo.",
                    "Bastante humedad en el ambiente, vístase con ropa de algodón bien holgada y fresca.",
                    "Vaporón húmedo típico del trópico, manténgase en lugares ventilados y bien hidratado."
                )
                WeatherCategory.NIGHT -> listOf(
                    "Ya es de nochecita, descanse y que duerma pura vida.",
                    "La noche está tranquila. Acomódese y tómese un cafecito.",
                    "Dulces sueños, mae. Que mañana sea un mejor día.",
                    "Noche estrellada y bonita, perfecta para descansar bien.",
                    "El clima de noche invita a cobijarse rico. Hasta mañana.",
                    "Noche serena en el barrio, apague las luces y descanse con tranquilidad."
                )
                // removed NICE_DAY and NIGHT_CLEAR
            }
        } else {
            when (category) {
                WeatherCategory.EXTREME_HEAT -> listOf(
                    "Mae, qué calor más hijueputa. El asfalto parece lava y suda uno hasta la raya del culo.",
                    "Diay malparido, ¿quién dejó el portón del infierno abierto? Esta mierda es insoportable.",
                    "Mae, el sol le está pegando un beso del diablo a la tierra. ${temperature.toInt()} grados de puro sufrimiento y sudor.",
                    "¿Quién dejó el horno abierto? Esta vara es un infierno concho, pedazo de bestia.",
                    "Diay carepicha, hágase a la idea de que va a sudar hasta lo que no se ha comido.",
                    "Mae, hoy el asfalto derrite hasta las chanclas. Busque sombra antes de quedar como chicharrón, imbécil.",
                    "Qué calor más hijueputa, mae. Ni el abanico en nivel 3 salva de esta vara hedionda.",
                    "Mae, este calor está para andar en pelotas en la choza mandando todo a la picha.",
                    "Suda uno hasta sentado sin hacer ni picha, qué infierno más asqueroso.",
                    "Mae, esto parece el cerro de la muerte pero prendido en candela. Qué asco de bochorno.",
                    "El sol pica tanto que si sale se le derrite hasta el cerebro, carepicha.",
                    "Me cago en el sol y en este calor de mierda. Ni una birra aguanta fría tres minutos.",
                    "Mae, qué sancocho más desgraciado. Ando con los huevos fritos y pegados al pantalón.",
                    "Si sale ahorita es porque es un soberano masoquista. Quédese encerrado con el abanico, pedazo de animal.",
                    "Coman picha con este calor hijueputa, mae.",
                    "Ey usted, feto con patas, mejor no salga con este calor.",
                    "Mae, el aire quema los pulmones. Parece que San Pedro nos metió en un air fryer al máximo.",
                    "Hasta respirar da pereza con este calor tan malparido. Cero ganas de bretear.",
                    "Diay carebarro, ¿quería verano? Ahí tiene su verano, atragántese de calor ahora.",
                    "Ni el diablo aguanta esta temperatura en Puntarenas, qué despiche de calorón.",
                    "Mae, un paso afuera y queda rostizado como pollo de bombillo. Quédese adentro, idiota.",
                    "Bañarse con agua fría no sirve de ni picha, a los dos segundos ya está sudando sopa otra vez.",
                    "Sol hijueputa sin piedad. Andar en bus con este calor es la muerte en vida, mae.",
                    "Mae, esto es una freidora de aire con patas. Me estoy sancochando vivo, carepicha.",
                    "¿${temperature.toInt()} grados? Váyase a la mierda, a este paso voy a terminar evaporado.",
                    "Hasta el sobaco me hierve, mae. Qué clase de infierno más pura mierda.",
                    "Diay mae, el sol está ensañado conmigo y con usted. Qué ganas de mentarle la madre al cielo.",
                    "Mae, me estoy derritiendo peor que candela de velorio. Qué hijueputa calor más criminal.",
                    "Diay carepicha, ${temperature.toInt()} grados en el lomo y el bus sin una sola ventana que abra. Qué tortura china.",
                    "¿Pero qué es este sancocho de mierda? Siento que si me siento en la banqueta me hago fondue de nalgas.",
                    "Mae, el diablo vino a pasar vacaciones a Costa Rica y nos dejó el fogón prendido al máximo. Qué asco.",
                    "Hasta el perro de la vecina anda con la lengua afuera pidiendo piedad, qué calorón más malparido.",
                    "Mae, ¿${temperature.toInt()}°C? El asfalto de Chepe está tan caliente que se le pegan las suelas de las chancletas al cruzar la avenida segunda.",
                    "Diay carepicha, el bus viene sin una sola ventana abierta y con 50 personas sudando sopa. Qué cámara de tortura más desgraciada.",
                    "El sol está bajando en línea recta a calcinarle las ideas. Póngase gorra antes de que le dé un derrame de puro bochorno, idiota.",
                    "Mae, esto no es calor, es un ataque terrorista del Sol contra Costa Rica. Ni con cinco pipas frías se apaga este fogón.",
                    "Diay carebarro, ¿quería verano? Ahí tiene su verano, atragántese de polvo y sudor de sobaco hasta que pida cacao.",
                    "El asiento del carro quema peor que plancha de panadería. Si se sienta con short corto va a gritar de puro dolor, bestia.",
                    "Mae, qué calor más maldito. Hasta el hielo de la coquita se derritió antes de que le diera el primer trago.",
                    "Sancocho nacional a ${temperature.toInt()} grados: el país entero huele a protector solar vencido y asfalto tostado."
                )
                WeatherCategory.COLD -> listOf(
                    "Mae, qué frío más carepicha. Se me congelaron hasta las ganas de vivir.",
                    "Diay mae, ¿desde cuándo Cartago conquistó todo el país? Qué frío más hijueputa.",
                    "Mae, saque la cobija de tigre con urgencia. Esto ya parece el Polo Norte, qué concho.",
                    "Mae, se me congeló hasta la jupa. Ponga café chorreado ya o aquí morimos todos congelados.",
                    "${temperature.toInt()}°C en Costa Rica y la gente ya anda como esquimal de Suiza. Qué playada de frío.",
                    "Mae, el viento le congela hasta los pensamientos. Ni a putas me baño hoy si no calienta la ducha.",
                    "Diay mae, qué frío más malparido. Tápese bien si no quiere amanecer con los mocos colgando.",
                    "Mae, ando con los huevos en la garganta del frío que hace. Ni tres suéteres calientan esta mierda.",
                    "Qué frío más pura mierda, ni metido entre las cobijas se quita este entumecimiento.",
                    "Diay mae, este clima está para quedarse en cama y mandar todo a la picha.",
                    "Coman picha, voy a agarrar neumonía con este clima de mierda.",
                    "Ey usted, feto con patas, tápese que si tiembla de frío se desbarata.",
                    "Mae, se me congelan las ideas con este hijueputa frío. La jupa no me da para pensar ni picha.",
                    "Parece que abrieron el congelador de la universal en todo el Valle Central, qué frío más hijueputa.",
                    "Mae, si sale en pantaloneta con este frío es un completo retrasado mental. Póngase pantalones.",
                    "Se me están cayendo los dedos del frío, mae. Hasta los perros andan con suéter hoy.",
                    "Me cago en el viento helado este. Ni con aguadulce hirviendo entra uno en calor.",
                    "Clima malparido nivel: no quiero ni sacar la mano de la cobija para apagar la alarma.",
                    "Mae, qué clase de congelador. El piso parece cubeta de hielo, camine con medias o muérase.",
                    "Diay carepicha, saque la chompipa del ropero que este friazo no es jugando.",
                    "Hasta la taza del baño está helada como témpano, qué sufrimiento más concho.",
                    "Frío asqueroso que cala hasta los huesos. Manden sopa o manden café, pero rápido.",
                    "Mae, ando con los labios partidos y el moco congelado, qué frío más desgraciado.",
                    "Si no tiene cobija de tigre hoy se muere de hipotermia, pedazo de bestia.",
                    "Parece que San Pedro se durmió con el freezer abierto, qué friazo más hijueputa.",
                    "Mae, qué friazo más hijueputa, tengo los pezones que parecen timbres de bus.",
                    "Diay malparido, ¿quién dejó la puerta del congelador abierta en el Irazú? Se me congeló hasta el apellido.",
                    "Qué playada de frío, mae. Salir del baño caliente es una ruleta rusa de neumonía.",
                    "Mae, ando con dos buzos, tres medias y todavía me tiemblan las muelas del frío que hace. Una mierda completa.",
                    "Frío concho que cala hasta el tuétano. Solo un borracho saldría en camiseta hoy, pedazo de bestia.",
                    "Mae, qué friazo más hijueputa. Parece que todo el país se volvió las faldas del volcán Irazú a las 4 am.",
                    "Diay carepicha, se me congeló hasta la risa. Ni con tres tazas de café chorreado y dos buzos entra uno en calor.",
                    "Frío concho nivel: la taza del baño está tan helada que da pánico sentarse a orinar, qué tortura china.",
                    "Mae, ando con la jupa tiesa y los labios partidos de este frío tan desgraciado. Saque la cobija de tigre o muérase.",
                    "Diay malparido, ¿quién pidió este clima de Suiza para Costa Rica? Aquí somos tropicales, no pingüinos de cafetal.",
                    "Se me están cayendo las orejas del friazo, mae. Si no prende el calentador de agua hoy, no hay quién se bañe.",
                    "Frío concho que cala hasta el tuétano. Hasta las lagartijas andan buscando suéter, qué despiche de clima.",
                    "Mae, si sale a la calle en camiseta hoy con este friazo es porque de verdad no tiene ni dos neuronas vivas."
                )
                WeatherCategory.HEAVY_RAIN -> listOf(
                    "Qué soberano despiche de agua, mae. Se cayó el cielo y las calles parecen ríos de caca.",
                    "Típico aguacero tico: hace 10 minutos había sol y ahora parece el diluvio universal. Qué picha.",
                    "Diay mae, se vino el baldazo de la gran puta. Salga en lancha o acepte que va a mamar.",
                    "Los caños de la calle ya parecen los canales de Venecia versión Chepe marginal, mae.",
                    "Salga en lancha mejor, pedazo de bestia. Si sale en tenis blancas ya las dio por perdidas a la mierda.",
                    "Mae, el baldazo está tan rajado que hasta los sapos andan con salvavidas pidiendo cacao.",
                    "Diay, la presa en la General Cañas va a durar 4 horas con este aguacero. Mejor jale a dormir a la choza.",
                    "Mae, el cielo está llorando como si le debieran un platal. Qué despiche de agua.",
                    "Lluvia de la grandísima puta. Las alcantarillas colapsaron y el agua llega a la rodilla.",
                    "Mae, con este baldazo ni los patos quieren salir. No sea carepicha y quédese adentro.",
                    "Coman picha con este baldazo de mierda, así no hay quién viva.",
                    "Ey usted, feto con patas, si sale a la calle con esta tempestad se va por la alcantarilla.",
                    "Se vino el chubasco malparido a joder la tarde. Toda la ropa del tendedero se fue a la picha.",
                    "Mae, si se le ocurre salir con sombrilla de 2 rojos de pulpería, se le va a despichar en 3 segundos.",
                    "El agua cae con ganas de ahogar a medio mundo. Qué diluvio más hijueputa.",
                    "Diay mae, el cielo se abrió como llave de tubo rota. Qué baldazo más concho.",
                    "Calle inundada, zapatos llenos de barro y choferes tirando agua a los peatones. Qué belleza de país.",
                    "Mae, llueve tanto que hasta el waze se rindió. Prepárese para comer presa 3 horas.",
                    "Baldazo nivel: si saca la cabeza por la ventana se ahoga de una vez, animal.",
                    "Me cago en la lluvia, se me metió el agua hasta por debajo de la puerta de la choza.",
                    "Qué clase de baldazo más desgraciado. Ni con capa impermeable se salva de quedar como sopa.",
                    "El cielo está vomitando agua sin asco. No sea idiota y no se meta a cruzar ríos en carro.",
                    "Ni Noé en su arca aguantaría esta correntada de agua tan hijueputa.",
                    "Mae, el techo de zinc parece ametralladora con este aguacero, no se oye ni una mierda.",
                    "Baldazo descomunal, las alcantarillas están eructando barro y basura por toda la calle.",
                    "Mae, qué baldazo más hijueputa. Las aceras ya no existen, ahora son rápidos del río Reventazón.",
                    "Diay carebarro, ¿salió en tenis de tela con este diluvio? Prepárese para andar nadando en sopa de barro todo el día.",
                    "El cielo se cagó en todo hoy. Qué aguacero más descarado, hasta los limpiaparabrisas salieron volando.",
                    "Mae, llueve tan duro que el agua entra de lado, por arriba y por debajo de las puertas. Me cago en el invierno.",
                    "Baldazo nivel: si frena de golpe en la pista va a terminar parqueado en la sala del vecino, carepicha.",
                    "¡QUÉ BALDAZO MÁS HIJUEPUTA, MAE! La Circunvalación ya parece los rápidos del río Pacuare y los carros van flotando.",
                    "Diay carepicha, ¿vio cómo baja el agua por la cuesta? Los caños colapsaron y el barro le llega a los tobillos al bajarse del bus.",
                    "Mae, temporal cerrado: el agua cae con tanta furia en el techo de zinc que no se oye ni cuando le mientan la madre.",
                    "Se vino el diluvio universal sobre el país. Los limpiavidrios van a la velocidad de la luz y no se ve ni picha a tres metros.",
                    "Diay carebarro, ¿salió en tenis blancas con este aguacero? Prepárese para tirarlas a la basura cuando llegue a la choza.",
                    "Mae, llueve tan concho que hasta las palomas del parque andan pidiendo refugio en las sodas. Quédese adentro, idiota.",
                    "Baldazo nivel: si frena de golpe en la pista va a hacer tres trompos antes de quedar parqueado en la cuneta, bestia.",
                    "El cielo se abrió como catarata del Toro Amarillo. No juegue de torear correntadas en carro si no quiere salir en las noticias."
                )
                WeatherCategory.RAIN -> listOf(
                    "Puta lluvia más necia, mae. De esas lloviznas pendejas que joden parejo todo el santo día.",
                    "Está lloviznando, mae. No se confíe que en cualquier momento se viene el baldazo traicionero.",
                    "Lleve paraguas aunque sea feo, mae. Si se moja después anda llorando con gripe en el brete como un imbécil.",
                    "El cielo anda escupiendo agua asquerosa. Típica tarde tica para aplatanarse y no hacer ni verga.",
                    "Diay mae, el clima bipolar de este país no falla: sol de mañana, lluvia a la tarde para despichar los planes.",
                    "Mae, guarde la ropa del tendero que ya se le mojó todita por andar de dejado, carepicha.",
                    "Llueve lo suficiente para joderle el día, pero no tanto para que le den libre en el brete. Qué estafa.",
                    "Agüita pendeja pero que moja hasta el alma. No sea idiota y lleve sombrilla.",
                    "Lluvia tica asquerosa, pura humedad, barriales y zapatos hediondos a humedad.",
                    "Mae, qué llovizna más malparida. Parece que el cielo no se decide si orinar o cagar.",
                    "Si va a salir con esta lluvia a mojar la calle, coman picha todos.",
                    "Ey usted, feto con patas, tápese bien que si se moja se disuelve.",
                    "Mire esa agüita necia... perfecto para que la pista se llene de chocones que no saben manejar en lluvia.",
                    "Clima hijueputa: se le moja la espalda aunque ande sombrilla gigante.",
                    "Diay mae, ¿quién pidió lluvia a esta hora? Ya se me despichó la salida con los compas.",
                    "Lluvia concho que solo sirve para resfriar a la gente y empapar las medias.",
                    "Mae, ande sombrilla o prepárese para oler a perro mojado todo el resto del día.",
                    "El cielo está meando finito. Una delicia para andar en bus sudado y con las ventanas cerradas.",
                    "Qué pereza de lluvia, mae. Todo se vuelve gris, lerdo y lleno de presas interminables.",
                    "Métale ganas al café, porque con esta lluvia el cerebro se le puso en modo vegetal.",
                    "Llovizna conchuda que solo sirve para encharcar la acera y mancharle los pantalones de barro.",
                    "Mae, el pelo se me esponjó como escoba vieja con esta llovizna hedionda.",
                    "Agüita que no calienta ni enfría, solo jode la paciencia de todo el mundo.",
                    "Llovizna pendeja que moja como si fuera suegra metiche: despacito pero jodiendo todo el santo día.",
                    "Mae, qué lluvia más hijueputa. El agua me resbaló por la espalda y ando con los calzones húmedos, qué delicia.",
                    "Diay mae, el cielo orinando finito solo para despichar el peinado y enlodar los pantalones. Pura maldad.",
                    "Agüita asquerosa que no sirve ni para lavar el carro pero sí para dejarlo todo manchado de polvo mojado.",
                    "Mae, ¿para qué putas lavé la ropa ayer si hoy iba a caer esta agüita traicionera? Me cago en San Pedro.",
                    "Puta llovizna más necia y traicionera, mae. Moja despacito pero le deja los pantalones empapados hasta la rodilla.",
                    "Diay mae, el cielo orinando finito solo para joderle el peinado y llenarle los zapatos de barro. Qué pura maldad.",
                    "Agüita concha de invierno tico: no escampa ni un segundo pero tampoco llueve duro para que cancelen el brete, qué estafa.",
                    "Mae, esa llovizna asquerosa huele a asfalto mojado y humo de camión. Una delicia de ambiente para andar en la calle.",
                    "Lluvia pendeja que no limpia nada pero sí ensucia el parabrisas con polvo chicloso. Qué chicha da esta vara.",
                    "Mae, lleve sombrilla aunque le dé vergüenza. Si se moja con este pelo de gato después anda con mocos y fiebre de polo.",
                    "Tarde pasada por agua en Chepe: presas de tres horas, gente empujándose en las paradas y el cielo meando sin parar.",
                    "Mae, guarde la ropa del tendedero antes de que esa llovizna callada se la deje hedionda a humedad, carepicha."
                )
                WeatherCategory.THUNDERSTORM -> listOf(
                    "MAE, METASE A LA CHOZA. Eso no es música de fondo, son rayos partiéndolo todo, pedazo de idiota.",
                    "ALERTA MAE: tormenta eléctrica. Métase a la casa antes de que San Pedro lo fulmine por andar de polo.",
                    "Diay mae, el cielo está tirando pichazos de electricidad. No se haga el valiente bajo un árbol, carepicha.",
                    "Mae, apague la compu y desconecte la pantalla, que esto se puso feo de verdad o se le quema todo.",
                    "Cielo en modo rave eléctrico con truenos. Si le cae una chispa no venga a quejarse aquí, malparido.",
                    "Mae, parece que Dios está jugando boliche allá arriba y tirando granadas. Qué rayería más rajada.",
                    "Qué pichazo de truenos, mae. Desconecte la refri y la tele si no quiere llorar después.",
                    "Rayería asquerosa, mejor no se asome ni a la ventana a jugar de vivo.",
                    "Mae, el cielo está más enojado que su tata cuando le rayó el carro. Tremendo despiche eléctrico.",
                    "Trueno que retumba en el pecho, mae. Hasta a los chatas se les quitó lo valientes con este rayo.",
                    "Cielo demoníaco tirando centellas. Salga con pararrayos en la jupa si quiere morirse rápido.",
                    "Mae, qué clase de bombazos están cayendo. Casi me cago del susto con el último relámpago.",
                    "Diay mae, tormenta eléctrica nivel fin del mundo. No sea tan animal de meterse a una piscina ahorita.",
                    "Se fue la luz con el primer pichazo de trueno, fijo. Clásico del ICE en invierno.",
                    "Mae, ese trueno sonó como si hubiera explotado una bomba en el patio del vecino. Qué picha.",
                    "Tormenta brava, rayería continua. Quédese quieto en un rincón y no invente estupideces.",
                    "Rayo de la gran puta, hasta la tierra vibró. Métase a la choza, so pedazo de animal.",
                    "Diay mae, ¿quiere terminar como carbón de parrillada? Deje de andar afuera con esta rayería.",
                    "Un relámpago más y me quedo ciego, qué clase de despiche eléctrico.",
                    "MAE, ESE RAYO CAYÓ A LA VUELTA. Casi me cago en los pantalones del susto, qué pichazo más sordo.",
                    "San Pedro anda tirando bombas atómicas allá arriba. Desconecte la play station si no quiere verla salir en humo, idiota.",
                    "Diay carepicha, ¿qué hace afuera con paraguas de punta metálica? ¿Está buscando que le reinicien la vida con 100.000 voltios?",
                    "Qué rayería más demoníaca, mae. El cielo está rugiendo como suegra enojada un domingo a mediodía.",
                    "¡ZAPATAZO DE RAYO, MAE! Hasta los pelos de los brazos se me erizaron del bombazo. Desconecte la tele ya, pedazo de bestia.",
                    "Diay carepicha, ¿qué hace afuera con semejante rayería? ¿Quiere que San Pedro le mande 50.000 voltios a la jupa?",
                    "Mae, los truenos retumban como si estuvieran dinamitando la montaña a la par. Métase a la choza y cierre todo.",
                    "Rayería endemoniada sobre el Valle Central: relámpagos cegadores y truenos que le hacen temblar el pecho del susto.",
                    "¡BOMBAZO EN EL TENDIDO ELÉCTRICO! Se fue la luz en tres cantones de un solo golpe. Típico del invierno tico, malparido.",
                    "Mae, parece que el fin del mundo empezó en Costa Rica. No sea caballo y no se pare debajo de latas de zinc ni árboles.",
                    "Truenos conchos haciendo aullar a todos los perros del barrio. Qué clase de despiche eléctrico más violento.",
                    "Mae, rayería brava: el cielo truena tan duro que las alarmas de los carros se prendieron solas en toda la cuadra."
                )
                WeatherCategory.CLOUDS -> listOf(
                    "El sol pidió vacaciones y nos dejó esta nube gris de mierda. Qué picha de día.",
                    "Cielo gris nivel 'hoy no tengo ganas de hacer ni picha en el brete ni en la choza'.",
                    "Pura nube gris. Ni llueve ni hace sol, pura indecisión tica que da cólera.",
                    "El cielo está más cerrado que pulpería un domingo a las 9 de la noche, mae.",
                    "Mae, día gris y aburrido como misa de 6 am. Ni la sombrilla sabe si la van a usar hoy.",
                    "Cielo color sobaco de trailero. Qué agüevazón de día más hijueputa.",
                    "Pura nube y cero sol, día perfecto para la depre y mandarlo todo al carajo.",
                    "Mae, qué pereza de clima, está más feo que pegarle a la mamá en el día del padre.",
                    "Nublado carepicha, ni fu ni fa, puro nubarrón para arruinarle el ánimo a cualquiera.",
                    "Mire para arriba, mae: un techo de cemento gris. Qué ganas de no existir hoy.",
                    "Coman picha, el cielo está igual de feo que el gobierno.",
                    "Ey usted, feto con patas, ni siquiera el sol lo quiere ver hoy con esta nubosidad.",
                    "Diay mae, el cielo parece pantalla descompuesta. Cero luz, pura tristeza ambiental.",
                    "Cielo con cara de culo. Se ve venir la mala vibra desde 10 kilómetros.",
                    "Mae, está tan nublado que hasta las palomas andan agüevadas en el parque.",
                    "Día concho y desteñido. Ni para tomar fotos sirve esta porquería de luz.",
                    "Nubes pesadas que no sueltan ni una gota pero tapan todo. Qué estafa de cielo.",
                    "Mae, ese montón de nubes estorban más que un dolor de huevos. Qué clima más sobaco.",
                    "Cielo nublado asqueroso, parece que Dios agarró un borrador sucio y lo pasó por todo lado.",
                    "Ni para llover sirven esas hijueputas nubes, solo para hacer bulto y quitar la luz.",
                    "Día gris cara de culo. Cero motivación da ver este cielo tan pura mierda.",
                    "Diay mae, las nubes andan jugando de vivas tapando todo el sol. Qué chicha da esta vara.",
                    "Clima nublado tan sin gracia, es como comer arroz blanco sin sal, pura picha.",
                    "Día de esos que la nube le dice 'coma mierda' a sus planes de salir y 'coma mierda' a quedarse en casa.",
                    "Mae, qué cielo más sarnoso, puro nubarrón atravesado que no deja ver un puto rayo de sol.",
                    "Esa pinche capa gris en el cielo me quita hasta las ganas de levantarme, qué nublado más hijueputa.",
                    "Cielo tapado como cañería vieja. Cero sol, puro aburrimiento carepicha.",
                    "Ni para un lado ni para el otro, nublado desgraciado que solo da pereza existencial.",
                    "Cielo gris sobaco de chancleta. Un día tan deprimente que dan ganas de llorar viendo la pared.",
                    "Mae, qué nublado más hijueputa. Parece que pintaron el cielo con agua de trapo sucio, qué agüevazón.",
                    "Pura nube tapando el sol para que todo parezca una película soviética en blanco y negro. Qué pereza de país.",
                    "Diay mae, ¿y el sol? Se lo robaron los políticos fijo, porque con este cielo gris lo único que hay es miseria."
                )
                WeatherCategory.CLEAR_SKIES -> listOf(
                    "Cielo despejado, mae. No hay ni media nube para tapar el solazo que le va a quemar la jupa.",
                    "Despejado total, mae. Aproveche para lavar las cobijas antes de que al cielo se le ocurra cambiar de opinión.",
                    "El cielo está limpito, mae. Ni un rayón de nube. Salga a asolearse la jupa un rato, no sea vago.",
                    "Sol abierto, carepicha. Salga a respirar aire fresco en vez de estar pegado al celular todo el puto día.",
                    "Mae, ni una sola nube en kilómetros. Si no sale hoy es porque de verdad es un ermitaño de mierda.",
                    "Cielo azul perfecto, mae. Pero no se duerma en los laureles, que el sol despejado es pura trampa de la naturaleza, carepicha.",
                    "Despejadito y brillante, ideal para que le encandile los ojos si se le olvidaron las gafas, idiota.",
                    "Qué cielo más limpio, mae. Aproveche para salir a la calle antes de que esta vara se ponga a llover de la nada.",
                    "Día despejado de película. Lástima que usted tenga que estar encerrado breteando como esclavo malpagado.",
                    "Ni media nube tapando el sol. Salga a mover las nalgas que la vitamina D le hace falta para esa cara de enfermo, mae.",
                    "Cielo limpiecito, sin una puta nube. Parece mentira que esté tan bonito afuera y usted amargado como siempre.",
                    "Mae, si con este cielo despejado no sale de la cueva, es porque ya es un antisocial irrecuperable. Vaya respire aire, imbecil.",
                    "Qué día más pichudo y despejado. Lástima que su vida amorosa no sea tan clara, carepicha.",
                    "Puro sol y cielo azul. Si se queda encerrado viendo series, que Dios me lo perdone por ser tan tremendo cerote.",
                    "Cielo azul chillón, mae. Vaya queme grasa afuera en vez de estar rascándose las bolas.",
                    "Ni media nube en el horizonte. No hay excusa válida para andar de amargado hoy, carebarro."
                )
                WeatherCategory.HIGH_HUMIDITY -> listOf(
                    "${humidity}% de humedad. Básicamente estamos respirando una sopa hirviendo, qué asco de bochorno.",
                    "Mae, el aire pesa tres toneladas. Me suda hasta el apellido con este bochorno tan malparido.",
                    "Humedad al ${humidity}%. El paño de baño no se seca ni en tres semanas, qué picha más asquerosa.",
                    "Mae, esto no es aire, es caldo de patas caliente con vapor. Qué calor más pegajoso y hediondo.",
                    "Bochorno rajado, mae. La camisa ya se le pegó a la espalda y los sobacos parecen cataratas.",
                    "Diay carepicha, ${humidity}% de humedad. Parece que uno está metido en un sauna con ropa puesta.",
                    "Mae, qué asco de vaporón. Siente uno que se está ahogando en tierra firme.",
                    "Aire espeso nivel: puede cortar la humedad con cuchillo de mantequilla. Qué mierda más pegoste.",
                    "Suda uno solo con parpadear. El pelo se le esponjó como estropajo viejo, qué desastre.",
                    "Humedad asquerosa que aplatana a cualquiera. Dan ganas de meterse a una hielera y no salir nunca.",
                    "Ando más pegajoso que chicle en zapato, qué asco de bochorno hijueputa.",
                    "Humedad al tope, mae. Respira uno y siente que le entra medio litro de agua caliente al pulmón.",
                    "Humedad del ${humidity}%, mae. La ropa se le pega a las verijas y siente uno que anda nadando en sopa tibia.",
                    "Qué vaporón más asqueroso, parece que alguien tiró agua caliente en una sauna con 20 gordos sudando. Insoportable.",
                    "Mae, ${humidity}% de humedad: esto no es aire, es una sopa tibia de mondongo que se le mete por los pulmones al respirar.",
                    "Bochorno descomunal: suda uno hasta pestañeando y la camisa parece pegada con goma loca al pecho, carepicha.",
                    "Diay malparido, ${humidity}% de humedad y cero brisa. El paño de baño amanece más mojado que ayer, qué asco de clima.",
                    "Mae, el bochorno en Costa Rica hoy es insoportable. Los sobacos parecen cataratas de La Paz y el humor anda por el suelo."
                )
                WeatherCategory.STRONG_WIND -> listOf(
                    "Mae, agárrese la peluca que este viento lo manda directo a comer mierda a Puntarenas.",
                    "Viento rajado a ${windSpeed.toInt()} km/h: las tejas del vecino van volando por la cuadra, mae.",
                    "Diay mae, si abre la sombrilla va a salir volando como Mary Poppins versión carepicha.",
                    "Los alisios andan sueltos y emputados, mae. Cierre las ventanas si no quiere que la choza parezca un remolino de basura.",
                    "Mae, este ventolero está para salir volando a la verga. Hasta los postes se bambolean.",
                    "Agárrese duro mae, que este viento se lleva hasta los calzones del tendedero y los manda a Cartago.",
                    "Qué ventolero más hijueputa. El polvo de la calle se le mete hasta en los ojos y en la boca.",
                    "Chiflón malparido que le desacomoda la jupa en medio segundo. Ni con gel aguanta ese peinado.",
                    "Mae, ese viento ruge como si le estuvieran mentando la madre al barrio entero.",
                    "Cuidado con una lata de zinc voladora, mae. Con este ventolero todo se vuelve un proyectil asesino.",
                    "Ventolero que le arranca las ideas, mae. Camine inclinado si no quiere que lo voltee un ráfagazo.",
                    "Mae, el viento le chifla en las orejas como si le estuvieran diciendo que coma mierda en estéreo.",
                    "Mae, este ventolero de mierda me tiró el portón en la jupa. Andan volando hasta las bolsas de basura de hace un mes.",
                    "Chiflón hijueputa que le da vuelta a la sombrilla y lo deja haciendo el ridículo frente a todo el mundo en la parada.",
                    "El viento anda tan bravo que le arranca las cejas si camina en contra. Agárrese de un poste, pedazo de animal.",
                    "Mae, ráfagas de ${windSpeed.toInt()} km/h: los alisios andan arrancando latas de zinc y desarmando sombrillas por todo el país.",
                    "Chiflón asesino en la parada del bus: el viento le vuela la gorra a la pista y un trailer de 18 ruedas se la hace tortilla.",
                    "Diay carebarro, agárrese de un poste porque este ventolero de mierda lo va a mandar a comer tierra a la cuneta.",
                    "Mae, el viento sopla tan duro que las puertas de la choza tiemblan como si quisieran entrar los espantos. Cierre con llave.",
                    "Ventolero bravo levantando nubes de polvo y tierra: los ojos le arden y la boca le sabe a arenilla de calle, qué asco.",
                    "Mae, las tejas del vecino van planeando por el aire como frisbees asesinos. No camine cerca de tapias viejas, animal.",
                    "Chiflón de los alisios que le desacomoda la ropa y le congela las ideas. Qué ventolero más concho y descarado.",
                    "Mae, si saca la sombrilla con este viento va a parecer paraguas de piñata rota en dos segundos. Ni lo intente."
                )
                WeatherCategory.HIGH_UV -> listOf(
                    "Mae, el Sol anda con ganas de borrarlo del mapa. Póngase bloqueador o va a terminar como pellejo frito.",
                    "El UV está en ${String.format(java.util.Locale.US, "%.1f", uvIndex)}, carepicha. Un minuto afuera y le da cáncer de piel en la jupa.",
                    "Bloqueador factor 100 y sombrero de paja, idiota. No se haga el vikingo bronceado que parece camarón de feria.",
                    "El sol está criminal hoy. Póngase bloqueador YA, pedazo de bestia, o aténgase a arder toda la noche.",
                    "Radiación ultravioleta que pela hasta las piedras. No salga a mediodía a jugar de gallito.",
                    "Mae, el sol está tan bravo que parece un láser espacial. Se le va a despichar la cara si no se cuida.",
                    "Si sale sin gorra hoy es porque de verdad no tiene ni dos neuronas activas en esa jupa.",
                    "Sol asesino nivel: quema hasta a través del parabrisas del carro. Cuídese el lomo, mae.",
                    "El índice UV está que arde, mae. Si no se echa bloqueador va a parecer manzana envenenada.",
                    "Cuidado con ese solazo, carepicha. No juegue de machito y búsquese una sombrita.",
                    "El sol está tirando rayos láser directo a la calva. Póngase bloqueador o va a quedar rojo como tomate de feria, carepicha.",
                    "Radiación nivel Chernobyl en San José. Si sale sin gorra se le va a freír el poco de masa cerebral que le queda, bestia.",
                    "Mae, índice UV en ${String.format(java.util.Locale.US, "%.1f", uvIndex)}: el sol le está quemando el lomo como chuleta en parrilla. Póngase bloqueador YA.",
                    "Radiación ultravioleta que pela las piedras: si sale a mediodía sin gorra va a terminar con la jupa roja como semáforo, idiota.",
                    "Diay carepicha, el sol no alumbra hoy, CASTIGA. No juegue de macho alfa bronceado que va a amanecer llorando del ardor."
                )
                WeatherCategory.PERFECT -> listOf(
                    "Mae, por fin el clima no está hecho una mierda. Aproveche porque en este país lo bueno no dura ni media hora.",
                    "Clima perfecto hoy, mae. Fijo algún despiche bíblico va a pasar más tarde para compensar.",
                    "Día de gloria en Costa Rica: no hace ni un calor de mierda ni un frío carepicha. Salga a disfrutar, vago.",
                    "Hoy el clima amaneció de buenas pulgas, qué milagro. Salga de la choza a mover las nalgas, mae.",
                    "Ni lluvia jodiendo, ni viento botando árboles, ni sol calcinando. Qué belleza de día, no lo cague quejándose.",
                    "Clima nivel dios hoy, mae. Si se queda encerrado viendo tiktoks es un soberano desperdicio de vida.",
                    "Apenas para una birra fría o un café con la doña. El clima hoy no tiene ni un solo pero, mae.",
                    "Disfrute este clima tuanis, porque mañana seguro cae un baldazo que inunda la cuadra entera.",
                    "Coman picha, pero con este clima tan tuanis no me voy a amargar.",
                    "Ey usted, feto con patas, hasta a usted se le ve mejor cara con este clima.",
                    "Un clima tan bueno que hasta da desconfianza, mae. Algo malo va a pasar fijo.",
                    "Día impecable, ni una puta queja. Vaya viva la vida un rato, pedazo de amargado."
                )
                WeatherCategory.TROPICAL -> listOf(
                    "${temperature.toInt()}°C y clima tropical. Clima perfecto para jalar a la playa a hartarse mariscos y birra.",
                    "Calorcito sabroso, carepicha. Un copo con leche condensada en el puerto caería de perlas ahorita.",
                    "Típico clima tico de media mañana: tibio, brillante y con ganas de mandar el brete a la picha.",
                    "Mae, qué clima más sabroso para estar en hamaca tirando hueva en vez de estar aguantando broncas ajenas.",
                    "Sol caribeño pegando rico. Lástima que uno sea pobre y tenga que bretear en vez de estar surfeando.",
                    "Clima tropical sabrosón, apenas para andar en chancletas sin que nadie le joda la existencia.",
                    "Calorcito de trópico rico, para mandar a la mierda la rutina y pegarse una escapada.",
                    "Mae, con este clima tropical el cuerpo lo único que pide es hamaca, pipa fría y cero estrés."
                )
                WeatherCategory.NIGHT -> listOf(
                    "Mae, ya es de noche. Deje de revisar el clima como un enfermo y váyase a dormir, carepicha.",
                    "Noche tranquila, mae. Buena para mandarse un café con pan y jatear como la gente decente.",
                    "Cielo despejado de noche. Si ve una estrella fugaz pida que no hayan choques mañana en la pista, malparido.",
                    "Ya es tarde, mae. Ni el clima está despierto a estas horas. Descanse esa jupa y suelte el cel.",
                    "Diay mae, ¿a las tantas de la noche viendo el clima? Vaya duerma que mañana le toca madrugar y mamar presa.",
                    "La noche está fresca y oscura. Váyase a la cama antes de que le salgan los espantos, idiota.",
                    "Noche para aplatanarse con la cobija y no saber de ni picha hasta mañana. Apague la luz ya.",
                    "Deje de espamear la app de noche, no sea intenso. El clima de mañana se revisa mañana, pedazo de animal.",
                    "Coman picha, ya es de noche, déjenme en paz.",
                    "Ey usted, feto con patas, deje el celular ya y duérmase de una vez.",
                    "Diay desvelado de mierda, ¿no tiene brete mañana o qué? Apague la pantalla y jatee.",
                    "A estas horas hasta el sereno se fue a dormir. Suelte el cel y duerma, carebarro."
                )
            }
        }
        val specialInsults = if (specialLocation != null && !isForeign) {
            SpecialLocationInsults.getSpecialLocationInsults(
                location = specialLocation,
                category = category,
                combo = null,
                isFamilyFriendly = isFamilyFriendlyMode,
                temperature = temperature,
                feelsLike = temperature,
                humidity = humidity,
                windSpeed = windSpeed,
                precipitationProbability = 50
            )
        } else emptyList()

        val baseComments = if (specialInsults.isNotEmpty()) specialInsults + comments else comments
        val seasonalDynamic = getSeasonalInsultsFor(category, isFamilyFriendlyMode)
        val finalComments = if (seasonalDynamic.isNotEmpty()) {
            baseComments + seasonalDynamic
        } else {
            baseComments
        }
        val eligibleComments = if (isForeign) {
            // Filter out comments with local Costa Rican places when abroad so they don't clash with foreign location
            finalComments.filter { comment ->
                !comment.contains("Costa Rica", ignoreCase = true) &&
                !comment.contains("Chepe", ignoreCase = true) &&
                !comment.contains("Cartago", ignoreCase = true) &&
                !comment.contains("Puntarenas", ignoreCase = true) &&
                !comment.contains("Zurquí", ignoreCase = true) &&
                !comment.contains("canal 7", ignoreCase = true) &&
                !comment.contains("General Cañas", ignoreCase = true) &&
                !comment.contains("Sabana", ignoreCase = true) &&
                !comment.contains("Puerto", ignoreCase = true)
            }.ifEmpty { finalComments }
        } else {
            finalComments
        }
        val filtered = if (excludeComment != null && eligibleComments.size > 1) {
            eligibleComments.filter { it != excludeComment }
        } else {
            eligibleComments
        }
        return foreignPrefix + com.example.util.SmartInsultSelector.getUnique(filtered, randomSeed)
    }

    fun getTicoConditionTitle(weatherCode: Int, isDay: Boolean, isFamilyFriendlyMode: Boolean = true): String {
        return if (isFamilyFriendlyMode) {
            when (weatherCode) {
                0 -> if (isDay) "Día soleado" else "Noche despejada"
                1, 2 -> if (isDay) "Día con nubosidad parcial" else "Noche parcialmente nublada"
                3 -> "Día nublado"
                45, 48 -> "Neblina fuerte"
                51, 53, 55 -> "Llovizna suave"
                56, 57 -> "Llovizna helada"
                61, 63 -> "Lluvia moderada"
                65 -> "Lluvia fuerte"
                66, 67 -> "Lluvia helada"
                71, 73, 75, 77 -> "Nieve"
                80, 81 -> "Chubascos rápidos"
                82 -> "Lluvia torrencial"
                85, 86 -> "Chubascos fríos"
                95 -> "Tormenta eléctrica"
                96, 99 -> "Tormenta con granizo"
                else -> "Clima variable"
            }
        } else {
            when (weatherCode) {
                0 -> if (isDay) "Solazo que pela" else "Noche despejada, pura vida"
                1, 2 -> if (isDay) "Sol con nubecitas" else "Noche medio nublada"
                3 -> "Nublado agüevado"
                45, 48 -> "Neblina nivel Cerro de la Muerte"
                51, 53, 55 -> "Garúa de verano"
                56, 57 -> "Garúa helada"
                61, 63 -> "Lluvia tica de rigor"
                65 -> "Baldazo concho"
                66, 67 -> "Aguacero helado"
                71, 73, 75, 77 -> "Nieve (¿nieve en Chepe? ¡Diay mae!)"
                80, 81 -> "Chubasco traicionero"
                82 -> "Diluvio universal"
                85, 86 -> "Chubasco con frío"
                95 -> "Tormenta eléctrica rajada"
                96, 99 -> "Tormenta con granizo (¡corra!)"
                else -> "Clima indefinido, mae"
            }
        }
    }

    fun getStandardDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Cielo despejado"
            1 -> "Principalmente despejado"
            2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Niebla o neblina"
            51, 53, 55 -> "Llovizna leve"
            56, 57 -> "Llovizna engelante"
            61, 63 -> "Lluvia moderada"
            65 -> "Lluvia fuerte"
            66, 67 -> "Lluvia helada"
            71, 73, 75, 77 -> "Nevada"
            80, 81, 82 -> "Chubascos violentos"
            85, 86 -> "Chubascos de nieve"
            95 -> "Tormenta eléctrica"
            96, 99 -> "Tormenta eléctrica con granizo"
            else -> "Condiciones variables"
        }
    }
}
