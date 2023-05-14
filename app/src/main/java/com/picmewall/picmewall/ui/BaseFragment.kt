package com.picmewall.picmewall.ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.picmewall.picmewall.NavigationHost
import com.picmewall.picmewall.utils.autoCleared

abstract class BaseFragment<T : ViewDataBinding, V : ViewModel> : Fragment() {
    var binding by autoCleared<T>()

    val navController: NavController
        get() = findNavController()

    var navHost: NavigationHost? = null

//    @Inject
//    lateinit var preferencesHelper: PreferencesHelper

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    abstract val br: Int

    /**
     * @return layout resource id
     */
    @get:LayoutRes
    abstract val layoutId: Int

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract val viewModel: V

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationHost) {
            navHost = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        navHost = null
    }

    fun registerToolbar(toolbar: MaterialToolbar) {
        val host = navHost ?: return
        toolbar.apply {
            host.registerToolbarWithNavigation(this)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(br, viewModel)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.executePendingBindings()
    }

    fun updateStatusBarBackgroundColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                requireActivity().window.statusBarColor = Color.parseColor(color)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun navigateTo(direction: NavDirections) {
        try {
            navController.navigate(direction)
        } catch (e: Exception) {

        }
    }
}