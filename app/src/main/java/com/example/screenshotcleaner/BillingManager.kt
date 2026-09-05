package com.example.screenshotcleaner

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

class BillingManager(context: Context, private val onPremium: (Boolean)->Unit): PurchasesUpdatedListener {
    private val prefs=context.getSharedPreferences("premium",Context.MODE_PRIVATE)
    private val client=BillingClient.newBuilder(context).setListener(this).enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build()
    var premium:Boolean=prefs.getBoolean("premium",false); private set
    fun connect(){ client.startConnection(object:BillingClientStateListener{
        override fun onBillingServiceDisconnected(){}
        override fun onBillingSetupFinished(r:BillingResult){ if(r.responseCode==BillingClient.BillingResponseCode.OK) query() }
    }) }
    private fun query(){ client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()){r,list-> if(r.responseCode==0 && list.any{it.products.contains(PRODUCT_ID) && it.purchaseState==Purchase.PurchaseState.PURCHASED}){ premium=true;prefs.edit().putBoolean("premium",true).apply();onPremium(true)} } }
    fun buy(activity:Activity){ client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(QueryProductDetailsParams.Product.newBuilder().setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP).build())).build()){r,details->
        val d=details.firstOrNull() ?: return@queryProductDetailsAsync
        val params=BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(d).build()
        client.launchBillingFlow(activity,BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
    }}
    override fun onPurchasesUpdated(r:BillingResult?, purchases:MutableList<Purchase>?){ if(r?.responseCode==0) purchases.orEmpty().forEach{ if(it.products.contains(PRODUCT_ID) && it.purchaseState==Purchase.PurchaseState.PURCHASED){ if(!it.isAcknowledged) client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(it.purchaseToken).build()){}; premium=true;prefs.edit().putBoolean("premium",true).apply();onPremium(true)}} }
    companion object { const val PRODUCT_ID="remove_ads_lifetime" }
}
