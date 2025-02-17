/*
 * Copyright 2021-2024 VastGui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ave.vastgui.tools.view.toast

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.ave.vastgui.tools.config.ToolsConfig
import com.ave.vastgui.tools.content.ContextHelper

// Author: Vast Gui
// Email: guihy2019@gmail.com
// Date: 2022/3/10 15:27
// Description: Toast utils.
// Documentation: https://ave.entropy2020.cn/documents/tools/core-topics/ui/toast/simple-toast/

/**
 * Simple Toast.
 *
 * @since 0.5.3
 */
object SimpleToast {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        Toast.LENGTH_LONG,
        Toast.LENGTH_SHORT
    )
    annotation class Duration

    private var mToast: Toast? = null

    /**
     * @param msg message of the toast.
     * @param context context.
     * @since 0.5.3
     */
    @JvmStatic
    @JvmOverloads
    fun showShortMsg(
        msg: String,
        context: Context = ContextHelper.getAppContext()
    ) {
        showToast(context, msg, Toast.LENGTH_SHORT)
    }

    /**
     * @param id message string id of the toast.
     * @param context context.
     * @since 0.5.3
     */
    @JvmStatic
    @JvmOverloads
    fun showShortMsg(
        @StringRes id: Int,
        context: Context = ContextHelper.getAppContext()
    ) {
        showToast(context, context.getString(id), Toast.LENGTH_SHORT)
    }


    /**
     * @param msg message of the toast.
     * @param context context.
     * @since 0.5.3
     */
    @JvmStatic
    @JvmOverloads
    fun showLongMsg(
        msg: String,
        context: Context = ContextHelper.getAppContext()
    ) {
        showToast(context, msg, Toast.LENGTH_LONG)
    }

    /**
     * @param id message string id of the toast.
     * @param context context.
     * @since 0.5.3
     */
    @JvmStatic
    @JvmOverloads
    fun showLongMsg(
        @StringRes id: Int,
        context: Context = ContextHelper.getAppContext()
    ) {
        showToast(context, context.getString(id), Toast.LENGTH_LONG)
    }

    /**
     * Show toast
     *
     * @since 0.5.3
     */
    private fun showToast(context: Context, msg: String, @Duration duration: Int) {
        if (ToolsConfig.isMainThread()) {
            makeToast(context, msg, duration)
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.post { makeToast(context, msg, duration) }
        }
    }

    /**
     * Make toast
     *
     * @since 0.5.3
     */
    private fun makeToast(context: Context, msg: String, @Duration duration: Int) {
        mToast?.cancel()
        mToast = makeText(context, msg, duration)
        mToast?.show()
    }
}