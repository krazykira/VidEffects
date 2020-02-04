package com.videffects.sample.view

import android.app.Activity
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.videffects.sample.model.AssetsGalleryModel
import com.videffects.sample.model.screenHeight
import com.videffects.sample.model.screenWidth
import com.videffects.sample.model.toPx


class AssetsGalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recyclerView = RecyclerView(this)
        recyclerView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        recyclerView.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        recyclerView.adapter = PreviewAdapter(AssetsGalleryModel(this))
        recyclerView.addItemDecoration(SpacesItemDecoration())
        setContentView(recyclerView)
    }

    private class PreviewAdapter(private val model: AssetsGalleryModel) : RecyclerView.Adapter<PreviewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewHolder {
            val activity = parent.context as Activity
            val imageView = ImageView(activity)
            val width = activity.screenWidth() / 2
            val height = activity.screenHeight() / 3
            imageView.layoutParams = ViewGroup.LayoutParams(width, height)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            return PreviewHolder(imageView)
        }

        override fun onBindViewHolder(holder: PreviewHolder, position: Int) {
            holder.setThumbnail(model.getThumbnail(position))
            holder.itemView.setOnClickListener { v -> VideoActivity.startActivity(v.context, model.getAssetName(position)) }
        }

        override fun getItemCount(): Int {
            return model.count
        }
    }

    private class PreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setThumbnail(thumbnail: Drawable?) {
            (itemView as ImageView).setImageDrawable(thumbnail)
        }
    }

    private class SpacesItemDecoration : ItemDecoration() {

        private val space = 4.toPx()

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column: Int = position % SPAN_COUNT
            outRect.left = column * space / SPAN_COUNT
            outRect.right = space - (column + 1) * space / SPAN_COUNT
            if (position >= SPAN_COUNT) {
                outRect.top = space
            }
        }
    }

    companion object {
        private const val SPAN_COUNT = 2
    }
}