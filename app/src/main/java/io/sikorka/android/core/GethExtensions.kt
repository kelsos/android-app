package io.sikorka.android.core

import org.ethereum.geth.*

fun Addresses.all() = object : Iterable<Address> {
  override fun iterator() = object : Iterator<Address> {
    private var current = 0L

    override fun hasNext(): Boolean = current < size()

    override fun next(): Address = get(current++)
  }
}


fun BigInts.values() = object : Iterable<BigInt> {
  override fun iterator() = object : Iterator<BigInt> {
    private var current = 0L

    override fun hasNext(): Boolean = current < size()

    override fun next(): BigInt = get(current++)
  }
}

fun Accounts.all() = object : Iterable<Account> {
  override fun iterator() = object : Iterator<Account> {
    private var current = 0L

    override fun hasNext(): Boolean = current < size()

    override fun next(): Account = get(current++)

  }
}

fun PeerInfos.all(): Iterable<PeerInfo> = object : Iterable<PeerInfo> {
  override fun iterator() = object : Iterator<PeerInfo> {
    var current = 0L
    override fun hasNext(): Boolean = current < size()

    override fun next(): PeerInfo = get(current++)
  }
}


