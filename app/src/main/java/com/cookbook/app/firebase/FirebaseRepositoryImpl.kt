package com.cookbook.app.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cookbook.app.model.Recipe
import com.cookbook.app.model.User
import com.cookbook.app.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(private val auth: FirebaseAuth, private val firebaseFireStore: FirebaseFirestore) :
    FirebaseRepository {
    private val usersRef: CollectionReference = firebaseFireStore.collection("USERS")
    private val recipeRef: CollectionReference = firebaseFireStore.collection("RECIPES")
    override fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Constants.loggedUserId = getLoggedUserId()
                    callback(true, null)
                } else {
                    val errorMessage = getFirebaseErrorMessage(task.exception)
                    callback(false, errorMessage)
                }
            }
    }

    override fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthException -> {
                            when (exception.errorCode) {
                                "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
                                "ERROR_USER_NOT_FOUND" -> "No account found for this email. Please sign up first."
                                "ERROR_WRONG_PASSWORD" -> "The password is incorrect. Please try again."
                                "ERROR_USER_DISABLED" -> "This account has been disabled."
                                else -> "Authentication failed. Please try again."
                            }
                        }
                        else -> exception?.localizedMessage ?: "Unknown error occurred. Please try again."
                    }
                    callback(false, errorMessage)
                }
            }
    }

    override fun saveUserData(user: User, imageUri: Uri) {
        val userId = auth.currentUser?.uid
        user.userId = userId!!
        uploadUserImageToFirebaseStorage(imageUri,userId) { success, downloadUrl ->
            if (success) {
                user.profileImageUrl = downloadUrl
                usersRef.document(userId).set(user)
            } else {
                println("Failed to upload image: $downloadUrl")
            }
        }
    }

    override fun updateUserData(user: User, imageUri: Uri?, callback: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid
        user.userId = userId!!
        if (imageUri == null){
            val updates = hashMapOf<String, Any>(
                "name" to user.name,
            )
            usersRef.document(userId).update(updates)
            callback(true,"User Detail updated Successfully")
            return
        }
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.profileImageUrl!!)
        storageRef.delete()
            .addOnSuccessListener {
                uploadUserImageToFirebaseStorage(imageUri,userId) { success, downloadUrl ->
                    if (success) {
                        user.profileImageUrl = downloadUrl
                        usersRef.document(userId).update(mapOf(
                            "name" to user.name,
                            "profileImageUrl" to user.profileImageUrl,
                        ),)
                        callback(true,"User Detail updated Successfully")
                    } else {
                        callback(false,"Something went wrong")
                    }
                }
            }
    }

    override fun checkUserAuth(): Boolean {
        if(auth.currentUser != null){
            Constants.loggedUserId = auth.currentUser!!.uid
        }
        return auth.currentUser != null
    }

    override fun getLoggedUserId(): String {
        return auth.currentUser!!.uid
    }

    override fun fetchUserDetails(id: String) {
        usersRef.document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        Constants.loggedUser = user
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error fetching user data", exception)
            }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun addRecipeToFireStore(
        context: Context,
        recipe: Recipe,
        callback: (Boolean, String?,Recipe?) -> Unit
    ) {
        uploadRecipeImageToFirebaseStorage(recipe.imageUri, recipe.recipeId) { success, imageUrl ->
            if (success && imageUrl != null) {
                recipe.imageUrl = imageUrl

                recipeRef.document(recipe.recipeId).set(recipe)
                    .addOnSuccessListener {
                        callback(true, "Recipe added successfully",recipe)
                    }
                    .addOnFailureListener { exception ->
                        callback(false, exception.localizedMessage,null)
                    }
            }
            else if (!success && imageUrl == null) {
                recipeRef.document(recipe.recipeId).set(recipe)
                    .addOnSuccessListener {
                        callback(true, "Recipe added successfully",recipe)
                    }
                    .addOnFailureListener { exception ->
                        callback(false, exception.localizedMessage,null)
                    }
            }
            else {
                callback(false, "Failed to upload image: $imageUrl",null)
            }
        }
    }

    override fun updateRecipeToFireStore(
        context: Context,
        recipe: Recipe,
        callback: (Boolean, String?, Recipe?) -> Unit
    ) {
        uploadRecipeImageToFirebaseStorage(recipe.imageUri, recipe.recipeId) { success, imageUrl ->
            if (success && imageUrl != null) {
                recipe.imageUrl = imageUrl

                recipeRef.document(recipe.recipeId).update(recipe.toMap())
                    .addOnSuccessListener {
                        callback(true, "Recipe detail updated successfully",recipe)
                    }
                    .addOnFailureListener { exception ->
                        callback(false, exception.localizedMessage,null)
                    }
            }
            else if (!success && imageUrl == null) {
                recipeRef.document(recipe.recipeId).update(recipe.toMap())
                    .addOnSuccessListener {
                        callback(true, "Recipe details updated successfully",recipe)
                    }
                    .addOnFailureListener { exception ->
                        callback(false, exception.localizedMessage,null)
                    }
            }
            else {
                callback(false, "Failed to upload image: $imageUrl",null)
            }
        }
    }

    override fun deleteRecipeFromFireStore(recipeId: String, callback: (Boolean, String?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("recipes")
            .document(recipeId)
            .delete()
            .addOnSuccessListener {
                callback(true, "Recipe deleted successfully!")
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }

    override fun getAllRecipeFromFireStore(
        callback: (Boolean, List<Recipe>?) -> Unit
    ) {
        // Pull the entire collection – no filtering
        recipeRef
            .get()
            .addOnSuccessListener { snapshot ->
                val recipes = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Recipe::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                callback(true, recipes)           // success (may be empty)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                callback(false, null)             // failure
            }
    }

    override fun getRecipeId(): String {
        return recipeRef.document().id
    }

    private fun uploadUserImageToFirebaseStorage(imageUri: Uri, userId:String, callback: (Boolean, String?) -> Unit) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val uniqueFileName = "images/users/${userId}/profile.jpg" // Unique file path in the storage
        val imageRef = storageReference.child(uniqueFileName)

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(true, downloadUri.toString())
                }.addOnFailureListener { exception ->
                    callback(false, exception.message)
                }
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message)
            }
    }

    private fun uploadRecipeImageToFirebaseStorage(imageUri: String?,recipeId:String, callback: (Boolean, String?) -> Unit) {
        if (imageUri == null){
            callback(false, null)
        }
        else{
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference
            val uniqueFileName = "images/recipes/${recipeId}/image.jpg" // Unique file path in the storage
            val imageRef = storageReference.child(uniqueFileName)

            imageRef.putFile(Uri.parse(imageUri))
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        callback(true, downloadUri.toString())
                    }.addOnFailureListener { exception ->
                        callback(false, exception.message)
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false, exception.message)
                }
        }

    }

    private fun getFirebaseErrorMessage(exception: Exception?): String {
        return when (exception) {
            is FirebaseAuthWeakPasswordException -> "The password is too weak. Please choose a stronger password."
            is FirebaseAuthInvalidCredentialsException -> "The email address is badly formatted. Please enter a valid email."
            is FirebaseAuthUserCollisionException -> "An account already exists with this email. Please log in or use a different email."
            is FirebaseAuthInvalidUserException -> "The user does not exist or has been disabled."
            is FirebaseAuthException -> "Authentication failed: ${exception.message}"
            else -> "An unknown error occurred. Please try again later."
        }
    }
}