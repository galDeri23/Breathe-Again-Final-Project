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
                val items = mutableListOf<ShareItem>()
                for (child in snapshot.children) {
                    val item = child.getValue(ShareItem::class.java)
                    if (item != null) {
                        items.add(item)
                    }
                }
                Log.d("ShareViewModel", "Fetched ${items.size} items: $items")
                _shareItems.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ShareViewModel", "Error fetching data: ${error.message}")
            }
        })
    }

    fun addShareItem(daysSmokeFree: Int, moneySaved: Double) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("ShareViewModel", "Adding new share item for user $userId")

            val newItem = ShareItem(userId, daysSmokeFree, moneySaved)
            val newItemKey = database.push().key  // יצירת מפתח ייחודי לכל פריט

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
