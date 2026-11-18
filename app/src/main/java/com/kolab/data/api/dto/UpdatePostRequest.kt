package com.kolab.data.api.dto

data class UpdatePostRequest(
    val description: String? = null,
    val location: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
    val filter: String? = null,
    val isStudioOrGear: Boolean? = null
)
