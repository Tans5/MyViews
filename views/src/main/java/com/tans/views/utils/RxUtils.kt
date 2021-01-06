package com.tans.views.utils

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private class WeakCallBack<T>(val completeCheck: (T) -> Boolean = { false }): (T) -> Unit {
    var emitterRef: ObservableEmitter<T>? = null

    override fun invoke(p1: T) {
        emitterRef?.onNext(p1)
        if (completeCheck(p1)) {
            emitterRef?.onComplete()
        }
    }
}

private class CacheAllWeakCallBack<T>(val completeCheck: (T) -> Boolean = { false }): (T) -> Unit {
    var emitterRef: ObservableEmitter<T>? = null
    val isStarted = AtomicBoolean(false)
    val beforeQueue: Queue<T> = ArrayDeque()

    override fun invoke(p1: T) {
        val emitter = emitterRef
        if (emitter == null) {
            if (!isStarted.get()) beforeQueue.offer(p1)
            return
        }

        if (beforeQueue.isNotEmpty()) {
            var e: T? = beforeQueue.poll()
            while (e != null) {
                emitter.onNext(e)
                if (completeCheck(p1)) emitter.onComplete()
                e= beforeQueue.poll()
            }
        }
        emitter.onNext(p1)
        if (completeCheck(p1)) {
            emitter.onComplete()
        }
    }
}

fun <T> callToRx(completeCheck: (T) -> Boolean = { false }, ignoreItemsBeforeCreate: Boolean = false): Pair<(T) -> Unit, Observable<T>> {
    return if (ignoreItemsBeforeCreate){
        val callback = WeakCallBack<T>(completeCheck)
        val obs: Observable<T> = Observable.create<T> { emitter ->
            callback.emitterRef  = emitter
        }.doFinally { callback.emitterRef = null }

        callback to obs
    } else {
        val callback = CacheAllWeakCallBack<T>(completeCheck)
        val obs: Observable<T> = Observable.create<T> { emitter ->
            callback.emitterRef  = emitter
            callback.isStarted.set(true)
        }.doFinally { callback.emitterRef = null }

        callback to obs
    }
}