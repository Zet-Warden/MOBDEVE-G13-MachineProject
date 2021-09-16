package com.mobdeve.s11s13.group13.mp.vaccineph.helpers

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.mobdeve.s11s13.group13.mp.vaccineph.helpers.appointmentscreenactivityhelper.AppointmentData
import kotlinx.coroutines.tasks.await

object DB {

    /**
     * Creates a document to the specified collection
     *
     * Generates a new document to the specified collection
     * The newly generated document is given an automatic ID
     *
     * @param[collection] - the collection where the new document will be put
     * @param[data] - the data to put in the document
     * @param[callback] - a callback which is called and given the resulting DocumentReference for
     *                  the newly added document upon success
     */
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

    /**
     * Creates a named document to the specified collection
     *
     * Generates a new document with the specified id as its name to the specified collection
     *
     * @param[collection] - the name of the collection where the new document will be put
     * @param[documentId] - the name of the newly generated document
     * @param[data] - the data to be put in the document
     * @param[callback] - a callback which is called upon success
     */
    fun createNamedDocumentToCollection(
        collection: String,
        documentId: String,
        data: Any,
        callback: () -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .document(documentId)
            .set(data)
            .addOnSuccessListener {
                callback()
            }
    }

    /**
     * Reads a document/s based on a specified query
     *
     * Calls [readDocumentFromCollection] with a default limit of 10,000
     *
     * @param[query] - the query to be read upon
     * @param[callback] - the callback to be called and given the QuerySnapshot upon success
     * @return a task with the resulting QuerySnapshot
     */
    fun readDocumentFromCollection(
        query: Query,
        callback: (QuerySnapshot) -> Unit = {}
    ): Task<QuerySnapshot> {
        return readDocumentFromCollection(query, 10_000L, callback)
    }

    /**
     * Reads document/s based on a specified query
     *
     * Reads document/s and limits the resulting document/s based on the limit parameter
     *
     * @param[query] - the query to be read upon
     * @param[limit] - the maximum number of documents to be returned
     * @param[callback] - the callback to be called and given the QuerySnapshot upon success
     * @return a task with the resulting QuerySnapshot
     */
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

    /**
     * Similar to [readDocumentFromCollection] but can be used with Kotlin's coroutines
     *
     * Use this function to wait for the result
     *
     * @param[query] - the query to be read upon
     * @return the resulting QuerySnapshot
     */
    suspend fun asyncReadDocumentFromCollection(
        query: Query,
    ): QuerySnapshot {
        return asyncReadDocumentFromCollection(query, 10_000L)
    }

    /**
     * Similar to [readDocumentFromCollection] but can be used with Kotlin's coroutines
     *
     * Limits the resulting documents
     * Use this function to wait for the result
     *
     * @param[query] - the query to be read upon
     * @param[limit] - the maximum number of documents to be returned
     * @return the resulting QuerySnapshot
     */
    suspend fun asyncReadDocumentFromCollection(
        query: Query,
        limit: Long,
    ): QuerySnapshot {
        return query.limit(limit)
            .get().await()
    }

    /**
     * Reads a specific document from a given collection
     *
     * Use this function to wait for the result
     *
     * @param[collection] - the collection where the document lies
     * @param[documentId] - the name of the document
     * @return the DocumentSnapshot
     */
    suspend fun asyncReadNamedDocumentFromCollection(
        collection: String,
        documentId: String
    ): DocumentSnapshot {
        val db = FirebaseFirestore.getInstance()
        return db.collection(collection)
            .document(documentId)
            .get().await()
    }

    /**
     * Updates documents based on the specified query
     *
     * @param[query] - the query to be read upon
     * @param[data] - the data to update the queried documents
     * @param[callback] - the callback to be called upon finishing the updates
     */
    fun updateDocumentFromCollection(
        query: Query,
        data: HashMap<String, Any>,
        callback: () -> Unit = {}
    ) {
        query.get()
            .addOnSuccessListener { documents ->
                documents.forEach {
                    it.reference.update(data)
                }
                callback()
            }
    }


