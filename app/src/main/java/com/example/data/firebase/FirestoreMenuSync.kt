package com.example.data.firebase

import android.util.Log
import com.example.data.local.dao.CafeDao
import com.example.data.local.entity.MenuItemEntity
import com.example.data.local.entity.OrderEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Service to sync menu items and special offers with Cloud Firestore in real-time.
 * Structure: collection("cafes").document(cafeId).collection("menu_items").document(itemId)
 */
class FirestoreMenuSync(
    private val cafeDao: CafeDao,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "FirestoreMenuSync"
        private const val COLLECTION_CAFES = "cafes"
        private const val SUBCOLLECTION_MENU = "menu_items"
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private var menuListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null

    /**
     * Starts listening to Firestore changes for menu items and syncing them to the local Room database.
     */
    fun startListening(cafeId: String) {
        if (menuListener != null) return

        try {
            val menuRef = firestore.collection(COLLECTION_CAFES)
                .document(cafeId)
                .collection(SUBCOLLECTION_MENU)

            menuListener = menuRef.addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore listen error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshots.documents) {
                                val item = docToMenuItemEntity(doc.id, cafeId, doc.data ?: continue)
                                cafeDao.insertMenuItem(item)
                            }
                            Log.d(TAG, "Synced ${snapshots.size()} items from Firestore to Room")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error saving Firestore items into Room", e)
                        }
                    }
                }
            }

            // Real-time listener for customer web orders
            val ordersRef = firestore.collection(COLLECTION_CAFES)
                .document(cafeId)
                .collection("orders")

            ordersListener = ordersRef.addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore orders listen error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            for (doc in snapshots.documents) {
                                val data = doc.data ?: continue
                                val order = OrderEntity(
                                    id = doc.id,
                                    cafeId = (data["cafeId"] as? String) ?: cafeId,
                                    tableNumber = (data["tableNumber"] as? Number)?.toInt() ?: 1,
                                    customerName = (data["customerName"] as? String) ?: "",
                                    orderNotes = (data["orderNotes"] as? String) ?: "",
                                    itemsSummary = (data["itemsSummary"] as? String) ?: "",
                                    itemsJson = (data["itemsJson"] as? String) ?: "[]",
                                    subtotal = (data["subtotal"] as? Number)?.toDouble() ?: 0.0,
                                    serviceFee = (data["serviceFee"] as? Number)?.toDouble() ?: 0.0,
                                    totalAmount = (data["totalAmount"] as? Number)?.toDouble() ?: 0.0,
                                    status = (data["status"] as? String) ?: "APPROVAL",
                                    isPaid = (data["isPaid"] as? Boolean) ?: false,
                                    paidAt = (data["paidAt"] as? Number)?.toLong(),
                                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                                )
                                cafeDao.insertOrder(order)
                            }
                            Log.d(TAG, "Synced incoming web orders to Room")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing web orders into Room", e)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start listening to Firestore", e)
        }
    }

    /**
     * Uploads or updates a menu item to Firestore.
     */
    fun syncMenuItemToFirestore(item: MenuItemEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val data = menuItemToMap(item)
                firestore.collection(COLLECTION_CAFES)
                    .document(item.cafeId)
                    .collection(SUBCOLLECTION_MENU)
                    .document(item.id)
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Successfully synced item ${item.id} to Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing item ${item.id} to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Deletes a menu item from Firestore.
     */
    fun deleteMenuItemFromFirestore(cafeId: String, itemId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                firestore.collection(COLLECTION_CAFES)
                    .document(cafeId)
                    .collection(SUBCOLLECTION_MENU)
                    .document(itemId)
                    .delete()
                    .await()
                Log.d(TAG, "Successfully deleted item $itemId from Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting item $itemId from Firestore: ${e.message}")
            }
        }
    }

    /**
     * Syncs cafe details (name, logo, etc.) to Firestore so the web menu displays the customized logo and name.
     */
    fun syncCafeProfileToFirestore(cafeId: String, name: String, logoIconName: String) {
        scope.launch(Dispatchers.IO) {
            try {
                firestore.collection(COLLECTION_CAFES)
                    .document(cafeId)
                    .set(
                        mapOf(
                            "id" to cafeId,
                            "name" to name,
                            "logoIconName" to logoIconName,
                            "updatedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()
                Log.d(TAG, "Successfully synced cafe profile to Firestore")
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing cafe profile to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Seeds initial local items to Firestore if Firestore is empty.
     */
    fun seedLocalItemsToFirestore(cafeId: String, localItems: List<MenuItemEntity>) {
        scope.launch(Dispatchers.IO) {
            try {
                val menuRef = firestore.collection(COLLECTION_CAFES)
                    .document(cafeId)
                    .collection(SUBCOLLECTION_MENU)

                val snapshot = menuRef.limit(1).get().await()
                if (snapshot.isEmpty && localItems.isNotEmpty()) {
                    Log.d(TAG, "Firestore collection empty. Seeding ${localItems.size} items to Firestore...")
                    for (item in localItems) {
                        menuRef.document(item.id).set(menuItemToMap(item)).await()
                    }
                    Log.d(TAG, "Seeding to Firestore completed!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking/seeding Firestore: ${e.message}")
            }
        }
    }

    private fun menuItemToMap(item: MenuItemEntity): Map<String, Any?> {
        return mapOf(
            "id" to item.id,
            "cafeId" to item.cafeId,
            "categoryId" to item.categoryId,
            "name" to item.name,
            "description" to item.description,
            "originalPrice" to item.originalPrice,
            "discountedPrice" to item.discountedPrice,
            "discountPercentage" to item.discountPercentage,
            "isSpecialOffer" to item.isSpecialOffer,
            "showInOffers" to item.showInOffers,
            "isAvailable" to item.isAvailable,
            "iconName" to item.iconName,
            "customImageUri" to item.customImageUri,
            "offerBackgroundImageUri" to item.offerBackgroundImageUri,
            "updatedAt" to System.currentTimeMillis()
        )
    }

    private fun docToMenuItemEntity(id: String, defaultCafeId: String, map: Map<String, Any?>): MenuItemEntity {
        val cafeId = (map["cafeId"] as? String) ?: defaultCafeId
        val categoryId = (map["categoryId"] as? String) ?: "cat_coffee"
        val name = (map["name"] as? String) ?: ""
        val description = (map["description"] as? String) ?: ""
        val originalPrice = when (val p = map["originalPrice"]) {
            is Number -> p.toDouble()
            else -> 0.0
        }
        val discountedPrice = when (val p = map["discountedPrice"]) {
            is Number -> p.toDouble()
            else -> null
        }
        val discountPercentage = when (val p = map["discountPercentage"]) {
            is Number -> p.toInt()
            else -> null
        }
        val isSpecialOffer = (map["isSpecialOffer"] as? Boolean) ?: false
        val showInOffers = (map["showInOffers"] as? Boolean) ?: false
        val isAvailable = (map["isAvailable"] as? Boolean) ?: true
        val iconName = (map["iconName"] as? String) ?: "coffee"
        val customImageUri = map["customImageUri"] as? String
        val offerBackgroundImageUri = map["offerBackgroundImageUri"] as? String

        return MenuItemEntity(
            id = id,
            cafeId = cafeId,
            categoryId = categoryId,
            name = name,
            description = description,
            originalPrice = originalPrice,
            discountedPrice = discountedPrice,
            discountPercentage = discountPercentage,
            isSpecialOffer = isSpecialOffer,
            showInOffers = showInOffers,
            isAvailable = isAvailable,
            iconName = iconName,
            customImageUri = customImageUri,
            offerBackgroundImageUri = offerBackgroundImageUri
        )
    }

    fun stopListening() {
        menuListener?.remove()
        menuListener = null
        ordersListener?.remove()
        ordersListener = null
    }
}
