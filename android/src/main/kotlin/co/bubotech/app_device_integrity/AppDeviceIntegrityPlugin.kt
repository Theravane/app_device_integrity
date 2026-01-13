package co.bubotech.app_device_integrity

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** AppDeviceIntegrityPlugin */
class AppDeviceIntegrityPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {

  private lateinit var channel: MethodChannel
  private lateinit var context: Context
  private var activity: Activity? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app_attestation")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getAttestationServiceSupport") {
      if (call.argument<Long>("gcp") != null) {
        val nonce = call.argument<String>("nonce")
        val attestation = AppDeviceIntegrity(
          context,
          call.argument<Long>("gcp")!!,
          nonce
        )
        attestation.integrityTokenResponse.addOnSuccessListener { response ->
          val integrityToken: String = response.token()
          result.success(integrityToken.toString())
        }.addOnFailureListener { e ->
          println("integrityToken Error:="+e)
          result.error("INTEGRITY_ERROR", e.message, e)
        }
      } else {
        result.error("INVALID_ARGUMENTS", "GCP project number is required", null)
      }
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity;
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }
}