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

package com.ave.vastgui.tools.viewbinding

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.ave.vastgui.core.extension.cast
import java.lang.reflect.ParameterizedType
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// Author: Vast Gui
// Email: guihy2019@gmail.com
// Date: 2023/3/4
// Documentation: https://ave.entropy2020.cn/documents/tools/architecture-components/ui-layer-libraries/view-bind/vb-delegate/
// Documentation: https://ave.entropy2020.cn/documents/tools/architecture-components/ui-layer-libraries/view-bind/vb-reflection/
// Reference: https://juejin.cn/post/6960914424865488932

/**
 * Creates an instance of the binding class for the [Activity] to use.
 *
 * ```json
 * class VbActivity2 : Activity() {
 *
 *     private val viewBindingProperty = viewBinding(ActivityVbBinding::inflate)
 *     private val mBinding by viewBindingProperty
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(mBinding.root)
 *     }
 *
 *     override fun onDestroy() {
 *         super.onDestroy()
 *         viewBindingProperty.clear()
 *     }
 *
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbActivity.kt">Example</a>
 */
@JvmName("viewBindingActivity")
@Suppress("UnusedReceiverParameter")
inline fun <V : ViewBinding> Activity.viewBinding(
    crossinline viewBinder: (LayoutInflater) -> V
) = LazyViewBindingProperty { activity: Activity ->
    viewBinder(activity.layoutInflater)
}

/**
 * Creates an instance of the binding class for the
 * [ComponentActivity] to use. [ActivityViewBindingProperty] will call
 * [ActivityViewBindingProperty.clear] to clear ViewBinding when the
 * lifecycle of current activity is [Lifecycle.State.DESTROYED] to prevent
 * memory leaks.
 *
 * ```kotlin
 * class VbActivity1 : ComponentActivity() {
 *
 *     private val mBinding by viewBinding(ActivityVbBinding::inflate)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(mBinding.root)
 *     }
 *
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbActivity.kt">Example</a>
 */
@JvmName("viewBindingActivity")
@Suppress("UnusedReceiverParameter")
inline fun <V : ViewBinding> ComponentActivity.viewBinding(
    crossinline viewBinder: (LayoutInflater) -> V
) = ActivityViewBindingProperty { activity: ComponentActivity ->
    viewBinder(activity.layoutInflater)
}

/**
 * Creates an instance of the binding class for the [ComponentActivity] to
 * use.
 *
 * ```kotlin
 * class VbActivity3 : ComponentActivity(R.layout.activity_vb) {
 *     private val mBinding by viewBinding(ActivityVbBinding::bind)
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbActivity.kt">Example</a>
 */
@JvmName("viewBindingActivity")
@Suppress("UnusedReceiverParameter")
inline fun <V : ViewBinding> ComponentActivity.viewBinding(
    crossinline viewBinder: (View) -> V,
    crossinline viewProvider: (ComponentActivity) -> View = ::findRootView
) = ActivityViewBindingProperty { activity: ComponentActivity ->
    viewBinder(viewProvider(activity))
}

/**
 * Creates an instance of the binding class for the [ComponentActivity] to
 * use.
 *
 * ```kotlin
 * class VbActivity4 : ComponentActivity(R.layout.activity_vb) {
 *
 *     // root is the root layout id of ActivityVbBinding
 *     private val mBinding by viewBinding(ActivityVbBinding::bind, R.id.root)
 *
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbActivity.kt">Example</a>
 */
@JvmName("viewBindingActivity")
@Suppress("UnusedReceiverParameter")
inline fun <V : ViewBinding> ComponentActivity.viewBinding(
    crossinline viewBinder: (View) -> V,
    @IdRes viewBindingRootId: Int
) = ActivityViewBindingProperty { activity: ComponentActivity ->
    viewBinder(activity.requireViewByIdCompat(viewBindingRootId))
}

/**
 * Creates an instance of the binding class for the [Fragment] to use.
 *
 * ```kotlin
 * class VbFragment1 : Fragment(R.layout.fragment_sender) {
 *
 *     private val mBinding by viewBinding(FragmentSenderBinding::bind)
 *
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbFragment.kt">Example</a>
 */
@JvmName("viewBindingFragment")
inline fun <F : Fragment, V : ViewBinding> Fragment.viewBinding(
    crossinline viewBinder: (View) -> V,
    crossinline viewProvider: (F) -> View = Fragment::requireView
): ViewBindingProperty<F, V> = when (this) {
    is DialogFragment -> cast(DialogFragmentViewBindingProperty { fragment: F ->
        viewBinder(viewProvider(fragment))
    })

    else -> FragmentViewBindingProperty { fragment: F ->
        viewBinder(viewProvider(fragment))
    }
}

