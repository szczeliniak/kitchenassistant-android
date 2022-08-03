package pl.szczeliniak.kitchenassistant.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import androidx.appcompat.widget.AppCompatTextView
import pl.szczeliniak.kitchenassistant.android.databinding.DropdownRecipeBinding
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Recipe
import pl.szczeliniak.kitchenassistant.android.ui.utils.AppCompatTextViewUtils.Companion.fillOrHide

class RecipeDropdownArrayAdapter(context: Context) : ArrayAdapter<Recipe>(context, 0, ArrayList()) {

    private val allRecipes = ArrayList<Recipe>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val binding: DropdownRecipeBinding?

        if (convertView == null) {
            binding = DropdownRecipeBinding.inflate(LayoutInflater.from(context))
            viewHolder = ViewHolder(binding.recipeName, binding.recipeCategory, binding.recipeAuthor)
            binding.root.tag = viewHolder
        } else {
            binding = DropdownRecipeBinding.bind(convertView)
            viewHolder = binding.root.tag as ViewHolder
        }

        getItem(position)?.let {
            viewHolder.nameTextView.text = it.name
            viewHolder.categoryTextView.fillOrHide(it.category?.name, viewHolder.categoryTextView)
            viewHolder.authorTextView.fillOrHide(it.author, viewHolder.authorTextView)
        }

        return binding.root
    }

    data class ViewHolder(
        val nameTextView: AppCompatTextView,
        val categoryTextView: AppCompatTextView,
        val authorTextView: AppCompatTextView
    )

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null) {
                    val list = filterRecipes(constraint)
                    results.values = list
                    results.count = list.size
                }
                return results
            }

            private fun filterRecipes(constraint: CharSequence): ArrayList<Recipe> {
                return ArrayList(allRecipes.filter { r ->
                    r.name.lowercase().contains(constraint.toString().lowercase())
                })
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                results?.values?.let {
                    val items = it as ArrayList<*>
                    if (items.isNotEmpty()) {
                        clear()
                        items.forEach { r -> add(r as Recipe) }
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun refresh(recipes: List<Recipe>) {
        allRecipes.clear()
        allRecipes.addAll(recipes)
    }

}