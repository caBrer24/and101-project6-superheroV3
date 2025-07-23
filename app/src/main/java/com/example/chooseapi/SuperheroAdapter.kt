package com.example.chooseapi


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SuperheroAdapter(private val superheroItems: List<Superhero>) :
    RecyclerView.Adapter<SuperheroAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val heroImageView: ImageView = view.findViewById(R.id.heroImage)
        val heroNameTextView: TextView = view.findViewById(R.id.heroName)


        val heroFullNameValueTextView: TextView = view.findViewById(R.id.hero_item_full_name_value)
        val heroFirstAppearanceValueTextView: TextView = view.findViewById(R.id.hero_item_first_appearance_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.superhero_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentHero = superheroItems[position]

        holder.heroNameTextView.text = currentHero.name


        holder.heroFullNameValueTextView.text = currentHero.fullName ?: "N/A"
        holder.heroFirstAppearanceValueTextView.text = currentHero.firstAppearance ?: "N/A"

        Glide.with(holder.itemView.context)
            .load(currentHero.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.heroImageView)

        holder.itemView.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "Tapped on ${currentHero.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount() = superheroItems.size
}

