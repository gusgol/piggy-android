package com.goldhardt.core.data.datasource

import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseDocument
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.model.ExpenseUpdate
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreExpenseRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ExpenseRemoteDataSource {

    private companion object {
        const val COLLECTION = "expenses"
        const val FIELD_USER_ID = "userId"
        const val FIELD_NAME = "name"
        const val FIELD_AMOUNT = "amount"
        const val FIELD_DATE = "date"
        const val FIELD_CATEGORY_ID = "categoryId"
        const val FIELD_IS_FIXED = "isFixed"
        const val FIELD_CREATED_AT = "createdAt"
    }

    private fun Instant.toTimestamp(): Timestamp = Timestamp(epochSecond, nano)

    override fun observeExpenses(
        userId: String,
        startAt: Timestamp,
        endAt: Timestamp,
        categoryId: String?,
    ): Flow<List<Expense>> = callbackFlow {
        var q: Query = firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_USER_ID, userId)
            .whereGreaterThanOrEqualTo(FIELD_DATE, startAt)
            .whereLessThanOrEqualTo(FIELD_DATE, endAt)
            .orderBy(FIELD_DATE, Query.Direction.DESCENDING)

        if (!categoryId.isNullOrEmpty()) {
            q = q.whereEqualTo(FIELD_CATEGORY_ID, categoryId)
        }

        val registration = q.addSnapshotListener { snap: QuerySnapshot?, err: FirebaseFirestoreException? ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val expenses = mapExpenses(snap)
            trySend(expenses)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getExpenses(
        userId: String,
        startAt: Timestamp,
        endAt: Timestamp,
        categoryId: String?,
    ): List<Expense> {
        var q: Query = firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_USER_ID, userId)
            .whereGreaterThanOrEqualTo(FIELD_DATE, startAt)
            .whereLessThanOrEqualTo(FIELD_DATE, endAt)
            .orderBy(FIELD_DATE, Query.Direction.DESCENDING)

        if (!categoryId.isNullOrEmpty()) {
            q = q.whereEqualTo(FIELD_CATEGORY_ID, categoryId)
        }

        val snap = q.get().await()
        return mapExpenses(snap)
    }

    override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense {
        val docRef = firestore.collection(COLLECTION).document()
        val data = hashMapOf<String, Any?>(
            FIELD_NAME to form.name,
            FIELD_AMOUNT to form.amount,
            FIELD_DATE to form.date.toTimestamp(),
            FIELD_CATEGORY_ID to form.categoryId,
            FIELD_IS_FIXED to form.isFixed,
            FIELD_USER_ID to userId,
            FIELD_CREATED_AT to FieldValue.serverTimestamp(),
        )
        docRef.set(data).await()
        val snap = docRef.get().await()
        val doc = snap.toObject(ExpenseDocument::class.java)
        return Expense(
            id = snap.id,
            name = doc?.name ?: form.name,
            amount = doc?.amount ?: form.amount,
            date = (doc?.date ?: form.date.toTimestamp()).toInstant(),
            categoryId = doc?.categoryId ?: form.categoryId,
            isFixed = doc?.isFixed ?: form.isFixed,
            createdAt = (doc?.createdAt ?: Timestamp.now()).toInstant(),
            userId = doc?.userId ?: userId,
        )
    }

    override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) {
        val data = mutableMapOf<String, Any?>()
        update.name?.let { data[FIELD_NAME] = it }
        update.amount?.let { data[FIELD_AMOUNT] = it }
        update.date?.let { d ->
            data[FIELD_DATE] = d.toTimestamp()
        }
        update.categoryId?.let { data[FIELD_CATEGORY_ID] = it }
        update.isFixed?.let { data[FIELD_IS_FIXED] = it }

        if (data.isEmpty()) return

        firestore.collection(COLLECTION)
            .document(expenseId)
            .set(data, SetOptions.merge())
            .await()
    }

    override suspend fun deleteExpense(expenseId: String) {
        firestore.collection(COLLECTION)
            .document(expenseId)
            .delete()
            .await()
    }

    private fun mapExpenses(snap: QuerySnapshot?): List<Expense> {
        if (snap == null) return emptyList()
        return snap.documents.mapNotNull { doc ->
            val data = doc.toObject(ExpenseDocument::class.java)
            val name = data?.name
            val amount = data?.amount
            val date = data?.date
            val categoryId = data?.categoryId
            val isFixed = data?.isFixed ?: false
            val createdAt = data?.createdAt ?: Timestamp.now()
            val userId = data?.userId
            if (name == null || amount == null || date == null || categoryId == null || userId == null) {
                null
            } else {
                Expense(
                    id = doc.id,
                    name = name,
                    amount = amount,
                    date = date.toInstant(),
                    categoryId = categoryId,
                    isFixed = isFixed,
                    createdAt = createdAt.toInstant(),
                    userId = userId,
                )
            }
        }
    }
}
