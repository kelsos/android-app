package io.sikorka.android.ui.settings.peermanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.sikorka.android.core.configuration.peers.PeerDataSource
import io.sikorka.android.core.configuration.peers.PeerEntry
import io.sikorka.android.data.Result
import io.sikorka.android.events.Event
import io.sikorka.android.utils.schedulers.AppDispatchers
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.File

class PeerManagerViewModel(
  private val peerDataSource: PeerDataSource,
  private val dispatchers: AppDispatchers
) : ViewModel() {

  private val peers: MutableLiveData<List<PeerEntry>> = MutableLiveData()
  private val events: MutableLiveData<Event<Int>> = MutableLiveData()
  private var deferred: Deferred<Unit>? = null

  fun peers(): LiveData<List<PeerEntry>> {
    return peers
  }

  fun events(): LiveData<Event<Int>> {
    return events
  }

  fun load() {
    deferred = async(dispatchers.io) {
      val result = peerDataSource.peers()
      when (result) {
        is Result.Success -> peers.postValue(result.data)
        is Result.Failure -> events.postValue(Event(result.code))
      }
    }
  }

  fun save(peers: List<PeerEntry>) {
    launch(dispatchers.io) {
      peerDataSource.savePeers(peers)
    }
  }

  fun download(url: String, merge: Boolean) {
    launch(dispatchers.io) {
      peerDataSource.loadPeersFromUrl(url, merge)
    }
  }

  fun saveFromFile(file: File) {
    launch(dispatchers.io) {
      peerDataSource.loadPeersFromFile(file, true)
    }
  }
}