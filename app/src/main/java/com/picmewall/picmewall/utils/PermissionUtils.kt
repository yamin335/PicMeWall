package com.picmewall.picmewall.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.picmewall.picmewall.custom_components.CustomAlertDialog

class PermissionUtils {
    companion object {
        fun checkPermission(activity: AppCompatActivity, permissionsArray: Array<String>): Boolean {
            var permissionResult = 0
            permissionsArray.forEach { permission ->
                permissionResult += activity.checkSelfPermissionCompat(permission)
            }
            return PackageManager.PERMISSION_GRANTED == permissionResult
        }

        fun checkPermissionRationale(activity: AppCompatActivity, permissionsArray: Array<String>): Boolean {
            var shouldProvidePermissionRationale = false

            permissionsArray.forEach { permission ->
                shouldProvidePermissionRationale = shouldProvidePermissionRationale || activity.shouldShowRequestPermissionRationaleCompat(permission)
            }
            return shouldProvidePermissionRationale
        }

        fun requestPermission(activity: AppCompatActivity, permissionsArray: Array<String>, fragmentManager: FragmentManager, permissionRequestCode: Int) {
            val shouldProvidePermissionRationale = checkPermissionRationale(activity, permissionsArray)

            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            if (shouldProvidePermissionRationale) {
                val rationaleDialog = CustomAlertDialog(onYesCallback = {
                    activity.requestPermissionsCompat(permissionsArray, permissionRequestCode)
                    }, onNoCallback = {
                    Toast.makeText(activity, "Permission Denied!", Toast.LENGTH_SHORT).show()
                }, "Requires Permission", "Please allow required permissions for flawless user experience", "Next")
                rationaleDialog.show(fragmentManager, "#PERMISSION_REQUEST_DIALOG")
            } else {
                // Request permission. It's possible this can be auto answered if device policy
                // sets the permission in a given state or the user denied the permission
                // previously and checked "Never ask again".
                activity.requestPermissionsCompat(permissionsArray, permissionRequestCode)
            }
        }

        fun goToSettings(context: Context, packageName: String) {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also { intent ->
                context.startActivity(intent)
            }
        }
    }
}