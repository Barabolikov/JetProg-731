package com.example.jetprog

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform