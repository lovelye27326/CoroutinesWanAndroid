package com.kuky.demo.wan.android.ui.system

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.kuky.demo.wan.android.R
import com.kuky.demo.wan.android.base.BaseDialogFragment
import com.kuky.demo.wan.android.base.OnItemClickListener
import com.kuky.demo.wan.android.databinding.DialogKnowledgeSystemBinding
import com.kuky.demo.wan.android.entity.SystemCategory
import com.kuky.demo.wan.android.entity.SystemData
import com.kuky.demo.wan.android.utils.Injection
import com.kuky.demo.wan.android.utils.screenWidth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author kuky.
 * @description
 */
class KnowledgeSystemDialogFragment : BaseDialogFragment<DialogKnowledgeSystemBinding>() {
    var mOnClick: ((KnowledgeSystemDialogFragment, String?, String?, Int) -> Unit)? = null

    private val mFirstAdapter by lazy { KnowledgeSystemTypeAdapter() }

    private val mSecAdapter by lazy { KnowledgeSystemSecTypeAdapter() }

    private val mViewModel by lazy {
        ViewModelProvider(requireActivity(), Injection.provideKnowledgeSystemViewModelFactory())
            .get(KnowledgeSystemViewModel::class.java)
    }

    private var mFirstData: SystemData? = null

    override fun layoutId() = R.layout.dialog_knowledge_system

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun initDialog(view: View, savedInstanceState: Bundle?) {
        mBinding.firstAdapter = mFirstAdapter
        mBinding.secAdapter = mSecAdapter
        mBinding.divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        mBinding.firstItemClick = OnItemClickListener { position, _ ->
            mFirstAdapter.getItemData(position)?.let {
                mFirstData = it
                mViewModel.firstSelectedPosition.value = position
                mViewModel.children.value = it.children as MutableList<SystemCategory>
                mViewModel.secSelectedPosition.value = -1
            }
        }
        mBinding.offset = (screenWidth * 0.4f).toInt()

        mViewModel.children.observe(this, Observer<MutableList<SystemCategory>> {
            mSecAdapter.setNewData(it)
        })

        mBinding.secItemClick = OnItemClickListener { position, _ ->
            mSecAdapter.getItemData(position)?.let {
                mOnClick?.invoke(this, mFirstData?.name, it.name, it.id)
                mViewModel.secSelectedPosition.value = position
            }
        }

        launch {
            mViewModel.getTypeList().catch { dismiss() }
                .collectLatest {
                    mFirstAdapter.setNewData(it)
                    mFirstData = it[mViewModel.firstSelectedPosition.value ?: 0]
                    mViewModel.children.value =
                        it[mViewModel.firstSelectedPosition.value ?: 0].children as MutableList<SystemCategory>
                }
        }

        mViewModel.firstSelectedPosition.observe(this, Observer<Int> {
            mFirstAdapter.updateSelectedPosition(it)
            mBinding.firstPosition = it
        })

        mViewModel.secSelectedPosition.observe(this, Observer<Int> {
            mSecAdapter.updateSelectedPosition(it)
            mBinding.secPosition = it
        })
    }

    override fun dialogFragmentAttributes() = dialog?.window?.attributes?.apply {
        width = (screenWidth * 0.8f).toInt()
        height = width
        gravity = Gravity.CENTER
    }
}
