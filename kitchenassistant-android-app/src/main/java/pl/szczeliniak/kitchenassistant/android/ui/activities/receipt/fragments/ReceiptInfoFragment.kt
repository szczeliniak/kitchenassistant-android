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

            r.description?.let {
                binding.fragmentReceiptInfoTextviewDescription.text = r.description
            } ?: run {
                binding.fragmentReceiptInfoDescriptionLayout.visibility = View.GONE
            }

            r.author?.let {
                binding.fragmentReceiptInfoTextviewAuthor.text = r.author
            } ?: run {
                binding.fragmentReceiptInfoAuthorLayout.visibility = View.GONE
            }

            r.source?.let {
                binding.fragmentReceiptInfoTextviewUrl.text = r.source
            } ?: run {
                binding.fragmentReceiptInfoUrlLayout.visibility = View.GONE
            }
        }
    }

    override fun onReceiptChanged() {
        loadData()
    }

}