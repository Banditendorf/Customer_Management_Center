package com.cmc.customer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmc.customer.model.Material
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MaterialViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _materials = MutableStateFlow<List<Material>>(emptyList())
    val materials: StateFlow<List<Material>> = _materials

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _allMaterials = MutableStateFlow<List<Material>>(emptyList())
    val allMaterials: StateFlow<List<Material>> = _allMaterials

    init {
        loadMaterials()
    }

    /**
     * Loads all materials and categories.
     */
    fun loadMaterials() {
        viewModelScope.launch {
            db.collection("materials").get()
                .addOnSuccessListener { snapshot ->
                    val materialList = mutableListOf<Material>()
                    val categoryList = mutableListOf<String>()

                    snapshot.documents.forEach { doc ->
                        val category = doc.id
                        categoryList.add(category)
                        val content = doc.get("icerik") as? Map<*, *> ?: return@forEach

                        content.forEach { (_, entry) ->
                            val map = entry as? Map<*, *> ?: return@forEach
                            materialList.add(
                                Material(
                                    code = map["code"] as? String ?: "",
                                    shelf = map["shelf"] as? String ?: "",
                                    category = category,
                                    stock = (map["stock"] as? Long)?.toInt() ?: 0,
                                    kritikStok = (map["kritikStok"] as? Long)?.toInt() ?: 0,
                                    description = map["description"] as? String ?: ""
                                )
                            )
                        }
                    }

                    _materials.value = materialList
                    _allMaterials.value = materialList
                    _categories.value = categoryList.sorted()
                }
        }
    }

    /**
     * Adds a new material under its category.
     */
    fun addMaterial(material: Material) {
        viewModelScope.launch {
            val uuid = UUID.randomUUID().toString()
            val matMap = mapOf(
                "code" to material.code,
                "shelf" to material.shelf,
                "stock" to material.stock,
                "kritikStok" to material.kritikStok,
                "description" to material.description
            )
            val ref = db.collection("materials").document(material.category)
            ref.get().addOnSuccessListener { doc ->
                if (doc.exists()) ref.update("icerik.$uuid", matMap)
                else ref.set(mapOf("icerik" to mapOf(uuid to matMap)))
                loadMaterials()
            }
        }
    }

    /**
     * Updates an existing material's data.
     */
    fun updateMaterial(updated: Material) {
        viewModelScope.launch {
            val ref = db.collection("materials").document(updated.category)
            ref.get().addOnSuccessListener { doc ->
                val content = doc.get("icerik") as? Map<*, *> ?: return@addOnSuccessListener
                val key = content.entries.find {
                    (it.value as? Map<*, *>)?.get("code") == updated.code
                }?.key as? String ?: return@addOnSuccessListener

                val updatedMap = mapOf(
                    "code" to updated.code,
                    "shelf" to updated.shelf,
                    "stock" to updated.stock,
                    "kritikStok" to updated.kritikStok,
                    "description" to updated.description
                )
                ref.update("icerik.$key", updatedMap)
                loadMaterials()
            }
        }
    }

    /**
     * Deletes a material by key from Firestore.
     */
    fun deleteMaterial(material: Material) {
        viewModelScope.launch {
            val ref = db.collection("materials").document(material.category)
            ref.get().addOnSuccessListener { doc ->
                val content = doc.get("icerik") as? Map<*, *> ?: return@addOnSuccessListener
                val key = content.entries.find {
                    (it.value as? Map<*, *>)?.get("code") == material.code
                }?.key as? String ?: return@addOnSuccessListener

                ref.update("icerik.$key", FieldValue.delete())
                loadMaterials()
            }
        }
    }

    /**
     * Updates the stock count directly for a given material.
     */
    fun updateStock(category: String, code: String, newStock: Int) {
        viewModelScope.launch {
            val ref = db.collection("materials").document(category)
            ref.get().addOnSuccessListener { doc ->
                val content = doc.get("icerik") as? Map<*, *> ?: return@addOnSuccessListener
                val key = content.entries.find {
                    (it.value as? Map<*, *>)?.get("code") == code
                }?.key as? String ?: return@addOnSuccessListener

                // update nested stock field
                ref.update("icerik.$key.stock", newStock)
                loadMaterials()
            }
        }
    }

    /**
     * Decreases stock amount by given quantity.
     */
    fun decreaseStock(code: String, amount: Int) {
        val item = _materials.value.find { it.code == code } ?: return
        val newStock = (item.stock - amount).coerceAtLeast(0)
        updateStock(item.category, code, newStock)
    }

    fun addCategory(category: String) {
        viewModelScope.launch {
            db.collection("materials").document(category)
                .set(mapOf("icerik" to mapOf<String, Any>()))
                .addOnSuccessListener { loadMaterials() }
        }
    }

    fun deleteCategory(category: String) {
        viewModelScope.launch {
            db.collection("materials").document(category)
                .delete()
                .addOnSuccessListener { loadMaterials() }
        }
    }

    /**
     * Overloads to match screen calls with extra parameters.
     */
    fun loadMaterials(vararg args: Any?) = loadMaterials()
    fun updateMaterial(updated: Material, vararg args: Any?) = updateMaterial(updated)
    fun deleteMaterial(material: Material, vararg args: Any?) = deleteMaterial(material)
}