/**
 * Creates an instance of the binding class for the [Fragment] to use.
 *
 * ```kotlin
 * class VbFragment2 : DialogFragment(R.layout.fragment_sender) {
 *
 *     val mBinding by viewBinding(FragmentSenderBinding::bind, R.id.fragment_sender_root)
 *
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbFragment.kt">Example</a>
 */
@JvmName("viewBindingFragment")
inline fun <F : Fragment, V : ViewBinding> Fragment.viewBinding(
    crossinline viewBinder: (View) -> V,
    @IdRes viewBindingRootId: Int
): ViewBindingProperty<F, V> = when (this) {
    is DialogFragment -> cast(viewBinding(viewBinder) { fragment: DialogFragment ->
        fragment.getRootView(viewBindingRootId)
    })

    else -> viewBinding(viewBinder) { fragment: F ->
        fragment.requireView().requireViewByIdCompat(viewBindingRootId)
    }
}

/**
 * Creates an instance of the binding class for the [ViewGroup] to use.
 *
 * ```kotlin
 * class ViewGroupGetViewBindingByDelegate @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
 *     LinearLayout(context, attrs) {
 *
 *     private val mBinding by viewBinding(ViewgroupVbBinding::bind)
 *     // Or
 *     // private val mBinding by viewBinding(ViewgroupVbBinding::bind, R.id.root)
 *
 *     init {
 *         inflate(context, R.layout.viewgroup_vb, this)
 *     }
 *
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbViewGroup.kt">Example</a>
 */
@JvmName("viewBindingViewGroup")
inline fun <V : ViewBinding> ViewGroup.viewBinding(
    crossinline viewBinder: (View) -> V,
    crossinline viewProvider: (ViewGroup) -> View = { this }
) = LazyViewBindingProperty { viewGroup: ViewGroup ->
    viewBinder(viewProvider(viewGroup))
}

/**
 * Creates an instance of the binding class for the [ViewGroup] to use.
 *
 * ```kotlin
 * class ViewGroupGetViewBindingByDelegate @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
 *     LinearLayout(context, attrs) {
 *
 *     private val mBinding by viewBinding(ViewgroupVbBinding::bind)
 *     // Or
 *     // private val mBinding by viewBinding(ViewgroupVbBinding::bind, R.id.root)
 *
 *     init {
 *         inflate(context, R.layout.viewgroup_vb, this)
 *     }
 *
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbViewGroup.kt">Example</a>
 */
@JvmName("viewBindingViewGroup")
@Suppress("UnusedReceiverParameter")
inline fun <V : ViewBinding> ViewGroup.viewBinding(
    crossinline viewBinder: (View) -> V,
    @IdRes viewBindingRootId: Int
) = LazyViewBindingProperty { viewGroup: ViewGroup ->
    viewBinder(viewGroup.requireViewByIdCompat(viewBindingRootId))
}

/**
 * Creates an instance of the binding class for the
 * [RecyclerView.ViewHolder] to use.
 *
 * ```kotlin
 * class ArticleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
 *     private val binding by viewBinding(ItemArticleBinding::bind)
 *     val title = binding.articleTitle
 *     val shareUser = binding.articleShareUser
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbAdapter.kt">Example</a>
 */
@JvmName("viewBindingViewHolder")
@Suppress("UnusedReceiverParameter")
inline fun <V : ViewBinding> RecyclerView.ViewHolder.viewBinding(
    crossinline viewBinder: (View) -> V,
    crossinline viewProvider: (RecyclerView.ViewHolder) -> View = RecyclerView.ViewHolder::itemView
) = LazyViewBindingProperty { vh: RecyclerView.ViewHolder ->
    viewBinder(viewProvider(vh))
}

/**
 * Creates an instance of the binding class for the
 * [RecyclerView.ViewHolder] to use.
 *
 * ```kotlin
 * class ArticleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
 *     private val binding by viewBinding(ItemArticleBinding::bind, R.id.article_root)
 *     val title = binding.articleTitle
 *     val shareUser = binding.articleShareUser
 * }
 * ```
 *
 * @since 0.5.2
 * @see <a href="https://github.com/SakurajimaMaii/Android-Vast-Extension/blob/develop/app/src/main/kotlin/com/ave/vastgui/app/activity/vbdelegate/VbAdapter.kt">Example</a>
 */
@JvmName("viewBindingViewHolder")
@Suppress("UnusedReceiverParameter")
inline fun <V : ViewBinding> RecyclerView.ViewHolder.viewBinding(
    crossinline viewBinder: (View) -> V,
    @IdRes viewBindingRootId: Int
) = LazyViewBindingProperty { vh: RecyclerView.ViewHolder ->
    viewBinder(vh.itemView.requireViewByIdCompat(viewBindingRootId))
}

interface ViewBindingProperty<in R : Any, out V : ViewBinding> : ReadOnlyProperty<R, V> {
    @MainThread
    fun clear()
}

