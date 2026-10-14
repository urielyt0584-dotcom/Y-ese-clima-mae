package com.example.domain

import com.example.data.model.WeatherCategory
import java.util.Locale
import kotlin.random.Random

object WeatherForeignInsults {

    /**
     * Determines whether given coordinates are roughly within continental Costa Rica boundaries.
     * Costa Rica coordinates: lat [8.0, 11.3], lon [-86.0, -82.5].
     */
    fun isInsideCostaRica(latitude: Double, longitude: Double): Boolean {
        if (!latitude.isFinite() || !longitude.isFinite() || (latitude == 0.0 && longitude == 0.0)) {
            return true // default safe if coordinates are not provided
        }
        return latitude in 8.0..11.3 && longitude in -86.0..-82.5
    }

    /**
     * Checks if a location is considered outside Costa Rica based on country name or coordinates.
     */
    fun isForeignLocation(country: String?, latitude: Double = 0.0, longitude: Double = 0.0): Boolean {
        val clean = country?.trim() ?: ""
        val isCr = clean.equals("Costa Rica", ignoreCase = true) ||
                clean.equals("CR", ignoreCase = true) ||
                clean.equals("C.R.", ignoreCase = true) ||
                clean.equals("CostaRica", ignoreCase = true)

        if (latitude.isFinite() && longitude.isFinite() && (latitude != 0.0 || longitude != 0.0)) {
            if (!isInsideCostaRica(latitude, longitude)) {
                return true
            }
        }

        return clean.isNotBlank() && !isCr
    }

    /**
     * Returns a human-friendly string for the country name to be used inside text.
     */
    fun cleanForeignCountryName(country: String?): String {
        val raw = country?.trim() ?: ""
        if (raw.isBlank() || raw.equals("Extranjero", ignoreCase = true) || raw.equals("Fuera de Costa Rica", ignoreCase = true) || raw.equals("Fuera del país", ignoreCase = true)) {
            return "el extranjero"
        }
        return when (raw.lowercase(Locale.ROOT)) {
            "united states", "usa", "us", "eeuu", "estados unidos de américa" -> "Estados Unidos"
            "spain" -> "España"
            "germany", "deutschland" -> "Alemania"
            "france" -> "Francia"
            "italy", "italia" -> "Italia"
            "mexico", "méxico" -> "México"
            "canada", "canadá" -> "Canadá"
            "colombia" -> "Colombia"
            "panama", "panamá" -> "Panamá"
            "nicaragua" -> "Nicaragua"
            "argentina" -> "Argentina"
            "chile" -> "Chile"
            "united kingdom", "uk", "reino unido", "england", "inglaterra" -> "Reino Unido"
            else -> raw
        }
    }

    fun getForeignPrefix(country: String, isFamilyFriendlyMode: Boolean, rand: Random = Random.Default): String {
        val c = cleanForeignCountryName(country)
        return if (!isFamilyFriendlyMode) {
            listOf(
                "Mae, ¿qué picha hace en $c? ",
                "Mucho viaje a $c pero seguro anda mamando, carepicha. ",
                "Diay mae, huyendo a $c... ",
                "¿Se cree muy pichudo por andar en $c? ",
                "Pura vida mis huevos, allá en $c ni saben qué es eso. ",
                "Mae, allá en $c a nadie le importa si llueve o no, pero tome. ",
                "¿Se fue a $c para seguir quejándose del clima como un idiota? ",
                "Vea el playo este en $c creyendo que el clima le tiene lástima. ",
                "Mucho pasaporte sellado en $c pero la misma cara de caballo de siempre. ",
                "Diay mae, ¿tan mal le iba en Costa Rica que se largó a $c a comer mierda? ",
                "Salúdeme a la gente de $c, de parte de este malparido clima. ",
                "Se cree gringo o europeo por estar en $c, pero el acento polo no se le quita ni a putas. ",
                "¿$c? Allá tampoco lo aguantan por insoportable, carebarro. ",
                "Gasta en pasajes a $c para andar metido en el celular viendo el clima, pedazo de animal. ",
                "Pudo irse hasta $c, pero lo mongolo se lo llevó de equipaje de mano. ",
                "Mae, en $c el clima también se le caga encima, para que no extrañe la choza. ",
                "¿Paseando en $c? Ojalá le caiga un aguacero que le empape hasta el pasaporte. ",
                "Diay carepicha en $c, ¿ya se le olvidó cómo hablar como la gente o todavía dice mae? ",
                "Mucho $c y mucha vara, pero sigue siendo el mismo muerto de hambre de siempre. ",
                "Presumiendo que anda en $c y el clima allá está igual de hediondo que su actitud. ",
                "Pagando en moneda extranjera en $c para parir con el clima exactamente igual. ",
                "¿Tanto presumir $c en Instagram para terminar encerrado por el clima? No sea fantoche. ",
                "Allá en $c con ese clima miserable y en Costa Rica con cafecito chorreado. Tome pa' que lleve. ",
                "Diay polazo en $c, ¿creía que el clima del primer mundo venía con calefacción incluida en la calle? ",
                "Mae, en $c nadie le va a tener compasión por andar llorando por el clima. ",
                "Mae, ¿qué diablos anda inventando en $c? ",
                "Mucho pasaporte sellado en $c y se fue sin empacar ni media sombrilla, carepicha. ",
                "Diay mae, ¿tan cansado de la platina estaba que se fue a $c a tragar friazo? ",
                "Mae, en $c el metro estará muy limpio pero el clima le está metiendo un susto de muerte. ",
                "Salude a las autoridades de $c de parte de este malparido clima. ",
                "Paseando en $c con plata fiada de la tarjeta y el clima le arruinó el itinerario, carebarro. ",
                "Mae, en $c ni pagando propina obligatoria en dólares le arreglan este clima del demonio. ",
                "Mucho tour por $c y las tenis ya le huelen a cañería tapada de tanto charco, pedazo de animal. ",
                "Diay fantoche en $c, ¿creyó que en el extranjero el cielo no se emputaba o qué? Tome pa' que lleve. ",
                "Mae, en $c el café sabe a agua sucia de manguera y el clima le escupe en la jupa. ",
                "Andar de pata caliente por $c para terminar encerrado en el cuarto de hotel, qué papelón más concho. ",
                "Diay mae, viajó miles de kilómetros hasta $c solo para que el clima lo tratara como a una basura. ",
                "Mae, en $c nadie le va a traducir las quejas del clima, sufra en silencio como los grandes. ",
                "Tanto rajar en historias de Instagram que andaba en $c y vea la cara de velorio que se maneja. ",
                "Mae, pagó una fortuna en boletos para venir a $c a congelarse los mocos, qué genio financiero. "
            ).random(rand)
        } else {
            listOf(
                "¡Saludos especiales hasta $c! ",
                "¡Qué paseador por $c! ",
                "Pura vida desde Costa Rica hasta $c. ",
                "Disfrute mucho su viaje por $c, pura vida. ",
                "Esperamos que la esté pasando de lo más bonito en $c. ",
                "Un abrazo tico hasta $c, pura vida. ",
                "Desde la tierrita del pura vida le enviamos un saludo hasta $c. ",
                "Muchos éxitos en su estadía por $c. ",
                "¡Qué orgullo ver a un tico paseando por $c! ¡Pura vida! ",
                "¡Un saludo con sabor a café chorreado hasta $c! ",
                "¡Que disfrute cada rincón de $c, pura vida y bendiciones! ",
                "¡Un abrazo bien tico desde la tierrita del pinto hasta $c! ",
                "Paseando por $c y dejando en alto el pura vida, ¡que la pase de maravilla! ",
                "¡Muchos éxitos en su aventura por $c! Llévese un buen abrigo por si acaso. "
            ).random(rand)
        }
    }

