package com.woowa.banchan.ui.screen.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.woowa.banchan.R
import com.woowa.banchan.databinding.FragmentOrderBinding
import com.woowa.banchan.ui.extensions.repeatOnLifecycle
import com.woowa.banchan.ui.extensions.toVisibility
import com.woowa.banchan.ui.navigator.OnBackClickListener
import com.woowa.banchan.ui.navigator.OnOrderDetailClickListener
import com.woowa.banchan.ui.screen.orderdetail.OrderDetailFragment
import kotlinx.coroutines.launch

class OrderFragment : Fragment(), OnOrderDetailClickListener {

    private var _binding: FragmentOrderBinding? = null
    private val binding: FragmentOrderBinding get() = requireNotNull(_binding)

    private val orderViewModel: OrderViewModel by activityViewModels()

    private val orderListAdapter by lazy {
        OrderListAdapter(
            onClickItem = { orderViewModel.navigateToDetail(it) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observeData()
    }

    private fun initView() {
        binding.rvOrderList.adapter = orderListAdapter
        binding.viewModel = orderViewModel
    }

    private fun observeData() {
        viewLifecycleOwner.repeatOnLifecycle {

            launch {
                orderViewModel.state.collect {
                    val isNotEmpty = it.orderInfoList.isNotEmpty()
                    binding.rvOrderList.visibility = isNotEmpty.toVisibility()
                    binding.llEmpty.visibility = (!isNotEmpty).toVisibility()
                    binding.ivLockandlock.visibility = (!isNotEmpty).toVisibility()

                    if (isNotEmpty)
                        orderListAdapter.submitList(it.orderInfoList)
                    else
                        binding.ivLockandlock.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.translate_infinity
                            )
                        )
                }
            }

            launch {
                orderViewModel.eventFlow.collect {
                    when (it) {
                        is UiEvent.ShowToast -> showToastMessage(it.message)
                        is UiEvent.NavigateToOrderDetail -> navigateToOrderDetail(it.id)
                        is UiEvent.NavigateToBack -> (activity as OnBackClickListener).navigateToBack()
                    }
                }
            }
        }
    }

    override fun navigateToOrderDetail(id: Long) {
        parentFragmentManager.popBackStack("Order", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in,
                R.anim.slide_out,
                R.anim.slide_in,
                R.anim.slide_out
            )
            .addToBackStack("Order")
            .add(R.id.fcv_main, OrderDetailFragment.newInstance(id))
            .commit()
    }

    private fun showToastMessage(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}