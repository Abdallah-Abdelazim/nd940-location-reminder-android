package com.abdallah_abdelazim.locationreminder.feature.reminders.reminderslist

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.abdallah_abdelazim.locationreminder.R
import com.abdallah_abdelazim.locationreminder.base.BaseFragment
import com.abdallah_abdelazim.locationreminder.base.NavigationCommand
import com.abdallah_abdelazim.locationreminder.databinding.FragmentRemindersBinding
import com.abdallah_abdelazim.locationreminder.feature.authentication.AuthenticationActivity
import com.abdallah_abdelazim.locationreminder.utils.setup
import com.firebase.ui.auth.AuthUI
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    override val viewModel: RemindersListViewModel by viewModel()

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_reminders, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)

        binding.refreshLayout.setOnRefreshListener { viewModel.loadReminders() }

        viewModel.showLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading) binding.refreshLayout.isRefreshing = false;
        }

        setupRecyclerView()

        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()

        // load the reminders list on the ui
        viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        // use the navigationCommand live data to navigate between the fragments
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

        // setup the recycler view using the extension function
        binding.remindersRecyclerView.setup(adapter)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
            val intent = Intent(
                requireContext(),
                com.abdallah_abdelazim.locationreminder.feature.authentication.AuthenticationActivity::class.java
            )
            startActivity(intent)
            requireActivity().finish()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), R.string.logout_failed, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

}
