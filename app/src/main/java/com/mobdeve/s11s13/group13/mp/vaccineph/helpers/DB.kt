package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
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

    suspend fun asyncMergeDataToNamedDocument(
        collection: String,
        documentId: String,
        data: Any,
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection).document(documentId).set(data, SetOptions.merge()).await()
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

    suspend fun createAppointmentTransaction(
        collection: String,
        documentId: String,
    ) : Boolean {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(collection).document(documentId)
        val maxRef = db.collection("vaccination centers").document("IGFv6WBHNmji9luGF2HZ")

        return db.runTransaction { transaction ->
            val appointmentSnapShot = transaction.get(docRef)
            val size = appointmentSnapShot.getLong("count")?.toInt() ?: 0

            val maxSnapShot = transaction.get(maxRef)
            val max = maxSnapShot.getLong("max capacity")?.toInt() ?: 0

            val newCount : Int
            if(size < max) {
                newCount = size + 1

                println("Does it exist? ${appointmentSnapShot.exists()}")
                println(newCount)
                val mobileNumbers = appointmentSnapShot.toObject(AppointmentData::class.java)?.mobileNumbers
                mobileNumbers?.add(User.mobileNumber)

                transaction.update(docRef, "count", newCount)
                transaction.update(docRef, "mobileNumbers", mobileNumbers)
                //newCount
            }
            size < max
        }.await()
    }

    suspend fun deleteAppointmentTransaction(
        collection: String,
        documentId: String,
    ) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(collection).document(documentId)

        db.runTransaction { transaction ->
            val appointmentSnapShot = transaction.get(docRef)
            val mobileNumbers = appointmentSnapShot.toObject(AppointmentData::class.java)?.mobileNumbers

            mobileNumbers?.remove(User.mobileNumber)
            val size = appointmentSnapShot.getLong("count")?.toInt() ?: 0
            val newCount = if(size != 0) size - 1 else size

            if(newCount == 0) {
                docRef.delete()
            } else {
                transaction.update(docRef, "count", newCount)
                transaction.update(docRef, "mobileNumbers", mobileNumbers)
            }

        }.await()
    }


}