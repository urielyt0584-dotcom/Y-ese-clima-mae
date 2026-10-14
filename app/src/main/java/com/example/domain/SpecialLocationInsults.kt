package com.example.domain

import com.example.data.model.WeatherCategory
import java.util.Locale

enum class SpecialLocation(val displayName: String) {
    GOLFITO("Golfito"),
    PASO_CANOAS("Paso Canoas"),
    CERRO_DE_LA_MUERTE("El Cerro de la Muerte"),
    CIUDAD_NEILY("Ciudad Neily"),
    RIO_CLARO("Río Claro")
}

object SpecialLocationInsults {

    /**
     * Detects if a location corresponds to one of our special Costa Rican locations
     * by GPS coordinates or name patterns.
     * Coordinate checking has high precision and runs prior to generic canton names
     * to prevent inland districts like Río Claro (Guaycará) from being confused with coastal Golfito town.
     */
    fun detectSpecialLocation(
        locationName: String?,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ): SpecialLocation? {
        val normName = locationName?.lowercase(Locale.ROOT)?.trim() ?: ""

        // 1. Explicit specific name matching (town/district level)
        if (normName.contains("cerro de la muerte") || normName.contains("cerro dela muerte") ||
            normName.contains("cerro muerte") || normName.contains("de la muerte")) {
            return SpecialLocation.CERRO_DE_LA_MUERTE
        }
        if (normName.contains("paso canoas") || normName.contains("canoas")) {
            return SpecialLocation.PASO_CANOAS
        }
        if (normName.contains("ciudad neily") || normName.contains("neily")) {
            return SpecialLocation.CIUDAD_NEILY
        }
        if (normName.contains("rio claro") || normName.contains("río claro") ||
            normName.contains("guaycara") || normName.contains("guaycará") ||
            normName.contains("cruce de rio claro") || normName.contains("cruce de río claro")) {
            return SpecialLocation.RIO_CLARO
        }

        // 2. High-precision GPS coordinate detection (prioritized over generic canton names)
        if (latitude.isFinite() && longitude.isFinite() && (latitude != 0.0 || longitude != 0.0)) {
            // El Cerro de la Muerte (Cordillera de Talamanca / Ruta 2 summit, ~3.000m to 3.491m)
            if (latitude in 9.46..9.68 && longitude in -83.86..-83.66) {
                return SpecialLocation.CERRO_DE_LA_MUERTE
            }

            // Río Claro (Guaycará crossing on Interamericana Sur, inland valley)
            // Longitude is east of -83.14 (inland), whereas Golfito port town is west on the coast (-83.23..-83.15).
            // This prevents Río Claro from EVER being confused with Golfito!
            if (latitude in 8.61..8.78 && longitude in -83.14..-82.98) {
                return SpecialLocation.RIO_CLARO
            }

            // Ciudad Neily (Corredores valley, east of Río Claro)
            if (latitude in 8.59..8.71 && longitude in -83.00..-82.89) {
                return SpecialLocation.CIUDAD_NEILY
            }

            // Paso Canoas (Border crossing Costa Rica / Panama)
            if (latitude in 8.47..8.59 && longitude in -82.89..-82.78) {
                return SpecialLocation.PASO_CANOAS
            }

            // Golfito town (Coastal town / Depósito Libre on Golfo Dulce bay, west of -83.15)
            if (latitude in 8.58..8.68 && longitude in -83.23..-83.15) {
                return SpecialLocation.GOLFITO
            }
        }

        // 3. Fallback generic name matching (only if not caught as Río Claro by coordinates or district)
        if (normName.contains("golfito")) {
            return SpecialLocation.GOLFITO
        }

        return null
    }

    /**
     * Backward-compatible overload for existing calls.
     */
    fun getSpecialLocationInsults(
        location: SpecialLocation,
        isFamilyFriendly: Boolean,
        temperature: Double,
        feelsLike: Double,
        windSpeed: Double,
        category: WeatherCategory
    ): List<String> {
        return getSpecialLocationInsults(
            location = location,
            category = category,
            combo = null,
            isFamilyFriendly = isFamilyFriendly,
            temperature = temperature,
            feelsLike = feelsLike,
            humidity = 80,
            windSpeed = windSpeed,
            precipitationProbability = 50
        )
    }

    /**
     * Returns dedicated, weather-aware insults tailored specifically to the given SpecialLocation.
     * Categorizes insults according to actual weather (Rain, Thunderstorm, Heat, Cold, Wind, Combos)
     * to guarantee that rain/thunderstorms never complain about melting asphalt, and vice versa.
     */
    fun getSpecialLocationInsults(
        location: SpecialLocation,
        category: WeatherCategory,
        combo: WeatherComboType? = null,
        isFamilyFriendly: Boolean = false,
        temperature: Double = 25.0,
        feelsLike: Double = 25.0,
        humidity: Int = 80,
        windSpeed: Double = 10.0,
        precipitationProbability: Int = 50
    ): List<String> {
        val tempInt = temperature.toInt()
        val feelsInt = feelsLike.toInt()
        val windInt = windSpeed.toInt()

        if (isFamilyFriendly) {
            return getFamilyFriendlySpecialInsults(location, category, combo, tempInt, feelsInt, windInt, precipitationProbability)
        }

        return getUnfilteredSpecialInsults(location, category, combo, tempInt, feelsInt, windInt, humidity, precipitationProbability)
    }

    private fun getUnfilteredSpecialInsults(
        location: SpecialLocation,
        category: WeatherCategory,
        combo: WeatherComboType?,
        tempInt: Int,
        feelsInt: Int,
        windInt: Int,
        humidity: Int,
        precipProb: Int
    ): List<String> {
        return when (location) {
            SpecialLocation.RIO_CLARO -> getRioClaroUnfiltered(category, combo, tempInt, feelsInt, windInt, humidity, precipProb)
            SpecialLocation.CIUDAD_NEILY -> getCiudadNeilyUnfiltered(category, combo, tempInt, feelsInt, windInt, humidity, precipProb)
            SpecialLocation.PASO_CANOAS -> getPasoCanoasUnfiltered(category, combo, tempInt, feelsInt, windInt, humidity, precipProb)
            SpecialLocation.CERRO_DE_LA_MUERTE -> getCerroDeLaMuerteUnfiltered(category, combo, tempInt, feelsInt, windInt, precipProb)
            SpecialLocation.GOLFITO -> getGolfitoUnfiltered(category, combo, tempInt, feelsInt, windInt, humidity, precipProb)
        }
    }

