package com.example.domain

data class ClothingItem(
    val name: String,
    val emoji: String,
    val isUseless: Boolean = false,
    val sarcasticComment: String? = null
)

data class ClothingRecommendation(
    val clothingItems: List<ClothingItem>,
    val uselessItem: ClothingItem?,
    val verdict: String
)
