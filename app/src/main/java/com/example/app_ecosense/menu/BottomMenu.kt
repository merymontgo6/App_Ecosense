package com.example.app_ecosense.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.app_ecosense.PantallaHome
import com.example.app_ecosense.R
import com.example.app_ecosense.empresa.Contacte
import com.example.app_ecosense.empresa.Dispositius
import com.example.app_ecosense.empresa.InfoNosaltres

class BottomMenu : Fragment() {

    private var homeBoto: ImageButton? = null
    private var infoBoto: ImageButton? = null
    private var disBoto: ImageButton? = null
    private var conBoto: ImageButton? = null

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_menu, container, false)
        homeBoto = view.findViewById(R.id.homeBoto)
        infoBoto = view.findViewById(R.id.infoBoto)
        disBoto = view.findViewById(R.id.disBoto)
        conBoto = view.findViewById(R.id.conBoto)
        homeBoto?.setOnClickListener { startActivity(Intent(requireContext(), PantallaHome::class.java)) }
        infoBoto?.setOnClickListener { startActivity(Intent(requireContext(), InfoNosaltres::class.java)) }
        disBoto?.setOnClickListener { startActivity(Intent(requireContext(), Dispositius::class.java)) }
        conBoto?.setOnClickListener { startActivity(Intent(requireContext(), Contacte::class.java)) }
        return view
    }
}
