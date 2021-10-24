package com.example.placebook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.placebook.R
import com.example.placebook.databinding.BookmarkItemBinding
import com.example.placebook.ui.MapsActivity
import com.example.placebook.viewmodel.MapsViewModel

class BookmarkAdapter(
    private var bookmarkList: List<MapsViewModel.BookemarkerView>?,
    private val activity: MapsActivity
) : RecyclerView.Adapter<BookmarkAdapter.Viewholder>() {

    class Viewholder(val binding : BookmarkItemBinding, val activity: MapsActivity) :
        RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    val bookmark = itemView.tag as MapsViewModel.BookemarkerView
                    activity.moveToBookmark(bookmark)
                }
            }

    }
    fun setBookmarkData(bookmark : List<MapsViewModel.BookemarkerView>){
        this.bookmarkList = bookmark
        notifyDataSetChanged()
}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkAdapter.Viewholder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = BookmarkItemBinding.inflate(layoutInflater , parent , false)
        return Viewholder(binding , activity)
    }

    override fun onBindViewHolder(holder: BookmarkAdapter.Viewholder, position: Int) {
      bookmarkList?.let { list ->
      val bookmarkViewData = list[position]
          holder.binding.root.tag = bookmarkViewData
          holder.binding.bookmarkData = bookmarkViewData
          holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_baseline_info_24)



      }
    }

    override fun getItemCount() = bookmarkList?.size  ?:0
}