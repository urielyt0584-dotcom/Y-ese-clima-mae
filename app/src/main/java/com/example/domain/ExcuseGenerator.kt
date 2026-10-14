package com.example.domain

import com.example.data.model.WeatherCategory
import com.example.util.SmartInsultSelector

object ExcuseGenerator {
    fun generateExcuse(category: WeatherCategory?, isFamilyFriendly: Boolean = true): String {
        if (category == null) return "Profe, disculpe la tardanza. Hubo un choque en la principal y la presa está rajada. Ya casi le llego."
        
        val pool = when (category) {
            WeatherCategory.HEAVY_RAIN, WeatherCategory.THUNDERSTORM, WeatherCategory.RAIN -> listOf(
                "Profe, disculpe la tardanza, es que hay un baldazo rajado y la calle parece un río. Ya casi le llego.",
                "Profe, qué pena, pero se vino un aguacero de la nada y estoy escampando un toque porque si no llego hecho sopa.",
                "Buenas profe, voy un toque atrasado. La lluvia hizo un desmadre con las presas y el bus no avanza ni a palos.",
                "Profe, disculpe, pero con este baldazo no hay sombrilla que aguante. Ya voy en camino apenas baje un poco."
            )
            WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_UV -> listOf(
                "Profe, disculpe la demora. Hace un calor insoportable y el bus en el que venía se varó. Ya casi llego.",
                "Buenas profe, voy un poco atrasado. El solazo derritió la calle y hay una presa increíble. Ya voy llegando.",
                "Profe, qué pena, con este bochorno me descompensé un toque en la calle. Ya tomé agua y voy para allá."
            )
            WeatherCategory.STRONG_WIND -> listOf(
                "Profe, disculpe la tardanza. Hay un ventolero súper peligroso y se cayó una rama en la calle, bloqueando el paso. Ya casi le llego.",
                "Buenas profe, voy atrasado. El viento está botando cosas en la calle y el bus tuvo que desviarse."
            )
            WeatherCategory.NIGHT -> listOf(
                "Profe, disculpe, se me hizo tardísimo y con esta oscuridad no pasaba el bus. Ya voy en camino.",
                "Buenas profe, voy un toque atrasado. En la noche el transporte está fatal hoy."
            )
            else -> listOf(
                "Profe, disculpe la tardanza. Hubo un choque en la principal y la presa está de locos. Ya casi llego.",
                "Buenas profe, voy un toque atrasado. El bus se varó y me tocó esperar el otro. Ya voy llegando.",
                "Profe, qué pena la demora. Hubo un problema en la casa de última hora pero ya voy saliendo soplado.",
                "Profe, disculpe, me agarró una presa rarísima para ser esta hora. Ya casi le llego."
            )
        }
        
        if (isFamilyFriendly) {
            return pool.random()
        } else {
            val unfilteredPool = when (category) {
                WeatherCategory.HEAVY_RAIN, WeatherCategory.THUNDERSTORM, WeatherCategory.RAIN -> listOf(
                    "Profe, qué madre, se vino un reverendo baldazo y estoy atrapado. La calle está echa picha de presa. Llego apenas pueda.",
                    "Profe, disculpe, pero hay un temporal de los mil demonios y el bus no se mueve ni a putas. Ya voy en camino.",
                    "Profe, qué despiche de aguacero. Las calles están inundadas y esto no avanza ni mierda. Ya voy para allá."
                )
                WeatherCategory.EXTREME_HEAT, WeatherCategory.HIGH_UV -> listOf(
                    "Profe, qué calor más hijueputa, me bajé a comprar agua porque me estaba muriendo y perdí el bus. Ya casi llego.",
                    "Profe, el bus parece un maldito horno y va a vuelta de rueda por culpa de una presa malparida. Voy atrasado."
                )
                else -> listOf(
                    "Profe, qué madre, hubo un choque pendejo ahí y la presa está de la gran puta. Ya casi llego.",
                    "Profe, se despichó todo en la calle por un choque, voy un toque atrasado. Llego apenas se quite la presa."
                )
            }
            return unfilteredPool.random()
        }
    }
}
