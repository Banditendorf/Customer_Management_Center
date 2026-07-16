package com.cmc.customer.model

data class UserPermissions(
    val manageUser: Boolean = false,           // KullanÄ±cÄ± ekle/sil/gÃ¼ncelle
    val manageMachine: Boolean = false,        // MCMCne ekle/sil/gÃ¼ncelle
    val manageCompany: Boolean = false,        // Åirket ekle/sil/gÃ¼ncelle
    val manageMaintenance: Boolean = false,    // BakÄ±m kaydÄ± ekle/sil/gÃ¼ncelle
    val manageCategory: Boolean = false,       // Kategori ekle/sil/gÃ¼ncelle
    val manageMaterial: Boolean = false,       // Malzeme ekle/sil/gÃ¼ncelle
    val callCustomer: Boolean = false,         // MÃ¼ÅŸteri arama / arama ekranÄ±na eriÅŸim
    val viewCompanies: Boolean = false,            // Åirketleri gÃ¶rÃ¼ntÃ¼leme izni
    val viewMaintenancePlans: Boolean = false,     // Planlanan bakÄ±mlarÄ± gÃ¶rÃ¼ntÃ¼leme izni
    val viewPreparationLists: Boolean = false,     // HazÄ±rlanacak listeleri gÃ¶rÃ¼ntÃ¼leme izni
    val viewMaterialsList: Boolean = false,        // Malzemeleri gÃ¶rÃ¼ntÃ¼leme izni
    val viewUsers: Boolean = false,                // KullanÄ±cÄ±larÄ± gÃ¶rÃ¼ntÃ¼leme izni
    val viewNotifications: Boolean = false         // Bildirimleri gÃ¶rÃ¼ntÃ¼leme izni
)
