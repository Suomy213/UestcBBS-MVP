package com.scatl.uestcbbs.base

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.scatl.uestcbbs.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Created by sca_tl on 2022/12/30 17:03
 */
abstract class BaseVBDialogFragment<P: BaseVBPresenter<V>, V: BaseView, VB: ViewBinding>: DialogFragment(), View.OnClickListener {

    protected lateinit var binding: VB
    protected var presenter: P? = null

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = getViewBinding()

        dialog?.let {
            it.window?.apply {
                attributes = attributes.apply {
                    windowAnimations = R.style.popwindow_anim
                }
            }
        }

        getBundle(arguments)

        presenter = initPresenter()
        presenter?.attachView(this as V)

        initView()

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dialog.window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes.blurBehindRadius = 64
            }
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()
        if (registerEventBus() && !EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        //设置动画、位置、宽度等属性（必须放在onStart方法中）
        dialog?.window?.let {
            it.attributes = it.attributes.apply {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                width = ViewGroup.LayoutParams.MATCH_PARENT
                gravity = Gravity.BOTTOM
            }
            //设置背景，加入这句使界面水平填满屏幕
            it.setBackgroundDrawableResource(R.drawable.shape_dialog_fragment)
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
    }

    override fun onClick(v: View) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventBusReceived(baseEvent: BaseEvent<Any>?) {
        baseEvent?.let {
            receiveEventBusMsg(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (registerEventBus() && EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        presenter?.detachView()
    }

    protected abstract fun getViewBinding(): VB
    protected abstract fun initView()
    protected abstract fun initPresenter(): P
    protected open fun getBundle(bundle: Bundle?) { }
    protected open fun registerEventBus() = false
    protected open fun receiveEventBusMsg(baseEvent: BaseEvent<Any>) { }

}