open class LazyViewBindingProperty<in R : Any, out V : ViewBinding>(
    private val viewBinder: (R) -> V
) : ViewBindingProperty<R, V> {

    private var viewBinding: V? = null

    @MainThread
    override fun getValue(thisRef: R, property: KProperty<*>): V {
        viewBinding?.let { return it }

        return viewBinder(thisRef).also {
            this.viewBinding = it
        }
    }

    /** Clear the ViewBinding object. */
    @MainThread
    override fun clear() {
        viewBinding = null
    }
}

abstract class LifecycleViewBindingProperty<in R : Any, out V : ViewBinding>(
    private val viewBinder: (R) -> V
) : ViewBindingProperty<R, V> {

    private var viewBinding: V? = null

    protected abstract fun getLifecycleOwner(thisRef: R): LifecycleOwner

    @MainThread
    override fun getValue(thisRef: R, property: KProperty<*>): V {
        viewBinding?.let { return it }

        val lifecycle = getLifecycleOwner(thisRef).lifecycle
        val viewBinding = viewBinder(thisRef)
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            Log.w(
                this::class.java.simpleName,
                "Access to viewBinding after Lifecycle is destroyed or hasn't created yet. " +
                        "The instance of viewBinding will be not cached."
            )
        } else {
            lifecycle.addObserver(ClearOnDestroyLifecycleObserver(this))
            this.viewBinding = viewBinding
        }
        return viewBinding
    }

    @MainThread
    override fun clear() {
        viewBinding = null
    }

    private class ClearOnDestroyLifecycleObserver(
        private val property: LifecycleViewBindingProperty<*, *>
    ) : DefaultLifecycleObserver {

        private companion object {
            private val mainHandler = Handler(Looper.getMainLooper())
        }

        @MainThread
        override fun onDestroy(owner: LifecycleOwner) {
            mainHandler.post { property.clear() }
        }

    }
}

class FragmentViewBindingProperty<in F : Fragment, out V : ViewBinding>(
    viewBinder: (F) -> V
) : LifecycleViewBindingProperty<F, V>(viewBinder) {

    override fun getLifecycleOwner(thisRef: F): LifecycleOwner {
        try {
            return thisRef.viewLifecycleOwner
        } catch (ignored: IllegalStateException) {
            error("Fragment doesn't have view associated with it or the view has been destroyed")
        }
    }

}

class DialogFragmentViewBindingProperty<in F : DialogFragment, out V : ViewBinding>(
    viewBinder: (F) -> V
) : LifecycleViewBindingProperty<F, V>(viewBinder) {

    override fun getLifecycleOwner(thisRef: F): LifecycleOwner {
        return if (thisRef.showsDialog) {
            thisRef
        } else {
            try {
                thisRef.viewLifecycleOwner
            } catch (ignored: IllegalStateException) {
                error("Fragment doesn't have view associated with it or the view has been destroyed")
            }
        }
    }
}

class ActivityViewBindingProperty<in A : ComponentActivity, out V : ViewBinding>(
    viewBinder: (A) -> V
) : LifecycleViewBindingProperty<A, V>(viewBinder) {

    override fun getLifecycleOwner(thisRef: A): LifecycleOwner {
        return thisRef
    }

}

fun <V : View> View.requireViewByIdCompat(@IdRes id: Int): V {
    return ViewCompat.requireViewById(this, id)
}

fun <V : View> Activity.requireViewByIdCompat(@IdRes id: Int): V = try {
    ActivityCompat.requireViewById(this, id)
} catch (exception: IllegalArgumentException) {
    throw IllegalArgumentException(
        "Maybe you need to use ${this::class.java.superclass.simpleName}" +
                "(@LayoutRes int contentLayoutId) to provide root view explicitly.",
        exception
    )
}

fun findRootView(activity: Activity): View {
    val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
    checkNotNull(contentView) { "Activity has no content view" }
    return when (contentView.childCount) {
        1 -> contentView.getChildAt(0)
        0 -> error(
            "Content view has no children. Maybe you need to use ${activity::class.java.superclass.simpleName}" +
                    "(@LayoutRes int contentLayoutId) to provide root view explicitly."
        )

        else -> error("More than one child view found in Activity content view.")
    }
}

fun DialogFragment.getRootView(viewBindingRootId: Int): View {
    val dialog = checkNotNull(dialog) {
        "DialogFragment doesn't have dialog. Use viewBinding delegate after onCreateDialog"
    }
    val window = checkNotNull(dialog.window) { "Fragment's Dialog has no window" }
    return with(window.decorView) {
        if (viewBindingRootId != 0) requireViewByIdCompat(
            viewBindingRootId
        ) else this
    }
}

/**
 * Creates an instance of the binding class for the activity to use.
 *
 * @param base The Activity base class you define. The default value is
 *     null.
 * @receiver Currently need to get the Activity of ViewBinding.
 * @since 0.5.2
 */
