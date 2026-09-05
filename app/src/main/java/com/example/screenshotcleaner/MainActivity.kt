package com.example.screenshotcleaner

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdView
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class MainActivity:ComponentActivity(){
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main)
    private lateinit var scanner:Scanner; private lateinit var adapter:ShotAdapter; private lateinit var summary:TextView; private lateinit var progress:ProgressBar
    private val selected=mutableSetOf<Long>(); private var shots=emptyList<Shot>(); private lateinit var billing:BillingManager
    override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main);scanner=Scanner(contentResolver);setup();requestPermission();billing=BillingManager(this){hideAds()};billing.connect();MobileAds.initialize(this){} }
    private fun setup(){
        summary=findViewById(R.id.summary);progress=findViewById(R.id.progress)
        val list=findViewById<RecyclerView>(R.id.list); list.layoutManager=LinearLayoutManager(this); adapter=ShotAdapter(shots,selected){updateSummary()};list.adapter=adapter
        findViewById<Button>(R.id.scan).setOnClickListener{scan()}; findViewById<Button>(R.id.all).setOnClickListener{selected.clear();selected.addAll(shots.map{it.id});adapter.notifyDataSetChanged();updateSummary()};findViewById<Button>(R.id.clear).setOnClickListener{selected.clear();adapter.notifyDataSetChanged();updateSummary()};findViewById<Button>(R.id.delete).setOnClickListener{confirmDelete()};findViewById<Button>(R.id.pro).setOnClickListener{billing.buy(this)}
    }
    private fun requestPermission(){val ps=if(Build.VERSION.SDK_INT>=34) arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) else if(Build.VERSION.SDK_INT>=33) arrayOf(Manifest.permission.READ_MEDIA_IMAGES) else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE); if(ps.any{ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}) ActivityCompat.requestPermissions(this,ps,10)}
    private fun scan(){progress.visibility=View.VISIBLE;summary.text="Scanning screenshots…";scope.launch(Dispatchers.IO){val base=scanner.scanScreenshots();val enriched=base.mapIndexed{idx,s-> if(idx<3000) s.copy(exactHash=scanner.exactHash(s)) else s };shots=enriched;val total=shots.sumOf{it.size};withContext(Dispatchers.Main){progress.visibility=View.GONE;selected.clear();adapter.submit(shots);summary.text="${shots.size} screenshots • ${fmt(total)}";showSmartSummary()}}}
    private fun showSmartSummary(){val groups=shots.filter{it.exactHash!=null}.groupBy{it.exactHash!!}.filter{it.value.size>1};val dupBytes=groups.values.sumOf{g->g.drop(1).sumOf{it.size}};if(groups.isNotEmpty())summary.text+="
${groups.size} duplicate groups • ${fmt(dupBytes)} removable duplicates"}
    private fun confirmDelete(){if(selected.isEmpty()){Toast.makeText(this,"Select screenshots first",Toast.LENGTH_SHORT).show();return};AlertDialog.Builder(this).setTitle("Delete selected screenshots?").setMessage("${selected.size} screenshot(s) will be moved to the system trash when supported.").setNegativeButton("Cancel",null).setPositiveButton("Delete"){_,_->deleteSelected()}.show()}
    private fun deleteSelected(){scope.launch(Dispatchers.IO){var n=0;shots.filter{selected.contains(it.id)}.forEach{s->if(contentResolver.delete(s.uri,null,null)>0)n++};withContext(Dispatchers.Main){selected.clear();Toast.makeText(this@MainActivity,"$n screenshot(s) deleted",Toast.LENGTH_SHORT).show();scan()}}}
    private fun updateSummary(){if(shots.isNotEmpty())summary.text="${shots.size} screenshots • ${selected.size} selected"}
    private fun hideAds(){findViewById<View>(R.id.adPlaceholder).visibility=View.GONE;findViewById<Button>(R.id.pro).text="Premium ✓"}
    private fun fmt(b:Long)=when{b<1024->"$b B";b<1024*1024->"${b/1024} KB";b<1024*1024*1024->"%.1f MB".format(b/1024.0/1024.0);else->"%.2f GB".format(b/1024.0/1024.0/1024.0)}
    override fun onDestroy(){scope.cancel();super.onDestroy()}
}
