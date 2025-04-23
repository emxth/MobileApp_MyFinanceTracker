package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val success = importTransactionsFromUri(requireContext(), it)
            val message = if (success) "Data imported successfully" else "Import failed"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set header title
        val headerTitle = view.findViewById<TextView>(R.id.pageTitle)
        headerTitle?.text = "Settings"

        val exceedSwitch = view.findViewById<SwitchMaterial>(R.id.budget_exceed_notification_switch)
        val reachingSwitch = view.findViewById<SwitchMaterial>(R.id.budget_exceed_reaching_notification_switch)
        val settingsPref = requireContext().getSharedPreferences("notification_settings", Context.MODE_PRIVATE)

        // Load switch states
        exceedSwitch.isChecked = settingsPref.getBoolean("notify_exceed", true)
        reachingSwitch.isChecked = settingsPref.getBoolean("notify_reaching", true)

        // Save changes
        exceedSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsPref.edit().putBoolean("notify_exceed", isChecked).apply()
        }
        reachingSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsPref.edit().putBoolean("notify_reaching", isChecked).apply()
        }

        // Setup Export and Import
        val btnExport = view.findViewById<Button>(R.id.btn_export)
        val btnImport = view.findViewById<Button>(R.id.btn_import)

        btnExport.setOnClickListener {
            if (exportTransactionsToFile(requireContext())) {
                Toast.makeText(requireContext(), "Data exported successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show()
            }
        }

        btnImport.setOnClickListener {
            importLauncher.launch(arrayOf("application/json")) // Allow user to pick a JSON file
        }
    }

    // Setup Export
    private fun exportTransactionsToFile(context: Context): Boolean {
        val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val gson = Gson()
        val allData = prefs.all
            .filterKeys { it.startsWith("transaction_") }

        return try {
            val json = gson.toJson(allData)
            val file = File(context.filesDir, "transactions_backup.json")
            file.writeText(json)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Setup import
    private fun importTransactionsFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() ?: "" }

            val gson = Gson()
            val type = object : TypeToken<Map<String, String>>() {}.type
            val data: Map<String, String> = gson.fromJson(json, type)

            val editor = context.getSharedPreferences("transactions", Context.MODE_PRIVATE).edit()
            for ((key, value) in data) {
                editor.putString(key, value)
            }
            editor.apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}