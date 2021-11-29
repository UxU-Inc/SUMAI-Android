package co.kr.sumai.voi

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.kr.sumai.R
import co.kr.sumai.databinding.DialogVoiDeleteBinding
import co.kr.sumai.databinding.RecyclerCreateModelItemBinding
import co.kr.sumai.func.AvatarSettings
import co.kr.sumai.func.loadPreferences
import co.kr.sumai.net.voi.ModelDeleteRequest
import co.kr.sumai.net.voi.ModelDeleteResponse
import co.kr.sumai.net.voi.VoiceModel
import co.kr.sumai.net.voiService
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CreateModelRecyclerViewAdapter(
    val context: Context,
    private val modelList: MutableList<VoiceModel?>,
    private val refresh: () -> Unit)
    : RecyclerView.Adapter<CreateModelRecyclerViewAdapter.ViewHolder>() {

    private val avatar = AvatarSettings()

    private val userID = loadPreferences(context, "loginData", "id")
    private val userName = loadPreferences(context, "loginData", "name")

    private var isLoading = false

    inner class ViewHolder(private val binding: RecyclerCreateModelItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: VoiceModel?) {
            if (model == null) return
            if (model.model_image.isNullOrEmpty() && model.user_image.isNullOrEmpty()) {  // 모델, 프로필 이미지 없으면
                val drawable = ContextCompat.getDrawable(context, R.drawable.circle)?.mutate() as GradientDrawable
                drawable.setColor(Color.parseColor("#" + avatar.toMD5(userID).substring(1, 7)))

                Glide.with(context)
                    .load(drawable)
                    .circleCrop()
                    .into(binding.modelImage)

                binding.modelOwner.text = avatar.reName(userName)
            } else {  // 모델, 프로필 이미지 있으면
                Glide.with(context)
                    .load(model.model_image ?: model.user_image)
                    .circleCrop()
                    .into(binding.modelImage)
            }
            binding.modelName.text = model.model_name

            // 삭제 상태면 시간 표시
            if(!(model.model_delete_time.isNullOrEmpty() || model.model_delete_date.isNullOrEmpty())) {
                binding.root.setOnClickListener(null)
                binding.deleteLayout.visibility = View.VISIBLE

                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val modelDeleteDate = format.parse(model.model_delete_date)?.time!!
                val serverCurrentTime = format.parse(model.server_current_time)?.time!!

                val remainTime = modelDeleteDate - serverCurrentTime
                object : CountDownTimer(remainTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        var time = millisUntilFinished / 1000
                        val days = time / 86400
                        time %= 86400
                        val hours = time / 3600
                        time %= 3600
                        val minutes = time / 60
                        time %= 60
                        val seconds = time

                        val initTime = "${days}일 ${hours}시 ${minutes}분 ${seconds}초"
                        binding.remainTime.text = initTime
                    }

                    override fun onFinish() {
                        Handler(Looper.getMainLooper()).postDelayed({
                            refresh()
                        }, 3000)
                    }
                }.start()
            } else { // 삭제 상태가 아니면
                binding.root.setOnClickListener {
                    val intent = Intent(context, ModelSettingsActivity::class.java)
                    context.startActivity(intent)
                }
                binding.deleteLayout.visibility = View.INVISIBLE
            }

            // 삭제 다이얼로그
            fun showDialog(cancel: Boolean) {
                val binding = DialogVoiDeleteBinding.inflate(LayoutInflater.from(context))
                val dialog = AlertDialog.Builder(context)
                    .setView(binding.root)
                    .create()

                binding.title.text = if (cancel) "삭제를 취소하시겠습니까?" else "정말 삭제하시겠습니까?"
                binding.message.text = model.model_name
                binding.negativeBtn.setOnClickListener {
                    dialog.dismiss()
                }
                binding.positiveBtn.setOnClickListener {
                    if (isLoading) return@setOnClickListener

                    requestModelDelete(model.model_idx, cancel) {
                        dialog.dismiss()
                        refresh()
                    }
                }

                dialog.show()
            }
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener { showDialog(cancel = false) }
            binding.btnDeleteCancel.setOnClickListener { showDialog(cancel = true) }
        }

        private fun requestModelDelete(modelIdx: String, cancel: Boolean, execute: () -> Unit) {
            isLoading = true
            val res: Call<ModelDeleteResponse> =
                if (cancel) voiService.getModelDeleteCancel(ModelDeleteRequest(userID, modelIdx))
                else voiService.getModelDelete(ModelDeleteRequest(userID, modelIdx))

            res.enqueue(object : Callback<ModelDeleteResponse> {
                override fun onResponse(call: Call<ModelDeleteResponse>, response: Response<ModelDeleteResponse>) {
                    if (response.isSuccessful && response.body()?.code == 20100) {
                        if (!cancel) Toast.makeText(context, "해당 시간이 지난 후에 모델이 삭제됩니다.", Toast.LENGTH_SHORT).show()
                        execute()
                    } else {
                        Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<ModelDeleteResponse>, t: Throwable) {
                    Toast.makeText(context, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerCreateModelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(modelList[position])
    }

    override fun getItemCount() = modelList.size
}