    // =========================================================================
    // 1. RÍO CLARO (Cruce Interamericana Sur, Guaycará)
    // =========================================================================
    private fun getRioClaroUnfiltered(
        category: WeatherCategory,
        combo: WeatherComboType?,
        tempInt: Int,
        feelsInt: Int,
        windInt: Int,
        humidity: Int,
        precipProb: Int
    ): List<String> {
        val isRainy = category in listOf(WeatherCategory.THUNDERSTORM, WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN) ||
                precipProb >= 60 ||
                combo in listOf(WeatherComboType.THUNDER_WIND, WeatherComboType.RAIN_WIND, WeatherComboType.TRIFECTA)

        // 1. Combo-specific insults for Río Claro
        if (combo != null) {
            when (combo) {
                WeatherComboType.THUNDER_WIND, WeatherComboType.TRIFECTA -> return listOf(
                    "¡MAE, EN EL CRUCE DE RÍO CLARO CON TORMENTA ELÉCTRICA Y VENTOLERO! Los trailers patinan en la pista mojada y el techo de la soda tiembla como gelatina. ¡NO CRUCE ESA PISTA, CAREPICHA!",
                    "Diay mae, rayería y ráfagas de ${windInt} km/h en Río Claro... si se le cae una rama de árbol encima del carro en plena Interamericana, ahí quedó su viaje al Sur, malparido.",
                    "Combo del despiche en Río Claro: relámpagos cegadores y viento que casi le arranca la puerta al bajarse del carro. Métase bajo techo y no juegue de vivo.",
                    "¡ZAPATAZO ELÉCTRICO EN RÍO CLARO! Rayería sobre el tendido de alta tensión y ráfagas de ${windInt} km/h moviendo los contenedores del cruce. ¡Ni se baje de la nave, pedazo de animal!",
                    "Mae, rayería y ventolero en Río Claro: el cruce parece zona de guerra y los furgones van con intermitentes a 10 km/h. Si se le apaga el motor ahí, rece para que no lo empuje un trailer.",
                    "Diay carepicha, relámpagos rajados sobre las lomas de Guaycará y viento levantando latas de zinc en Río Claro... ¡métase a la soda del cruce y cómase un vigorón mientras reza!"
                )
                WeatherComboType.RAIN_WIND -> return listOf(
                    "Baldazo con viento en Río Claro: el agua pega en diagonal contra el parabrisas y los furgoneros no ven ni una picha. Maneje con los cuatro ojos abiertos, bestia.",
                    "Mae, ventolero y lluvia en el cruce de Río Claro... la sombrilla no le duró ni tres metros antes de doblarse como taco viejo. Quedó como sopa en media calle, carebarro.",
                    "Lluvia con ráfagas en Río Claro: las canaletas de la Interamericana colapsaron y el agua corre como río bravo. Cuidado con el hidroplaneo, idiota.",
                    "Temporal cruzado con ventolero en Río Claro: los limpiaparabrisas parecen aspas de helicóptero fuera de control y los trailers tiran olas de barro que tapan el vidrio. Vaya al suave, bestia.",
                    "Baldazo con ráfagas en el puro cruce de Río Claro: el viento mete el agua hasta adentro de los negocios y la calle parece una piscina de lodo. ¡No adelante en curva, malparido!",
                    "Mae, ventolero y aguacero en Río Claro: la sombrilla se le desarmó en tres segundos y quedó empapado hasta la médula. Ni el chofer de Tracopa se atreve a correr así."
                )
                WeatherComboType.RAIN_AND_SUN -> return listOf(
                    "Mae, en Río Claro se está casando la venada: cae un baldazo concho pero el solazo le quema el coco a la vez. El asfalto echa vapor caliente como olla arrocera, qué clima más loco.",
                    "Lluvia con sol brillante en el cruce de Río Claro: un vaporón sofocante que le empaña las gafas al bajarse del carro. Ni se moje ni se asolee, idiota."
                )
                WeatherComboType.HEAT_HUMIDITY -> if (!isRainy) return listOf(
                    "MAE, Río Claro con ${tempInt}°C Y ${humidity}% de humedad: esto no es clima, es una olla de presión con olor a asfalto caliente y diesel. Se le pegaron los calzones al alma, carepicha.",
                    "Sancocho asesino en el cruce de Río Claro: ${feelsInt}°C de sensación térmica y bochorno del demonio. El aire quema y los sobacos parecen cataratas del Térraba.",
                    "Diay carebarro, ¿paró en Río Claro con este vaporón? Ni tomándose tres botellas de agua fría se le quita esta sensación de estar en un horno de leña.",
                    "Sensación de ${feelsInt} grados en Río Claro: los trailers echan humo caliente y respirar en la parada de bus es como tragar vapor hirviendo.",
                    "Mae, Río Claro al mediodía con ${feelsInt}°C de sensación térmica: respirar aquí es como pegarse la jupa al tubo de escape de un camión en subida. Hidrátese o se nos muere en el cruce, pedazo de bestia.",
                    "Diay bestia, parado en Río Claro con este bochorno... se le pegó la camiseta a las costillas y los sobacos parecen fuentes de La Hispanidad. Tómese un refresco bien frío ya."
                )
                else -> {}
            }
        }

        // 2. Category-specific insults for Río Claro
        // CRITICAL: If it is raining or stormy, NEVER return melting asphalt or extreme heat roasts!
        if (isRainy) {
            return listOf(
                "Mae, ¿vio ese rayo en el cruce de Río Claro? El cielo tronó tan duro que casi despicha el puente. Métase a techarse antes de que lo electrocuten, carepicha.",
                "Qué soberano baldazo en Río Claro, mae. El agua ya rebalsó los caños de la Interamericana y parece que el río se salió a la calle. Ni se le ocurra acelerar, idiota.",
                "Diay carepicha, ¿en Río Claro con esta tempestad? Los trailers de carga pesada van tirando olas de barro que tapan el parabrisas. Qué peligro de carretera.",
                "Mae, en Río Claro cuando llueve con rayería se cae el cielo de verdad. La soda del cruce está llena de choferes esperando que no se inunde el parqueo.",
                "Lluvia torrencial en Río Claro: el agua cae con furia sobre el zinc y no se oye ni una mierda. Quédese quieto comiendo pollo frito y no invente jaladas.",
                "Mae, relámpagos rajados sobre las montañas de Río Claro. Si anda en moto con este aguacero, es un auténtico donante de órganos con ruedas.",
                "Qué aguacero más concho en Río Claro. La calle parece río y los limpiavidrios andan en nivel supersónico sin dar abasto, malparido.",
                "Mae, temporal en el cruce de Río Claro: el asfalto es una pista de patinaje con diesel lavado. Vaya al suave si no quiere terminar metido en la cuneta.",
                "¡SANTA BÁRBARA BENDITA! Tremendo rayo que cayó cerca del cruce de Río Claro, hasta la radio del carro hizo estática. ¡Métase bajo techo antes de que lo fulminen, carepicha!",
                "Mae, ¿vio cómo baja el agua por la cuesta de Río Claro? El río Guaycará ruge como fiera y la Interamericana es puro peligro acuático. No juegue de torear trailers bajo la lluvia, bestia.",
                "Qué clase de baldazo sobre Río Claro, mae. Los caños de la carretera están vomitando espuma y ramas caídas. Si frena de golpe va a hacer un trompo de feria, idiota.",
                "Temporal cerrado en Río Claro: el cielo está negro como sobaco de grillo y el aguacero no afloja ni un milímetro. Si anda en moto, métase a una gasolinera ya o termina en el hospital.",
                "Diay malparido, ¿en Río Claro con este diluvio? La lluvia golpea el parabrisas con tanta furia que no se ven ni las rayas amarillas de la pista. Ponga las cuatro luces y no invente.",
                "Mae, rayería brava sobre las montañas de Río Claro. Los truenos retumban en el pecho que da miedo. Ni se le ocurra pararse debajo de un árbol en la orilla de la calle, animal.",
                "Aguacero concho en Río Claro: los trailers van tirando cortinas de agua que ciegan por completo al que viene atrás. Guarde distancia de 50 metros si aprecia su vida, carebarro.",
                "Mae, diluvio universal en el cruce de Río Claro hacia Golfito: el agua le llega a los aros del carro en las partes bajas. Si se le mete agua a la mufla, ahí quedó botado en Guaycará."
            )
        }

        return when (category) {
            WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.HIGH_UV, WeatherCategory.TROPICAL -> listOf(
                "Mae, ¿paró en el cruce de Río Claro con este calorón? Aproveche para mandarse un pollo frito con refresco gigante porque con este sancocho no va a llegar vivo a ningún lado.",
                "Diay carepicha, Río Claro con ${tempInt}°C y el sol rajando la jupa. El asfalto de la Interamericana está que derrite las llantas de los trailers.",
                "Río Claro a mediodía: ${tempInt} grados de puro bochorno donde hasta los perros callejeros buscan sombra detrás de las llantas de los camiones, idiota.",
                "Mae, el bochorno de Río Claro es criminal. El aire caliente de la carretera se le mete por la nariz como si estuviera respirando humo de mofle.",
                "Diay playo, parado en Río Claro sudando la gota gorda. Cómprese un jugo frío con hielo antes de que le dé una pálida en medio cruce, carebarro.",
                "En Río Claro el sol pega sin compasión. Si se baja del carro sin lentes y gorra va a terminar con la cara roja como camarón del puerto.",
                "Mae, el sol en el cruce de Río Claro no alumbra, CASTIGA. El aire acondicionado del carro va tirando el alma y el asiento quema peor que plancha de panadería.",
                "Río Claro a ${tempInt}°C: el bochorno de la carretera le quita la energía a cualquiera. Cómprese dos botellas de agua con hielo en la soda si no quiere terminar desmayado en la acera, carepicha.",
                "Diay bestia, Río Claro parece una freidora de pollo gigante con este solazo. No toque el volante con las manos peladas si dejó el carro al sol porque se queda sin huellas dactilares."
            )
            WeatherCategory.STRONG_WIND -> listOf(
                "Ventolero caliente levantando polvo en el cruce de Río Claro: los ojos se le llenan de tierra y el viento mueve los furgones vacíos en la pista.",
                "Mae, ráfagas de ${windInt} km/h en Río Claro: agárrese la gorra porque si se le vuela a la Interamericana, un trailer de 18 ruedas se la hace confeti.",
                "Ventolero caliente levantando arenilla en el cruce de Río Claro: los ojos se le llenan de polvo de trailer y la gorra le sale disparada hacia la cuneta."
            )
            else -> listOf(
                "Mae, cruce de Río Claro tranquilo hoy, pero no se confíe que en la Zona Sur el cielo se encabrona en cuestión de 15 minutos.",
                "Día nublado en Río Claro: no llueve pero junta un bochorno pegajoso que lo aplatana en el asiento del bus, carepicha.",
                "Noche en Río Claro: furgones parqueados, grillos cantando y un calor residual del asfalto que no deja dormir en paz si no tiene abanico.",
                "Nublado denso sobre Río Claro: no llueve todavía pero el bochorno se junta como olla a presión esperando reventar sobre el valle de Guaycará.",
                "Noche espesa en Río Claro: grillos, ranas y el rugido interminable de los trailers cruzando la Interamericana bajo un calor que no suelta."
            )
        }
    }

