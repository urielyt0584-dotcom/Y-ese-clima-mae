package com.example.util

object SmartInsultSelector {
    private val history = mutableListOf<String>()
    private val seedCache = LinkedHashMap<Int, String>()
    private const val MAX_HISTORY = 20

    @Synchronized
    fun getUnique(pool: List<String>, seed: Int?): String {
        if (pool.isEmpty()) return ""
        if (pool.size == 1) return pool.first()

        if (seed != null && seedCache.containsKey(seed)) {
            val cached = seedCache[seed]!!
            if (cached in pool) {
                return cached
            }
        }

        var available = pool.filter { it !in history }
        if (available.isEmpty()) {
            history.clear()
            available = pool
        }

        val chosen = if (seed != null) {
            val rand = java.util.Random(seed.toLong())
            available[rand.nextInt(available.size)]
        } else {
            available.random()
        }

        history.add(chosen)
        if (history.size > MAX_HISTORY) {
            history.removeAt(0)
        }
        
        if (seed != null) {
            seedCache[seed] = chosen
            if (seedCache.size > MAX_HISTORY) {
                val oldestKey = seedCache.keys.first()
                seedCache.remove(oldestKey)
            }
        }

        return chosen
    }
}
