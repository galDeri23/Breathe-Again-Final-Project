package com.example.finalproject_breathe_again.ui.share

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ShareViewModel : ViewModel() {

    private val _shareItems = MutableLiveData<List<ShareItem>>()
    val shareItems: LiveData<List<ShareItem>> = _shareItems

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("shareItems")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    init {
        fetchShareItems()
    }

    private fun fetchShareItems() {
        Log.d("ShareViewModel", "Fetching share items from Realtime Database")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(ShareItem::class.java) }
                _shareItems.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShareViewModel", "Error fetching shares: ${error.message}")
            }
        })
    }

    fun addShareItem(daysSmokeFree: Int, moneySaved: Double, progressGraph: Int) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val username = currentUser.displayName ?: "Anonymous"

            val newItem = ShareItem(
                username = username,
                userId = userId,
                daysFree = daysSmokeFree,
                moneySaved = moneySaved,
                progressGraph = progressGraph
            )

            val newItemKey = database.push().key
            if (newItemKey != null) {
                database.child(newItemKey).setValue(newItem)
                    .addOnSuccessListener {
                        Log.d("ShareViewModel", "Item added successfully: $newItem")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ShareViewModel", "Error adding item: ${e.message}", e)
                    }
            }
        } else {
            Log.e("ShareViewModel", "Current user is null. Cannot add share item.")
        }
    }
}
