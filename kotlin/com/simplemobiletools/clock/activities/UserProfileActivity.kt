package com.simplemobiletools.clock.activities
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.simplemobiletools.clock.R
import com.simplemobiletools.clock.helpers.*
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {
    lateinit var fullName: TextView
    lateinit var phoneNumber: TextView
    lateinit var email: TextView
    lateinit var age: TextView

    lateinit var fullName_c: TextView
    lateinit var phoneNumber_c: TextView
    lateinit var email_c: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        var tollBar : Toolbar = findViewById(R.id.tool_bar)
        menageToolBar(tollBar)
        window.statusBarColor = Color.parseColor("#F8FBF2")
        fullName = findViewById(R.id.name)
        phoneNumber = findViewById(R.id.phone)
        email = findViewById(R.id.email)
        age = findViewById(R.id.age)

        fullName_c = findViewById(R.id.c_name)
        phoneNumber_c = findViewById(R.id.c_phone)
        email_c = findViewById(R.id.c_email)

        fillBoxText()

        btn_edit.setOnClickListener {
            Intent(this, RegisterActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }

//            var temp = fullName.text
//            fullName.editableText
//            fullName.compoundDrawableTintList






    }

    private fun fillBoxText() {
        val preferences = getSharedPreferences(USER, MODE_PRIVATE)

        fullName.text = preferences.getString(USER_NAME,"")
        phoneNumber.text = preferences.getString(USER_PHONE,"")
        email.text = preferences.getString(USER_EMAIL,"")
        age.text = preferences.getString(USER_AGE,"")

        fullName_c.text = preferences.getString(USER_CONTACT_NAME,"")
        phoneNumber_c.text = preferences.getString(USER_CONTACT_PHONE,"")
        email_c.text = preferences.getString(USER_CONTACT_EMAIL,"")


    }


    private fun menageToolBar(tollBar: Toolbar) {
        setSupportActionBar(tollBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        tollBar.setNavigationOnClickListener {
            onBackPressed()
        }

    }

}