    fun getForeignRepeatedChecks(country: String, isFamilyFriendlyMode: Boolean): List<String> {
        val c = cleanForeignCountryName(country)
        return if (!isFamilyFriendlyMode) {
            listOf(
                "¿Cuarta vez revisando el clima en $c? Mae, el tiempo allá no va a cambiar porque usted lo mire cada dos segundos, carepicha. Asómese a la ventana o jale a pasear.",
                "Diay mae, ¿espera que la pantalla le arregle el clima de $c o qué? ¡Deje de espamear el botón, pedazo de malparido!",
                "Mae, si sigue actualizando el clima en $c va a gastar los datos en roaming y la batería antes de salir, idiota.",
                "Cuarta vez que revisa... mae, en $c tampoco lo van a salvar de este clima por darle al botón como un mono con hambre.",
                "¿Qué espera ver en $c, careverga? ¿Que de la nada se vuelva playa Tamarindo? Suelte el cel y viva la vida.",
                "Diay fantoche en $c, ni el meteorólogo local actualiza tanto. Vaya cómprese un café y deje de joder la pantalla.",
                "Tanto pasaje caro a $c para pasarse el viaje tocando el botón de actualizar. Pedazo de mamapichas.",
                "Mae, el clima de $c está igual de despichado que hace 30 segundos. ¡Suelte el teléfono ya!",
                "Diay mae, ¿cree que en $c el botón de refrescar viene con control remoto del cielo? Deje la intensidad, carebarro.",
                "Actualizando el clima en $c cada diez segundos... mae, cómprese una vida en moneda local o disfrute el paseo, idiota.",
                "¿Otra vez tocando la pantalla en $c? Ni que fuera la app del banco revisando si le depositaron el aguinaldo. Suelte el cel.",
                "Mae, en $c el cielo no le va a pedir permiso a esta app para cambiar. Asome los ojos por la ventana y no joda más."
            )
        } else {
            listOf(
                "¿Otra vez revisando el clima de $c? Tranquilo, tómese un cafecito que el tiempo no cambia tan rápido.",
                "¡Mucho cuidado con la batería en $c! Si sigue actualizando el clima se le va a descargar el teléfono.",
                "Cuarta vez que revisa el clima en $c... asómese a la ventana a ver qué tal está afuera, pura vida.",
                "Paciencia por $c, que el clima cambiará a su propio ritmo. ¡Disfrute el paseo!",
                "¿Esperando un milagro en la pantalla en $c? Relájese y disfrute su viaje, pura vida.",
                "¡Cuidado con los datos en el extranjero en $c! Mejor guarde batería para tomar fotos bonitas.",
                "Tranquilo por $c, el clima no va a variar en dos minutos. ¡A disfrutar la vista!"
            )
        }
    }

