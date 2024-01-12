package com.example.playlist_maker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.playlist_maker.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {

    private lateinit var settingBinding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(settingBinding.root)

        settingBinding.settingsToolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        settingBinding.shareTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.support_share_link))
            intent.type = "text/plain"
            try {
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@SettingsActivity,
                    getString(R.string.not_found_app),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        settingBinding.callTextView.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SENDTO)
            sendIntent.data = Uri.parse("mailto:")
            sendIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_mail)))
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject))
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message))
            try {
                startActivity(sendIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@SettingsActivity,
                    getString(R.string.not_found_app),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        settingBinding.agreementTextView.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.agreement_link))
            try {
                startActivity(sendIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this@SettingsActivity,
                    getString(R.string.not_found_app),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val sharedPrefs = getSharedPreferences(App.PLAYLIST_MAKER_SHARED_PREFS, MODE_PRIVATE)
        settingBinding.switch1.isChecked = (applicationContext as App).darkTheme
        settingBinding.switch1.setOnCheckedChangeListener { _, checked ->
            (applicationContext as App).switchTheme(checked)
            sharedPrefs.edit { putBoolean(App.DARK_THEME_KEY, checked) }
        }
    }
}