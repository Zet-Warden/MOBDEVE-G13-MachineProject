package com.mobdeve.s11s13.group13.mp.vaccineph.helpers.navbarhelper

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot

object Database {
    fun createDocumentToCollection(collection: String, data: Any, callback: () -> Unit = {}) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .add(data)
            .addOnSuccessListener {
                callback()
            }
    }

    fun readDocumentFromCollection(query: Query, callback: (QueryDocumentSnapshot) -> Unit) {
        query.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach {
                        callback(it)
                    }
                } else {
                    println("Document not found: $query")
                }
            }
    }

    fun updateDocumentFromCollection(
        query: Query,
        data: MutableMap<String, Any>,
        callback: () -> Unit
    ) {
        query.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach {
                        it.reference.update(data)
                    }
                } else {
                    println("Document not found: $query")
                }
            }
    }

    fun deleteDocumentFromCollection(query: Query) {
        query.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    documents.forEach {
                        it.reference.delete()
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
        val query = db.collection(collection)

        queries.forEach {
            query.whereEqualTo(it.first, it.second)
        }
        return query
    }
}