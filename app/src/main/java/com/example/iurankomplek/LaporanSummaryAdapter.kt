package com.example.iurankomplek

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LaporanSummaryAdapter(private var summaryData: LaporanSummaryData?) :
    RecyclerView.Adapter<LaporanSummaryAdapter.ListViewHolder>() {
    
    fun setSummaryData(data: LaporanSummaryData) {
        this.summaryData = data
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return ListViewHolder(view)
    }
    
    override fun getItemCount(): Int = if (summaryData != null) 1 else 0
    
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val data = summaryData
        if (data != null) {
            holder.IuranPerwargaTextView.text = data.iuranPerwargaText
            holder.jumlahIuranBulananTextView.text = data.jumlahIuranBulananText
            holder.totalIuranRekapTextView.text = data.totalIuranRekapText
            holder.pengeluaranTextView.text = data.pengeluaranText
            holder.pemanfaatanTextView.text = data.pemanfaatanText
        }
    }
    
    class ListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var IuranPerwargaTextView: TextView = itemView.findViewById(R.id.IuranPerwargaTextView)
        var jumlahIuranBulananTextView: TextView = itemView.findViewById(R.id.jumlahIuranBulananTextView)
        var totalIuranRekapTextView: TextView = itemView.findViewById(R.id.totalIuranRekapTextView)
        var pengeluaranTextView: TextView = itemView.findViewById(R.id.pengeluaranTextView)
        var pemanfaatanTextView: TextView = itemView.findViewById(R.id.pemanfaatanTextView)
    }
}

data class LaporanSummaryData(
    val iuranPerwargaText: String,
    val jumlahIuranBulananText: String,
    val totalIuranRekapText: String,
    val pengeluaranText: String,
    val pemanfaatanText: String
)