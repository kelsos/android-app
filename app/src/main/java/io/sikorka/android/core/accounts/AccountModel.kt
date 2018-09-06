package io.sikorka.android.core.accounts

data class AccountModel(
  val addressHex: String,
  val ethBalance: Double = NO_BALANCE,
  val precision: Int = 3
) {
  companion object {
    const val NO_BALANCE = -1.0
  }
}