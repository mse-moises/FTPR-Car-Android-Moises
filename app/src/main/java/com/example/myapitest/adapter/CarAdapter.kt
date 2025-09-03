package com.example.myapitest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapitest.model.Car
import com.example.myapitest.R
import com.bumptech.glide.Glide

class CarAdapter(
    private val cars: List<Car>
) : RecyclerView.Adapter<CarAdapter.ItemViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarAdapter.ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_car_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarAdapter.ItemViewHolder, position: Int) {
        val car = cars[position]
        holder.model.text = car.name
        holder.year.text = car.year
        holder.license.text = car.license
        
        Glide.with(holder.itemView.context)
            .load(car.imageUrl)
            .into(holder.imageView)


    }

    override fun getItemCount(): Int = cars.size;

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)  {
        val imageView: ImageView = view.findViewById(R.id.image)
        val model: TextView = view.findViewById(R.id.model)
        val year: TextView = view.findViewById(R.id.year)
        val license: TextView = view.findViewById(R.id.license)
    }

}