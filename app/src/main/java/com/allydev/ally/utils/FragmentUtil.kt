package com.allydev.ally.utils

import android.content.res.Resources

class FragmentUtil {
    companion object {
        fun getResourceStr(id: Int, resources:Resources): String {
            return resources.getString(id)
        }
    }
}