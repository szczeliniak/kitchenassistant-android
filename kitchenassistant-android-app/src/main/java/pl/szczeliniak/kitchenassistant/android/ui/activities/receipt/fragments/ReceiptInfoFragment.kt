package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptInfoBinding

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ReceiptInfoFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptInfoBinding.inflate(inflater)
        loadData()
        return binding.root
    }

    private fun loadData() {
        receipt?.let { r ->
            binding.fragmentReceiptInfoTextviewName.text = r.name
            binding.fragmentReceiptInfoTextviewDescription.text = getOrDefault(r.description)
            binding.fragmentReceiptInfoTextviewAuthor.text = getOrDefault(r.author)
            binding.fragmentReceiptInfoTextviewUrl.text = getOrDefault(r.source)
        }
    }

    private fun getOrDefault(text: String?): CharSequence {
        if (text.isNullOrEmpty()) {
            return "---"
        }
        return text
    }

    override fun onReceiptChanged() {
        loadData()
    }

}