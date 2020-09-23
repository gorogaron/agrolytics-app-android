package com.agrolytics.agrolytics_android.ui.images.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.utils.BitmapUtils
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.extensions.animateSlide
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_images.view.*


class ImagesAdapter(private val listener: OnImageListener,
                    private val savedLength: Float): RecyclerView.Adapter<ImagesAdapter.ImagesHolder>() {

	var itemList = mutableListOf<ImageItem>()
	var deleteShown : Boolean = false
	lateinit var slidingNeeded : BooleanArray

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesHolder {
		val view = LayoutInflater.from(parent.context)
				.inflate(R.layout.item_images, parent, false)
		return ImagesHolder(view)
	}

	override fun onBindViewHolder(holder: ImagesHolder, position: Int) {
		holder.bind(itemList[position], position)

		holder.mView.setOnClickListener {
			if (deleteShown) {
				setImageItemSelected(holder.item)
				listener.onImageSelected(holder.item)
			}
		}

		holder.mView.setOnLongClickListener {
			holder.item?.let { listener.onLongClicked(it) }
			true
		}

		holder.itemView.iv_image.setOnClickListener {
			holder.item?.let { listener.showImage(it) }
		}
	}

	override fun getItemCount(): Int {
		return itemList.size
	}

	inner class ImagesHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
		var item: ImageItem? = null

		fun bind(item: ImageItem, position: Int) = with(itemView) {
			this@ImagesHolder.item = item

			if (position % 2 == 0) {
				itemView.images_adapter_root_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.lightAdapterColor))
			} else {
				itemView.images_adapter_root_layout.setBackgroundColor(ContextCompat.getColor(context, R.color.darkAdapterColor))
			}

			if (item.isPushedToServer) {
				item.length?.let {
					itemView.tv_volume_adapter.text =
						context.getString(R.string.wood_volume_value, ((item.volume ?: 1.0) * (item.length ?: 0.0)))
				}
			} else {
				itemView.tv_volume_adapter.text = "-"
			}


			check_box.isChecked = item.isChecked

			itemView.tv_time_image_created_on.text = item.time
//			itemView.tv_length_new.text = item.length.toString()
			itemView.tv_length_new.text = String.format("%.2f", item.length)
			itemView.tv_woodtype_adapter.text = item.woodType
			//itemView.tv_adapter_id.text = item.id
			itemView.tv_adapter_id.visibility = View.GONE
			item.serverImage?.let {
				if (it.isEmpty()) {
					Glide.with(context).load(item.localPath).into(itemView.iv_image)
				} else {
					if (it.startsWith("https",true)) {
						Glide.with(context).load(it).into(itemView.iv_image)
					} else {
						Glide.with(context).load(BitmapUtils.getImage(item.serverImage)).into(itemView.iv_image)
					}
				}
			} ?: run {
				Glide.with(context).load(item.localPath).into(itemView.iv_image)
			}

			if (deleteShown) {
				if (slidingNeeded[position] == true){
					container_data.animateSlide(300L,0f,(180).toFloat(),1.0f)
					slidingNeeded[position] = false
				}
				else{
					container_data.x = 180f
				}
				check_box.visibility = View.VISIBLE
			} else {
				container_data.animateSlide(300L,0f,0f,1.0f)
				check_box.visibility = View.GONE
				item.isChecked = false
				check_box.isSelected = false
			}

			check_box.setOnClickListener { setImageItemSelected(item) }
		}
	}

	fun showDeleteBoxes() {
		deleteShown = !deleteShown
		if (deleteShown) slidingNeeded.fill(true) else slidingNeeded.fill(false)
		notifyDataSetChanged()
	}

	fun selectAll() {
		for (item in itemList) {
			item.isChecked = !isAllSelected()
		}
		notifyDataSetChanged()
	}

	fun deselectAll() {
		for (item in itemList) {
			item.isChecked = false
		}
		notifyDataSetChanged()
	}

	private fun isAllSelected(): Boolean {
		var allSelected = true
		for (item in itemList) {
			allSelected = item.isChecked
		}
		return allSelected
	}

	fun getAllSelected(): ArrayList<ImageItem> {
		val selectedList = arrayListOf<ImageItem>()
		for (item in itemList) {
			if (item.isChecked) {
				selectedList.add(item)
			}
		}
		return selectedList
	}

	fun setImageItemSelected(imageItem: ImageItem?) {
		imageItem?.let {
			for (item in itemList) {
				if (item.id == imageItem.id) {
					item.isChecked = !item.isChecked
					notifyItemChanged(itemList.indexOf(item))
					break
				}
			}
		}
	}

	fun setList(itemList: List<ImageItem>) {
		this.itemList.clear()
		this.itemList.addAll(itemList)

		this.slidingNeeded = BooleanArray(this.itemList.size)
		this.slidingNeeded.fill(false)

		notifyDataSetChanged()
	}

	interface OnImageListener {
		fun onImageSelected(item: ImageItem?)
		fun onLongClicked(item: ImageItem)
		fun showImage(imageItem: ImageItem)
	}

}