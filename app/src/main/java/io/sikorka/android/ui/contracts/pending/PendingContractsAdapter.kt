package io.sikorka.android.ui.contracts.pending

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.sikorka.android.R
import io.sikorka.android.data.contracts.pending.PendingContract
import io.sikorka.android.helpers.fail
import io.sikorka.android.ui.bind


class PendingContractsAdapter : RecyclerView.Adapter<PendingContractsAdapter.PendingContractsViewHolder>() {

  private var data: List<PendingContract> = emptyList()

  override fun onBindViewHolder(holder: PendingContractsViewHolder?, position: Int) {
    holder?.apply {
      bindContract(data[adapterPosition])
    }
  }

  override fun getItemCount(): Int = data.size

  fun update(data: List<PendingContract>) {
    this.data = data
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PendingContractsViewHolder {
    val parentView = parent ?: fail("parent was not supposed to be null")
    val inflater = LayoutInflater.from(parentView.context)
    val view = inflater.inflate(R.layout.item__pending_contract, parentView, false)
    return PendingContractsViewHolder(view)
  }


  class PendingContractsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val address: TextView by itemView.bind(R.id.pending_contract__contract_address)

    private val transaction: TextView by itemView.bind(R.id.pending_contract__contract_transaction)

    fun bindContract(contract: PendingContract) {
      address.text = contract.contractAddress
      transaction.text = contract.transactionHash
    }
  }
}