    // =========================================================================
    // 2. CIUDAD NEILY (Valle de Corredores)
    // =========================================================================
    private fun getCiudadNeilyUnfiltered(
        category: WeatherCategory,
        combo: WeatherComboType?,
        tempInt: Int,
        feelsInt: Int,
        windInt: Int,
        humidity: Int,
        precipProb: Int
    ): List<String> {
        val isRainy = category in listOf(WeatherCategory.THUNDERSTORM, WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN) ||
                precipProb >= 60 ||
                combo in listOf(WeatherComboType.THUNDER_WIND, WeatherComboType.RAIN_WIND, WeatherComboType.TRIFECTA)

        if (combo != null) {
            when (combo) {
                WeatherComboType.THUNDER_WIND, WeatherComboType.TRIFECTA -> return listOf(
                    "MAE, tormenta eléctrica Y ventolero en Ciudad Neily: los techos de zinc de las casas suenan como que se los va a llevar el diablo. ¡MÉTESE A LA CHOZA, CAREPICHA!",
                    "Diay mae, rayería brava y ráfagas en Neily: el tendido eléctrico está echando chispas y los caños se desbordaron. No sea animal y no ande en la calle.",
                    "Tempestad violenta en Ciudad Neily: truenos que cimbran las paredes y ráfagas arrancando ramas en el parque. Quédese encerrado, idiota.",
                    "¡HAY DESPICHE EN CIUDAD NEILY! Tormenta eléctrica furiosa Y ráfagas de ${windInt} km/h doblando las palmeras del parque. ¡MÉTESE A UN LOCAL ANTES DE QUE LE CAIGA UN TENSOR, CAREPICHA!",
                    "Mae, rayería y ventolero en pleno centro de Neily: los transformadores están tirando chispas verdes y el agua se mete de lado a las tiendas. Quédese quieto adentro, idiota.",
                    "Diay bestia, ráfagas sacudiendo los rótulos comerciales en Ciudad Neily mientras los rayos caen sobre el cerro Corredores. Ni a los taxistas se les ocurre salir con este despiche."
                )
                WeatherComboType.RAIN_WIND -> return listOf(
                    "Aguacero con viento en Ciudad Neily: el agua se le mete por debajo de las puertas y la sombrilla salió volando hacia la frontera, carebarro.",
                    "Mae, ventolero y lluvia torrencial en Neily: ni los zancudos aguantan esta tempestad. Todo el mundo encerrado esperando el milagro.",
                    "Lluvia con ráfagas en el centro de Neily: los locales comerciales bajaron las cortinas porque el agua entra de lado como manguera.",
                    "Aguacero con ventolero en Ciudad Neily: las alcantarillas de la avenida principal reventaron y la sombrilla se le dobló como flor marchita. Quedó chorreando lodo, carebarro.",
                    "Mae, lluvia con ráfagas cruzadas en Neily: el agua le pega en la cara como perdigones. No sea animal y busque refugio en un corredor antes de pescar una neumonía.",
                    "Temporal azotando el valle de Corredores en Neily: el viento menea los árboles como si fueran de trapo y la calle es un canal de barro. Maneje con los ojos bien abiertos."
                )
                WeatherComboType.RAIN_AND_SUN -> return listOf(
                    "Lluvia con solazo en Ciudad Neily: se casó la venada en Corredores y el vapor que sube de las aceras parece baño turco. Qué combinación más incómoda, mae."
                )
                WeatherComboType.HEAT_HUMIDITY -> if (!isRainy) return listOf(
                    "MAE, Ciudad Neily con ${tempInt}°C Y ${humidity}% de humedad: esto no es una ciudad, es una olla de presión con usted adentro como tamal, pedazo de bestia.",
                    "Sancocho salvaje en Neily: ${feelsInt}°C de sensación térmica. El aire no se mueve y el sudor se le escurre por la espalda como manteca derretida, carepicha.",
                    "Diay mae, ¿calorón y humedad al ${humidity}% en Ciudad Neily? Si no tiene aire acondicionado o abanico industrial, prepárese para la agonía.",
                    "Vaporón criminal en Neily: ${feelsInt}°C de sensación térmica pegajosa. Si camina tres cuadras parece que salió de una piscina con ropa y todo, carepicha.",
                    "Diay carebarro, Ciudad Neily está hirviendo a fuego lento. Los abanicos solo mueven aire tibio con olor a humedad y asfalto. Tómese un granizado gigante urgente."
                )
                else -> {}
            }
        }

        if (isRainy) {
            return listOf(
                "Mae, qué clase de tempestad en Ciudad Neily. El cielo se cayó de un solo pichazo y el agua ya le llega al tobillo a los que andan a pie, carepicha.",
                "¿Vio ese trueno sobre el cerro en Ciudad Neily? Hasta los vidrios de la pulpería temblaron. Deje de jugar de valiente bajo la lluvia, bestia.",
                "Diay carebarro, ¿andando en Ciudad Neily con este baldazo? Los techos de zinc suenan como ametralladora en guerra y las calles son puro barrial.",
                "Lluvia torrencial en Neily, mae. El río Corredores viene bajando crecido y el cielo sigue negro como noche. Quédese bajo techo si no quiere mamar.",
                "Mae, en Ciudad Neily cuando llueve no son gotas, son baldazos con ganas de ahogar a todo el cantón. Cierre bien las celosías, idiota.",
                "Qué rayería más hijueputa sobre Ciudad Neily. Se fue la luz en medio pueblo y los truenos retumban en el pecho. No toque nada de metal, malparido.",
                "Temporal concho en Neily: el agua no para y el bochorno sigue pegado. Mojado y sudado al mismo tiempo, qué delicia de clima tropical.",
                "Mae, aguacero bravo en Ciudad Neily. Los carros van a paso de tortuga y las alcantarillas están eructando barro por todo lado.",
                "¡QUÉ CLASE DE TRUENO EN CIUDAD NEILY! Retumbó en el cerro que da a la frontera y las alarmas de los carros se prendieron todas solas. ¡Bajo techo ya, carepicha!",
                "Mae, el río Corredores en Neily viene bajando color café con leche y con troncos flotando. Si vive cerca de la orilla, esté alerta y no se haga el valiente, bestia.",
                "Temporal concho sobre Neily: cae tanta agua que el parque central parece una laguna artificial. Ni con botas de hule se salva de la empapada, idiota.",
                "Diay malparido, ¿andando en el centro de Neily con esta tempestad? Los caños colapsaron y el agua pasa sobre las aceras con fuerza. No se meta en corrientes hondas.",
                "Mae, relámpagos constantes sobre las montañas de Ciudad Neily. El cielo se ilumina como si fuera de día cada cinco segundos. Quédese en la choza viendo tele, animal.",
                "Lluvia torrencial en Ciudad Neily: el agua golpea el zinc con furia de mil demonios y no se escucha ni lo que uno mismo habla. Qué barbaridad de aguacero tropical.",
                "Mae, baldazo salvaje en Neily: la calle hacia la frontera es pura poza de agua traicionera. Despacio con el hidroplaneo si no quiere terminar contra un poste, carebarro.",
                "Qué aguacero más necio en Ciudad Neily. Lleva horas lloviendo sin parar y el bochorno sigue intacto abajo de las nubes. Mojado, sudado y con cólera, mae."
            )
        }

        return when (category) {
            WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.HIGH_UV, WeatherCategory.TROPICAL -> listOf(
                "Mae, ¿qué hace en Ciudad Neily? Esta vara no es una ciudad, es un baño sauna con techos de zinc al sol. Qué clase de bochorno más carepicha.",
                "Diay mae, en Ciudad Neily uno se baña con agua fría y a los 30 segundos ya está sudando sopa otra vez. Qué calor más asqueroso y pegajoso.",
                "Ciudad Neily a ${tempInt}°C: el sol pela y el aire está tan espeso que parece que uno respira caldo de pollo hirviendo, carebarro.",
                "Mae, qué sancocho en Ciudad Neily. Los abanicos solo tiran aire caliente y salir a la acera es como asomarse a un horno de panadería.",
                "En Ciudad Neily el clima le dice 'coma mierda' con ${tempInt} grados y humedad salvaje. Tómese un granizado con leche condensada o va a desmayarse, bestia.",
                "Mae, el asfalto de Neily está que derrite las chanclas. Busque sombra urgente si no quiere terminar rostizado como pollo de bombillo.",
                "Mae, qué calor más hijueputa en Ciudad Neily. El sol cae vertical como plomo derretido sobre el valle de Corredores. Si no tiene abanico, prepárese para sufrir.",
                "Ciudad Neily a ${tempInt}°C: el asfalto despide un calor que le tuesta las pantorrillas al caminar. Métase a un lugar con aire acondicionado o va a colapsar, carepicha.",
                "Diay animal, el sol de Neily le está friendo las pocas neuronas que le quedan. Busque sombra en el parque y tómese algo con bastante hielo antes de la pálida."
            )
            WeatherCategory.STRONG_WIND -> listOf(
                "Ventolero levantando techos en Ciudad Neily: asegure las láminas de zinc o amanece viendo las estrellas, carebarro.",
                "Mae, ráfagas de ${windInt} km/h en el valle de Corredores. Ni los zopilotes se animan a volar con este ventolón.",
                "Ráfagas calientes bajando de los cerros hacia Ciudad Neily: el polvo de la calle se le mete hasta en los dientes, cierre bien las ventanas."
            )
            else -> listOf(
                "Día con nubes en Ciudad Neily: parece que va a llover pero no llueve, solo junta bochorno para joder más tarde, carepicha.",
                "Sol radiante en el valle de Corredores en Neily, aproveche para hacer mandados temprano antes de que el vaporón de la tarde lo aplaste.",
                "Noche en Neily: calor pegajoso y mosquitos haciendo fiesta. Si no tiene repelente y abanico, que San Pedro se apiade de su alma.",
                "Cielo plomizo sobre Ciudad Neily: la humedad está en el 90% y la atmósfera está tan cargada que en cualquier segundo se viene el leñazo de agua.",
                "Noche pegajosa en Ciudad Neily: ni una sola hoja se mueve y el calor retenido en las paredes de concreto no deja dormir. Ponga el abanico en velocidad tres, mae."
            )
        }
    }

    // =========================================================================
    // 3. PASO CANOAS (Frontera Costa Rica - Panamá)
    // =========================================================================
    private fun getPasoCanoasUnfiltered(
        category: WeatherCategory,
        combo: WeatherComboType?,
        tempInt: Int,
        feelsInt: Int,
        windInt: Int,
        humidity: Int,
        precipProb: Int
    ): List<String> {
        val isRainy = category in listOf(WeatherCategory.THUNDERSTORM, WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN) ||
                precipProb >= 60 ||
                combo in listOf(WeatherComboType.THUNDER_WIND, WeatherComboType.RAIN_WIND, WeatherComboType.TRIFECTA)

        if (combo != null) {
            when (combo) {
                WeatherComboType.THUNDER_WIND, WeatherComboType.TRIFECTA -> return listOf(
                    "MAE, tormenta eléctrica Y ventolero en plena frontera de Paso Canoas: los toldos de los comercios van volando hacia Panamá. ¡MÉTESE A UN LOCAL, CAREPICHA!",
                    "Rayería endemoniada y ráfagas en la aduana de Canoas: los furgoneros no se bajan ni a mear. Quédese adentro antes de que le caiga una lata de zinc, idiota.",
                    "Despiche fronterizo nivel dios: relámpagos rajados sobre la aduana y viento que tumba rótulos de tiendas. Nadie cruza hoy, malparido.",
                    "¡DESPICHE TOTAL EN LA FRONTERA DE PASO CANOAS! Rayería rajada sobre la aduana Y ráfagas de ${windInt} km/h botando toldos de vendedores. ¡MÉTESE A UN ALMACÉN, CAREPICHA!",
                    "Mae, tormenta eléctrica y ventolero en Paso Canoas: los furgones parqueados en la línea fronteriza se menean con el viento y los rayos caen al lado de la pista. ¡Nadie cruza hoy, bestia!",
                    "Diay carepicha, relámpagos sobre territorio panameño y ventolón azotando los comercios ticos en Canoas. Si se le vuelan los dólares con el viento, no los recupera jamás, idiota."
                )
                WeatherComboType.RAIN_WIND -> return listOf(
                    "Aguacero con viento en Paso Canoas: se le mojaron las cajas de tenis piratas y el televisor que venía contrabandeando. Qué soberano despiche fronterizo.",
                    "Mae, lluvia con ráfagas en la línea fronteriza: las sombrillas de los vendedores se quebraron todas y el agua corre como río entre los puestos, malparido.",
                    "Ventolero y baldazo en la frontera: el agua le moja las facturas de la aduana y los furgoneros parqueados bloquean todo el paso.",
                    "Aguacero con ventolero en la pura frontera de Canoas: el agua entra cruzada a los pasillos comerciales y las cajas de compras van todas deshechas por la empapada. Qué despiche.",
                    "Mae, ráfagas y baldazo en la línea fronteriza: las aceras entre Costa Rica y Panamá son puro barrial color café. Corra bajo techo antes de perder hasta las chanclas, carebarro.",
                    "Temporal fronterizo con viento en Paso Canoas: la fila de furgones está varada y el agua corre como río entre las llantas de los trailers. No se arriesgue a cruzar a pie, malparido."
                )
                WeatherComboType.RAIN_AND_SUN -> return listOf(
                    "Solazo y lluvia simultánea en la línea fronteriza de Canoas: un vaporón que huele a asfalto húmedo y humo diesel. Ni comprando paraguas se salva de este bochorno, mae."
                )
                WeatherComboType.HEAT_HUMIDITY -> if (!isRainy) return listOf(
                    "MAE, Paso Canoas con ${tempInt}°C Y ${humidity}% de humedad: sudando en colones y en dólares a la vez. El bochorno aduanero es una tortura medieval, carepicha.",
                    "Sancocho infernal en la frontera: ${feelsInt}°C de sensación térmica entre furgones y asfalto hirviendo. Se le pegaron los calzones al alma, pedazo de bestia.",
                    "Diay carebarro, haciendo fila en la aduana con este bochorno de ${humidity}% de humedad... el sudor le chorrea por la frente directo a los ojos, qué asco.",
                    "MAE, Paso Canoas a ${tempInt}°C con ${humidity}% de humedad: sudando como testigo falso en dos países a la vez. El vapor de los furgones y el asfalto es una cámara de tortura, carepicha.",
                    "Sancocho aduanero en Paso Canoas: ${feelsInt}°C de sensación térmica. La ropa se le pega al cuerpo como engrudo y hacer fila al sol es un castigo bíblico, pedazo de bestia.",
                    "Diay carebarro, regateando precios en Canoas con este bochorno... tómese dos refrescos con hielo en el lado tico y otros dos en el panameño o le da un patatús en plena aduana."
                )
                else -> {}
            }
        }

        if (isRainy) {
            return listOf(
                "Mae, ¿en la frontera de Paso Canoas con este baldazo? Los caños colapsaron y el agua pasa de Costa Rica a Panamá sin pedir pasaporte, carepicha.",
                "Qué clase de rayería sobre la aduana de Paso Canoas. Un rayo cayó sobre un furgón y sonó como bomba, métase a un techo si no quiere morir chamuscado.",
                "Diay malparido, ¿comprando en Paso Canoas bajo la lluvia? Las bolsas de plástico se rompieron y toda la mercadería va empapada como sopa.",
                "Temporal fronterizo bravo en Canoas: las aceras desaparecieron bajo un barrial color chocolate y el aguacero no da tregua. Qué despiche de frontera.",
                "Mae, llueve a cántaros en Paso Canoas. El paso de camiones está colapsado y los choferes mentándole la madre a la lluvia. Qué belleza de viaje.",
                "Rayería continua entre la frontera tica y panameña en Canoas. Ni a los chanceros ambulantes se les ve la cara con este diluvio, quédese quieto adentro.",
                "Mae, aguacero traicionero en Canoas: hace 20 minutos había un solazo que quemaba y ahora parece el diluvio universal lavando la frontera entera.",
                "Lluvia concha en Paso Canoas que solo sirve para enlodar los zapatos nuevos que acaba de comprar. Ahora anda mojado y con cólera, idiota.",
                "¡QUÉ ZAPATAZO DE RAYO SOBRE LA ADUANA EN PASO CANOAS! Sonó como dinamita en ambos lados de la frontera y se fue la luz en los comercios. ¡Bajo techo ya, carepicha!",
                "Mae, qué soberano baldazo en Paso Canoas. El agua no respeta soberanía nacional y se cruza de Panamá a Costa Rica en ríos de barro. Guarde la mercadería, idiota.",
                "Temporal bravo en la frontera: las bolsas plásticas de las compras se deshicieron con el agua y los tenis nuevos van flotando en la cuneta. Qué soberano despiche, bestia.",
                "Diay malparido, ¿haciendo compras en Canoas con este diluvio universal? Las aceras desaparecieron bajo 15 centímetros de agua sucia. Camine con cuidado o se va al caño.",
                "Mae, relámpagos continuos entre la frontera tica y panameña en Canoas. Ni los choferes de furgón se bajan de las cabinas con semejante rayería. Espere que pase la tormenta.",
                "Lluvia torrencial en Paso Canoas: el agua golpea los techos de zinc de los almacenes con furia ensordecedora. Aproveche para tomarse un café adentro y no invente torear charcos.",
                "Mae, aguacero concho lavando la línea fronteriza: los carros van a dos por hora y el paso aduanero está colapsado. Paciencia de santo si tiene que sellar pasaporte hoy, carebarro.",
                "Qué diluvio en Paso Canoas, mae. La calle principal parece un estero y los limpiavidrios de los trailers no dan abasto. Si va a manejar de vuelta a Chepe, espere que escampe."
            )
        }

        return when (category) {
            WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.HIGH_UV, WeatherCategory.TROPICAL -> listOf(
                "Mae, ¿qué putas hace en Paso Canoas? ¿Cruzando a Panamá con este solazo asesino? Se le van a derretir las suelas de las tenis en el asfalto fronterizo, carepicha.",
                "Paso Canoas a ${tempInt}°C: calor de mierda, humo negro de furgones y un bochorno pegajoso que hace que la ropa huela a sobaco en 5 minutos.",
                "Diay carebarro, regateando precios en Paso Canoas con el sol pegándole en la jupa... compre bloqueador y agua fría antes de que le dé una insolación.",
                "Mae, el sol de Paso Canoas parece que pega con lupa gigante. Ni los taxistas piratas aguantan el calor dentro de los carros sin aire acondicionado.",
                "En Paso Canoas o lo quema el solazo fronterizo o lo aplasta el bochorno de la aduana. Búsquese una soda con abanico y no sufra de gratis, bestia.",
                "Paso Canoas con ${tempInt} grados: el asfalto echa humo y el aire huele a llanta caliente. Una delicia para andar haciendo fila a pie, idiota.",
                "Paso Canoas a ${tempInt}°C: calor asqueroso entre trailers, motores encendidos y polvo. Si no se hidrata va a terminar deshidratado en la aduana como momia egipcia, idiota.",
                "Diay animal, el sol de la frontera no perdona. Cómprese una gorra y póngase bloqueador factor mil o mañana va a amanecer despellejado como lagarto."
            )
            WeatherCategory.STRONG_WIND -> listOf(
                "Ventolero fronterizo en Paso Canoas: los furgoneros andan con los ojos llorosos de tanta tierra que levanta la calle.",
                "Ráfagas de ${windInt} km/h en Canoas: si no sostiene las bolsas de ropa barata se van a terminar yendo volando para David Chiriquí.",
                "Ventolero levantando tierra y bolsas vacías en la calle del comercio fronterizo en Canoas: agárrese los lentes porque el polvo vuela con furia."
            )
            else -> listOf(
                "Cielo gris sobre la frontera en Paso Canoas, tiene toda la pinta de que en cualquier momento se viene un baldazo a joder el comercio.",
                "Despejadito en Paso Canoas, aproveche para cruzar y hacer las compras antes de que el solazo del mediodía lo cocine vivo.",
                "Noche en Paso Canoas: luces de trailers, calor de asfalto y la frontera que nunca duerme. Cuide la billetera y descanse, carepicha.",
                "Cielo encapotado y amenazante sobre la línea de Canoas: tiene toda la cara de que se va a soltar un leñazo de agua en cualquier minuto, apresure las compras.",
                "Noche caliente en Paso Canoas: luces de aduana, furgones en fila interminable y un bochorno pegajoso que se niega a bajar del termómetro, mae."
            )
        }
    }

