package io.sikorka.android.data.contracts

data class BoundContractData(
  val contractAddress: String,
  val name: String,
  val usesDetector: Boolean,
  val detectorAddress: String
)