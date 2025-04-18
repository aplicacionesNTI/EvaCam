package com.itevebasa.evacam.actividades

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itevebasa.evacam.R
import com.itevebasa.evacam.modelos.Item

class ItemAdapter(
    private val itemList: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_adapter, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.codigoText.text = "Servicio: " + item.codigo
        holder.codVehiculoText.text = "Matrícula: " + item.codvehiculo


        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val codigoText: TextView = itemView.findViewById(R.id.codigoText)
        val codVehiculoText: TextView = itemView.findViewById(R.id.codVehiculoText)
    }
}