    // =========================================================================
    // 4. EL CERRO DE LA MUERTE (Ruta 2, Cordillera de Talamanca, páramo)
    // =========================================================================
    private fun getCerroDeLaMuerteUnfiltered(
        category: WeatherCategory,
        combo: WeatherComboType?,
        tempInt: Int,
        feelsInt: Int,
        windInt: Int,
        precipProb: Int = 0
    ): List<String> {
        if (combo != null) {
            when (combo) {
                WeatherComboType.TRIFECTA, WeatherComboType.COLD_WIND, WeatherComboType.COLD_RAIN -> return listOf(
                    "MAE, LA TRIFECTA MORTAL EN EL CERRO DE LA MUERTE: Frío de ${tempInt}°C, ventolero polar de ${windInt} km/h Y lluvia congelada. El precipicio al lado y cero visibilidad. ¡MANEJE A 20 KM/H Y RECE, CAREPICHA!",
                    "Diay suicida, viento que sacude el carro como papel y frío de congelador en el Cerro de la Muerte. Si se baja de la nave se le congela el cerebro en tres segundos, pedazo de bestia.",
                    "Mae, ¿viento polar Y llovizna helada en la cumbre del Cerro de la Muerte? Las curvas de la Ruta 2 son pura trampa con este temporal. Ni a putas corra, idiota.",
                    "Combo congelador en el Cerro de la Muerte: viento a ${windInt} km/h que le descuadra la puerta al abrirla y frío que parte los labios. Tápese con tres suéteres ya.",
                    "¡MAE, LA TRIFECTA POLAR EN EL CERRO DE LA MUERTE! Temperatura de ${tempInt}°C, viento ártico de ${windInt} km/h Y lluvia congelada. El guindo al lado y cero visibilidad. ¡MANEJE A 20 KM/H Y RECE, CAREPICHA!",
                    "Diay suicida con ruedas, ráfagas de ${windInt} km/h empujando la nave contra el abismo y frío de ${tempInt}°C que parte los huesos. Si se le empaña el parabrisas, ¡FALLECIMOS TODOS, BESTIA!",
                    "Mae, viento polar y llovizna helada en la cumbre del Cerro de la Muerte: las hojas de los robles están congeladas y el asfalto es pura manteca con musgo mojado. Baje marchando despacito, idiota.",
                    "Combo del terror en las alturas del Cerro de la Muerte: visibilidad de 1 metro por neblina espesa, ventolón que sacude la carrocería y frío que cala el tuétano. ¡No adelante en curva, malparido!"
                )
                WeatherComboType.THUNDER_WIND -> return listOf(
                    "MAE, ¿tormenta eléctrica Y viento en la cima del Cerro de la Muerte a más de 3.000 metros de altura? Los rayos caen al lado de la carretera, ¡QUÉDESE DENTRO DEL CARRO, MALPARIDO!",
                    "Rayería en el páramo con ventolero furioso: los cerros se iluminan con truenos ensordecedores. Maneje despacito y con luces de emergencia, bestia.",
                    "¡RAYERÍA A 3.300 METROS DE ALTURA CON VENTOLERO EN EL CERRO DE LA MUERTE! Los rayos caen sobre las antenas del páramo y el trueno suena como fin del mundo. ¡QUÉDESE DENTRO DEL CARRO, ANIMAL!",
                    "Mae, tormenta eléctrica en la cumbre del Cerro de la Muerte: relámpagos iluminando los precipicios con un viento huracanado. Si se baja a orinar, se electrocuta o se va al guindo, idiota."
                )
                WeatherComboType.RAIN_WIND -> return listOf(
                    "Temporal con viento en el Cerro de la Muerte: la lluvia pega horizontal contra el parabrisas y la neblina borra las líneas de la calle. Cuidado con el guindo, bestia.",
                    "Ráfagas polares y baldazo helado en las cumbres del Cerro de la Muerte: el desempañador del carro no da abasto y las llantas patinan en las hojas mojadas.",
                    "Temporal helado con viento cruzado en el Cerro de la Muerte: la lluvia pega horizontal contra las ventanas y el limpiaparabrisas no da abasto. Agarre ese volante con las dos manos, carebarro.",
                    "Ráfagas polares y aguacero helado en la Ruta 2: los trailers van botando vapor por los frenos en la bajada y las curvas no perdonan errores. Vaya con calma si quiere llegar vivo a Pérez."
                )
                else -> {}
            }
        }

        return when (category) {
            WeatherCategory.COLD -> listOf(
                "MAE, ¿QUÉ HACE EN EL CERRO DE LA MUERTE? Ni se le ocurra bajarse en pantaloneta, pedazo de animal, que aquí la temperatura (${tempInt}°C) le congela hasta el alma.",
                "Diay carepicha, ${tempInt}°C en el Cerro de la Muerte: se le congelaron los pensamientos y las ganas de vivir. Tómese un aguadulce hirviendo con queso o va a morir tieso.",
                "Mae, el frío del Cerro de la Muerte cala hasta el tuétano. El aire de páramo le congela la nariz y no hay cobija que alcance, qué concho de clima.",
                "Qué friíto más hijueputa en la cumbre del Cerro (${tempInt}°C). Hasta los robles centenarios andan temblando de frío, no juegue de vikingo y póngase jacket gruesa.",
                "Mae, ando con los dientes castañeando en el Cerro de la Muerte. Si se le apaga la calefacción del carro, prepárese para amanecer como paleta helada, idiota.",
                "En el Cerro de la Muerte el frío no es jugando: ${tempInt} grados y viento que parece sacado de la Antártida. Abríguese bien, pedazo de bestia.",
                "El Cerro de la Muerte le congeló hasta las ideas: ni con calefacción al máximo se le calientan los dedos de las patas, carebarro.",
                "MAE, ¿QUÉ HACE EN EL CERRO DE LA MUERTE EN PANTALONETA? La temperatura está en ${tempInt}°C y el aire de páramo le congela hasta los pensamientos, pedazo de feto congelado.",
                "Diay carepicha, ${tempInt}°C en la cumbre del Cerro: se le pusieron los labios morados y los dedos tiesos como estacas. Métase a La Georgina a tomarse una aguadulce hirviendo o no la cuenta.",
                "Mae, el frío del Cerro de la Muerte cala hondo como puñalada de hielo. Si se le apaga la calefacción del carro, prepárese para viajar como pollo de carnicería congelado, bestia.",
                "Qué friíto más concho en el Cerro (${tempInt}°C). Hasta los quetzales andan con bufanda y tiritando en las ramas. Abríguese bien el pecho si no quiere una pulmonía fija, idiota.",
                "Mae, el termómetro marca ${tempInt} grados en el páramo y el viento corta la cara como navaja de afeitar. Ni con dos cobijas de tigre se calienta uno aquí arriba, carebarro.",
                "En el Cerro de la Muerte el frío no es jugando: baje las ventanas medio centímetro y se le congela el cerebro de un solo viaje. Calefacción al máximo o muerte lenta, malparido.",
                "El Cerro de la Muerte haciendo honor a su nombre: ${tempInt}°C de pura congelación cordillerana. Si se le vara la nave aquí de noche, empiece a rezar el rosario, animal."
            )
            WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN, WeatherCategory.THUNDERSTORM, WeatherCategory.CLOUDS -> listOf(
                "NEBLINA NIVEL PARED EN EL CERRO DE LA MUERTE: visibilidad de 2 metros, lluvia helada y curvas asesinas. Encienda las intermitentes y no juegue de rápido, carepicha.",
                "Diay mae, pasando el Cerro de la Muerte con este aguacero y rayería... parece que va manejando por el cielo en medio de una pesadilla. Vaya despacio o se va al guindo.",
                "Mae, en el Cerro de la Muerte el asfalto mojado y las hojas caídas son puro jabón. Frene con motor si no quiere terminar parqueado en el fondo del precipicio, idiota.",
                "Cielo cerrado y neblina espesa como leche condensada en la cumbre del Cerro. No se ve ni la trompa del carro, qué terror de carretera, malparido.",
                "Lluvia helada en el Cerro de la Muerte, mae: el agua cae tan fría que duele en la piel. Quédese dentro del carro con el desempañador prendido al máximo.",
                "Mae, tormenta en las alturas del Cerro de la Muerte: relámpagos iluminando los abismos y truenos retumbando en el páramo. Qué experiencia más salvaje y peligrosa.",
                "Pared blanca de neblina en el Cerro de la Muerte. Si adelanta en raya continua con este clima, que Dios le tenga listo el cajón, carebarro.",
                "¡PARED DE NEBLINA NIVEL TERROR EN EL CERRO DE LA MUERTE! No se ve ni el bómper del carro de adelante, visibilidad cero y curvas asesinas. ¡LUCES INTERMITENTES Y DESPACITO, CAREPICHA!",
                "Mae, lluvia helada y neblina espesa como masa de tamal en la Ruta 2. El asfalto con hojas mojadas es una trampa mortal de jabón. Si corre, termina en el fondo del precipicio, idiota.",
                "Temporal cerrado en las cumbres del Cerro: el agua cae tan fría que entumece las manos en el volante. Desempañador a todo lo que dé y no juegue de piloto de Fórmula 1, bestia.",
                "Diay malparido, ¿adelantando en raya continua con esta neblina en el Cerro de la Muerte? Que Dios le guarde un campo en el camposanto porque el abismo no perdona estupideces.",
                "Mae, diluvio de páramo en el Cerro: cascadas de agua sucia cayendo de los taludes sobre la carretera. Cuidado con derrumbes y piedras en la pista, maneje con cuatro ojos.",
                "Neblina tan espesa en el Cerro que parece que va manejando adentro de una nube de leche condensada. No se orille demasiado que el guindo está a medio metro de la llanta, carebarro.",
                "Mae, aguacero helado sobre la cordillera de Talamanca. Las curvas hacia Pérez Zeledón están que patinan con diesel lavado. Vaya en segunda o tercera y use freno de motor, animal."
            )
            WeatherCategory.STRONG_WIND -> listOf(
                "Ventolero polar a ${windInt} km/h en el Cerro de la Muerte: el viento le empuja el carro de lado en las curvas. Sostenga el volante con las dos manos, animal.",
                "Mae, las ráfagas en la cima del Cerro rugen como mil demonios. Ni se asome a la orilla del mirador si no quiere salir volando hacia Pérez Zeledón.",
                "Ventolero polar a ${windInt} km/h en el Cerro de la Muerte: el chiflón menea los furgones como si fueran de cartón. Agárrese duro del volante en los pasos expuestos."
            )
            WeatherCategory.CLEAR_SKIES, WeatherCategory.PERFECT -> listOf(
                "Cielo despejado en el Cerro de la Muerte, qué milagro más raro. Se ven los dos océanos si tiene suerte, pero no se confíe que el viento sigue helado, mae.",
                "Sol brillante en el Cerro de la Muerte: el paisaje está pichudo como cuadro, pero el sol de altura quema con frío. Póngase bloqueador y jacket a la vez, idiota.",
                "Cielo despejado en el Cerro de la Muerte, qué milagro más raro. El paisaje de la cordillera está majestuoso, pero no se engañe con el sol que el frío sigue partiendo labios, mae."
            )
            else -> listOf(
                "Noche congelada en el Cerro de la Muerte: la carretera es un túnel negro entre la neblina y las estrellas de páramo. Maneje con cuidado y no se duerma al volante.",
                "Noche congelada en el Cerro de la Muerte: un túnel negro entre la neblina helada y las estrellas del páramo. Que la Virgen de los Ángeles lo acompañe en esa bajada, carepicha."
            )
        }
    }

