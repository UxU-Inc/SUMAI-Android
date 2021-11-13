package co.kr.sumai.voi

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import co.kr.sumai.R
import co.kr.sumai.databinding.RecyclerModelItemBinding
import co.kr.sumai.func.AvatarSettings
import co.kr.sumai.net.voi.VoiceModel
import com.bumptech.glide.Glide

class ModelRecyclerViewAdapter(
    val context: Context,
    private val modelList: MutableList<VoiceModel>,
    private val notifyChanged: (String) -> Unit)
    : RecyclerView.Adapter<ModelRecyclerViewAdapter.ViewHolder>() {

    private var selectedPos = RecyclerView.NO_POSITION

    private val avatar = AvatarSettings()

    inner class ViewHolder(private val binding: RecyclerModelItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(model: VoiceModel) {
            if (model.model_image.isNotEmpty()) {  // 프로필 이미지 있으면
                Glide.with(context)
                    .load(model.model_image)
                    .circleCrop()
                    .into(binding.modelImage)
            } else {  // 프로필 이미지 없으면
                val drawable = ContextCompat.getDrawable(context, R.drawable.circle)?.mutate() as GradientDrawable
                drawable.setColor(Color.parseColor("#" + avatar.toMD5(model.id).substring(1, 7)))

                Glide.with(context)
                    .load(drawable)
                    .circleCrop()
                    .into(binding.modelImage)

                binding.modelOwner.text = avatar.reName(model.user_name)
            }

            binding.modelName.text = model.model_name

            binding.root.setOnClickListener {
                notifyItemChanged(selectedPos)
                selectedPos = layoutPosition
                notifyChanged(model.model_idx)
                notifyItemChanged(selectedPos)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerModelItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (selectedPos == RecyclerView.NO_POSITION) {
            selectedPos = 0
            notifyChanged(modelList[0].model_idx)
        }
        viewHolder.itemView.isSelected = selectedPos == position
        viewHolder.bind(modelList[position])
    }

    override fun getItemCount() = modelList.size
}