/*
 * Copyright 2022 VastGui guihy2019@gmail.com
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

package cn.govast.vasttools.network

import androidx.lifecycle.lifecycleScope
import cn.govast.vasttools.base.BaseLifecycleOwner
import cn.govast.vasttools.extension.NotNUllSingleVar
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

// Author: Vast Gui
// Email: guihy2019@gmail.com
// Date: 2022/9/26
// Description: 
// Documentation:
// Reference:

class FlowBuilder<T : BaseApiResponse>(private val lifecycleOwner: BaseLifecycleOwner) :
    BaseNetState<T> {

    private var request: suspend () -> ApiResponse<T> by NotNUllSingleVar()
    private var start: () -> Unit = {}
    private var success: (data: T?) -> Unit = {}
    private var empty: () -> Unit = {}
    private var failed: (errorCode: Int?, errorMsg: String?) -> Unit = { _, _ -> }
    private var error: (e: Throwable?) -> Unit = { }
    private var complete: () -> Unit = {}

    fun initRequest(request: suspend () -> ApiResponse<T>) = apply {
        this.request = request
    }

    override fun onStartState(onStart: () -> Unit) = apply {
        start = onStart
    }

    override fun onEmptyState(onEmpty: () -> Unit) = apply {
        empty = onEmpty
    }

    override fun onFailedState(onFailed: (errorCode: Int?, errorMsg: String?) -> Unit) = apply {
        failed = onFailed
    }

    override fun onErrorState(onError: (e: Throwable?) -> Unit) = apply {
        error = onError
    }

    override fun onCompleteState(onComplete: () -> Unit) = apply {
        complete = onComplete
    }

    override fun onSuccessState(onSuccess: (data: T?) -> Unit) = apply {
        success = onSuccess
    }

    fun launchFlow() {
        lifecycleOwner.lifecycleScope.launch {
            flow {
                emit(request())
            }.onStart {
                start.invoke()
            }.onCompletion {
                complete.invoke()
            }.collect { response ->
                when (response) {
                    is ApiSuccessResponse -> success.invoke(response.data)
                    is ApiEmptyResponse -> empty.invoke()
                    is ApiFailedResponse -> failed.invoke(response.errorCode, response.errorMsg)
                    is ApiErrorResponse -> error.invoke(response.error)
                }
            }
        }
    }

}