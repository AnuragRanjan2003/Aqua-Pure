package com.example.waterquality

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.waterquality.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import models.User

class MainActivity : AppCompatActivity(), Communicator {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var header: View
    private lateinit var dataRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        val nav = findViewById<NavigationView>(R.id.nav)
        header = nav.getHeaderView(0)
        val user: FirebaseUser = mAuth.currentUser!!
        dataRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, binding.drawer, R.string.nav_open, R.string.nav_close)
        binding.drawer.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dataRef.get().addOnSuccessListener {
            val user2 = it.getValue(User::class.java)!!
            setViewData(user2)

        }
            .addOnFailureListener {
                Snackbar.make(binding.root, it.message.toString(), Snackbar.LENGTH_LONG).show()
            }




        switchFragment(InfoFragment())
        binding.nav.setNavigationItemSelectedListener {
            it.isChecked = true
            when (it.itemId) {
                R.id.home -> switchFragment(ImageFragment())
                R.id.profile -> switchFragment(InfoFragment())
                R.id.log_out -> logOut()
            }
            true
        }


    }


    private fun setViewData(user: User) {
        header.findViewById<TextView>(R.id.head_name).text = user.name
        header.findViewById<TextView>(R.id.head_email).text = user.email
        Glide.with(this).load(user.imageUrl)
            .into(header.findViewById<CircleImageView>(R.id.head_profile_image))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    private fun logOut() {
        mAuth.signOut()
        startActivity(Intent(this, AskActivity::class.java))
        finishAffinity()
    }

    private fun switchFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        binding.drawer.closeDrawers()
    }

    override fun passUri(url: String, uri: Uri) {
        val bundle = Bundle()
        bundle.putString("url", url)
        bundle.putString("uri", uri.toString())
        val trans = this.supportFragmentManager.beginTransaction()

        val frag2 = AnalysisFragment()

        frag2.arguments = bundle
        trans.replace(binding.frame.id, frag2)
        trans.commit()
    }

    override fun passBack() {
        val bundle = Bundle()
        val transaction = supportFragmentManager.beginTransaction()
        val frag1 = ImageFragment()
        frag1.arguments = bundle
        transaction.replace(binding.frame.id, frag1)
        transaction.commit()
    }


}