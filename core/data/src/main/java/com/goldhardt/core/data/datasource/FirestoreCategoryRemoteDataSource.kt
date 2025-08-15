package com.goldhardt.core.data.datasource

import com.goldhardt.core.data.model.Category
import com.goldhardt.core.data.model.CategoryDocument
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCategoryRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : CategoryRemoteDataSource {

    private val collectionName = "categories"

    override fun observeCategories(userId: String): Flow<List<Category>> = callbackFlow {
        val registration = firestore.collection(collectionName)
            .whereEqualTo("userId", userId)
            .orderBy("name")
            .addSnapshotListener { snap: QuerySnapshot?, err: FirebaseFirestoreException? ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(mapCategories(snap))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun getCategories(userId: String): List<Category> {
        val snap = firestore.collection(collectionName)
            .whereEqualTo("userId", userId)
            .orderBy("name")
            .get()
            .await()
        return mapCategories(snap)
    }

    override suspend fun updateCategory(category: Category) {
        val docId = category.id
        val data = mapOf(
            "name" to category.name,
            "icon" to category.icon,
            "color" to category.color,
        )
        firestore.collection(collectionName)
            .document(docId)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun addCategory(userId: String, name: String, icon: String, color: String): Category {
        val docRef = firestore.collection(collectionName).document()
        val data = mapOf(
            "name" to name,
            "icon" to icon,
            "color" to color,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp(),
        )
        docRef.set(data).await()
        // Read back the document to get resolved server timestamp
        val snap = docRef.get().await()
        val createdAt = snap.getTimestamp("createdAt") ?: Timestamp.now()
        return Category(
            id = snap.id,
            name = name,
            createdAt = createdAt,
            userId = userId,
            icon = icon,
            color = color,
        )
    }

    override suspend fun deleteCategory(categoryId: String) {
        firestore.collection(collectionName)
            .document(categoryId)
            .delete()
            .await()
    }

    private fun mapCategories(snap: QuerySnapshot?): List<Category> {
        if (snap == null) return emptyList()
        return snap.documents.mapNotNull { doc ->
            val data = doc.toObject(CategoryDocument::class.java)
            val name = data?.name
            val createdAt = data?.createdAt
            val userId = data?.userId
            val icon = data?.icon
            val color = data?.color
            if (name == null || createdAt == null || userId == null || icon == null || color == null) {
                null
            } else {
                Category(
                    id = doc.id,
                    name = name,
                    createdAt = createdAt,
                    userId = userId,
                    icon = icon,
                    color = color,
                )
            }
        }
    }
}
