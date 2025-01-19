package com.makinul.background.remover.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import javax.inject.Inject

open class BaseRepo @Inject constructor() {
    val TAG = "BaseRepo"

    suspend inline fun <reified T : Any> getItems(
        reference: DatabaseReference
    ): Flow<List<T>?> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val items = dataSnapshot.children.map { ds ->
                    ds.getValue(T::class.java)
                }
                this@callbackFlow.trySendBlocking(items.filterNotNull())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                this@callbackFlow.trySendBlocking(null)
            }
        }
        reference.addListenerForSingleValueEvent(postListener)

        awaitClose {
            reference.removeEventListener(postListener)
        }
    }

    suspend inline fun <reified T : Any> getItem(
        reference: DatabaseReference
    ): Flow<T?> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                this@callbackFlow.trySendBlocking(dataSnapshot.getValue(T::class.java))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                this@callbackFlow.trySendBlocking(null)
            }
        }
        reference.addListenerForSingleValueEvent(postListener)
        awaitClose {
            reference.removeEventListener(postListener)
        }
    }

//    suspend inline fun <reified T : Any> getItem(reference: DatabaseReference): Flow<T?> =
//        callbackFlow {
//            val postListener = object : ValueEventListener {
//                override fun onDataChange(dataSnapshot: DataSnapshot) {
//                    // Get Post object and use the values to update the UI
//                    this@callbackFlow.trySendBlocking(dataSnapshot.getValue(T::class.java))
//                }
//
//                override fun onCancelled(databaseError: DatabaseError) {
//                    // Getting Post failed, log a message
//                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
//                    this@callbackFlow.trySendBlocking(null)
//                }
//            }
//            reference.addListenerForSingleValueEvent(postListener)
//
//            awaitClose {
//                reference.removeEventListener(postListener)
//            }
//        }

//    suspend fun uploadVideo(storageRef: StorageReference, file: File) {
//        val uri = Uri.fromFile(file)
//        val riversRef = storageRef.child("images/${uri.lastPathSegment}")
//
//        val uploadTask = riversRef.putFile(uri)
//        // Register observers to listen for when the download is done or if it fails
//        uploadTask.addOnFailureListener {
//            // Handle unsuccessful uploads
//            Log.e(TAG, "message ${it.message}")
//        }.addOnSuccessListener { taskSnapshot ->
//            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
//            Log.e(TAG, "taskSnapshot $taskSnapshot")
//        }
//    }
//
//    suspend fun uploadVideoNew(
//        storageRef: StorageReference, file: File
//    ): Flow<String> = callbackFlow {
//        val uri = Uri.fromFile(file)
//        val riversRef = storageRef.child("images/${uri.lastPathSegment}")
//
//        val uploadTask = riversRef.putFile(uri)
//        // Register observers to listen for when the download is done or if it fails
//        uploadTask.addOnFailureListener {
//            // Handle unsuccessful uploads
//            Log.e(TAG, "message ${it.message}")
//        }.addOnSuccessListener { taskSnapshot ->
//            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
//            Log.v(TAG, "taskSnapshot $taskSnapshot")
//            this@callbackFlow.trySendBlocking("")
//        }
//        awaitClose {
//            uploadTask.cancel()
//        }
//    }
//
//    suspend fun uploadImage(storageRef: StorageReference, file: File) {
//        val uri = Uri.fromFile(file)
//        val riversRef = storageRef.child("images/${uri.lastPathSegment}")
//
//        val uploadTask = riversRef.putFile(uri)
//        // Register observers to listen for when the download is done or if it fails
//        uploadTask.addOnFailureListener {
//            // Handle unsuccessful uploads
//            Log.e(TAG, "message ${it.message}")
//        }.addOnSuccessListener { taskSnapshot ->
//            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
//            Log.e(TAG, "taskSnapshot $taskSnapshot")
//        }
//    }
//
//    suspend fun uploadImageArray(storageRef: StorageReference, byteArray: ByteArray) {
//        val imageRef = storageRef.child("images/new_image.png")
//        val uploadTask = imageRef.putBytes(byteArray)
//        uploadTask.addOnFailureListener {
//            // Handle unsuccessful uploads
//            Log.e(TAG, "message ${it.message}")
//        }.addOnSuccessListener { taskSnapshot ->
//            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
//            Log.e(TAG, "taskSnapshot $taskSnapshot")
//        }
//    }

    suspend inline fun <reified T : Any> getMaps(
        reference: DatabaseReference
    ): Flow<Map<String, T?>?> = callbackFlow {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val maps: HashMap<String, T?> = HashMap()
                dataSnapshot.children.forEach { ds ->
                    ds?.key?.let { key ->
                        maps[key] = ds.getValue(T::class.java)
                    }
                }
                this@callbackFlow.trySendBlocking(maps)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                this@callbackFlow.trySendBlocking(null)
            }
        }
        reference.addListenerForSingleValueEvent(postListener)

        awaitClose {
            reference.removeEventListener(postListener)
        }
    }
}