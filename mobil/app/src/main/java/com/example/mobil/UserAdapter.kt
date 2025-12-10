package com.example.mobil

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobil.databinding.AdminKullaniciCardBinding

class UserAdapter(
    private val userList: ArrayList<User>,
    private val onUpdateClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit,
    private val onFreezeClick: (User) -> Unit) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: AdminKullaniciCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = AdminKullaniciCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]

        // 1. Verileri Yazdır
        holder.binding.txtUserName.text = currentUser.name
        holder.binding.txtUserEmail.text = currentUser.email


        holder.binding.btnUpdate.setOnClickListener {
            onUpdateClick(currentUser)
        }
        holder.binding.btnDelete.setOnClickListener {
            onDeleteClick(currentUser)
        }
        holder.binding.btnFreeze.setOnClickListener {
            onFreezeClick(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}