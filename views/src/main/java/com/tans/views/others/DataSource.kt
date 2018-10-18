package com.tans.views.others

sealed class Option<out T> {

    abstract fun isEmpty(): Boolean

    data class Some<out T>(val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false

    }

    object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true
    }

    companion object {
        fun none() = None
    }
}

fun none() = Option.None

fun <A> A.some() = Option.Some(this)