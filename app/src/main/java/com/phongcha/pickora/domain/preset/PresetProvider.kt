package com.phongcha.pickora.domain.preset

import android.content.Context
import com.phongcha.pickora.R

data class Preset(
    val id: String,
    val nameRes: Int,
    val emoji: String,
    val itemResIds: List<Int>,
    val targetRoute: String = "wheel"
) {
    fun resolveItems(context: Context): List<String> =
        itemResIds.map { context.getString(it) }
}

object PresetProvider {

    fun getAllPresets(): List<Preset> = listOf(
        Preset(
            id = "what_to_eat",
            nameRes = R.string.preset_what_to_eat,
            emoji = "\uD83C\uDF54",
            itemResIds = listOf(
                R.string.preset_item_pizza, R.string.preset_item_sushi, R.string.preset_item_burger,
                R.string.preset_item_pho, R.string.preset_item_tacos, R.string.preset_item_pasta,
                R.string.preset_item_salad, R.string.preset_item_ramen, R.string.preset_item_fried_chicken,
                R.string.preset_item_curry
            )
        ),
        Preset(
            id = "truth_or_dare",
            nameRes = R.string.preset_truth_or_dare,
            emoji = "\uD83C\uDFAD",
            itemResIds = listOf(R.string.preset_item_truth, R.string.preset_item_dare)
        ),
        Preset(
            id = "random_punishment",
            nameRes = R.string.preset_random_punishment,
            emoji = "\uD83D\uDE08",
            itemResIds = listOf(
                R.string.preset_item_pushups, R.string.preset_item_sing, R.string.preset_item_dance,
                R.string.preset_item_joke, R.string.preset_item_impression, R.string.preset_item_accent,
                R.string.preset_item_story, R.string.preset_item_spicy
            )
        ),
        Preset(
            id = "movie_genre",
            nameRes = R.string.preset_movie_night,
            emoji = "\uD83C\uDFAC",
            itemResIds = listOf(
                R.string.preset_item_action, R.string.preset_item_comedy, R.string.preset_item_horror,
                R.string.preset_item_romance, R.string.preset_item_scifi, R.string.preset_item_thriller,
                R.string.preset_item_animation, R.string.preset_item_documentary
            )
        ),
        Preset(
            id = "workout",
            nameRes = R.string.preset_random_workout,
            emoji = "\uD83D\uDCAA",
            itemResIds = listOf(
                R.string.preset_item_pushups_ex, R.string.preset_item_squats, R.string.preset_item_plank,
                R.string.preset_item_burpees, R.string.preset_item_jumping_jacks, R.string.preset_item_lunges,
                R.string.preset_item_situps, R.string.preset_item_mountain_climbers
            )
        ),
        Preset(
            id = "drink_order",
            nameRes = R.string.preset_what_to_drink,
            emoji = "\uD83E\uDDCB",
            itemResIds = listOf(
                R.string.preset_item_coffee, R.string.preset_item_milk_tea, R.string.preset_item_smoothie,
                R.string.preset_item_juice, R.string.preset_item_water, R.string.preset_item_soda,
                R.string.preset_item_matcha, R.string.preset_item_hot_chocolate
            )
        ),
        Preset(
            id = "travel",
            nameRes = R.string.preset_where_to_travel,
            emoji = "\u2708\uFE0F",
            itemResIds = listOf(
                R.string.preset_item_japan, R.string.preset_item_korea, R.string.preset_item_thailand,
                R.string.preset_item_france, R.string.preset_item_italy, R.string.preset_item_usa,
                R.string.preset_item_australia, R.string.preset_item_vietnam
            )
        ),
        Preset(
            id = "who_pays",
            nameRes = R.string.preset_who_pays,
            emoji = "\uD83D\uDCB0",
            itemResIds = listOf(
                R.string.seed_player_1, R.string.seed_player_2, R.string.seed_player_3, R.string.seed_player_4
            ),
            targetRoute = "name"
        )
    )
}
