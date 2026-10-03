package com.example.model

/**
 * Core Data Models for VendorOS Super App
 */

data class UserProfile(
    val uid: String = "user_default_1",
    val name: String = "Chidi Okafor",
    val email: String = "vendor@vendoros.ng",
    val phone: String = "+2348012345678",
    val photoUrl: String = "",
    val activeMode: String? = null, // "vendor", "skills", "both", or null if not yet chosen
    val selectedState: String = "Lagos",
    val selectedLGA: String = "Ikeja",
    val selectedArea: String = "Allen Avenue",
    val businessName: String = "Classic Wears & Gadgets",
    val trustScore: Double = 5.0,
    val totalDeliveries: Int = 18,
    val verifiedDeliveries: Int = 16,
    val pendingDeliveries: Int = 2,
    val autoReplyOn: Boolean = true,
    val isPro: Boolean = false,
    val proPlan: String = "none", // none, basic, pro, super
    val workingHoursStart: String = "08:00",
    val workingHoursEnd: String = "21:00",
    val createdAt: Long = System.currentTimeMillis(),
    val proExpiry: Long? = null,
    val paymentMethod: String = "opay_secure",
    val lastPaymentId: String? = null,
    val aiModeEnabled: Boolean = true
)

data class AutoRule(
    val ruleId: String,
    val userId: String,
    val keywords: List<String>,
    val replyText: String,
    val imageUrl: String? = null,
    val type: String = "keyword", // welcome, keyword, away, busy
    val isActive: Boolean = true,
    val triggeredCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val aiEnhanced: Boolean = false
)

data class DeliveryProof(
    val proofId: String,
    val vendorId: String,
    val vendorName: String,
    val customerName: String,
    val customerPhoneFull: String, // private
    val customerPhoneMasked: String, // e.g. ***1234
    val orderId: String, // ORD-xxxxx
    val photoUrl: String,
    val videoUrl: String? = null,
    val status: String = "pending", // pending, confirmed
    val rating: Double = 5.0,
    val comment: String = "",
    val publicLink: String = "",
    val qrData: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null
)

data class SkillMaster(
    val masterId: String,
    val userId: String,
    val businessName: String,
    val ownerName: String,
    val skillCategory: String,
    val skillName: String,
    val otherSkills: List<String> = emptyList(),
    val yearsExperience: Int = 5,
    val description: String,
    val state: String,
    val lga: String,
    val area: String,
    val address: String,
    val lat: Double = 6.5244,
    val lng: Double = 3.3792,
    val pricePerMonth: Int = 5000,
    val pricePerWeek: Int? = 1500,
    val hasAccommodation: Boolean = false,
    val accommodationFee: Int = 0,
    val images: List<String> = emptyList(),
    val videoUrl: String? = null,
    val phone: String,
    val whatsapp: String,
    val isAvailable: Boolean = true,
    val rating: Double = 5.0,
    val totalStudents: Int = 8,
    val views: Int = 120,
    val isPromoted: Boolean = false,
    val isVerified: Boolean = true,
    val openingHours: String = "08:00 - 20:00 (Mon-Sat)",
    val createdAt: Long = System.currentTimeMillis()
)

data class Inquiry(
    val inquiryId: String,
    val learnerId: String,
    val learnerName: String,
    val masterId: String,
    val masterName: String,
    val skill: String,
    val message: String,
    val status: String = "pending", // pending, accepted, rejected, completed
    val learnerPhone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class MessageLog(
    val logId: String,
    val userId: String,
    val customerName: String,
    val incomingMessage: String,
    val repliedWith: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "replied",
    val aiUsed: Boolean = false
)

data class PaymentRecord(
    val paymentId: String,
    val uid: String,
    val email: String,
    val plan: String,
    val amount: Int,
    val method: String = "opay_secure_link",
    val reference: String,
    val status: String = "pending", // pending, confirmed, failed
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val sqlSynced: Boolean = false,
    val encryptedRef: String = "",
    val secureToken: String = "",
    val clientHash: String = "",
    val secureLink: String = "",
    val maskedAccount: String = "7081****44"
)
