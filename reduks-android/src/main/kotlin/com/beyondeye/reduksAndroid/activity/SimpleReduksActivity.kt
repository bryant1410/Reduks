package com.beyondeye.reduksAndroid.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.beyondeye.reduks.*
import com.beyondeye.reduksAndroid.activity.ActionRestoreState
import com.beyondeye.reduksAndroid.activity.ReduksActivity


/**
 * An activity base class for avoiding writing boilerplate code for initializing reduks and handling save and restoring reduks state
 * on onSaveInstanceState/onRestoreInstanceState activity life-cycle events
 * automatically handle save and restore of store state on activity recreation using a special custom action [ActionRestoreState]
 * Created by daely on 6/13/2016.
 */
abstract class SimpleReduksActivity<S>: ReduksActivity<S>, AppCompatActivity() {
    lateinit override var reduks: Reduks<S>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Kovenant with standard dispatchers for android (See http://kovenant.komponents.nl/android/config/)
        //startKovenant() //(before  initReduks()!!)
        reduks=initReduks()
    }

    override fun <T> storeCreator(): StoreCreator<T> = SimpleStore.Creator<T>()

    //override for making this function visible to inheritors
    override fun onStop() {
        super.onStop()
    }
    //override for making this function visible to inheritors
    override fun onStart() {
        super.onStart()
    }
    override fun onDestroy() {
        // Dispose of the Kovenant thread pools
        // for quicker shutdown you could use
        // force=true, which ignores all current
        // scheduled tasks
        // see  (See http://kovenant.komponents.nl/android/config/)
        //stopKovenant()
        super.onDestroy()
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        ActionRestoreState.saveReduksState(reduks,outState)
        super.onSaveInstanceState(outState)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        ActionRestoreState.restoreReduksState(reduks,savedInstanceState)
    }

}