package go.planner.plannergo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class TutorialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
    }

    fun onStart(v: View){
        startActivity(Intent(this, TutorialActivity2::class.java))
    }

    fun onQuit(v: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
