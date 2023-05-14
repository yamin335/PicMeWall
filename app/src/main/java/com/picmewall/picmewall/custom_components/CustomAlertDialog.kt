package com.picmewall.picmewall.custom_components

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.picmewall.picmewall.R

class CustomAlertDialog internal constructor(
    private val onYesCallback: () -> Unit,
    private val onNoCallback: () -> Unit,
    private val title: String,
    private val subTitle: String,
    private val yesButtonTitle: String = "Yes",
    private val noButtonTitle: String = "No") : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val exitDialog: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(requireActivity())
            .setTitle(title)
            .setIcon(R.mipmap.ic_launcher)
            .setCancelable(false)
            .setPositiveButton(yesButtonTitle) { _, _ ->
                onYesCallback.invoke()
                dismiss()
            }
            .setNegativeButton(noButtonTitle) { _, _ ->
                onNoCallback.invoke()
                dismiss()
            }
        if (subTitle.isNotBlank()) {
            exitDialog.setMessage(subTitle)
        }
        return exitDialog.create()
    }
}