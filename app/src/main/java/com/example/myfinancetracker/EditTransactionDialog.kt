package com.example.myfinancetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

//class EditTransactionDialog : BottomSheetDialogFragment() {
//    companion object {
//        fun newInstance(transaction: Transaction) = EditTransactionDialog().apply {
//            arguments = Bundle().apply {
//                putParcelable("transaction", transaction)
//            }
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        val view = inflater.inflate(R.layout.dialog_edit_transaction, container, false)
//        val transaction = arguments?.getParcelable<Transaction>("transaction")!!
//
//        // Pre-fill fields with transaction data
//        view.findViewById<EditText>(R.id.edit_title).setText(transaction.title)
//        // ... other fields
//
//        view.findViewById<Button>(R.id.btn_save).setOnClickListener {
//            // Handle save logic
//            dismiss()
//        }
//
//        return view
//    }
//}