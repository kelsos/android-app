package io.sikorka.android.data

sealed class Result<T> {
  class Success<T>(val data: T) : Result<T>()
  class Failure<T>(val code: Int = Codes.GENERIC_FAILURE) : Result<T>()

  object Codes {
    const val GENERIC_FAILURE = 10000
  }
}