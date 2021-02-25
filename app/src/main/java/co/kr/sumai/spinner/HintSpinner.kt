package co.kr.sumai.spinner

import android.R
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HintSpinner<T>(content: Context?, resource: Int, objects: Array<T>?) : ArrayAdapter<Any?>(content!!, resource, objects!!) {
    override fun isEnabled(position: Int): Boolean {
        return position > 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: TextView = super.getDropDownView(position, convertView, parent) as TextView
        v.setTextColor(if(position > 0) Color.BLACK else Color.GRAY)
        return v
    }
}