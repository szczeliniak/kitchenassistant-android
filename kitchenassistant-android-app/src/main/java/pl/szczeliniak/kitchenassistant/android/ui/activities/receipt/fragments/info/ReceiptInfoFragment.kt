package pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.databinding.FragmentReceiptInfoBinding
import pl.szczeliniak.kitchenassistant.android.ui.activities.receipt.fragments.ReceiptActivityFragment
import pl.szczeliniak.kitchenassistant.android.ui.utils.setTextOrDefault

@AndroidEntryPoint
class ReceiptInfoFragment : ReceiptActivityFragment() {

    private lateinit var binding: FragmentReceiptInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadData()
    }

    private fun loadData() {
        receipt?.let { r ->
            binding.fragmentReceiptInfoTextviewDescription.setTextOrDefault(r.description)
            binding.fragmentReceiptInfoTextviewAuthor.setTextOrDefault(r.author)
            binding.fragmentReceiptInfoTextviewUrl.setTextOrDefault(r.source)
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}