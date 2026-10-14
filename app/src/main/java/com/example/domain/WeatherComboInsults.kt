package com.example.domain

import java.util.Locale

object WeatherComboInsults {

    fun getComboInsults(
        combo: WeatherComboType,
        isFamilyFriendly: Boolean,
        temperature: Double,
        feelsLike: Double,
        humidity: Int,
        windSpeed: Double,
        uvIndex: Double
    ): List<String> {
        val tempInt = temperature.toInt()
        val feelsInt = feelsLike.toInt()
        val windInt = windSpeed.toInt()
        val uvStr = String.format(Locale.US, "%.1f", uvIndex)

        return if (!isFamilyFriendly) {
            when (combo) {
                WeatherComboType.TRIFECTA -> listOf(
                    "MAE, LA TRIFECTA DEL DESPICHE: ¡Baldazo, ventolero de mierda Y frío polar juntos! El cielo se le está cagando encima en 4K y sonido envolvente. NO SALGA, CAREPICHA.",
                    "Diay mae, combo triple del despiche: lluvia conchuda, viento que rompe sombrillas y frío que congela los mocos. Si tiene que salir en bus hoy, acepte que su día murió.",
                    "Mae, la trifecta: ¿frío de ${tempInt}°C, baldazo Y ráfagas de ${windInt} km/h? San Pedro hoy se levantó con ganas de joderle la vida a todo el país, qué despiche de temporal.",
                    "Trifecta maldita: zapatos llenos de agua helada, la sombrilla vuelta un trapo roto por el viento y temblando de frío. Qué maldición de clima más desgraciada.",
                    "Mae, este combo no es un día normal, es la trifecta del despiche. Cancele todo, tápese con tres cobijas de tigre y mándelo todo a la grandísima puta.",
                    "Combo apocalíptico: se le congela la jupa, el viento le dobla el paraguas y el agua le llega a los tobillos. Ni a mi peor enemigo le deseo esta cochinada.",
                    "Trifecta de temporal: llueve de medio lado por el viento y el frío cala los huesos. Salir hoy es una reverenda estupidez nivel legendario, carebarro.",
                    "MAE, LA TRIFECTA DEL APOCALIPSIS TICO: baldazo torrencial, frío congelante y ráfagas que le arrancan el zinc al vecino. Si hoy tenía planes de salir, llore y acéptese perdedor, carepicha.",
                    "Trifecta conchuda: el agua le llega a los tobillos, el viento le dio vuelta al paraguas y los dientes le castañean de puro frío. Ni a un diputado en campaña le deseo esta paridera.",
                    "Mae, ¿frío de ${tempInt}°C, ventolero de ${windInt} km/h Y baldazo cerrado? San Pedro agarró el mapa de Costa Rica y le tiró una cubeta de agua con hielo, qué soberano despiche."
                )
                WeatherComboType.THUNDER_WIND -> listOf(
                    "MAE, ¿tormenta eléctrica Y ventolero de ${windInt} km/h? El apocalipsis llegó y no avisó. Desconecte todo y métase bajo la cama antes de que una lámina de zinc le corte la jupa, carepicha.",
                    "Rayería endemoniada con ráfagas botando ramas y cables. Si se va la luz y sale a jugar de electricista, que Dios me lo perdone por semejante idiota.",
                    "Diay mae, el cielo está tirando rayos y el viento arrancando tejas. Quédese quieto adentro y rece lo que se sepa, pedazo de bestia.",
                    "Mae, truenos que hacen vibrar el piso y viento que sacude la choza entera. Afuera hay una carnicería climática, ni asome los ojos.",
                    "Ráfagas asesinas con relámpagos que lo dejan ciego. Cierre las celosías o va a tener que barrer pedazos de vidrio con la escoba, carebarro.",
                    "Mae, parece que Thor y San Pedro se agarraron a pichazos borrachos en el cielo. Qué clase de tempestad más violenta.",
                    "¡BOMBAZO EN EL CIELO! Rayos partiendo palmeras y ráfagas de ${windInt} km/h levantando techos. Desconecte la tele, la compu y hasta el microondas antes de que el ICE le cobre un ojo de la cara, carebarro.",
                    "Mae, relámpagos que le iluminan el alma y ventolero botando vallas publicitarias en la pista. Métase a la casa y no juegue de macho alfa bajo la tempestad, pedazo de animal.",
                    "Rayería endemoniada sobre el Valle Central: los truenos suenan como cañonazos de guerra y el viento mueve hasta los buses en la parada. ¡Ni se asome a la puerta, carepicha!"
                )
                WeatherComboType.RAIN_WIND -> listOf(
                    "Mae, ¿lluvia Y ventolero de ${windInt} km/h? Si abre esa sombrilla barata va a parecer un taco al revés y va a salir volando como una bruja malparida.",
                    "Baldazo con viento concho, mae. El agua le pega de lado, las piernas empapadas y la sombrilla vuelta mierda. Qué soberano despiche.",
                    "Diay carepicha, combo de lluvia con ráfagas asesinas. Ni Noé con paraguas de titanio se salva de quedar como sopa.",
                    "Mae, el viento le dobla la sombrilla y el aguacero le empapa el lomo al mismo tiempo. Qué clase de combo más hijueputa.",
                    "Aguacero con ráfagas de ${windInt} km/h: la sombrilla se le va a meter por el culo si intenta abrirla. Váyase a la choza mejor.",
                    "Mae, llueve en diagonal por el ventolero. Ni que ande con traje de buzo se salva de empaparse hasta las bolas.",
                    "La sombrilla de dos rojos de la pulpería duró 4 segundos antes de quebrarse con este viento. Ahora anda mojado y con cólera, carebarro.",
                    "Baldazo con ráfagas de ${windInt} km/h: la sombrilla le duró menos que quincena en diciembre. Quedó con el paraguas destripado en la mano y el agua bañándole la espalda, qué papelón.",
                    "Mae, temporal cruzado con ventolero: los carros van tirando olas de barro que tapan el vidrio y el viento le mete el agua hasta adentro de los zapatos. Vaya al suave, bestia.",
                    "Lluvia horizontal con ráfagas conchas: ni con tres capas impermeables se salva de quedar como sopa fría. ¡Qué bendita pereza de clima!"
                )
                WeatherComboType.HEAT_HUMIDITY -> listOf(
                    "Mae, ${tempInt}°C Y ${humidity}% de humedad. Esto no es clima, es una olla de carne hirviendo con usted adentro. Me suda hasta el apellido, carepicha.",
                    "Combo asqueroso: calor infernal con una humedad del demonio. Siento que respiro sopa de mondongo tibia, qué sobaco más hijueputa.",
                    "Diay mae, ¿calorón y bochorno juntos? La camisa se le pegó a las tetillas y apesta a perro mojado solo con pestañear.",
                    "Mae, esto parece un sauna clandestino de mala muerte. Calor asqueroso y vapor que no deja ni respirar, qué picha de bochorno.",
                    "Sensación térmica de ${feelsInt}°C con ${humidity}% de humedad. Me estoy sancochando vivo, qué ganas de arrancarme la piel.",
                    "Mae, no corre ni una gota de aire y el vapor le hierve los sesos. Qué combo más insoportable y malparido.",
                    "El paño del baño no se seca ni en tres días y uno suda hasta durmiendo. Qué asco de bochorno repugnante.",
                    "Mae, ${tempInt}°C con ${humidity}% de humedad: esto es una olla de carne hirviendo a fuego lento y usted es el chayote del fondo. Se le pegaron los calzones al alma, carepicha.",
                    "Sancocho asesino nacional: respirar aquí es como pegarse un paño mojado con agua tibia en la jupa. Los sobacos parecen cataratas de La Paz, qué asco de bochorno.",
                    "Diay carebarro, ¿calorón y humedad al tope? La camisa se le transparenta del sudor y el asiento del carro parece una freidora de pollo. Hidrátese o se nos muere en la acera."
                )
                WeatherComboType.COLD_RAIN -> listOf(
                    "Mae, ¿lluvia Y frío de ${tempInt}°C al mismo tiempo? Receta perfecta para neumonía con diarrea. Qué combo más hijo de puta.",
                    "Diay mae, el cielo le tira agua helada para que se le congelen hasta las ideas. Mojado y con frío como feto abandonado, qué pura mierda.",
                    "Mojarse con este frío helado es de animales. Si sale a la calle hoy es porque de verdad se odia a usted mismo, carepicha.",
                    "Mae, ${tempInt}°C bajo un aguacero concho. Siento los dedos del pie como cubitos de hielo, me cago en este clima.",
                    "Combo de Cartago con Zurquí en su peor día: lluvia helada constante. Saque la cobija de tigre y mande el brete al carajo.",
                    "Mae, agüita helada cayendo sin parar. Ni con tres suéteres mojados se quita esta tiritadera de dientes.",
                    "Agua congelada en la nuca y zapatos con charcos fríos. Qué combinación más nefasta y desgraciada.",
                    "Mae, lluvia con frío de ${tempInt}°C: la combinación favorita de los virus respiratorios y de la depre dominical. Mojado, con los mocos tiesos y sin ganas de vivir, qué concho.",
                    "Agua helada cayendo parejo sobre las aceras rotas de Chepe. Los zapatos le chapalean con cada paso y las rodillas le duelen del friazo. Métase a la choza a tomar café ya, pedazo de idiota."
                )
                WeatherComboType.COLD_WIND -> listOf(
                    "Mae, ¿frío de ${tempInt}°C con ráfagas de ${windInt} km/h? El chiflón le parte los labios y le congela el cerebro, qué ventolero más carepicha.",
                    "Diay mae, el viento helado le entra hasta por el culo. Saque la chompipa y la bufanda de la abuela o va a quedar tieso.",
                    "Mae, este viento parece aire directo de la morgue. Frío, seco y arranca-peluquines, qué concho.",
                    "Ráfagas congeladas a ${windInt} km/h. Si sale en pantaloneta se le van a caer las bolas del frío, pedazo de idiota.",
                    "Mae, el chiflón se cuela por debajo de la puerta como si nada. Qué combo de frío y ventolero más hijueputa.",
                    "Viento helado que le desacomoda la jupa y le congela las orejas. Camine rápido a meterse a la choza, malparido.",
                    "Mae, chiflón de ${windInt} km/h con ${tempInt}°C: el viento le corta los labios como navaja vieja y le entra por la espalda como un fantasma. Saque el abrigo de lana o termine con tortícolis, carepicha.",
                    "Viento helado de los alisios bajando por las cordilleras: le despeluca la jupa y le congela las ideas. Si sale en camiseta hoy es para declararlo inimputable, idiota."
                )
                WeatherComboType.HEAT_UV -> listOf(
                    "Mae, ¿${tempInt}°C Y radiación UV en ${uvStr}? Dios agarró una lupa gigante y le está quemando la jupa a usted directamente, carepicha.",
                    "Combo sartén cósmica: un solazo que derrite el asfalto y rayos UV radiactivos. Si sale sin bloqueador va a quedar como pellejo de chicharrón chamuscado.",
                    "Diay mae, el sol está con ganas de desintegrarlo a nivel atómico. Póngase bloqueador ya, so pedazo de animal, o aténgase a amanecer pelándose la piel.",
                    "Mae, calor infernal y el UV por las nubes. Si camina por la acera se le derriten hasta las suelas de las chancletas, qué barbaridad.",
                    "Calor de la gran puta con radiación asesina. Salga solo si quiere que le dé insolación en 5 minutos, malparido.",
                    "El sol está tan violento que parece un soplete de soldador en la jupa. Póngase sombrero o muérase, carebarro.",
                    "Mae, ¿${tempInt}°C y radiación UV en ${uvStr}? El sol está bajando en línea recta a calcinarle las pecas y el lomo. Póngase bloqueador factor 100 o va a parecer piñata chamuscada, bestia.",
                    "Horno solar con radiación asesina: el volante del carro quema peor que carbón de parrillada y la acera echa ondas de calor. Busque sombra antes de sufrir un colapso térmico, malparido."
                )
                WeatherComboType.RAIN_AND_SUN -> listOf(
                    "Mae, ¿lloviendo Y con semejante solazo encima? Diay sí, ¡se está casando la venada, carepicha! Clima bipolar más raro que la puta.",
                    "Sol que pela y gotas cayendo al mismo tiempo. Parece que el cielo tiene esquizofrenia no diagnosticada, qué despiche de clima.",
                    "Mae, ¿lluvia con solazo caliente? El diablo le está dando de pichazos a la mujer allá arriba, qué bochorno más loco.",
                    "Diay mae, ni el cielo sabe qué putas quiere hoy: se moja uno pero suda como un cerdo por el sol. Pura indecisión tica.",
                    "Lluvia caliente con sol abierto. Sale un arcoíris y uno con la ropa pegada y empapada, qué estafa más grande.",
                    "Cielo esquizofrénico total: gotas cayendo sobre el asfalto hirviendo. Vapor instantáneo y calor mojado, qué mierda más bizarra.",
                    "Mae, ¡se está casando la venada con racha tropical! Cae un aguacero que rebalsa caños pero el solazo le quema el coco a la vez. El asfalto echa vapor caliente como olla arrocera, qué clima más loco.",
                    "Lluvia caliente con sol abierto: el cielo no sabe si regalarle un arcoíris o darle un golpe de calor. Quedó empapado y sancochado al mismo tiempo, qué despiche de clima."
                )
                WeatherComboType.HEAT_WIND -> listOf(
                    "Mae, ¿${tempInt}°C Y ventolero de ${windInt} km/h? Esto es como poner un abanico adentro de una freidora de aire. Qué aire más seco y caliente, carepicha.",
                    "Viento caliente que le tira tierra a los ojos mientras suda como condenado. Qué combo más concho y hediondo.",
                    "Diay mae, las ráfagas parecen el aliento del mismísimo Satanás soplándole en la nuca. Qué bochorno con ventolero.",
                    "Polvazal en la jupa con un calorón que derrite las ideas. Ni para refrescar sirve este hijueputa viento."
                )
                WeatherComboType.NIGHT_STORM -> listOf(
                    "Mae, ¿escucha ese baldazo en el techo de zinc? Parece que le están tirando piedras a la choza. Noche de diluvio, duérmase si los truenos lo dejan, carepicha.",
                    "Noche con aguacero descomunal y rayería. Si se le mete el agua por la ventana no sea vago y ponga un trapo antes de jatear.",
                    "Diay mae, llueve a cántaros de noche. Ni las ranas aguantan tanta agua. Cierre todo y a dormir antes de que se vaya la luz.",
                    "Truenos retumbando en la oscuridad y el techo de zinc ametrallado. Apague la pantalla y métase a la cama, animal.",
                    "Mae, medianoche con rayería y baldazo en el techo de zinc: parece que le están tirando sacos de piedras a la casa. Tápese con la cobija hasta la jupa y rece para que no se inunde el patio, carepicha.",
                    "Diluvio nocturno con truenos que hacen vibrar las ventanas: si dejó la ropa tendida afuera, ya es propiedad comunal del barro. Duerma si puede, idiota."
                )
                WeatherComboType.NIGHT_COLD -> listOf(
                    "Mae, ya es de noche y hace un frío como para congelar pingüinos en Cartago. Métase bajo la cobija de tigre y duérmase de una puta vez, carepicha.",
                    "Noche helada, mae. La cama parece un cubo de hielo y la nariz le gotea. Tápese hasta las orejas y deje de tocar el cel.",
                    "Diay mae, ¿a las tantas de la noche temblando de frío y viendo el clima? Suelte el teléfono y métale medias a esas patas congeladas.",
                    "Frío concho de madrugada, el piso parece témpano. Si saca un pie de la cobija se le congela, duerma ya."
                )
                WeatherComboType.HUMIDITY_FOG -> listOf(
                    "Mae, ${humidity}% de humedad con neblina espesa. Parece que estamos atrapados en el Zurquí adentro de una nube de vapor hirviendo, qué asco de sopa.",
                    "Cero visibilidad y un bochorno pegoste que ahoga. Se le pegan las pestañas de tanta humedad, carepicha.",
                    "Diay mae, esto parece el pantano de Shrek versión tica: neblina, humedad al tope y un pegoste en la piel que da asco."
                )
            }
        } else {
            when (combo) {
                WeatherComboType.TRIFECTA -> listOf(
                    "¡La trifecta del temporal, mae! Lluvia fuerte, frío y viento al mismo tiempo. ¡Mejor no asomar la nariz!",
                    "Aguacero con viento fuerte y bastante frío, quédese calentito en casa tomando café si puede.",
                    "Temporal completo hoy: agua, viento y bajonazo de temperatura. Cuídese mucho en carretera.",
                    "Día de temporal pesado, abríguese bien y no se arriesgue con este clima tan bravo, pura vida.",
                    "Trifecta de invierno: frío, viento y aguacero. Clima perfecto para cobija, tele y un buen aguadulce."
                )
                WeatherComboType.THUNDER_WIND -> listOf(
                    "Tormenta eléctrica con viento muy fuerte, mae. Desconecte los aparatos y quédese seguro en casa.",
                    "Rayos y ventolero fuerte al mismo tiempo, mantenga a las mascotas tranquilas y no salga.",
                    "Clima peligroso: rayería y ráfagas bravas. La seguridad primero, pura vida.",
                    "Tormenta fuerte con viento, tenga candelas a mano por si se va la luz y descanse tranquilo."
                )
                WeatherComboType.RAIN_WIND -> listOf(
                    "Lluvia con viento fuerte, mae. Ni intente abrir la sombrilla que se le da vuelta seguro.",
                    "Aguacero y ventolero al mismo tiempo, el agua cae de lado. ¡Tápese bien!",
                    "Qué combo de lluvia y viento, las sombrillas andan sufriendo hoy, pura vida.",
                    "Lluvia con ráfagas bravas, mejor espérese a que se calme un poco antes de salir.",
                    "El viento lleva la lluvia directo a la cara, camine con cuidado y busque refugio."
                )
                WeatherComboType.HEAT_HUMIDITY -> listOf(
                    "Mucho calor y humedad alta hoy, el bochorno está que no perdona, mae.",
                    "Calorcito pesado y vaporón, tome bastante agua para no deshidratarse.",
                    "Un bochorno tremendo: calor con aire pesado. Póngase cómodo en la choza.",
                    "Humedad y calor juntos, apenas para tomarse algo bien frío a la sombra, pura vida.",
                    "Aire bien cargado de humedad con calor, manténgase en lugares ventilados."
                )
                WeatherComboType.COLD_RAIN -> listOf(
                    "Frío y lluvia juntos, mae. Salga bien abrigado y con sombrilla que el resfrío anda cerca.",
                    "Agua helada y bajonazo de temperatura, apenas para un buen café con pan dulce.",
                    "Clima frío y pasado por agua, tápese bien el pecho para no enfermarse.",
                    "Frío con llovizna constante, día perfecto para quedarse en camita bajo la cobija, pura vida.",
                    "Agüita fría de invierno, tómese una sopita caliente para calentar el cuerpo."
                )
                WeatherComboType.COLD_WIND -> listOf(
                    "Viento fuerte y frío helado hoy, mae. Abríguese bien la nariz y las orejas.",
                    "Qué chiflón más bravo, el viento frío cala los huesos hoy. ¡Póngase suéter grueso!",
                    "Ráfagas frías que congelan el ambiente, un día para café caliente y bufanda, pura vida.",
                    "Mucho viento y temperatura baja, cuidado con los portazos en la choza.",
                    "El viento helado está picando duro hoy, cuídese de las corrientes de aire."
                )
                WeatherComboType.HEAT_UV -> listOf(
                    "Mucho calor y el sol está picando fuertísimo con UV alto. ¡Póngase bloqueador y gorra, mae!",
                    "Solazo bravo con radiación bien alta, busque la sombrita y tome mucha agua.",
                    "Calor y radiación fuerte hoy, evite asolearse al mediodía, pura vida.",
                    "El sol está que quema hoy, cuídese la piel y no ande a lo loco bajo el sol.",
                    "Día con radiación muy intensa y calor, hidrátese bien y proteja a los niños."
                )
                WeatherComboType.RAIN_AND_SUN -> listOf(
                    "¡Lloviendo y con sol al mismo tiempo, mae! Como dicen los abuelos: ¡se está casando la venada!",
                    "Sol y lluvia juntos, qué clima más curioso hoy en Costa Rica, pura vida.",
                    "Gotas de lluvia con rayos de sol, busque el arcoíris que el día está de película.",
                    "Lluvia con sol brillante, clima bien tico y vacilón hoy.",
                    "El cielo no se decide si llover o calentar, ¡el famoso sol de venado en pleno!"
                )
                WeatherComboType.HEAT_WIND -> listOf(
                    "Calor con viento fuerte hoy, mae. El aire sopla tibio y levanta bastante polvo.",
                    "Ráfagas de viento con temperatura alta, hidrátese bien y use gafas para el polvo.",
                    "Día ventoso y caluroso, cuídese los ojos de la tierra en la calle, pura vida."
                )
                WeatherComboType.NIGHT_STORM -> listOf(
                    "Noche lluviosa con baldazo sobre el techo, arrúyese con el sonido del agua y que descanse bien.",
                    "Llueve fuerte esta noche, asegure bien las ventanas antes de dormir, pura vida.",
                    "Noche de aguacero y truenos a lo lejos, perfecto para descansar calientito en cama."
                )
                WeatherComboType.NIGHT_COLD -> listOf(
                    "Noche bastante fría, mae. Saque la cobija gruesa y descanse calentito, pura vida.",
                    "Noche heladita ideal para un buen chocolate caliente y a la cama a dormir.",
                    "Madrugada fría, abrigue bien la camita para dormir sabroso, pura vida."
                )
                WeatherComboType.HUMIDITY_FOG -> listOf(
                    "Neblina espesa con humedad muy alta, mae. Maneje despacito y con luces si anda afuera.",
                    "Muchísima humedad y neblina en el ambiente, parece clima del Zurquí o Monteverde, pura vida."
                )
            }
        }
    }
}
