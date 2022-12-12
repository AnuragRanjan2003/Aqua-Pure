package com.example.waterquality

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.waterquality.databinding.ActivityMainBinding
import com.example.waterquality.databinding.NavHeaderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import models.User2

class MainActivity : AppCompatActivity() , Communicator {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var header: NavHeaderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        header = NavHeaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        val user: FirebaseUser = mAuth.currentUser!!
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, binding.drawer, R.string.nav_open, R.string.nav_close)
        binding.drawer.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val user2 =
            User2(user.displayName.toString(), user.email.toString(), user.photoUrl, user.uid)

        header.userdata = user2
        Glide.with(this).load(user2.photo).into(header.profileImage)

        switchFragment(InfoFragment())
        binding.nav.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> switchFragment(ImageFragment())
                R.id.profile -> switchFragment(InfoFragment())
                R.id.log_out -> logOut()
            }
            true
        }

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

    override fun passUri(uri: String) {
        val bundle = Bundle()
        bundle.putString("url",uri)
        val trans = this.supportFragmentManager.beginTransaction()

        val frag2 = AnalysisFragment()

        frag2.arguments = bundle
        trans.replace(binding.frame.id,frag2)
        trans.commit()
    }
}