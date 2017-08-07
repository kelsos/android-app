package io.sikorka.test_geth.mvp

interface Presenter<in T : BaseView> {
  fun attach(view: T)

  fun detach()

  val isAttached: Boolean
}