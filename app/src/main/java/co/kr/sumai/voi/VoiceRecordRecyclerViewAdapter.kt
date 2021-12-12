package co.kr.sumai.voi

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import co.kr.sumai.databinding.DialogVoiDeleteBinding
import co.kr.sumai.databinding.RecyclerVoiceRecordItemBinding
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.Record
import co.kr.sumai.net.voi.RecordDeleteRequest
import co.kr.sumai.net.voiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URLEncoder

class VoiceRecordRecyclerViewAdapter(
    val context: Context,
    private val recordList: MutableList<Record>,
    private var mediaPlayer: MediaPlayer?,
    private val deleteItem: (Int) -> Unit)
    : RecyclerView.Adapter<VoiceRecordRecyclerViewAdapter.ViewHolder>() {

    private val userID = loadPreferences(context, "loginData", "id")

    private var isLoading = false
    private var isPlay = false

    inner class ViewHolder(private val binding: RecyclerVoiceRecordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: Record, position: Int) {
            binding.recordIdx.text = record.item_idx.toString()
            binding.recordSentence.text = record.sentence

            binding.btnDelete.setOnClickListener {
                val binding = DialogVoiDeleteBinding.inflate(LayoutInflater.from(context))
                val dialog = AlertDialog.Builder(context).setView(binding.root).create()

                binding.title.text = "녹음을 삭제하시겠습니까?"
                binding.description.visibility = View.GONE
                binding.message.text = "${record.item_idx}. ${record.sentence}"
                binding.negativeBtn.setOnClickListener {
                    dialog.dismiss()
                }
                binding.positiveBtn.setOnClickListener Dialog@ {
                    if (isLoading) return@Dialog

                    requestRecordDelete(record.idx) {
                        dialog.dismiss()
                        deleteItem(position)
                    }
                }

                dialog.show()
            }
            binding.btnPlay.setOnClickListener {
                playVoice(record.record_file_name)
            }
            binding.btnReRecord.setOnClickListener {
                val intent = Intent(context, SpeechRecordActivity::class.java)
                context.startActivity(intent)
            }
        }

        private fun playVoice(rawUrl: String) {
            // Need Sync nodejs(database), voi-web, mobile
            val url = URLEncoder.encode(rawUrl.substring(rawUrl.lastIndexOf("https://")), "EUC-KR")
                .replace("%3A", ":").replace("%2F", "/")

            if (isPlay) {
                switchPlaying(false)
                mediaPlayer?.stop()
                isPlay = false
                return
            }

            if (mediaPlayer != null) {
                mediaPlayer?.release()
                mediaPlayer = null
            }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepare()
                switchPlaying(true)
                start()
                setOnCompletionListener {
                    switchPlaying(false)
                }
            }
        }

        private fun switchPlaying(toggle: Boolean) {
            if(toggle) {
                binding.btnPlay.text = "정지"
                isPlay = true
            } else {
                binding.btnPlay.text = "듣기"
                isPlay = false
            }
        }

        private fun requestRecordDelete(idx: Int, execute: () -> Unit) {
            isLoading = true
            val res: Call<Unit> = voiService.requestRecordDelete(RecordDeleteRequest(userID, idx.toString()))

            res.enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        execute()
                    } else {
                        Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerVoiceRecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(recordList[position], position)
    }

    override fun getItemCount() = recordList.size
}