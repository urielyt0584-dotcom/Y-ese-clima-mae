package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class FirebaseUserProfile(
    val userId: String = "",
    val displayName: String = "",
    val favoriteCantons: List<String> = emptyList(),
    val sanPedroBlamesCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirebaseCommunityBlame(
    val id: String = "",
    val userUid: String = "",
    val userName: String = "",
    val canton: String = "",
    val comment: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

class FirebaseRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "FirebaseRepository"

        fun create(context: Context): FirebaseRepository {
            val databaseId = context.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return FirebaseRepository(firestore)
        }
    }

    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun syncUserProfile(
        displayName: String,
        favoriteCantons: List<String>,
        sanPedroBlamesCount: Int
    ): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Usuario no autenticado"))
        return try {
            val userDoc = db.collection("users").document(user.uid)
            val snapshot = userDoc.get().await()
            val existingCreatedAt = snapshot.getTimestamp("createdAt")

            val payload = hashMapOf<String, Any>(
                "userId" to user.uid,
                "displayName" to displayName.ifBlank { user.displayName ?: "Mae Anónimo" }.take(60),
                "favoriteCantons" to favoriteCantons.take(30),
                "sanPedroBlamesCount" to sanPedroBlamesCount.toLong(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingCreatedAt != null) {
                payload["createdAt"] = existingCreatedAt
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }

            userDoc.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncUserProfile", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): FirebaseUserProfile? {
        val user = auth.currentUser ?: return null
        return try {
            val doc = db.collection("users").document(user.uid).get().await()
            if (doc.exists()) {
                val userId = doc.getString("userId") ?: user.uid
                val displayName = doc.getString("displayName") ?: (user.displayName ?: "")
                val favoriteCantons = (doc.get("favoriteCantons") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val blames = doc.getLong("sanPedroBlamesCount") ?: 0L
                val createdAt = doc.getTimestamp("createdAt")
                val updatedAt = doc.getTimestamp("updatedAt")
                FirebaseUserProfile(
                    userId = userId,
                    displayName = displayName,
                    favoriteCantons = favoriteCantons,
                    sanPedroBlamesCount = blames,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getUserProfile", e)
            null
        }
    }

    suspend fun postCommunityBlame(canton: String, comment: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("Debes iniciar sesión con Google para culpar a San Pedro en la nube"))
        return try {
            val blameId = java.util.UUID.randomUUID().toString()
            val payload = hashMapOf<String, Any>(
                "id" to blameId,
                "userUid" to user.uid,
                "userName" to (user.displayName?.take(60) ?: "Mae de $canton"),
                "canton" to canton.take(100),
                "comment" to comment.take(300),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("community_blames").document(blameId).set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error postCommunityBlame", e)
            Result.failure(e)
        }
    }

    fun observeCommunityBlames(): Flow<List<FirebaseCommunityBlame>> = callbackFlow {
        val registration = db.collection("community_blames")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing community blames", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: doc.id
                    val userUid = doc.getString("userUid") ?: ""
                    val userName = doc.getString("userName") ?: "Mae Anónimo"
                    val canton = doc.getString("canton") ?: "Costa Rica"
                    val comment = doc.getString("comment") ?: ""
                    val createdAt = doc.getTimestamp("createdAt")
                    FirebaseCommunityBlame(id, userUid, userName, canton, comment, createdAt)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    fun signInWithGoogle(
        context: Context,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Configuración de Google Sign-In incompleta (falta default_web_client_id)")
            return
        }

        val credentialManager = CredentialManager.create(context)
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val activity = context as? Activity ?: run {
                    onError("Contexto no es una Activity")
                    return@launch
                }
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onSuccess()
                } else {
                    onError("Tipo de credencial no esperado")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelado por el usuario: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In falló", e)
                onError(e.localizedMessage ?: "Error al iniciar sesión con Google")
            }
        }
    }

    fun signOut(context: Context, scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        val credentialManager = CredentialManager.create(context)
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
