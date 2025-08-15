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

    private companion object {
        const val COLLECTION = "categories"
        const val FIELD_USER_ID = "userId"
        const val FIELD_NAME = "name"
        const val FIELD_ICON = "icon"
        const val FIELD_COLOR = "color"
        const val FIELD_CREATED_AT = "createdAt"
    }

    override fun observeCategories(userId: String): Flow<List<Category>> = callbackFlow {
        val registration = firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_USER_ID, userId)
            .orderBy(FIELD_NAME)
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
        val snap = firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_USER_ID, userId)
            .orderBy(FIELD_NAME)
            .get()
            .await()
        return mapCategories(snap)
    }

    override suspend fun updateCategory(category: Category) {
        val docId = category.id
        val data = mapOf(
            FIELD_NAME to category.name,
            FIELD_ICON to category.icon,
            FIELD_COLOR to category.color,
        )
        firestore.collection(COLLECTION)
            .document(docId)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun addCategory(userId: String, name: String, icon: String, color: String): Category {
        val docRef = firestore.collection(COLLECTION).document()
        val data = mapOf(
            FIELD_NAME to name,
            FIELD_ICON to icon,
            FIELD_COLOR to color,
            FIELD_USER_ID to userId,
            FIELD_CREATED_AT to FieldValue.serverTimestamp(),
        )
        docRef.set(data).await()
        // Read back the document to get resolved server timestamp
        val snap = docRef.get().await()
        val createdAt = snap.getTimestamp(FIELD_CREATED_AT) ?: Timestamp.now()
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
        firestore.collection(COLLECTION)
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
