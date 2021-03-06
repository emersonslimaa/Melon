/*
 * Copyright (c) 2019.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.forcetower.uefs.feature.siecomp.speaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.NavUtils
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.lifecycle.Observer
import com.forcetower.uefs.R
import com.forcetower.uefs.core.injection.Injectable
import com.forcetower.uefs.core.vm.UViewModelFactory
import com.forcetower.uefs.databinding.FragmentEventSpeakerBinding
import com.forcetower.uefs.feature.shared.UFragment
import com.forcetower.uefs.feature.shared.extensions.inTransaction
import com.forcetower.uefs.feature.shared.extensions.postponeEnterTransition
import com.forcetower.uefs.feature.shared.extensions.provideActivityViewModel
import com.forcetower.uefs.feature.siecomp.editor.CreateSpeakerFragment
import com.forcetower.uefs.feature.siecomp.session.PushUpScrollListener
import com.forcetower.uefs.feature.siecomp.speaker.EventSpeakerActivity.Companion.SPEAKER_ID
import javax.inject.Inject

class SpeakerFragment : UFragment(), Injectable {
    @Inject
    lateinit var factory: UViewModelFactory

    private lateinit var speakerViewModel: SIECOMPSpeakerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        speakerViewModel = provideActivityViewModel(factory)
        speakerViewModel.setSpeakerId(requireNotNull(arguments).getLong(SPEAKER_ID))
        activity?.postponeEnterTransition(500L)

        val binding = FragmentEventSpeakerBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = this@SpeakerFragment
        }

        speakerViewModel.hasProfileImage.observe(this, Observer {
            if (!it) {
                activity?.startPostponedEnterTransition()
            }
        })

        val headLoadListener = object : ImageLoadListener {
            override fun onImageLoaded() { activity?.startPostponedEnterTransition() }
            override fun onImageLoadFailed() { activity?.startPostponedEnterTransition() }
        }

        val speakerAdapter = SpeakerAdapter(this, speakerViewModel, headLoadListener)
        binding.speakerDetailRecyclerView.run {
            adapter = speakerAdapter
            itemAnimator?.run {
                addDuration = 120L
                moveDuration = 120L
                changeDuration = 120L
                removeDuration = 100L
            }
            doOnLayout {
                addOnScrollListener(
                    PushUpScrollListener(binding.up, it, R.id.speaker_name, R.id.speaker_grid_image)
                )
            }
        }
        binding.up.setOnClickListener {
            NavUtils.navigateUpFromSameTask(requireActivity())
        }

        speakerViewModel.access.observe(this, Observer {
            binding.editFloat.visibility = if (it != null) {
                VISIBLE
            } else {
                GONE
            }
        })

        binding.editFloat.setOnClickListener {
            fragmentManager?.inTransaction {
                replace(R.id.speaker_container, CreateSpeakerFragment().apply {
                    arguments = this@SpeakerFragment.arguments
                })
                addToBackStack(null)
            }
        }

        return binding.root
    }

    companion object {
        fun newInstance(speakerId: Long): SpeakerFragment {
            return SpeakerFragment().apply {
                arguments = bundleOf(SPEAKER_ID to speakerId)
            }
        }
    }
}