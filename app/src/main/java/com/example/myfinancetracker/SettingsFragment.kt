package com.example.myfinancetracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import com.example.myfinancetracker.databinding.FragmentSettingsBinding
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.AppDatabase
import data.entity.NotificationSettingsEntity
import kotlinx.coroutines.launch
import util.SessionManager
import java.io.File

@SuppressLint("StaticFieldLeak")
private var _binding: FragmentSettingsBinding? = null
private val binding get() = _binding!!

class SettingsFragment : Fragment() {
    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            val success = importTransactionsFromUri(requireContext(), it)
            val message = if (success) "Data imported successfully" else "Import failed"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val headerTitle = view.findViewById<TextView>(R.id.pageTitle)
        headerTitle.text = "Settings"

        val exceedSwitch = view.findViewById<SwitchMaterial>(R.id.budget_exceed_notification_switch)
        val reachingSwitch = view.findViewById<SwitchMaterial>(R.id.budget_exceed_reaching_notification_switch)

        val db = AppDatabase.getDatabase(requireContext())
        val settingsDao = db.notificationSettingsDao()
        val userId = SessionManager.getUserId(requireContext())

        lifecycleScope.launch {
            val settings = settingsDao.getSettings(userId)
            exceedSwitch.isChecked = settings?.notifyExceed ?: true
            reachingSwitch.isChecked = settings?.notifyReaching ?: true
        }

        exceedSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val current = settingsDao.getSettings(userId)
                val updated = NotificationSettingsEntity(
                    userId = userId,
                    notifyExceed = isChecked,
                    notifyReaching = current?.notifyReaching ?: true
                )
                settingsDao.insertOrUpdate(updated)
            }
        }

        reachingSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val current = settingsDao.getSettings(userId)
                val updated = NotificationSettingsEntity(
                    userId = userId,
                    notifyExceed = current?.notifyExceed ?: true,
                    notifyReaching = isChecked
                )
                settingsDao.insertOrUpdate(updated)
            }
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

        binding.textBtnLogout.setOnClickListener {
            SessionManager.clearSession(requireContext())
            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }

    companion object {}
}