    fun getForeignCategoryInsults(
        category: WeatherCategory,
        country: String,
        isFamilyFriendlyMode: Boolean,
        temperature: Double,
        feelsLike: Double,
        windSpeed: Double,
        humidity: Int = 80,
        uvIndex: Double = 5.0
    ): List<String> {
        val c = cleanForeignCountryName(country)
        val tempInt = temperature.toInt()
        val feelsInt = feelsLike.toInt()
        val windInt = windSpeed.toInt()

        return if (!isFamilyFriendlyMode) {
            when (category) {
                WeatherCategory.COLD -> listOf(
                    "¿Tanto brete para irse a $c a congelarse el culo, carepicha? Allá en Costa Rica tomando cafecito con pan y usted aquí tragando escarcha a ${tempInt}°C.",
                    "Diay playo, se cree muy pichudo por estar en $c y anda con los dedos tiesos y los mocos como estalactitas. Páguese la calefacción, muerto de hambre.",
                    "Mucho primer mundo en $c pero este frío hijueputa no se lo quita ni a putas. Allá en Chepe con 20 grados ya nos estábamos quejando y vea la paridera suya.",
                    "Mae, ¿${tempInt}°C en $c? Se le congeló hasta el apellido, carebarro. Camine con cuidado antes de que se desnuque en el hielo por polo.",
                    "Diay mae, ¿no era que Costa Rica era un pueblo y usted quería mundo? Ahí tiene su frío polar en $c, chupe hielo, imbécil.",
                    "Andar en $c con cinco abrigos puestos y todavía temblando como perro en moto. Qué papelón más concho, mae.",
                    "Mae, ese frío de $c no es de Dios. Se le van a caer las orejas, pedazo de bestia. Váyase a la choza a llorar por una cobija de tigre.",
                    "¿Extrañando el solcito tico, carepicha? Quédese allá en $c chupando frío polar por presumido.",
                    "Mae, pagando renta en dólares o euros en $c para vivir metido en un congelador. Me cago en diez, qué brillante idea.",
                    "Diay esquimal de cafetal, ¿ya aprendió a caminar en la nieve en $c o sigue resbalándose como un mongolo?",
                    "Tanto pasaporte sellado en $c para terminar con sabañones en las patas. Qué despiche de frío, mae.",
                    "Mae, en $c el viento corta como gillete a ${windInt} km/h. Tápese esa jupa antes de que se le congele la única neurona que le queda.",
                    "Allá en Costa Rica un fresquito de cas y una carnita asada, y usted en $c comiendo sopa instantánea para no morir de hipotermia.",
                    "Mae, ¿${tempInt} grados en $c? A esa temperatura ni los pingüinos salen a la calle. Deje de engañarse, la está pasando del asco.",
                    "Diay mae, si sale a la calle en $c con este frío se le van a quebrar los huesos como galleta soda. Póngase pantalones.",
                    "¿${feelsInt}°C de sensación térmica en $c? Ya parece un cubito de hielo con patas. Saludos desde Costa Rica donde no morimos congelados.",
                    "Mae, en $c hasta el agua del inodoro amanece con hielo. Quédese bajo las cobijas y rece por un rayito de sol tropical.",
                    "Tanto rajar que se iba para $c y ahora anda pidiendo cacao por una bolsa de agua caliente. Qué concho el frío.",
                    "Mae, en $c hace un frío que no perdona: ${tempInt}°C y las piernas le tiemblan como gelatina. Ya quisiera una cobijita de tigre y una taza de aguadulce, carepicha.",
                    "Diay mae, caminando en $c con hielo en la acera y las manos moradas. Si se resbala aquí le cobran el rescate en euros o dólares, idiota.",
                    "Tanto rajar de 'viajar al invierno' en $c y anda con los labios partidos y el moco tieso pidiendo cacao. Qué playada de friazo.",
                    "Mae, ¿${tempInt} grados en $c? A esa temperatura hasta el teléfono se le apaga de la cólera. Métase a un café con calefacción antes de que se vuelva estatua de hielo.",
                    "En Costa Rica con 18 grados ya sacábamos la chompipa, y aquí en $c está aguantando ${tempInt}°C como un polo congelado. Tome pa' que lleve.",
                    "Mae, el viento polar en $c le entra por el cuello como si le hubieran metido una bolsa de hielos por la espalda. Qué concho el clima.",
                    "Diay carebarro, ¿le gusta la nieve en $c? Espérese a que le empiece a entrar aguanieve congelada adentro de las tenis, a ver si sigue sonriendo.",
                    "Temblando de frío en $c y pensando en el gallo pinto caliente con natilla de la choza. Qué dolor más grande, carepicha.",
                    "Mae, ni poniéndose tres pares de medias se le calientan los pies en $c. Camine rápido a la choza o se le caen los dedos.",
                    "Sensación de ${feelsInt}°C en $c: esto es tortura en el extranjero pagada con tarjeta de crédito. Saludos cordiales."
                )
                WeatherCategory.EXTREME_HEAT -> listOf(
                    "Mae, ¿huyó de Costa Rica para venirse a achicharrar a $c? Este calor de mierda parece castigo del infierno, carepicha.",
                    "Diay malparido, ${tempInt}°C en $c y sin una playa tica cerca para meter las patas. Qué clase de estafa de viaje.",
                    "Mucho $c y mucha vara, pero aquí está sudando la gota gorda como testigo falso. Qué asco de bochorno.",
                    "Mae, el asfalto de $c le va a derretir hasta las tenis. Métase a un mall con aire acondicionado antes de que le dé un soponcio, idiota.",
                    "Presumiendo viaje a $c y anda sancochado como tamal de diciembre. Coma mierda con este bochorno.",
                    "Diay carepicha, ¿creía que solo en Puntarenas hacía calor? Tome su dosis de horno en $c para que no hable paja.",
                    "Mae, ni el aire acondicionado de $c da abasto con este calorón hijueputa. Se le va a derretir el cerebro.",
                    "Sudar en dólares o euros en $c es igual de hediondo que sudar en colones, pedazo de animal.",
                    "Mae, un paso afuera en $c y queda rostizado a ${tempInt}°C. Deje de inventar y quédese encerrado con el abanico al máximo.",
                    "Diay playo, ¿quería verano en el extranjero? Ahí tiene su sancocho en $c, tráguese ese vaporón asqueroso.",
                    "Mae, el sol en $c le está pegando como si le debiera plata. Cuidado le da insolación por andar de pata caliente.",
                    "¿${tempInt}°C en $c? Esto parece una freidora de aire del tamaño de una ciudad. Qué pesadilla de clima.",
                    "Hasta el sobaco le hierve en $c, carebarro. Allá no hay pipa fría que lo salve de semejante brasa.",
                    "Mae, ${tempInt}°C en $c y ni una sola soda con refresco de cas o chan con leche para salvarle la vida. Se le va a deshidratar el alma, carepicha.",
                    "Diay malparido, ¿el verano en $c era lo que quería? El asfalto de la avenida le quema la suela de las tenis y el aire parece aliento de dragón.",
                    "Mae, el bochorno de $c lo tiene sudando la gota gorda en medio de los turistas. Parece que se orinó encima de puro sudor, pedazo de bestia.",
                    "Un horno internacional a ${tempInt} grados en $c. Hasta el pasaporte se le pegó al bolsillo de la camisa con este sancocho.",
                    "Diay carebarro, ni con cinco botellas de agua tibia en $c se le quita la sed. El calor de aquí es seco y concho como patada de mula.",
                    "Mae, caminar en $c a mediodía con este solazo es buscarse una insolación de gratis en país ajeno. Métase al metro con aire ya.",
                    "Sancocho de primer mundo en $c: respirar es como meter la cara adentro de un horno de panadería. Qué pesadilla de viaje, idiota.",
                    "Mae, en $c el sol no perdona a los turistas. Si no se puso bloqueador va a parecer manzana envenenada en las fotos familiares.",
                    "Diay fantoche, sudando en moneda extranjera a ${tempInt}°C. Qué rico pagar pasaje para venir a hervir como yuca en sopa.",
                    "Mae, el aire acondicionado del bus en $c huele a sudor concentrado de 40 nacionalidades distintas gracias a estos ${tempInt} grados. Delicioso."
                )
                WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN -> listOf(
                    "Mae, se vino el diluvio en $c. ¿Para esto pagó boleto de avión, carepicha? Para mojarse como rata en país ajeno.",
                    "Diay mae, el cielo en $c se cayó a pedazos. Ni crea que por ser primer mundo las alcantarillas no se despichan, imbécil.",
                    "Mucho viaje internacional a $c pero anda con los zapatos empapados y cara de idiota bajo la lluvia.",
                    "Mae, el aguacero en $c le lavó hasta el orgullo. Métase a un alero antes de que se le joda el celular, pedazo de bestia.",
                    "Diay carebarro, pensó que se salvaba de los baldazos de Chepe y vea el despiche de agua que le cayó en $c.",
                    "Lluvia asquerosa en $c. Se le arruinó el itinerario y las fotos de Instagram se fueron a la picha.",
                    "Mae, esa lluvia en $c no es jugando. Compre sombrilla aunque le cobren un ojo de la cara en moneda extranjera.",
                    "Empapado de pies a cabeza en $c y sin ropa limpia en la maleta. Qué concho el clima con usted, playo.",
                    "Diay mae, ¿tan salado es que hasta en $c le persiguen las tormentas? Ni los santos lo salvan de este chubasco.",
                    "El agua cae en $c con ganas de ahogar a medio mundo. Váyase a la choza a ver tele porque hoy no hace ni picha.",
                    "Mae, lluvia con ráfagas en $c: si abre la sombrilla va a salir volando como Mary Poppins versión polo.",
                    "Lluvia traicionera en $c. Camine pegado a las paredes para que los carros no lo bañen de charco, animal.",
                    "Mae, el baldazo en $c le lavó hasta el itinerario de vacaciones. La sombrilla de 15 euros se le dobló al revés y anda mojado como rata, carepicha.",
                    "Diay mae, ¿creyó que solo en el Zurquí caían palos de agua? Este diluvio en $c colapsó las calles y las alcantarillas andan flotando basura fina.",
                    "Lluvia concha en $c: el agua le baja por la nuca y le llega hasta la rabadilla. Qué manera más miserable de turistear, idiota.",
                    "Mae, las tenis empapadas haciendo 'chish-chish' con cada paso por las calles de $c. Si le da hongos en las patas por mojado se lo merece, carebarro.",
                    "Diay carepicha, ¿diluvio universal en $c? Quédese en el hotel comiendo galletas porque afuera lo que hay es un río revuelto.",
                    "El cielo de $c se le vino encima sin avisar. Todas las fotos de los monumentos quedaron con un cielo negro deprimente y usted con cara de perro regañado.",
                    "Mae, lluvia traicionera en $c: un carro le pasó a la par a 80 km/h y le tiró una ola de agua sucia de pies a cabeza. Bienvenido al extranjero, animal.",
                    "Ni capa impermeable de astronauta aguanta el aguacero que está cayendo en $c. Guarde el pasaporte en bolsa Ziploc si no quiere quedarse varado.",
                    "Mae, pagó tour en bus descapotable en $c y se vino el aguacero del siglo. Se empapó de lo lindo por pagar gustos caros, imbécil.",
                    "Diluvio concho en $c: el agua cae con tanta furia que no se ven ni los semáforos. Póngase bajo techo y llore por la plata del boleto."
                )
                WeatherCategory.THUNDERSTORM -> listOf(
                    "MAE, tormenta eléctrica salvaje en $c: los rayos caen entre los edificios y el suelo tiembla con cada trueno. Métase a un sótano antes de que lo electrocuten, carepicha.",
                    "Diay carebarro, ¿rayería brava en $c? Desconecte los cargadores y no se pare debajo de árboles en los parques extranjeros, pedazo de bestia.",
                    "Cielo endemoniado con relámpagos en $c: hasta las sirenas de la policía andan sonando del despiche de la rayería. Qué susto más concho, mae.",
                    "Mae, rayería internacional: los truenos retumban como bombas y el cielo se pone blanco en plena noche. No juegue de valiente y quédese quieto.",
                    "¡BOMBAZO EN $c! Ese rayo sonó como si hubieran despedazado la torre de televisión. Vaya rece un Padre Nuestro antes de que se vaya la luz.",
                    "Diay malparido, ¿en $c con tormenta eléctrica y caminando con sombrilla de varilla metálica? Es un pararrayos ambulante con pasaporte tico.",
                    "Relámpagos rajados sobre $c: la tormenta no cree en fronteras ni en visas. Apague el celular si no quiere que le caiga una chispa.",
                    "Mae, qué tempestad más salvaje en $c. Ni en la peor época lluviosa de Costa Rica se ven truenos tan escandalosos. Tápese la jupa."
                )
                WeatherCategory.STRONG_WIND -> listOf(
                    "Mae, ráfagas de ${windInt} km/h en $c: el viento le arrancó la gorra, le desarmó la sombrilla y casi le vuela el boleto de avión de las manos, carepicha.",
                    "Chiflón asesino en las avenidas de $c: el viento corta como bisturí y levanta basura por todo lado. Camine agachado si no quiere salir volando como cometa.",
                    "Diay carebarro, ventolero bravo en $c: las puertas de las tiendas se azotan solas y la gente anda corriendo despavorida. Qué despiche de brisa.",
                    "Mae, el viento en $c le mueve hasta el abrigo y no lo deja caminar en línea recta. Parece que va borracho por la acera, pedazo de animal.",
                    "Ráfagas polares a ${windInt} km/h en $c: los ojos se le llenan de lágrimas y la jupa se le congela en tres segundos. Busque refugio ya.",
                    "Mae, este ventolero de $c suena como turbina de avión en despegue. Agárrese de un poste si no quiere terminar en la otra acera."
                )
                WeatherCategory.HIGH_UV -> listOf(
                    "Mae, radiación ultravioleta brava en $c: el sol de mediodía le está friendo el coco y las orejas. Póngase bloqueador si no quiere pelarse como culebra, carepicha.",
                    "Diay malparido, caminando en $c bajo semejante solazo sin gorra ni lentes. Va a terminar con los ojos rojos y la nariz como chile dulce.",
                    "Sol asesino en $c: la radiación penetra hasta la sombra de los edificios. Póngase protector solar factor 50 o va a arder toda la noche en el hotel.",
                    "Mae, el sol en $c quema como soplete. No se haga el bronceado de revista que lo que parece es camarón de muelle, bestia."
                )
                WeatherCategory.CLOUDS -> listOf(
                    "Mae, qué cielo más gris y deprimente en $c. Parece que el clima tiene depresión clínica, qué agüeve más hijueputa.",
                    "Diay mae, no se ve ni picha con este cielo nublado en $c. Allá en Costa Rica con solcito y usted aquí en una película de terror.",
                    "Mucho $c pero el cielo parece panza de burro. Qué día más aplatanado y hediondo, carepicha.",
                    "Mae, en $c ni el sol quiere asomarse a saludarlo a usted. Día asquerosamente agüevado.",
                    "Extrañando el verdor y la luz de Costa Rica, ¿verdad malparido? Ese gris de $c le marchita hasta el alma.",
                    "Cielo cerrado en $c: día ideal para aplatanarse en la cama y lamentar todas sus decisiones de vida, imbécil.",
                    "Mae, el clima en $c parece velorio de pueblo. Puro gris, frío y ganas de mandar todo al carajo.",
                    "Cielo color asfalto mojado en $c: parece que no amanece nunca y las horas se arrastran como babosas. Qué tristeza internacional."
                )
                WeatherCategory.HIGH_HUMIDITY -> listOf(
                    "Mae, ${humidity}% de humedad en $c: respirar es como tragar vapor de olla de tamales. La ropa se le pegó al pellejo y apesta a bochorno extranjero, carepicha.",
                    "Diay carebarro, ${humidity}% de humedad en $c. Camina diez metros y parece que salió de una piscina con ropa puesta. Qué asco de bochorno pegajoso.",
                    "Vaporón de primer mundo en $c: el pelo se le esponjó como estropajo y las axilas le chorrean sin parar. Ni el aire acondicionado da abasto."
                )
                WeatherCategory.CLEAR_SKIES -> listOf(
                    "Día despejado en $c, aproveche mae... porque mañana seguro le cae un despiche de clima y vuelve a mamar.",
                    "Diay carepicha, qué suerte la suya: solcito bonito en $c. No se quede encerrado aplatanado y salga a gastar plata.",
                    "Muy lindo el cielo en $c, pero le apuesto a que le hace falta un buen chifrijo y una Imperial para coronar, playo.",
                    "Mae, disfrute los 15 minutos de sol en $c antes de que el clima bipolar se le cague en los planes.",
                    "Cielo azul en $c... raro en ese país. Aproveche antes de que empiece a llover o a congelar, idiota.",
                    "Sol radiante en $c. Salga a tomar aire fresco para que no se le note tanto la cara de amargado.",
                    "Día despejado y con solazo en $c: aproveche para caminar 30.000 pasos y que le salgan ampollas en las patas, carepicha.",
                    "Diay mae, qué milagro: cielo azul sin una nube en $c. Saque la cámara y gaste plata en recuerdos caros antes de que el clima cambie de opinión.",
                    "Cielo despejado en $c, pero no se crea la gran cosa que sigue siendo el mismo polo de siempre pero con fondo bonito, idiota.",
                    "Mae, sol brillante en $c: salga a gastar los viáticos y a tomarse selfies para rajar con los compas del brete en Costa Rica."
                )
                WeatherCategory.PERFECT -> listOf(
                    "Mae, clima de ensueño en $c: ni frío ni calor, una verdadera delicia. Disfrútelo porque un tico con suerte con el clima en el extranjero es un milagro bíblico.",
                    "Día perfecto en $c: el cielo está de postal. No se quede encerrado en el cuarto de hotel como un ermitaño y salga a vivir la vida, pura vida."
                )
                else -> getGeneralForeignRoasts(country, isFamilyFriendlyMode = false)
            }
        } else {
            when (category) {
                WeatherCategory.COLD -> listOf(
                    "¡Qué frío en $c! Saque el abrigo más grueso porque allá el clima no perdona. ¡Muchos saludos desde Costa Rica!",
                    "Diay, en $c haciendo frío polar (${tempInt}°C) y en Costa Rica con un cafecito bien caliente. ¡Abríguese bien, pura vida!",
                    "Esperamos que tenga buena calefacción en $c, porque ese friíto se las trae. ¡A cuidarse mucho!",
                    "Mucho paseo por $c, pero seguro ya le hace falta una buena cobijita tica. ¡Que disfrute el viaje!",
                    "Cuidado con el hielo en $c, camine despacito y con buen abrigo, pura vida.",
                    "Un saludo caluroso desde Costa Rica hasta $c para quitarle un poco ese frío tan bravo.",
                    "Abríguese de pies a cabeza en $c, que el frío de allá cala hondo. ¡Pura vida!",
                    "¿Ya extraña el cafecito chorreado con este frío de $c? Tómese algo caliente y cuídese.",
                    "El friíto de $c está de película, nada más cúbrase bien las orejas y el pecho, pura vida.",
                    "Abrigarse en capas es el secreto para disfrutar de $c con este clima fresco. ¡Un gran saludo tico!"
                )
                WeatherCategory.EXTREME_HEAT -> listOf(
                    "¡Qué calorón en $c! No olvide tomar bastante agüita y buscar sombrita, pura vida.",
                    "El sol está picando fuerte en $c (${tempInt}°C). ¡A cuidarse del golpe de calor y disfrutar el viaje!",
                    "Parece que el sol de $c compite con el de Guanacaste. ¡Póngase bloqueador y disfrute mucho!",
                    "Calorcito bravo por $c. Tómese un refresco bien frío para aguantar la caminata.",
                    "Un abrazo desde Costa Rica hasta $c. ¡Busque aire acondicionado que el calor está pesado!",
                    "Tome bastante agua y descanse bajo la sombra en $c, que el calor no es jugando, pura vida."
                )
                WeatherCategory.HEAVY_RAIN, WeatherCategory.RAIN -> listOf(
                    "¡Menudo aguacero en $c! Saque la sombrilla y no se moje, pura vida.",
                    "Parece que la lluvia de Costa Rica lo siguió hasta $c. ¡A resguardarse de la tormenta!",
                    "Día lluvioso por $c. Momento perfecto para visitar un museo o tomarse un café calentito.",
                    "Que la lluvia en $c no le apague el ánimo, ¡a disfrutar con cuidado y buen paraguas!",
                    "Cuidado con los charcos en $c. ¡Un saludo tico hasta allá!",
                    "Día pasado por agua en $c, nada que un buen impermeable y una actitud positiva no resuelvan, pura vida."
                )
                WeatherCategory.THUNDERSTORM -> listOf(
                    "Tormenta eléctrica en $c: manténgase bajo techo y lejos de ventanas abiertas, pura vida.",
                    "Rayería fuerte en $c: momento ideal para resguardarse en una cafetería y esperar que pase la tormenta."
                )
                WeatherCategory.STRONG_WIND -> listOf(
                    "Mucho viento en $c: sujete bien la bufanda y el sombrero para que no se le vuelen.",
                    "Ráfagas fuertes en $c: camine con precaución y disfrute la frescura del viento, pura vida."
                )
                WeatherCategory.HIGH_UV -> listOf(
                    "Índice UV alto en $c: póngase bloqueador solar y use lentes oscuros para protegerse la vista.",
                    "El sol está radiante en $c: protéjase bien la piel antes de salir a recorrer la ciudad."
                )
                WeatherCategory.CLOUDS -> listOf(
                    "Día nubladito en $c, perfecto para caminar tranquilo sin que el sol queme. ¡Pura vida!",
                    "Cielo gris por $c, pero con buena actitud todo sale bonito. ¡Saludos desde Costa Rica!",
                    "Un abrazo desde Costa Rica hasta $c. ¡Que tenga un lindo día aunque esté nublado!"
                )
                WeatherCategory.CLEAR_SKIES -> listOf(
                    "¡Qué día más hermoso en $c! Aproveche para pasear y tomar muchas fotos, pura vida.",
                    "Cielo despejado y bonito por $c. ¡Que disfrute muchísimo su estancia allá!",
                    "El clima se portó de maravilla hoy en $c. ¡Un gran abrazo tico hasta allá!"
                )
                WeatherCategory.PERFECT -> listOf(
                    "¡Clima perfecto en $c! Un día soñado para turistear y pasarla genial, pura vida.",
                    "Condiciones inmejorables en $c: disfrute cada minuto de este clima tan agradable."
                )
                else -> getGeneralForeignRoasts(country, isFamilyFriendlyMode = true)
            }
        }
    }

