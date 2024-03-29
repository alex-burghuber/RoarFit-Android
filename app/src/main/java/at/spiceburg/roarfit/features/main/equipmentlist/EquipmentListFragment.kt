package at.spiceburg.roarfit.features.main.equipmentlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import at.spiceburg.roarfit.R
import at.spiceburg.roarfit.features.main.MainActivity
import at.spiceburg.roarfit.features.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_equipment_list.*

class EquipmentListFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_equipment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val onEquipmentClicked: (equipment: String) -> Unit = { equipment ->
            val action =
                EquipmentListFragmentDirections.actionEquipmentListToExerciseList(equipment)
            findNavController().navigate(action)
        }

        val adapter = EquipmentListAdapter(requireContext(), onEquipmentClicked)
        recyclerview_equipmentlist_equipments.adapter = adapter
        recyclerview_equipmentlist_equipments.layoutManager = LinearLayoutManager(requireContext())

        val activity = (requireActivity() as MainActivity)

        viewModel.getEquipment().observe(viewLifecycleOwner) { res ->
            when {
                res.isSuccess() -> {
                    val equipment: Array<String> = res.data!!
                    adapter.setExerciseTemplates(equipment)
                    activity.progressMain.hide()
                }
                res.isLoading() -> {
                    activity.progressMain.show()
                }
                else -> {
                    activity.progressMain.hide()
                    activity.handleNetworkError(res.error!!)
                }
            }
        }
    }

    companion object {
        private val TAG = EquipmentListFragment::class.java.simpleName
    }
}
