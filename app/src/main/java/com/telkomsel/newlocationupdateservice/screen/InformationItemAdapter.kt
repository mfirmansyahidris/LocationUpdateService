package com.telkomsel.newlocationupdateservice.screen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.telkomsel.newlocationupdateservice.R
import com.telkomsel.newlocationupdateservice.model.Content2Column

/**
 ****************************************
created by -fi-
.::manca.fi@gmail.com ::.

10/02/2020, 12:15 PM
 ****************************************
 */

class InformationItemAdapter : RecyclerView.Adapter<InformationItemAdapter.ViewHolder>() {
    var data = listOf<Content2Column>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_text_2_column, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(data[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lable: TextView = itemView.findViewById(R.id.tv_lable)
        private val content: TextView = itemView.findViewById(R.id.tv_content)

        fun bindItem(items: Content2Column?) {
            lable.text = items?.col1
            content.text = items?.col2
        }
    }
}