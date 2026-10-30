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
        const val FIRESTORE_BATCH_LIMIT = 500
    }

    private fun Instant.toTimestamp(): Timestamp = Timestamp(epochSecond, nano)

    override fun observeExpenses(
        userId: String,
        startAt: Instant,
        endAt: Instant,
        categoryId: String?,
    ): Flow<List<Expense>> = callbackFlow {
        var q: Query = firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_USER_ID, userId)
            .whereGreaterThanOrEqualTo(FIELD_DATE, startAt.toTimestamp())
            .whereLessThanOrEqualTo(FIELD_DATE, endAt.toTimestamp())
            .orderBy(FIELD_DATE, Query.Direction.DESCENDING)

        if (!categoryId.isNullOrEmpty()) {
            q = q.whereEqualTo(FIELD_CATEGORY_ID, categoryId)
        }

        val registration = q.addSnapshotListener { snap: QuerySnapshot?, err: FirebaseFirestoreException? ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            val expenses = mapExpenses(snap)
            trySend(expenses)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getExpenses(
        userId: String,
        startAt: Instant,
        endAt: Instant,
        categoryId: String?,
    ): List<Expense> {
        var q: Query = firestore.collection(COLLECTION)
            .whereEqualTo(FIELD_USER_ID, userId)
            .whereGreaterThanOrEqualTo(FIELD_DATE, startAt.toTimestamp())
            .whereLessThanOrEqualTo(FIELD_DATE, endAt.toTimestamp())
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
        return Expense(
            id = docRef.id,
            name = form.name,
            amount = form.amount,
            date = form.date,
            categoryId = form.categoryId,
            isFixed = form.isFixed,
            createdAt = Instant.now(),
            userId = userId,
        )
    }

    override suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense> {
        if (forms.isEmpty()) return emptyList()
        val createdAt = Instant.now()
        val createdExpenses = mutableListOf<Expense>()

        forms.chunked(FIRESTORE_BATCH_LIMIT).forEach { chunk ->
            val batch = firestore.batch()
            val chunkModels = chunk.map { form ->
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
                batch.set(docRef, data)
                Expense(
                    id = docRef.id,
                    name = form.name,
                    amount = form.amount,
                    date = form.date,
                    categoryId = form.categoryId,
                    isFixed = form.isFixed,
                    createdAt = createdAt,
                    userId = userId,
                )
            }
            batch.commit().await()
            createdExpenses.addAll(chunkModels)
        }

        return createdExpenses
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
