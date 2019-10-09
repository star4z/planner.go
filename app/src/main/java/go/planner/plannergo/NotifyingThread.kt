package go.planner.plannergo

import android.util.Log
import java.util.concurrent.CopyOnWriteArraySet

abstract class NotifyingThread : Thread() {
    private val listeners: MutableSet<ThreadCompleteListener> = CopyOnWriteArraySet<ThreadCompleteListener>()
    fun addListener(listener: ThreadCompleteListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ThreadCompleteListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        for (listener in listeners) {
            listener.notifyOfThreadComplete(this)
        }
    }

    override fun run() {
        Log.d("NotifyingThread", "running")
        try {
            doRun()
        } finally {
            notifyListeners()
        }
    }

    abstract fun doRun()
}