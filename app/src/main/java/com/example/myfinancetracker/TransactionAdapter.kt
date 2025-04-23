package com.example.myfinancetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onItemLongClick: (Transaction, Int, View) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_title)
        val category: TextView = view.findViewById(R.id.text_category)
        val date: TextView = view.findViewById(R.id.text_date)
        val amount: TextView = view.findViewById(R.id.text_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        // Store data into transaction item
        holder.title.text = transaction.title
        holder.category.text = transaction.category
        holder.date.text = transaction.date
        holder.amount.text = "LKR%,.2f".format(transaction.amount)

        // Set color
        if (transaction.type == "income") {
            holder.amount.setTextColor(holder.itemView.context.getColor(R.color.green))
        } else {
            holder.amount.setTextColor(holder.itemView.context.getColor(R.color.red))
        }

        // Long-press listener
        holder.itemView.setOnLongClickListener { view ->
            onItemLongClick(transaction, position, view)
            true
        }

    }

    override fun getItemCount(): Int = transactions.size
}