    fun getGeneralForeignRoasts(country: String, isFamilyFriendlyMode: Boolean): List<String> {
        val c = cleanForeignCountryName(country)
        return if (!isFamilyFriendlyMode) {
            listOf(
                "Mae, ¿qué putas hace en $c? Se fue a largar a país ajeno y el clima lo tiene de rodillas pidiendo cacao.",
                "Diay carepicha en $c, ¿ya se le pegó el acento polo o todavía se acuerda de hablar como tico?",
                "Presumiendo pasaporte sellado en $c y allá anda pasando las de Caín con el clima. No sea fantoche.",
                "Mae, llorando por una botellita de Salsa Lizano en $c mientras el clima se le caga encima. Qué poesía.",
                "Pudo irse hasta el fin del mundo en $c, pero la mala suerte con el clima se la llevó de equipaje, imbécil.",
                "Diay muerto de hambre en $c, ¿cuántos riñones le costó el café para aguantar este clima hediondo?",
                "Mae, en $c la gente lo ve raro no por el clima, sino por la cara de mongolo que se maneja.",
                "Se creía muy cosmopolita viviendo en $c y vea: quejándose del clima exactamente igual que en Chepe.",
                "Diay mae, el clima de $c no le tiene compasión a los extranjeros y menos a usted, carebarro.",
                "Tanto presumir el viaje a $c en redes sociales y anda encerrado en el cuarto porque el clima está intratable.",
                "Mae, salude a la gente de $c de parte de este malparido clima que lo tiene pariendo chayotes.",
                "Gasta una fortuna en pasajes a $c para pasar todo el día pegado a esta app viendo si mejora el clima. Pedazo de animal.",
                "En $c el clima le cobra en dólares y le paga con tormentas. Tome pa' que lleve, carepicha.",
                "Allá en $c con ese clima y usted extrañando hasta las presas de la Circunvalación. No me joda.",
                "Diay mae, ¿tan mal le iba en Costa Rica que se fue a comer mierda a $c? Disfrute su estadía, idiota.",
                "Mae, ¿cuántas veces ha convertido los precios de $c a colones hoy para llorar mientras el clima le escupe en la jupa?",
                "Presumiendo fotos en $c pero por dentro añorando una olla de carne con yuca y un fresco de mora con leche. No sea hipócrita.",
                "Diay carebarro en $c, aquí en Costa Rica viendo su sufrimiento por historias de WhatsApp con una sonrisa de oreja a oreja.",
                "Mae, en $c el transporte público será puntual, pero el clima le está puntualizando que usted es un pobre diablo.",
                "Tanto alboroto para viajar a $c y terminar comiendo en McDonald's porque le dio miedo pedir comida en otro idioma bajo la lluvia.",
                "Mae, pagó roaming internacional para que esta app le recuerde que el clima en $c se le está cagando encima en tiempo real.",
                "Diay mae, ¿ya le hizo falta el gallopinto con huevo frito o todavía cree que el desayuno continental de $c lo llena?",
                "En $c usted no es turista VIP, es un mae más mojándose y tragando frío con cara de perdido. Ubíquese.",
                "Mae, si el clima de $c tuviera voz le diría en cuatro idiomas distintos que se devuelva para la choza.",
                "Diay fantoche, ¿cuántos filtros le metió a la foto en $c para tapar el aguacero y el cielo gris? Admita la derrota."
            )
        } else {
            listOf(
                "¡Saludos especiales hasta $c! Cuéntenos, ¿cuánta falta le hace un buen gallopinto tico?",
                "Muchos éxitos en su estadía por $c, que el clima lo acompañe y la pase pura vida.",
                "Desde la tierra del pura vida le mandamos buenas vibras hasta $c. ¡A disfrutar cada rincón!",
                "No importa en qué parte de $c esté, un tico siempre lleva el calor de su tierra en el corazón.",
                "Esperamos que $c lo reciba con los brazos abiertos y un clima agradable. ¡Pura vida, mae!",
                "¡Qué paseador por $c! No olvide mandar fotos a la familia y abrigarse si hace falta.",
                "Un saludo tico hasta $c. ¡Disfrute cada día al máximo!",
                "¡Qué gran experiencia conocer $c! Que el buen tiempo le permita recorrer los lugares más lindos.",
                "Desde Costa Rica le mandamos un abrazo caluroso hasta $c. ¡Pura vida y a cuidarse del clima!",
                "¡Saludos hasta $c! Que no le falte una buena taza de café para sentirse como en casa."
            )
        }
    }

