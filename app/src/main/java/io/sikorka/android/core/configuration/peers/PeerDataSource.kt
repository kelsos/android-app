package io.sikorka.android.core.configuration.peers

import io.sikorka.android.data.Result
import java.io.File

interface PeerDataSource {

  suspend fun peers(): Result<List<PeerEntry>>

  suspend fun savePeers(
    peers: List<PeerEntry>,
    merge: Boolean = false
  )

  suspend fun loadPeersFromUrl(url: String, merge: Boolean = true)

  suspend fun loadPeersFromFile(file: File, merge: Boolean = true)
}