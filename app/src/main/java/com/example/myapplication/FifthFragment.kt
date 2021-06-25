package com.example.myapplication

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.example.myapplication.databinding.FragmentFifthBinding
import kotlinx.coroutines.delay

class MyString(val str : String) {

}

/**
 * Fifth fragment class
 * This fragment is the actual "game GUI", where three cards are presented to the GUI
 * The current player needs to make a statement within 5 sec. The leftover time is displayed in real-time
 * The other player need to evaluate the made statement. If the current player did a mistake, they need to push the "wrong" button
 *
 * @author Lukas Boril
 * @version 2021.06.11
 */
class FifthFragment : Fragment() {

    private var _binding: FragmentFifthBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFifthBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set view to landscape
        getActivity()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // link the timer CountDownViewModel to the textView
        val modelTime: CountDownViewModel by activityViewModels()
        modelTime.leftOverTime.observe(viewLifecycleOwner, Observer<Int> { newVal ->
            // update UI
            binding.f5TimerTextView.text = newVal.toString()
        })

        // Setting up a timer that counts down from 5
        var timePassed= 0
        val timer = object: CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                modelTime.leftOverTime.value = (5-timePassed)
                timePassed++
            }

            // once it's done, inform the backend that no mistake was made and that it's the next plaxers turn
            override fun onFinish() {
                timePassed= 0
                val requestQueue = Volley.newRequestQueue(requireContext())

                var request = StringRequest(
                    Request.Method.PUT, "http://10.0.2.2:8080/roundCounter",
                    Response.Listener<String> {
                    },
                    Response.ErrorListener {
                        //use the porvided VolleyError to display
                        //an error message
                        Log.e("ERROR", it.message!! )
                    })
                requestQueue.add(request)

                request = StringRequest(
                    Request.Method.GET, "http://10.0.2.2:8080/next",
                    Response.Listener<String> {
                    },
                    Response.ErrorListener {
                        //use the porvided VolleyError to display
                        //an error message
                        Log.e("ERROR", it.message!! )
                    })
                requestQueue.add(request)

                // navigate back to next-player fragment
                view?.post {
                    findNavController().navigate(R.id.action_fifthFragment_to_FourthFragment)
                }
            }
        }
        // start the timer
        timer.start()

        // Binding of the button and setting it up. If it gets pushed the backend is informed that it's the next plaxers turn. Navigate to fragment 4
        binding.f5Button.setOnClickListener {
            // REQUEST: tell backend to get next player
            val requestQueue = Volley.newRequestQueue(requireContext())
            val request = StringRequest(
                Request.Method.GET, "http://10.0.2.2:8080/next",
                Response.Listener<String> {
                },
                Response.ErrorListener {
                    //use the porvided VolleyError to display
                    //an error message
                    Log.e("ERROR", it.message!! )
                })
            requestQueue.add(request)
            timer.cancel()
            view?.post {
                findNavController().navigate(R.id.action_fifthFragment_to_FourthFragment)
            }
        }

        // binding of textViews for cards
        var viewCard1 = binding.f5ImageView

        var viewCard2 = binding.f5ImageView2

        var viewCard3 = binding.f5ImageView3

        // getting new cards and displaying them in ImageView 0 to 2
        val requestQueue = Volley.newRequestQueue(requireContext())
        val request = StringRequest(
            Request.Method.GET, "http://10.0.2.2:8080/openCards",
            Response.Listener<String> { response ->
                val allOpenCards = ArrayList(Klaxon().parseArray<Card>(response))
                if (allOpenCards != null) {
                    var addressFirstCard = produceCardAccessString(allOpenCards.get(0))
                    var id = resources.getIdentifier("com.example.myapplication:drawable/" + addressFirstCard, null, null)
                    viewCard1.setImageResource(id)

                    var addressSecondCard = produceCardAccessString(allOpenCards.get(1))
                    id = resources.getIdentifier("com.example.myapplication:drawable/" + addressSecondCard, null, null)
                    viewCard2.setImageResource(id)

                    var addressThirdCard = produceCardAccessString(allOpenCards.get(2))
                    id = resources.getIdentifier("com.example.myapplication:drawable/" + addressThirdCard, null, null)
                    viewCard3.setImageResource(id)
                }
            },
            Response.ErrorListener {
                //use the porvided VolleyError to display
                //an error message
                Log.e("ERROR", it.message!! )
            })
        requestQueue.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // funtion to produce strings used to access the drawable
    fun produceCardAccessString(card : Card): String {
        var cardColor = card.getCardColor()
        var cardAnimal = card.getCardAnimal()
        val output = cardAnimal + "_" + cardColor
        return output.toLowerCase()
    }

}