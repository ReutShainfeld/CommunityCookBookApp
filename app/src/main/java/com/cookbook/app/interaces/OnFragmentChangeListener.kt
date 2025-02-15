package com.cookbook.app.interaces

import android.os.Bundle

interface OnFragmentChangeListener {
    fun navigateToFragment(fragmentId: Int,popUpId:Int=0,args: Bundle?=null)
}