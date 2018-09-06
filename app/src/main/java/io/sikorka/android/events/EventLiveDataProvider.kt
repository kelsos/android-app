package io.sikorka.android.events

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class EventLiveDataProvider {
  private val events = MutableLiveData<Event<Any>>()

  fun post(event: Event<Any>) {
    events.postValue(event)
  }

  fun events(): LiveData<Event<Any>> {
    return events
  }

  fun observe(owner: LifecycleOwner, observer: (Event<Any>) -> Unit) {
    events.observe(owner, Observer { event: Event<Any>? ->
      if (event != null) {
        observer(event)
      }
    })
  }
}