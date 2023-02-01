package com.example.projektwaluty

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log


class internetTest {

        private val TAG = internetTest::class.java.simpleName

        fun isInternetAvailable(context: Context): Boolean
        {
            val info = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            if (info == null)
            {
                return(false)
            }
            else
            {
                return(true)
            }

        }

}