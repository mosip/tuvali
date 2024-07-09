package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import io.mosip.tuvali.common.events.SecureChannelEstablishedEvent
import io.mosip.tuvali.verifier.Verifier
import io.mosip.tuvali.wallet.Wallet


class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
  var wallet= Wallet(this)
  var verifier = Verifier(this)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Common.requestForRequiredPermissions(this@MainActivity, this, this::showActionsView)
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (grantResults.any { it != 0 }) {
      println("Result list: $grantResults")
      showPermErrorView()
      return
    }
    Log.i("Tuvali", "${grantResults.joinToString()} for $requestCode")
    Common.requestForRequiredPermissions(this@MainActivity, this, this::showActionsView)
  }

  private fun showActionsView() {
    setContentView(R.layout.activity_main)
    findViewById<Button>(R.id.walletButton).let {
      it?.setOnClickListener {
        this.startScanning()
      }
    }

    findViewById<Button>(R.id.verifierButton).let {
      it?.setOnClickListener {
        this.startAdvertising()
      }
    }
  }

  private fun startScanning() {
    showLoadingLayout()
    updateLoadingText(getString(R.string.ScanningMessage))
    setCancelLoadingButton {
      this.stopScanning()
    }
    val uri =
      "OPENID4VP://connect:?name=OVPMOSIP&key=22d9039c7117c47756a759ca17c5622155a56b0cf7e4acd294813948fd34a136"
    wallet.startConnection(uri)
    wallet.subscribe {
       event ->
      println("Event: $event")
      when (event) {
        is SecureChannelEstablishedEvent -> showSendDataLayout(getString(R.string.data_transfer))
      }
    }
    Log.i("Tuvali","Starting Scan")
  }

  private fun startAdvertising() {
    var openId4vpURI = verifier.startAdvertisement("OVPMOSIP")

    Log.i("Tuvali", openId4vpURI)
    updateQRCodeData(openId4vpURI)
    showLoadingLayout()
    updateLoadingText(getString(R.string.broadcastingMessage))
    setCancelLoadingButton  {
      this.stopAdvertisement()
    }
    verifier.subscribe { event ->
      println("Event: $event")
      when (event) {
        is SecureChannelEstablishedEvent -> showSendDataLayout(getString(R.string.waiting_for_data))
      }
    }
    Log.i("Tuvali", "Waiting for wallet to connect")
  }

  private fun stopAdvertisement() {
    verifier.disconnect()
    showActionsLayout()
    Log.i("Tuvali", "Stopping advertising")
  }

  private fun stopScanning() {
    wallet.disconnect()
    showActionsLayout()
    Log.i("Tuvali","Stopping Scan")
  }

  private fun setCancelLoadingButton(onClick: (View) -> Unit) {
    findViewById<Button>(R.id.cancelLoadingBtn).let {
      it?.setOnClickListener(onClick)
    }
  }

  private fun showSendDataLayout(data: String) {
    println("Hello from here")
    findViewById<LinearLayout>(R.id.actionsLayout).let {
      it?.setVisibility(View.GONE)
    }
    updateTransferText(data)
    findViewById<LinearLayout>(R.id.sendDataLayout).let {
      it?.setVisibility(View.VISIBLE)
    }
  }

  private fun showActionsLayout() {
    findViewById<LinearLayout>(R.id.loaderLayout).let {
      it?.setVisibility(View.GONE)
    }

    findViewById<LinearLayout>(R.id.actionsLayout).let {
      it?.setVisibility(View.VISIBLE)
    }
  }

  private fun showLoadingLayout() {
    findViewById<LinearLayout>(R.id.actionsLayout).let {
      it?.setVisibility(View.GONE)
    }
    findViewById<LinearLayout>(R.id.loaderLayout).let {
      it?.setVisibility(View.VISIBLE)
    }
  }


  private fun showPermErrorView() {
    setContentView(R.layout.activity_main_error)
    findViewById<TextView>(R.id.errorText).text = getString(R.string.permission_error_message)
    findViewById<Button>(R.id.requestPermBtn).setOnClickListener {
      Common.requestForRequiredPermissions(
        this@MainActivity,
        this,
        this::showActionsView
      )
    }
  }

  private fun updateLoadingText(message: String) {
    findViewById<TextView>(R.id.loadingText).let {
      it?.setText(message)
    }
  }
  private fun updateQRCodeData(message: String) {
    findViewById<TextView>(R.id.qrCodeData).let {
      it?.setText(message)
    }
  }
  private fun updateTransferText(message: String) {
    findViewById<TextView>(R.id.transferText).let {
      it?.setText(message)
    }
  }

  /*private fun moveToChatActivity(mode: Int) {
    val intent = Intent(this@MainActivity, ChatActivity::class.java)
    intent.putExtra("mode", mode)
    startActivity(intent)
  }*/

  private fun failureDialog(errMessage: String) {
    runOnUiThread {
      AlertDialog.Builder(this)
        .setTitle("Connection Failed")
        .setMessage(errMessage)
        .show()
    }
  }
}