    fun getForeignComboInsults(
        combo: WeatherComboType,
        country: String,
        isFamilyFriendly: Boolean,
        temp: Double,
        wind: Double
    ): List<String> {
        val c = cleanForeignCountryName(country)
        val tempInt = temp.toInt()
        val windInt = wind.toInt()

        return if (!isFamilyFriendly) {
            when (combo) {
                WeatherComboType.TRIFECTA -> listOf(
                    "MAE, EN $c LE CAYÓ LA TRIFECTA DEL DESPICHE: ¡Baldazo, ventolero de ${windInt} km/h Y frío de ${tempInt}°C! ¿Para esto pagó pasaje de avión, carepicha? Quédese encerrado en el hotel.",
                    "Trifecta en $c: lluvia conchuda, viento asesino y frío polar. Ni crea que por estar en el extranjero se salva del despiche total, animal.",
                    "MAE, LA TRIFECTA INTERNACIONAL: tempestad de agua, ráfagas cortantes y congelamiento a ${tempInt}°C en $c. El clima le cobró peaje de entrada y lo dejó en bancarrota moral, idiota.",
                    "Combo del desastre en $c: lluvia horizontal, ventolero empujando basureros y friazo polar. Hasta los policías locales andan bajo techo y usted jugando de explorador, carebarro."
                )
                WeatherComboType.THUNDER_WIND -> listOf(
                    "MAE, tormenta eléctrica Y ventolero de ${windInt} km/h en $c. El apocalipsis internacional llegó. Cierre todo y métase bajo la cama, carebarro.",
                    "Rayería y ráfagas bravas en $c. No sea caballo y no salga a la calle a jugar de turista valiente.",
                    "¡RAYERÍA Y CHIFLÓN EN $c! Los relámpagos iluminan los rascacielos y el viento arranca sombrillas a diestra y siniestra. ¡Refúgiese ya, pedazo de bestia!",
                    "Diay carepicha en $c, ¿truenos que hacen saltar las alarmas de los carros y viento de ${windInt} km/h? Salir ahorita es comprar boleto para la morgue extranjera."
                )
                WeatherComboType.RAIN_WIND -> listOf(
                    "Mae, ¿lluvia Y ráfagas de ${windInt} km/h en $c? Si abre esa sombrilla barata en moneda extranjera va a salir volando como una bruja malparida.",
                    "Combo de baldazo y viento en $c: el agua le pega de medio lado y la ropa se le fue a la picha. Tome pa' que lleve.",
                    "Lluvia con ventolero cruzado en $c: el agua le entra por la capucha, por los bolsillos y le empapa los billetes del cambio. Qué salazón de viaje.",
                    "Mae, ráfagas de ${windInt} km/h con aguacero cerrado en $c: la sombrilla le duró tres segundos antes de doblarse como taco viejo. Camine empapado por polo."
                )
                WeatherComboType.HEAT_HUMIDITY -> listOf(
                    "Mae, bochorno asesino en $c: ${tempInt}°C con humedad insoportable. Sudando en moneda extranjera como tamal mal envuelto, carepicha.",
                    "Sauna asqueroso en $c: calor pegajoso y vapor que le quita el aire al caminar. La camiseta parece trapo de cocina mojado.",
                    "Mae, ${tempInt} grados con bochorno en $c: respirar es tragar caldo hirviendo en medio de la muchedumbre. Una delicia de turismo masoquista."
                )
                WeatherComboType.COLD_WIND -> listOf(
                    "Viento helado de ${windInt} km/h con ${tempInt}°C en $c. Se le congeló hasta el alma, playo. Extrañe el calor tico y llore.",
                    "Chiflón polar a ${windInt} km/h en $c: el viento le corta los labios y le congela las ideas. Póngase gorro o se le cae la única neurona que tiene.",
                    "Mae, ráfagas congeladas en $c: parece que le están tirando hielo picado con soplador industrial en la cara. Qué paridera más concha."
                )
                WeatherComboType.COLD_RAIN -> listOf(
                    "Mae, ¿lluvia Y frío de ${tempInt}°C en $c? El combo favorito de la neumonía internacional. Empapado y temblando como perro en canoa, carepicha.",
                    "Agua helada cayendo sobre las calles de $c: las tenis le chorrean hielo y el abrigo pesa diez kilos de pura agua. Qué día más maldito."
                )
                WeatherComboType.HEAT_UV -> listOf(
                    "Mae, ¿${tempInt}°C Y radiación UV criminal en $c? El sol le está friendo la jupa como huevo en sartén. Póngase bloqueador o va a parecer piñata chamuscada.",
                    "Horno solar con radiación extrema en $c: un paso afuera y le da golpe de calor en plena avenida turística. Busque aire acondicionado ya, animal."
                )
                WeatherComboType.RAIN_AND_SUN -> listOf(
                    "Mae, en $c se está casando la venada: aguacero con solazo hirviendo a la vez. El asfalto echa vapor y el bochorno lo aplatana en seco, qué clima más loco.",
                    "Lluvia caliente con sol abierto en $c: ni los locales entienden qué le pasa al cielo hoy. Quedó mojado y achicharrado a la vez, carebarro."
                )
                WeatherComboType.NIGHT_STORM -> listOf(
                    "Medianoche con tempestad y relámpagos en $c: el cielo truena durísimo y la ventana del hotel tiembla. Tápese la jupa y duerma si puede, carepicha.",
                    "Tormenta nocturna en $c: diluvio y truenos que no dejan pegar el ojo. Qué manera de pasar la noche en país ajeno."
                )
                else -> emptyList()
            }
        } else {
            when (combo) {
                WeatherComboType.TRIFECTA -> listOf(
                    "Combo completo de clima bravo en $c: lluvia fuerte, viento y frío. ¡Mejor quedarse descansando calientito, pura vida!",
                    "Clima bastante desafiante en $c con lluvia, frío y viento. Abríguese bien y tómese algo caliente, pura vida."
                )
                WeatherComboType.THUNDER_WIND -> listOf(
                    "Tormenta eléctrica con viento fuerte en $c. ¡Mucho cuidado y a resguardarse bajo techo!",
                    "Rayería y viento intenso en $c. Permanezca en un lugar seguro y cuide sus pertenencias, pura vida."
                )
                WeatherComboType.RAIN_WIND -> listOf(
                    "Lluvia con viento en $c. Sujete bien el paraguas y disfrute con precaución, pura vida.",
                    "Aguacero con ráfagas en $c: use impermeable y camine con cuidado en aceras resbalosas."
                )
                WeatherComboType.HEAT_HUMIDITY -> listOf(
                    "Bochorno fuerte en $c con calor y humedad. Manténgase bien hidratado y busque lugares frescos, pura vida."
                )
                WeatherComboType.COLD_WIND -> listOf(
                    "Viento fresco y frío en $c. Póngase un buen abrigo y bufanda para disfrutar la caminata."
                )
                WeatherComboType.COLD_RAIN -> listOf(
                    "Lluvia fría en $c: lleve ropa impermeable abrigada y disfrute un buen café caliente."
                )
                else -> emptyList()
            }
        }
    }
}