@JvmName("reflexViewBindingActivity")
fun <I : Activity, O : ViewBinding> I.reflectViewBinding(base: Class<I>? = null): O =
    reflectViewBinding(javaClass, layoutInflater, base)

/**
 * Creates an instance of the binding class for the fragment to use.
 *
 * @param base The Fragment base class you define. The default value is
 *     null.
 * @receiver Currently need to get the Fragment of ViewBinding.
 * @since 0.5.2
 */
@JvmName("reflexViewBindingFragment")
fun <I : Fragment, O : ViewBinding> I.reflectViewBinding(
    container: ViewGroup?, base: Class<I>? = null
): O = reflectViewBinding(javaClass, layoutInflater, container, base)

/**
 * Creates an instance of the binding class for the fragment to use.
 *
 * @param base The Fragment base class you define. The default value is
 *     null.
 * @receiver Currently need to get the Fragment of ViewBinding.
 * @since 0.5.2
 */
@JvmName("reflexViewBindingFragment")
fun <I : Fragment, O : ViewBinding> I.reflectViewBinding(base: Class<I>? = null): O =
    reflectViewBinding(javaClass, requireView(), base)

/**
 * Creates an instance of the binding class for the activity to use.
 *
 * @since 0.5.2
 */
private fun <VB : ViewBinding> reflectViewBinding(
    current: Class<*>, layoutInflater: LayoutInflater, base: Class<*>?
): VB {
    return try {
        if (base?.isAssignableFrom(current) == false) {
            throw RuntimeException("Can't get the viewBinding type.")
        }
        val genericSuperclass = current.genericSuperclass
        if (genericSuperclass is ParameterizedType) {
            val actualTypeArguments =
                genericSuperclass.actualTypeArguments
            for (actualTypeArgument in actualTypeArguments) {
                val tClass: Class<*> = try {
                    actualTypeArgument as Class<*>
                } catch (e: Exception) {
                    continue
                }
                if (ViewBinding::class.java.isAssignableFrom(tClass)) {
                    val inflate = tClass.getMethod("inflate", LayoutInflater::class.java)
                    return cast(inflate.invoke(null, layoutInflater))
                }
            }
        }
        reflectViewBinding(current.superclass, layoutInflater, base)
    } catch (e: Exception) {
        throw RuntimeException(e.message, e)
    }
}

/**
 * Creates an instance of the binding class for the fragment to use.
 *
 * @since 0.5.2
 */
private fun <VB : ViewBinding> reflectViewBinding(
    current: Class<*>,
    from: LayoutInflater,
    container: ViewGroup?,
    base: Class<*>?
): VB {
    return try {
        if (base?.isAssignableFrom(current) == false) {
            throw RuntimeException("Can't get the viewBinding type.")
        }
        val genericSuperclass = current.genericSuperclass
        if (genericSuperclass is ParameterizedType) {
            val actualTypeArguments =
                genericSuperclass.actualTypeArguments
            for (actualTypeArgument in actualTypeArguments) {
                val tClass: Class<*> = try {
                    actualTypeArgument as Class<*>
                } catch (e: Exception) {
                    continue
                }
                if (ViewBinding::class.java.isAssignableFrom(tClass)) {
                    val inflate = tClass.getMethod(
                        "inflate",
                        LayoutInflater::class.java,
                        ViewGroup::class.java,
                        Boolean::class.java
                    )
                    return cast(inflate.invoke(null, from, container, false))
                }
            }
        }
        reflectViewBinding(current.superclass, from, container, base)
    } catch (e: Exception) {
        throw RuntimeException(e.message, e)
    }
}

/**
 * Creates an instance of the binding class for the fragment to use.
 *
 * @since 0.5.2
 */
private fun <VB : ViewBinding> reflectViewBinding(
    current: Class<*>,
    view: View,
    base: Class<*>?
): VB {
    return try {
        if (base?.isAssignableFrom(current) == false) {
            throw RuntimeException("Can't get the viewBinding type.")
        }
        val genericSuperclass = current.genericSuperclass
        if (genericSuperclass is ParameterizedType) {
            val actualTypeArguments =
                genericSuperclass.actualTypeArguments
            for (actualTypeArgument in actualTypeArguments) {
                val tClass: Class<*> = try {
                    actualTypeArgument as Class<*>
                } catch (e: Exception) {
                    continue
                }
                if (ViewBinding::class.java.isAssignableFrom(tClass)) {
                    val inflate = tClass.getMethod("bind", View::class.java)
                    return cast(inflate.invoke(null, view))
                }
            }
        }
        reflectViewBinding(current.superclass, view, base)
    } catch (e: Exception) {
        throw RuntimeException(e.message, e)
    }
}