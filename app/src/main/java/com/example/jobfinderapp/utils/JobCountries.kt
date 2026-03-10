package com.example.jobfinderapp.utils

import com.example.jobfinderapp.entity.Country

object JobCountries {


    val countriesList = listOf<Country>(
        Country("Great Britain", "gb"),
        Country("USA", "us"),
        Country("Austria", "at"),
        Country("Australia", "au"),
        Country("Belgium", "be"),
        Country("Brazil", "br"),
        Country("Canada", "ca"),
        Country("Switzerland", "ch"),
        Country("Germany", "de"),
        Country("Spain", "es"),
        Country("France", "fr"),
        Country("India", "in"),
        Country("Italy", "it"),
        Country("Mexico", "mx"),
        Country("Netherlands", "nl"),
        Country("New Zealand", "nz"),
        Country("Poland", "pl"),
        Country("Singapore", "sg"),
        Country("South Africa", "za"),
    )

    val countriesLocationCode = mapOf<String, String>(
        "gb" to "UK",
        "us" to "US",
        "at" to "Österreich",
        "au" to "Australia",
        "be" to "België",
        "br" to "Brasil",
        "ca" to "Canada",
        "ch" to "Schweiz",
        "de" to "Deutschland",
        "es" to "España",
        "fr" to "France",
        "in" to "India",
        "it" to "Italia",
        "mx" to "México",
        "nl" to "Nederland",
        "nz" to "New Zealand",
        "pl" to "Polska",
        "sg" to "Singapore",
        "za" to "South Africa"
    )

    val countriesLocationCodeToNorm = mapOf<String, String>(
        "UK" to "Great Britain",
        "US" to "United States",
        "Österreich" to "Austria",
        "Australia" to "Australia",
        "België" to "Belgium",
        "Brasil" to "Brazil",
        "Canada" to "Canada",
        "Schweiz" to "Switzerland",
        "Deutschland" to "Germany",
        "España" to "Spain",
        "France" to "France",
        "India" to "India",
        "Italia" to "Italia",
        "México" to "Mexico",
        "Nederland" to "Netherlands",
        "New Zealand" to "New Zealand",
        "Polska" to "Poland",
        "Singapore" to "Singapore",
        "South Africa" to "South Africa"
    )

}