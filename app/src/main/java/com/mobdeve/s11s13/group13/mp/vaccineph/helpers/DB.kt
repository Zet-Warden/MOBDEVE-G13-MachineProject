package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object DB {
    fun createDocumentToCollection(
        collection: String,
        data: Any,
        callback: (DocumentReference) -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .add(data)
            .addOnSuccessListener {
                callback(it)
            }
    }

    fun createNamedDocumentToCollection(
        collection: String,
        document: String,
        data: Any,
        callback: () -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .document(document)
            .set(data)
            .addOnSuccessListener {
                callback()
            }
    }

    fun readDocumentFromCollection(
        query: Query,
        callback: (QuerySnapshot) -> Unit = {}
    ): Task<QuerySnapshot> {
        return readDocumentFromCollection(query, 10_000L, callback)
    }

    fun readDocumentFromCollection(
        query: Query,
        limit: Long,
        callback: (QuerySnapshot) -> Unit = {}
    ): Task<QuerySnapshot> {
        return query.limit(limit)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents)
            }
    }

    suspend fun asyncReadDocumentFromCollection(
        query: Query,
    ): QuerySnapshot {
        return asyncReadDocumentFromCollection(query, 10_000L)
    }

    suspend fun asyncReadDocumentFromCollection(
        query: Query,
        limit: Long,
    ): QuerySnapshot {
        return query.limit(limit)
            .get().await()
    }

    suspend fun asyncReadNamedDocumentFromCollection(
        collection: String,
        document: String
    ): DocumentSnapshot {
        val db = FirebaseFirestore.getInstance()
        return db.collection(collection)
            .document(document)
            .get().await()
    }

    fun updateDocumentFromCollection(
        query: Query,
        data: MutableMap<String, Any>,
        callback: () -> Unit = {}
    ) {
        query.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach {
                        it.reference.update(data).addOnSuccessListener {
                            callback()
                        }
                    }
                } else {
                    println("Document not found: $query")
                }
            }
    }

    fun updateNamedDocumentFromCollection(
        collection: String,
        document: String,
        data: MutableMap<String, Any>,
        callback: () -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .document(document)
            .update(data)
            .addOnSuccessListener {
                callback()
            }
    }

    fun mergeDataToNamedDocument(collection: String, documentId: String, data: Any) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection).document(documentId).set(data, SetOptions.merge())
    }


    fun deleteDocumentFromCollection(query: Query, callback: (QueryDocumentSnapshot) -> Unit = {}) {
        query.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach {
                        it.reference.delete()
                        callback(it)
                    }
                } else {
                    println("Document not found: $query")
                }
            }
    }

    fun createEqualToQuery(collection: String, query: Pair<String, Any?>): Query {
        return createEqualToQueries(collection, mutableListOf(query))
    }

    fun createEqualToQueries(collection: String, queries: MutableList<Pair<String, Any?>>): Query {
        val db = FirebaseFirestore.getInstance()
        var query: Query = db.collection(collection)

        queries.forEach {
            query = query.whereEqualTo(it.first, it.second)
        }
        return query
    }

    fun createArrayContainsQuery(collection: String, query: Pair<String, Any>): Query {
        val db = FirebaseFirestore.getInstance()
        return db.collection(collection).whereArrayContains(query.first, query.second)
    }

    fun createTransaction(
        collection: String,
        documentId: String,
        increment: Int,
        foo: suspend () -> Unit = {},
        callback: (Int) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(collection).document(documentId)

        GlobalScope.launch(Dispatchers.IO) {
            foo()
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val newCount = snapshot.getLong("count")!!.toInt() + increment


                transaction.update(docRef, "count", newCount)
                newCount
            }.addOnSuccessListener {
                callback(it)
            }
        }
    }

}