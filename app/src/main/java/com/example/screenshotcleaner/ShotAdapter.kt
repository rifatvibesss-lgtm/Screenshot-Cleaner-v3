package com.example.screenshotcleaner

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class ShotAdapter(private var items: List<Shot>, private val selected: MutableSet<Long>, private val onChanged: () -> Unit): RecyclerView.Adapter<ShotAdapter.VH>() {
    class VH(v: View): RecyclerView.ViewHolder(v) {
        val image=v.findViewById<ShapeableImageView>(R.id.thumb); val check=v.findViewById<CheckBox>(R.id.check); val name=v.findViewById<TextView>(R.id.name); val size=v.findViewById<TextView>(R.id.size)
    }
    override fun onCreateViewHolder(p: android.view.ViewGroup,v:Int): VH { val x=LayoutInflater.from(p.context).inflate(R.layout.item_shot,p,false); return VH(x) }
    override fun getItemCount()=items.size
    override fun onBindViewHolder(h:VH,pos:Int){ val s=items[pos]; h.name.text=s.name; h.size.text=format(s.size); h.check.setOnCheckedChangeListener(null); h.check.isChecked=selected.contains(s.id)
        h.image.setImageURI(s.uri); h.check.setOnCheckedChangeListener{_,yes->if(yes)selected.add(s.id) else selected.remove(s.id); onChanged()}; h.itemView.setOnClickListener{h.check.isChecked=!h.check.isChecked}
    }
    fun submit(x:List<Shot>){items=x;notifyDataSetChanged()}
    private fun format(b:Long)=when{b<1024->"$b B";b<1024*1024->"${b/1024} KB";else->"%.1f MB".format(b/1024.0/1024.0)}
}