    // =========================================================================
    // 5. GOLFITO (Golfo Dulce / Depósito Libre)
    // =========================================================================
    private fun getGolfitoUnfiltered(
        category: WeatherCategory,
        combo: WeatherComboType?,
        tempInt: Int,
        feelsInt: Int,
        windInt: Int,
        humidity: Int,
        precipProb: Int
    ): List<String> {
        val isRainy = category in listOf(WeatherCategory.THUNDERSTORM, WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN) ||
                precipProb >= 60 ||
                combo in listOf(WeatherComboType.THUNDER_WIND, WeatherComboType.RAIN_WIND, WeatherComboType.TRIFECTA)

        if (combo != null) {
            when (combo) {
                WeatherComboType.THUNDER_WIND, WeatherComboType.TRIFECTA -> return listOf(
                    "MAE, tormenta tropical Y ventolero en el Golfo Dulce: las lanchas están cabeceando en el muelle y el agua se mete a los locales del Depósito. ¡MÉTESE A UN TECHO, CAREPICHA!",
                    "Diay mae, rayería brava sobre el Golfo con ráfagas botando ramas de palmera. Quédese quieto en el hotel antes de que lo parta un rayo en la bahía, idiota."
                )
                WeatherComboType.RAIN_WIND -> return listOf(
                    "Aguacero con ventolero marino en Golfito: la lluvia pega de medio lado desde el mar y las sombrillas no sirven para ni picha. Quedó empapado de una vez.",
                    "Temporal cerrado con viento en Golfito: el oleaje en la bahía se picó y el agua cae sin asco. No se suba a lanchas hoy, bestia."
                )
                WeatherComboType.HEAT_HUMIDITY -> if (!isRainy) return listOf(
                    "MAE, Golfito con ${tempInt}°C Y ${humidity}% de humedad marina: aquí no se suda agua, se suda aceite hirviendo. El aire acondicionado es cuestión de vida o muerte, carepicha.",
                    "Sancocho costero en Golfito: ${feelsInt}°C de sensación térmica pegajosa. Si camina dos cuadras va a necesitar un trasplante de pulmón del sofoco, pedazo de bestia.",
                    "Diay carebarro, ¿haciendo fila en el Depósito Libre con este bochorno? La camisa parece trapo de cocina mojado y el calor no da tregua."
                )
                else -> {}
            }
        }

        if (isRainy) {
            return listOf(
                "Mae, vino hasta el fondo del Golfo Dulce en Golfito para que le caiga una tempestad que parece que se va a tragar el muelle entero. Qué diluvio más rajado.",
                "¿Vio ese rayo cayendo en el mar frente a Golfito? El golfo entero se iluminó de verde y retumbó en las montañas. Ni se le ocurra subirse a una panga ahorita, idiota.",
                "Diay carepicha, ¿en el Depósito Libre de Golfito con este baldazo? Se le va a inundar la cajuela del carro y las cajas de la pantalla se van a despichar todas.",
                "Temporal concho en Golfito: llueve tan duro que la selva parece una cortina de agua infinita. Quédese en el muelle tomándose algo y espere a que aclare, bestia.",
                "Mae, las alcantarillas de Golfito están desbordadas con este aguacero. El agua corre hacia la bahía con furia, no sea tan animal de meterse en charcos hondos.",
                "Aguacero tropical en Golfito: de esos que botan palos y dejan a medio pueblo sin tele. Qué clase de despiche de lluvia marina."
            )
        }

        return when (category) {
            WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.HIGH_UV, WeatherCategory.TROPICAL -> listOf(
                "¿Qué hace en Golfito, mae? ¿Vino a comprarse una pantalla de 65 pulgadas o qué picha? Con este vaporón se le van a derretir hasta las ganas de vivir.",
                "Mae, el bochorno de Golfito no es de Dios. En Golfito no hay término medio: o se cocina vivo con el bochorno marino (${tempInt}°C) o se ahoga en un aguacero concho.",
                "Diay carepicha, en Golfito el clima lo cocina al vapor como tamal de feria. Váyase a meter al Golfo Dulce antes de que se desmaye de la insolación, bestia.",
                "Coman picha con el calor de Golfito, ni las lanchas aguantan este sancocho. Aire acondicionado en nivel polo norte o muerte asegurada.",
                "Mae, en Golfito el sol refleja en el agua del golfo y le quema la jupa por partida doble. Use bloqueador factor mil o va a parecer tomate frito."
            )
            else -> listOf(
                "Día despejado sobre las aguas mansas del Golfo Dulce en Golfito. Aproveche para dar una vuelta en lancha antes de que el vapor de la tarde arme un chubasco.",
                "Noche tranquila en la bahía de Golfito: brisa marina tibia, luces reflejadas en el agua y un descanso bien merecido después de tanta paridera."
            )
        }
    }

