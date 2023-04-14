package pl.szczeliniak.kitchenassistant.android.ui.dialogs.confirmation

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import dagger.hilt.android.AndroidEntryPoint
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.databinding.DialogConfirmationBinding

@AndroidEntryPoint
class ConfirmationDialog : DialogFragment() {

    companion object {

        private const val TAG = "ConfirmationDialog"
        private const val CALLBACK_EXTRA = "CALLBACK_EXTRA"

        fun show(fragmentManager: FragmentManager, onConfirm: OnConfirm) {
            val bundle = Bundle()
            bundle.putSerializable(CALLBACK_EXTRA, onConfirm)
            val dialog = ConfirmationDialog()
            dialog.arguments = bundle
            dialog.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogConfirmationBinding
    private lateinit var onConfirm: OnConfirm

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        onConfirm = requireArguments().getParcelable(CALLBACK_EXTRA, OnConfirm::class.java)!!

        binding = DialogConfirmationBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(binding.root)
        builder.setPositiveButton(R.string.label_button_confirm) { _, _ -> }
        builder.setNegativeButton(R.string.label_button_cancel) { _, _ -> }
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val dialog = dialog as AlertDialog
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            onConfirm.onConfirm()
            dismiss()
        }
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener { dismiss() }
    }

    fun interface OnConfirm : java.io.Serializable {
        fun onConfirm()
    }

}