    /**
     * Updates a specific document from a collection
     *
     * @param[collection] - the name of the collection where the document is located
     * @param[documentId] - the name of the document to be updated
     * @param[data] - the data to update the specified document
     * @param[callback] - the callback to be called upon success
     */
    fun updateNamedDocumentFromCollection(
        collection: String,
        documentId: String,
        data: MutableMap<String, Any>,
        callback: () -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection)
            .document(documentId)
            .update(data)
            .addOnSuccessListener {
                callback()
            }
    }

    /**
     * Merges data to a specified document
     *
     * @param[collection] - the name of the collection where the document is located
     * @param[documentId] - the name of the specified document
     * @param[data] - the data to be merged
     */
    suspend fun asyncMergeDataToNamedDocument(
        collection: String,
        documentId: String,
        data: Any,
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collection).document(documentId).set(data, SetOptions.merge()).await()
    }

    /**
     * Deletes documents from a collection matching the specified query
     *
     * @param[query] - the query to be read upon
     * @param[callback] - the callback to be called upon and passed the resulting QuerySnapshot
     *                  after deleting all matching documents
     */
    fun deleteDocumentFromCollection(query: Query, callback: (QuerySnapshot) -> Unit = {}) {
        query.get()
            .addOnSuccessListener { documents ->
                documents.forEach {
                    it.reference.delete()
                }
                callback(documents)
            }
    }

    /**
     * Creates a query where the chosen field matches a specific value
     *
     * The query created can be passed into the functions in this document that ask for a query parameter
     *
     * @param[collection] - the name of the collection to be queried upon
     * @param[query] - a pair of values; first is the field name, second is the value
     * @return a query for documents that are equal to the specified pair
     */
    fun createEqualToQuery(collection: String, query: Pair<String, Any?>): Query {
        return createEqualToQueries(collection, mutableListOf(query))
    }

    /**
     * Creates a query where the chosen field matches a specific value
     *
     * This functions compound multiple whereEqualTo queries
     * The query created can be passed into the functions in this document that ask for a query parameter
     *
     * @param[collection] - the name of the collection to be queried upon
     * @param[query] - a list of pairs of values; first is the field name, second is the value
     * @return a query for documents that are equal to all the specified pairs in the list
     */
    fun createEqualToQueries(collection: String, queries: MutableList<Pair<String, Any?>>): Query {
        val db = FirebaseFirestore.getInstance()
        var query: Query = db.collection(collection)

        queries.forEach {
            query = query.whereEqualTo(it.first, it.second)
        }
        return query
    }

    /**
     * Creates a query where the chosen field (List) contains the specified value
     *
     * The query created can be passed into the functions in this document that ask for a query parameter
     *
     * @param[collection] - the name of the collection to be queried upon
     * @param[query] - a pair of values; first is the field name,
     *              second is the value to be looked for inside the field name
     * @return a query for documents whose field (List) contains the specified value
     */
    fun createArrayContainsQuery(collection: String, query: Pair<String, Any>): Query {
        val db = FirebaseFirestore.getInstance()
        return db.collection(collection).whereArrayContains(query.first, query.second)
    }

    /**
     * This function is specifically used for AppointmentScreenActivity
     *
     * This is used as data that is being worked upon can be corrupted by concurrent modification
     * This allows users to set appointments without breaking the maximum number of people to be accommodated
     *
     * The default collection for appointmentId is "appointments"
     * The default collection for locationId is "vaccine centers"
     *
     * @param[appointmentId] - the document name for a specific appointment; the default format is "<Appointment Date> - <Appointment Location>"
     * @param[locationId] - the document name for the document inside the "vaccine centers" collection
     * @return true if the created appointment does not exceed the maximum capacity set for the location
     */
    suspend fun createAppointmentTransaction(
        appointmentId: String,
        locationId: String = "IGFv6WBHNmji9luGF2HZ",
    ): Boolean {
        val db = FirebaseFirestore.getInstance()
        val apptRef = db.collection("appointments").document(appointmentId)
        val vaxCenterRef = db.collection("vaccination centers").document(locationId)

        return db.runTransaction { transaction ->
            val apptSnapshot = transaction.get(apptRef)
            // get the current count of people for this appointment
            val size = apptSnapshot.getLong("count")?.toInt()
                ?: 0 //if field "count" does not exist, then default to 0
            
            val vacCenterSnapshot = transaction.get(vaxCenterRef)
            // get the maximum capacity for specified vaccination center
            val max = vacCenterSnapshot.getLong("max capacity")?.toInt()
                ?: 0 //if field "max capacity" does not exist, then default to 0

            if (size < max) {
                val newCount = size + 1
                //get current mobileNumbers and add the User's mobileNumber
                val mobileNumbers =
                    apptSnapshot.toObject(AppointmentData::class.java)?.mobileNumbers
                mobileNumbers?.add(User.mobileNumber)

                //update the current count and the mobile numbers registered under the specified appointment document
                transaction.update(apptRef, "count", newCount)
                transaction.update(apptRef, "mobileNumbers", mobileNumbers)
            }
            size < max
        }.await()
    }

    /**
     * This function is specifically used for AppointmentScreenActivity
     *
     * This is used as data that is being worked upon can be corrupted by concurrent modification
     * This allows users to delete appointments without corrupting the mobile number/s registered under the specified appointment
     *
     * The default collection for appointmentId is "appointments"
     * The default collection for locationId is "vaccine centers"
     *
     * @param[appointmentId] - the document name for the document inside the "appointments" collection
     * @return true if the created appointment does not exceed the maximum capacity set for the location
     */
    suspend fun deleteAppointmentTransaction(
        appointmentId: String,
    ) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("appointments").document(appointmentId)

        db.runTransaction { transaction ->
            val appointmentSnapShot = transaction.get(docRef)

            // get the list of mobile numbers registered under the specified appointment document
            // and remove the User's mobile number from the list
            val mobileNumbers =
                appointmentSnapShot.toObject(AppointmentData::class.java)?.mobileNumbers
            mobileNumbers?.remove(User.mobileNumber)

            // get the current count of people for this appointment
            val size = appointmentSnapShot.getLong("count")?.toInt()
                ?: 0 //if field "count" does not exist, then default to 0
            // reduce the size by one, or if count is zero then as is (there are no negative counts for the number of people registered)
            val newCount = if (size != 0) size - 1 else size

            //update the current count and the mobile numbers registered under the specified appointment document
            transaction.update(docRef, "count", newCount)
            transaction.update(docRef, "mobileNumbers", mobileNumbers)
        }.await()
    }
}