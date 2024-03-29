package at.spiceburg.roarfit.features.main.camera

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import at.spiceburg.roarfit.R
import at.spiceburg.roarfit.features.main.MainActivity
import at.spiceburg.roarfit.features.main.MainViewModel
import github.nisrulz.qreader.QRDataListener
import github.nisrulz.qreader.QREader
import kotlinx.android.synthetic.main.fragment_camera.*
import java.util.*

class CameraFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var qrReader: QREader
    private lateinit var cameraView: SurfaceView

    private var statusBarColor: Int = 0
    private var navigationBarColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = (requireActivity() as MainActivity)

        val mainHandler = Handler(Looper.getMainLooper())
        val qrListener = QRDataListener { data ->
            mainHandler.post {
                // this@CameraFragment is required in this block
                viewModel.getEquipment().observe(this@CameraFragment.viewLifecycleOwner) { res ->
                    when {
                        res.isSuccess() -> {
                            activity.progressMain.hide()
                            val equipment: Array<String> = res.data!!
                            val foundEquipment: String? = equipment.find {
                                it.toLowerCase(Locale.US) == data.toLowerCase(Locale.US)
                            }
                            if (foundEquipment != null) {
                                text_camera_equipment.setText(foundEquipment)
                                val action =
                                    CameraFragmentDirections.actionCameraFragmentToExerciseListFragment(
                                        foundEquipment
                                    )
                                this@CameraFragment.findNavController().navigate(action)
                            }
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
        }

        cameraView = view.findViewById(R.id.surfaceview_camera)

        // init QR-Reader
        qrReader = QREader.Builder(requireContext(), cameraView, qrListener)
            .facing(QREader.BACK_CAM)
            .enableAutofocus(true)
            .width(cameraView.width)
            .height(cameraView.height)
            .build()

        button_camera_close.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()

        // change theme to fit camera
        requireActivity().apply {
            // allow the modification of system bar colors
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            // backup the main theme colors
            statusBarColor = window.statusBarColor
            navigationBarColor = window.navigationBarColor

            // set the camera theme colors
            window.statusBarColor = resources.getColor(R.color.black, null)
            window.navigationBarColor = resources.getColor(R.color.black, null)
        }

        qrReader.initAndStart(cameraView)
    }

    override fun onPause() {
        super.onPause()
        qrReader.releaseAndCleanup()
    }

    override fun onStop() {
        super.onStop()

        qrReader.stop()

        // reset theme
        requireActivity().apply {
            window.statusBarColor = statusBarColor
            window.navigationBarColor = navigationBarColor
        }
    }

    companion object {
        val TAG = CameraFragment::class.java.simpleName
    }
}
