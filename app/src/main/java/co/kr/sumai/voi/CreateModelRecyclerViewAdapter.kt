package co.kr.sumai.voi

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.kr.sumai.R
import co.kr.sumai.databinding.RecyclerCreateModelItemBinding
import co.kr.sumai.func.AvatarSettings
import co.kr.sumai.net.voi.VoiceModel
import com.bumptech.glide.Glide

class CreateModelRecyclerViewAdapter(
    val context: Context,
    private val modelList: MutableList<VoiceModel?>)
    : RecyclerView.Adapter<CreateModelRecyclerViewAdapter.ViewHolder>() {

    private val avatar = AvatarSettings()

    inner class ViewHolder(private val binding: RecyclerCreateModelItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: VoiceModel?) {
            if (model == null) return
            if (model.model_image.isNullOrEmpty()) {  // 프로필 이미지 없으면
//                val drawable = ContextCompat.getDrawable(context, R.drawable.circle)?.mutate() as GradientDrawable
//                drawable.setColor(Color.parseColor("#" + avatar.toMD5(model.id).substring(1, 7)))
//
//                Glide.with(context)
//                    .load(drawable)
//                    .circleCrop()
//                    .into(binding.modelImage)
//
//                binding.modelOwner.text = avatar.reName(model.user_name)
            } else {  // 프로필 이미지 있으면
                Glide.with(context)
                    .load(model.model_image)
                    .circleCrop()
                    .into(binding.modelImage)
            }
            binding.root.setOnClickListener {
                Log.e("asdf", "click")
            }
            binding.btnDelete.setOnClickListener {
                Log.e("asdf", "delete")
            }

            binding.modelName.text = model.model_name
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