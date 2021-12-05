package co.kr.sumai.voi

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.kr.sumai.databinding.RecyclerVoiceRecordItemBinding

class VoiceRecordRecyclerViewAdapter(
    val context: Context,
    private val recordList: MutableList<String>,)
    : RecyclerView.Adapter<VoiceRecordRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: RecyclerVoiceRecordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: String) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerVoiceRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(recordList[position])
    }

    override fun getItemCount() = recordList.size
}