package go.planner.plannergo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class TutorialActivityTitle : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
    }

    fun onStart(@Suppress("UNUSED_PARAMETER") v: View){
        startActivity(Intent(this, TutorialActivity::class.java))
    }

    fun onQuit(@Suppress("UNUSED_PARAMETER") v: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
