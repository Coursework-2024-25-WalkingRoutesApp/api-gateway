package ru.hse.api_gateway.dto

data class UserDto(
    var username: String,
    var email: String,
    var photoUrl: String?
)
