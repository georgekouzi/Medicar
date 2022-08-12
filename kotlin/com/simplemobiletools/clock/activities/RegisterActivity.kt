package com.simplemobiletools.clock.activities
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.extensions.config
import com.simplemobiletools.clock.helpers.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    lateinit var fullName: EditText
    lateinit var phoneNumber: EditText
    lateinit var email: EditText
    lateinit var age: EditText
    lateinit var nextBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        config.useEnglish =true

        window.statusBarColor = Color.parseColor("#F8FBF2")

        fullName = findViewById(R.id.name)
        phoneNumber = findViewById(R.id.phone)
        email = findViewById(R.id.email)
        age = findViewById(R.id.age)
        nextBtn = findViewById(R.id.btn_step)

        createView()

        nextBtn.setOnClickListener {
            createPreferences()
        }

    }

    private fun createView() {
        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        fullName.text.append(preferences.getString(USER_NAME,""))
        phoneNumber.text.append(preferences.getString(USER_PHONE,""))
        email.text.append(preferences.getString(USER_EMAIL,""))
        age.text.append(preferences.getString(USER_AGE,""))



    }

    private fun createPreferences() {
        when {
            fullName.text.isEmpty() -> Toast.makeText(this, "Please write your full name", Toast.LENGTH_SHORT).show()
            phoneNumber.text.isEmpty() -> Toast.makeText(this, "Please write your phone", Toast.LENGTH_SHORT).show()
            age.text.isEmpty() -> Toast.makeText(this, "Please write your age", Toast.LENGTH_SHORT).show()
            email.text.isEmpty() -> Toast.makeText(this, "Please write your email", Toast.LENGTH_SHORT).show()
            else -> {
                saveInMemory()
            }

        }

    }



    private fun saveInMemory() {

        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        preferences.edit().putString(USER_NAME, fullName.text.toString()).commit()
        preferences.edit().putString(USER_PHONE, phoneNumber.text.toString()).commit()
        preferences.edit().putString(USER_AGE, age.text.toString()).commit()
        preferences.edit().putString(USER_EMAIL, email.text.toString()).commit()

        createContactView()
        createContact()


    }

    private fun createContact() {
        btn_step.setOnClickListener {
            when {
                fullName.text.isEmpty() -> Toast.makeText(this, "Please write your full name", Toast.LENGTH_SHORT).show()
                phoneNumber.text.isEmpty() -> Toast.makeText(this, "Please write your phone", Toast.LENGTH_SHORT).show()
                !iron_switch.isChecked && !image_switch.isChecked -> Toast.makeText(this, "You must check at least one option", Toast.LENGTH_SHORT).show()
                email.text.isEmpty() -> Toast.makeText(this, "Please write your email", Toast.LENGTH_SHORT).show()
                else -> saveContactInMemory()

            }

        }

    }

    private fun saveContactInMemory() {
        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        preferences.edit().putString(USER_CONTACT_NAME, fullName.text.toString()).commit()
        preferences.edit().putString(USER_CONTACT_PHONE, phoneNumber.text.toString()).commit()
        preferences.edit().putString(USER_CONTACT_EMAIL, email.text.toString()).commit()

        preferences.edit().putBoolean(IMAGE_PROOF, image_switch.isChecked).commit()
        preferences.edit().putBoolean(PILL_BOX_PROOF, iron_switch.isChecked).commit()
        finishRegister()

    }

    private fun finishRegister() {
        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        preferences.edit().putBoolean(FIRST_TIME,false).commit()

        finish()


    }

    private fun createContactView() {
        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        titale.text = "Contact Information"
        cv_age.visibility = View.GONE
        iron_switch.visibility=View.VISIBLE
        image_switch.visibility=View.VISIBLE

        btn_step.text = "Finish"

        fullName.text.clear()
        phoneNumber.text.clear()
        email.text.clear()

        fullName.text.append(preferences.getString(USER_CONTACT_NAME,""))
        phoneNumber.text.append(preferences.getString(USER_CONTACT_PHONE,""))
        email.text.append(preferences.getString(USER_CONTACT_EMAIL,""))
    }
}
