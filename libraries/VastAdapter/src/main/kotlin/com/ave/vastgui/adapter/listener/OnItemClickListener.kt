/*
 * Copyright 2024 VastGui guihy2019@gmail.com
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

package com.ave.vastgui.adapter.listener

import android.view.View
import com.ave.vastgui.adapter.base.ItemWrapper

// Author: Vast Gui
// Email: guihy2019@gmail.com
// Date: 2022/10/10
// Documentation: https://ave.entropy2020.cn/documents/VastAdapter/

/**
 * 用于监听列表项的点击事件。
 *
 * @since 1.1.1
 */
fun interface OnItemClickListener<T> {
    /**
     * 当列表项被点击时会触发该回调。
     *
     * @since 1.1.1
     */
    fun onItemClick(view: View, pos: Int, data: ItemWrapper<T>)
}