    // =========================================================================
    // FAMILY FRIENDLY MODE
    // =========================================================================
    private fun getFamilyFriendlySpecialInsults(
        location: SpecialLocation,
        category: WeatherCategory,
        combo: WeatherComboType?,
        tempInt: Int,
        feelsInt: Int,
        windInt: Int,
        precipProb: Int = 0
    ): List<String> {
        val isRainy = category in listOf(WeatherCategory.THUNDERSTORM, WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN) ||
                precipProb >= 60 ||
                combo in listOf(WeatherComboType.THUNDER_WIND, WeatherComboType.RAIN_WIND, WeatherComboType.TRIFECTA)

        return when (location) {
            SpecialLocation.RIO_CLARO -> when {
                isRainy -> listOf(
                    "Qué baldazo más parejo en Río Claro, maneje con mucha precaución por la Interamericana y espere a que pase el agua fuerte, pura vida.",
                    "Tormenta con rayería en la zona de Río Claro, mejor quedarse bajo techo en la soda y disfrutar de un cafecito mientras escampa.",
                    "Lluvia constante sobre el cruce de Río Claro, mantenga las luces del vehículo encendidas y viaje despacito, pura vida.",
                    "El cielo se abrió con ganas en Río Claro, una buena tarde para descansar y no correr en carretera.",
                    "¡Qué aguacero tan impresionante en el cruce de Río Claro! Conduzca con muchísima precaución y disfrute de una parada en la soda local mientras pasa la tormenta, pura vida.",
                    "Tormenta con rayería sobre la zona de Guaycará y Río Claro. Encienda las luces del vehículo, baje la velocidad y espere a que el tiempo mejore.",
                    "Lluvia fuerte sobre la Interamericana Sur en Río Claro. Mantenga una distancia prudente con los camiones pesados y viaje seguro, pura vida."
                )
                category in listOf(WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.TROPICAL) -> listOf(
                    "¡En el cruce de Río Claro! Parada obligatoria para estirar las piernas y tomarse un fresco bien frío, que el calor aquí es bravo, pura vida.",
                    "Río Claro con su característico calor tropical de la Zona Sur. ¡A hidratarse bien y buscar buena sombrita!",
                    "Día caluroso y húmedo por Río Claro, tómese un vasito de agua fría antes de continuar su viaje por la Interamericana.",
                    "Calorcito de la Zona Sur en Río Claro, ¡que disfrute mucho el paso por esta hermosa región!",
                    "Día con bastante calor en Río Claro, la encrucijada del Sur. Aproveche para hidratarse bien y tomar un rico fresco de frutas antes de seguir su ruta."
                )
                else -> listOf(
                    "¡Saludos por Río Claro, la encrucijada del Sur! Tómese un cafecito o un fresco y disfrute el viaje, pura vida.",
                    "Día despejado y agradable por el cruce de Río Claro, ¡un viaje seguro y bonito por la Zona Sur!",
                    "Cielo despejado en Río Claro, ideal para continuar el viaje hacia Golfito o la frontera. ¡Maneje con cuidado y que disfrute el hermoso paisaje del Sur!"
                )
            }

            SpecialLocation.CIUDAD_NEILY -> when {
                isRainy -> listOf(
                    "Qué aguacero más fuerte en Ciudad Neily, mejor esperar bajo techo con un buen café caliente mientras pasa la lluvia, pura vida.",
                    "Tormenta con rayería sobre Neily, resguárdese en casa y cuide a los perritos de los truenos, pura vida.",
                    "Chubascos tropicales intensos en Ciudad Neily, las calles se llenan de agua rápido, camine con cuidado.",
                    "Tarde lluviosa en el valle de Corredores, ideal para descansar al son de las gotas en el techo.",
                    "¡Qué aguacero tan abundante en Ciudad Neily! El valle de Corredores recibe el agua con fuerza, manténgase seguro bajo techo con un buen café caliente, pura vida.",
                    "Tormenta eléctrica en los alrededores de Ciudad Neily. Guarde a sus mascotas y evite salir mientras persistan los truenos sobre la cordillera.",
                    "Lluvia fuerte sobre las calles de Neily. Conduzca con precaución, use luces bajas y respete los pasos de agua, pura vida."
                )
                category in listOf(WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.TROPICAL) -> listOf(
                    "¡Saludos en Ciudad Neily! Por aquí el calor y la humedad no perdonan, busque buena sombra y un fresco bien frío, pura vida.",
                    "Qué calorcito en Neily, mae. Tómese un vasito de agua fría y descanse del viaje, pura vida.",
                    "En Ciudad Neily el clima es bien cálido y tropical. ¡Lleve ropita fresca y sombrilla para el sol!",
                    "Ambiente de verano intenso en Neily, ¡a mantenerse hidratado y disfrutar del calorcito!",
                    "Ambiente muy cálido y tropical en Ciudad Neily. Recuerde hidratarse constantemente y buscar sombrita fresca para descansar del sol del Sur."
                )
                else -> listOf(
                    "Saludos en Ciudad Neily, corazón de Corredores. ¡Un abrazo fraternal en la hermosa Zona Sur!",
                    "Tarde fresca y bonita en Ciudad Neily después del calor del día, ¡pura vida!",
                    "Saludos cordiales desde Ciudad Neily, corazón del cantón de Corredores. ¡Que disfrute de la calidez de su gente y de su hermoso entorno natural!"
                )
            }

            SpecialLocation.PASO_CANOAS -> when {
                isRainy -> listOf(
                    "Qué aguacero más fuerte por Paso Canoas, tómese un cafecito bajo techo mientras pasa el temporal fronterizo.",
                    "Rayería activa en la frontera, manténgase seguro dentro de los comercios y proteja bien sus compras, pura vida.",
                    "Lluvia intensa en Paso Canoas, camine con paraguas y cuidado con los charcos en la acera.",
                    "Temporal sobre la línea fronteriza, paciencia con el tránsito y disfrute de sus compras bajo techo.",
                    "¡Qué fuerte aguacero sobre la frontera de Paso Canoas! Resguárdese dentro de los comercios y proteja bien sus compras mientras escampa, pura vida.",
                    "Tormenta eléctrica en la zona fronteriza entre Costa Rica y Panamá. Mantenga la calma, permanezca bajo techo seguro y espere a que pase la rayería.",
                    "Lluvia intensa sobre las calles de Paso Canoas. Conduzca con extrema precaución por la zona de carga pesada y guarde buena distancia entre vehículos."
                )
                category in listOf(WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.TROPICAL) -> listOf(
                    "¡En la pura frontera en Paso Canoas! Mucho calor por aquí, tómese un refresco bien frío mientras hace las compras.",
                    "Saludos en Paso Canoas, el cruce entre dos tierras hermanas. ¡A hidratarse bien que el calor de la frontera es pesado!",
                    "Paso Canoas con clima tropical bravo: sol que pica y compras que rinden, ¡cuídese la piel con bloqueador!",
                    "Día soleado en la frontera sur, ¡que tenga mucho éxito en sus compras y paseos!",
                    "Día muy soleado y caluroso en Paso Canoas. Hidrátese constantemente con agua o frescos naturales mientras realiza sus compras en la frontera, pura vida."
                )
                else -> listOf(
                    "¡Saludos en Paso Canoas! Que disfrute mucho su recorrido por la frontera entre Costa Rica y Panamá, pura vida.",
                    "Ambiente tranquilo hoy en la frontera, ¡un gran día para compartir y pasear!",
                    "¡Un saludo fraternal en la frontera de Paso Canoas, donde se unen dos pueblos hermanos! Que tenga una visita muy provechosa y un viaje seguro."
                )
            }

            SpecialLocation.CERRO_DE_LA_MUERTE -> when {
                category == WeatherCategory.COLD -> listOf(
                    "Qué frío más sabroso en el Cerro de la Muerte (${tempInt}°C). ¡Momento perfecto para tomarse un café chorreado bien caliente con pancito de altura!",
                    "El Cerro de la Muerte haciendo honor a su mística: frío de páramo, aire purísimo y paisajes inolvidables. ¡Abríguese de pies a cabeza, pura vida!",
                    "Tápese bien en el Cerro de la Muerte, que a ${tempInt} grados el viento de altura cala hondo, pura vida.",
                    "Clima helado en las cumbres del Cerro, ideal para saborear una rica aguadulce con queso Palmito.",
                    "¡Qué frío más delicioso en el majestuoso Cerro de la Muerte (${tempInt}°C)! Momento inmejorable para detenerse a saborear una aguadulce bien caliente con queso Palmito, pura vida.",
                    "El Cerro de la Muerte con su impresionante mística de páramo y aire purísimo. Abríguese de pies a cabeza con buen gorro y bufanda, pura vida."
                )
                category in listOf(WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN, WeatherCategory.THUNDERSTORM, WeatherCategory.CLOUDS) -> listOf(
                    "¡Por las majestuosas alturas del Cerro de la Muerte! Mucho cuidado con la neblina y las curvas, encienda las luces y maneje con mucha precaución, pura vida.",
                    "Cuidado con la visibilidad reducida en el Cerro de la Muerte. Baje despacio y disfrute de los hermosos robledales de la cordillera.",
                    "Lluvia fría y neblina espesa en la cima del Cerro de la Muerte, mantenga buena distancia entre vehículos.",
                    "Temporal de altura en el Cerro, la prudencia al volante es la mejor compañera en esta ruta.",
                    "Mucha precaución con la densa neblina y la lluvia fría en la cumbre del Cerro de la Muerte. Encienda las luces del vehículo, baje la velocidad y conserve su carril.",
                    "Temporal de altura en la cordillera de Talamanca. Maneje despacio por la Ruta 2, disfrute del imponente paisaje de robles y viaje con calma y seguridad."
                )
                else -> listOf(
                    "Día despejado y despeñaderos hermosos en las cumbres del Cerro de la Muerte. ¡Una bendición de vista en nuestra Costa Rica!",
                    "Cielo limpio en las alturas del Cerro, qué belleza poder contemplar la cordillera en todo su esplendor, pura vida.",
                    "Día despejado en las alturas del Cerro de la Muerte. Una vista privilegiada de las montañas de nuestra querida Costa Rica. ¡Abríguese bien y disfrute el viaje!"
                )
            }

            SpecialLocation.GOLFITO -> when {
                isRainy -> listOf(
                    "Mucho cuidado si navega por el Golfo Dulce con lluvia o rayería, mejor esperar en la marina a que pase el tiempo, pura vida.",
                    "Aguacero tropical en Golfito, el agua alimenta la selva y refresca el pueblo, ¡a disfrutarlo bajo techo!",
                    "Temporal en la costa de Golfito, lleve capa o sombrilla si va a hacer compras al Depósito Libre."
                )
                category in listOf(WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_HUMIDITY, WeatherCategory.TROPICAL) -> listOf(
                    "¡Saludos en Golfito! Qué calorcito sabroso, ideal para tomarse un copo bien cargado en el muelle o meterse al mar.",
                    "Calor tropical en el Golfo Dulce, ¡a mantenerse hidratado y disfrutar de la bella brisa marina!",
                    "Día soleado y cálido en Golfito, ¡aproveche para pasear y conocer este paraíso de la Zona Sur!"
                )
                else -> listOf(
                    "¡Saludos en Golfito! Paisajes hermosos en el Golfo Dulce, un rincón mágico de Costa Rica, pura vida.",
                    "Día tranquilo en la bahía de Golfito, ¡que disfrute al máximo su estancia en el Sur!"
                )
            }
        }
    }
}
