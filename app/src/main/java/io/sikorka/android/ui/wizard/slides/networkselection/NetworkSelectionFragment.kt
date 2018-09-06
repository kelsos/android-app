package io.sikorka.android.ui.wizard.slides.networkselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import io.sikorka.android.R
import io.sikorka.android.core.configuration.Network
import io.sikorka.android.ui.showShortSnack
import kotterknife.bindView
import org.koin.android.ext.android.inject

class NetworkSelectionFragment : Fragment() {

  private val ropstenSelection: TextView by bindView(R.id.network_selection__ropsten)
  private val mainnetSelection: TextView by bindView(R.id.network_selection__mainnet)
  private val rinkebySelection: TextView by bindView(R.id.network_selection__rinkeby)

  private val viewModel: NetworkSelectionViewModel by inject()

  override fun onStart() {
    super.onStart()
    updateNetworkSelection(viewModel.updateSelected())
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment__network_selection, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    mainnetSelection.setOnClickListener {
      mainnetSelection.showShortSnack(R.string.network_selection__network_not_available)
    }

    rinkebySelection.setOnClickListener {
      rinkebySelection.showShortSnack(R.string.network_selection__network_not_available)
    }

    ropstenSelection.setOnClickListener {
      val selectNetwork = viewModel.selectNetwork(Network.ROPSTEN)
      updateNetworkSelection(selectNetwork)
    }
  }

  private fun updateNetworkSelection(@Network.Selection network: Int) {
    ropstenSelection.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    rinkebySelection.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    mainnetSelection.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)

    val selection = when (network) {
      Network.ROPSTEN -> ropstenSelection
      Network.RINKEBY -> rinkebySelection
      Network.MAIN_NET -> mainnetSelection
      else -> null
    }

    val context = requireContext()
    var drawable = AppCompatResources.getDrawable(context, R.drawable.ic_check_black_24dp) ?: return
    drawable = DrawableCompat.wrap(drawable)
    DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.colorAccent))
    selection?.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    selection?.compoundDrawablePadding = 8
  }

  companion object {

    fun newInstance(): NetworkSelectionFragment {
      return NetworkSelectionFragment()
    }
  }
}