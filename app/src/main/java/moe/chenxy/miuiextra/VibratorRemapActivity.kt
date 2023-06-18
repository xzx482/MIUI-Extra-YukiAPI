package moe.chenxy.miuiextra

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.MaterialToolbar
import rikka.preference.MainSwitchPreference
import rikka.widget.mainswitchbar.OnMainSwitchChangeListener

class VibratorRemapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.vibrator_map_title)


        try {
            // getSharedPreferences will hooked by LSPosed and change xml file path to /data/misc/edxp**
            // will not throw SecurityException
            getSharedPreferences("chen_vibrator_settings", Context.MODE_WORLD_READABLE)
        } catch (exception: SecurityException) {
            AlertDialog.Builder(this)
                .setMessage("Unsupported Xposed detected! Please install and active LSPosed!")
                .setPositiveButton(
                    "OK"
                ) { _: DialogInterface?, _: Int -> finish() }
                .setNegativeButton("Ignore", null)
                .show()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = "chen_vibrator_settings"
            setPreferencesFromResource(R.xml.vibrator_mapper_perf, rootKey)

            val remapSwitch = findPreference<MainSwitchPreference>("enable_vibrator_remap")
            val remapSettings = findPreference<PreferenceCategory>("map_range_category")
            if (!remapSwitch!!.isChecked) {
                preferenceScreen.removePreference(remapSettings!!)
            }

            val mainSwitchChangeListener = OnMainSwitchChangeListener { _, isChecked ->
                this.context?.let { ChenUtils.performVibrateHeavyClick(it) }
                if (!isChecked) {
                    preferenceScreen.removePreference(remapSettings!!)
                } else {
                    preferenceScreen.addPreference(remapSettings!!)
                }
            }
            remapSwitch.addOnSwitchChangeListener(mainSwitchChangeListener)

            for (index in 0 until remapSettings!!.preferenceCount) run {
                val preference: EditTextPreference =
                    remapSettings.getPreference(index) as EditTextPreference;
                preference.setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_CLASS_NUMBER
                }
                preference.setOnPreferenceChangeListener { _, newValue ->
                    this.context?.let {
                        try {
                            ChenUtils.performVibrateAnyIndex(it, newValue.toString().toInt())
                        } catch (e: Exception) {
                            Log.e("Art_Chen", "performVibrate failed! msg: " + e.message)
                            return@setOnPreferenceChangeListener false
                        }
                    }
                    return@setOnPreferenceChangeListener true
                }
            }
        }
    }
}