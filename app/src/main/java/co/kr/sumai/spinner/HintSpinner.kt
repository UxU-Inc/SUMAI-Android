package co.kr.sumai.spinner

import android.R
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HintSpinner<T>(content: Context?, resource: Int, objects: Array<T>?) : ArrayAdapter<Any?>(content!!, resource, objects!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = super.getView(position, convertView, parent)
        if (position == count) {
            (v.findViewById<View>(R.id.text1) as TextView).text = ""
            (v.findViewById<View>(R.id.text1) as TextView).hint = getItem(count) as CharSequence?
        }
        return v
    }

    override fun getCount(): Int {
        return super.getCount() - 1
    }
}