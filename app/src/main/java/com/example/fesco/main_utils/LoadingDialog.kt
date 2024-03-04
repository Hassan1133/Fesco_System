package com.example.fesco.main_utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.fesco.R

class LoadingDialog {

    companion object {

        private lateinit var dialog: Dialog

        fun showLoadingDialog(context: Context?): Dialog? {
            dialog = Dialog(context!!)
            dialog!!.setContentView(R.layout.loading_dialog)
            dialog!!.setCancelable(false) // To prevent the dialog from being dismissed by touching outside

            // Make the dialog background transparent
            if (dialog!!.window != null) {
                dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            dialog!!.show()
            return dialog
        }

        fun hideLoadingDialog(dialog: Dialog?) {
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }
}