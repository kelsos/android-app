package io.sikorka.android.events


import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class RxBusImpl
@Inject
constructor() : RxBus {
  private val serializedRelay = PublishRelay.create<Any>().toSerialized()
  private val activeSubscriptions = HashMap<Any, MutableList<Disposable>>()

  @Suppress("UNCHECKED_CAST")
  override fun <T> register(receiver: Any, eventClass: Class<T>, onNext: (T) -> Unit) {
    //noinspection unchecked
    val subscription = serializedRelay.filter {
      it.javaClass == eventClass
    }.map { obj -> obj as T }.subscribe(onNext)

    updateSubscriptions(receiver, subscription)
  }

  override fun <T> register(receiver: Any, eventClass: Class<T>, main: Boolean, onNext: (T) -> Unit) {
    val subscription = register(eventClass, true, onNext)
    updateSubscriptions(receiver, subscription)
  }

  private fun updateSubscriptions(receiver: Any, subscription: Disposable) {
    val subscriptions: MutableList<Disposable> = activeSubscriptions[receiver] ?: LinkedList<Disposable>()
    subscriptions.add(subscription)
    activeSubscriptions.put(receiver, subscriptions)
  }

  override fun unregister(receiver: Any) {
    val subscriptions = activeSubscriptions.remove(receiver)
    if (subscriptions != null) {
      Observable.fromIterable(subscriptions).filter { !it.isDisposed }.subscribe { it.dispose() }
    }
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> register(eventClass: Class<T>, main: Boolean, onNext: (T) -> Unit): Disposable {
    //noinspection unchecked
    val observable = serializedRelay.filter { it.javaClass == eventClass }.map { obj -> obj as T }
    val scheduler = if (main) AndroidSchedulers.mainThread() else Schedulers.trampoline()
    return observable.observeOn(scheduler).subscribe(onNext)
  }

  override fun post(event: Any) {
    serializedRelay.accept(event)
  }
}