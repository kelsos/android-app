package io.sikorka.android.ui.accounts

import org.ethereum.geth.Account

interface AccountAdapterPresenter {
  fun setData(data: List<Account>)
  fun size(): Int
  fun item(position: Int): Account
  fun hex(position: Int): String
}