package go.planner.plannergo

interface ThreadCompleteListener {
    fun notifyOfThreadComplete(thread: Thread?)
}