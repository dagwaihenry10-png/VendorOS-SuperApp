package com.example.repository

import android.content.Context
import com.example.model.AutoRule
import com.example.model.DeliveryProof
import com.example.model.Inquiry
import com.example.model.MessageLog
import com.example.model.PaymentRecord
import com.example.model.SkillMaster
import com.example.model.UserProfile
import com.example.services.PaymentService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * AppRepository manages reactive state across all 3 modes:
 * Vendor Mode (Auto-Reply + Trust & Proofs), Skills Mode (Handwork Marketplace), and Super Mode.
 */
class AppRepository private constructor(private val context: Context) {

    private val _userProfile = MutableStateFlow(
        UserProfile(
            uid = "user_chidi_99",
            name = "Chidi Okafor",
            email = "chidi@classicwears.ng",
            phone = "+2348039281726",
            businessName = "Classic Fits & Tech Hub",
            selectedState = "Lagos",
            selectedLGA = "Ikeja",
            selectedArea = "Computer Village / Allen",
            activeMode = null, // Will trigger Role Selection on fresh launch or can be switched
            trustScore = 4.9,
            totalDeliveries = 42,
            verifiedDeliveries = 39,
            pendingDeliveries = 3,
            autoReplyOn = true,
            isPro = false,
            proPlan = "none",
            workingHoursStart = "08:00",
            workingHoursEnd = "21:00",
            aiModeEnabled = true
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _rules = MutableStateFlow<List<AutoRule>>(emptyList())
    val rules: StateFlow<List<AutoRule>> = _rules.asStateFlow()

    private val _proofs = MutableStateFlow<List<DeliveryProof>>(emptyList())
    val proofs: StateFlow<List<DeliveryProof>> = _proofs.asStateFlow()

    private val _masters = MutableStateFlow<List<SkillMaster>>(emptyList())
    val masters: StateFlow<List<SkillMaster>> = _masters.asStateFlow()

    private val _inquiries = MutableStateFlow<List<Inquiry>>(emptyList())
    val inquiries: StateFlow<List<Inquiry>> = _inquiries.asStateFlow()

    private val _logs = MutableStateFlow<List<MessageLog>>(emptyList())
    val logs: StateFlow<List<MessageLog>> = _logs.asStateFlow()

    private val _payments = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val payments: StateFlow<List<PaymentRecord>> = _payments.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val uid = _userProfile.value.uid

        // 1. Initial Auto-Reply Rules
        _rules.value = listOf(
            AutoRule(
                ruleId = "rule_1",
                userId = uid,
                keywords = listOf("price", "how much", "cost", "hm"),
                replyText = "Hello boss! Price starts from ₦6,500. Same day dispatch across Lagos. You wan make I pack your order now? 📦",
                type = "keyword",
                isActive = true,
                triggeredCount = 84,
                aiEnhanced = true
            ),
            AutoRule(
                ruleId = "rule_2",
                userId = uid,
                keywords = listOf("location", "where", "shop", "address"),
                replyText = "We dey Suite 14, Allen Avenue, Ikeja Lagos. We also deliver nationwide. What is your drop-off address? 🚚",
                type = "keyword",
                isActive = true,
                triggeredCount = 52,
                aiEnhanced = false
            ),
            AutoRule(
                ruleId = "rule_3",
                userId = uid,
                keywords = listOf("welcome", "hello", "hi", "good morning"),
                replyText = "Welcome to Classic Fits & Tech Hub! How fit we help you today? Check our Trust Score badge for verified deliveries. ✅",
                type = "welcome",
                isActive = true,
                triggeredCount = 120,
                aiEnhanced = true
            ),
            AutoRule(
                ruleId = "rule_4",
                userId = uid,
                keywords = listOf("scam", "trust", "pay on delivery", "pod"),
                replyText = "100% Verified Vendor with over 40+ confirmed customer deliveries! View our Trust Proofs directly on VendorOS. Safe & fast! 🛡️",
                type = "keyword",
                isActive = true,
                triggeredCount = 37,
                aiEnhanced = true
            )
        )

        // 2. Initial Delivery Proofs
        _proofs.value = listOf(
            DeliveryProof(
                proofId = "prf_101",
                vendorId = uid,
                vendorName = "Classic Fits & Tech Hub",
                customerName = "Adesuwa Bakare",
                customerPhoneFull = "08034567891",
                customerPhoneMasked = "***7891",
                orderId = "ORD-73921",
                photoUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80",
                status = "confirmed",
                rating = 5.0,
                comment = "Order arrived same day in Ikeja! Shoe original well well, definitely ordering again.",
                publicLink = "https://vendoros.ng/p/prf_101",
                qrData = "https://vendoros.ng/p/prf_101",
                createdAt = System.currentTimeMillis() - 86400000L * 2,
                confirmedAt = System.currentTimeMillis() - 86400000L
            ),
            DeliveryProof(
                proofId = "prf_102",
                vendorId = uid,
                vendorName = "Classic Fits & Tech Hub",
                customerName = "Emeka Nwosu",
                customerPhoneFull = "08129384756",
                customerPhoneMasked = "***4756",
                orderId = "ORD-48201",
                photoUrl = "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80",
                status = "confirmed",
                rating = 5.0,
                comment = "Smart watch received intact with warranty card. Best vendor!",
                publicLink = "https://vendoros.ng/p/prf_102",
                qrData = "https://vendoros.ng/p/prf_102",
                createdAt = System.currentTimeMillis() - 86400000L * 4,
                confirmedAt = System.currentTimeMillis() - 86400000L * 3
            ),
            DeliveryProof(
                proofId = "prf_103",
                vendorId = uid,
                vendorName = "Classic Fits & Tech Hub",
                customerName = "Folake Adeleke",
                customerPhoneFull = "09087654321",
                customerPhoneMasked = "***4321",
                orderId = "ORD-91024",
                photoUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
                status = "pending",
                rating = 5.0,
                comment = "Dispatched via GIG Logistics, awaiting customer confirmation.",
                publicLink = "https://vendoros.ng/p/prf_103",
                qrData = "https://vendoros.ng/p/prf_103",
                createdAt = System.currentTimeMillis() - 3600000L * 4
            )
        )

        // 3. Initial Nigerian Artisans / Handwork Masters
        _masters.value = listOf(
            SkillMaster(
                masterId = "master_1",
                userId = "user_ibrahim",
                businessName = "Master Ibrahim VIP Cuts",
                ownerName = "Ibrahim Babatunde",
                skillCategory = "Barber",
                skillName = "Celebrity Barber & Hair Stylist",
                otherSkills = listOf("Beard Grooming", "Hair Dyeing", "Dreadlocks"),
                yearsExperience = 9,
                description = "Master trainer with over 9 years training youth in modern fades, hair design, treatment and shop management. Trained over 40 apprentices.",
                state = "Lagos",
                lga = "Ikeja",
                area = "Allen Avenue",
                address = "12 Allen Avenue, Beside Mega Plaza, Ikeja",
                lat = 6.6018,
                lng = 3.3515,
                pricePerMonth = 7000,
                pricePerWeek = 2000,
                hasAccommodation = true,
                accommodationFee = 5000,
                images = listOf(
                    "https://images.unsplash.com/photo-1503951914875-452162b0f3f1?w=600&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1585747860715-2ba37e788b70?w=600&auto=format&fit=crop&q=80"
                ),
                phone = "08023456789",
                whatsapp = "2348023456789",
                isAvailable = true,
                rating = 4.9,
                totalStudents = 32,
                views = 480,
                isPromoted = true,
                isVerified = true,
                openingHours = "08:00 - 20:00 (Mon-Sat)"
            ),
            SkillMaster(
                masterId = "master_2",
                userId = "user_chinyere",
                businessName = "Chinyere Haute Couture & Fashion Academy",
                ownerName = "Madam Chinyere Okoye",
                skillCategory = "Tailoring",
                skillName = "Fashion Design & Pattern Cutting",
                otherSkills = listOf("Agbada Embroidery", "Bridal Wear", "Senator Styles"),
                yearsExperience = 12,
                description = "Learn male and female tailoring, modern pattern drafting, Agbada cutting and embroidery from scratch to professional finish.",
                state = "Lagos",
                lga = "Surulere",
                area = "Bode Thomas",
                address = "45 Bode Thomas Street, Surulere",
                lat = 6.4969,
                lng = 3.3582,
                pricePerMonth = 8500,
                pricePerWeek = 2500,
                hasAccommodation = false,
                accommodationFee = 0,
                images = listOf(
                    "https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=600&auto=format&fit=crop&q=80",
                    "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600&auto=format&fit=crop&q=80"
                ),
                phone = "08034561234",
                whatsapp = "2348034561234",
                isAvailable = true,
                rating = 5.0,
                totalStudents = 45,
                views = 620,
                isPromoted = true,
                isVerified = true,
                openingHours = "08:30 - 18:30 (Mon-Fri)"
            ),
            SkillMaster(
                masterId = "master_3",
                userId = "user_emmanuel",
                businessName = "GizmoTech Micro-Soldering & Phone Repair",
                ownerName = "Engr. Emmanuel Danladi",
                skillCategory = "Phone Repair",
                skillName = "Smartphone & Board Diagnostics",
                otherSkills = listOf("Screen Refurbishing", "IC Chip Replacement", "Software Flashing"),
                yearsExperience = 7,
                description = "Master smartphone board repairs, micro-soldering, short-circuit diagnostics, iPhone and Android chip replacement.",
                state = "Lagos",
                lga = "Ikeja",
                area = "Computer Village",
                address = "Shop B4, Pepple Street, Computer Village, Ikeja",
                lat = 6.5954,
                lng = 3.3431,
                pricePerMonth = 6000,
                pricePerWeek = 1800,
                hasAccommodation = true,
                accommodationFee = 4000,
                images = listOf(
                    "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=600&auto=format&fit=crop&q=80"
                ),
                phone = "08187654321",
                whatsapp = "2348187654321",
                isAvailable = true,
                rating = 4.8,
                totalStudents = 28,
                views = 310,
                isPromoted = false,
                isVerified = true,
                openingHours = "09:00 - 19:00 (Mon-Sat)"
            ),
            SkillMaster(
                masterId = "master_4",
                userId = "user_kemi",
                businessName = "Glow by Kemi Artistry Studio",
                ownerName = "Kemi Adeleke",
                skillCategory = "Makeup",
                skillName = "Bridal & Editorial Makeup Artist",
                otherSkills = listOf("Gele Tying", "Lash Extensions", "Skin Prep"),
                yearsExperience = 6,
                description = "Intensive professional training in bridal glam, studio makeup, HD photography look, gele tying, and client management.",
                state = "Lagos",
                lga = "Lagos Island",
                area = "Victoria Island",
                address = "Plot 8, Adeola Odeku, Victoria Island",
                lat = 6.4281,
                lng = 3.4219,
                pricePerMonth = 9000,
                pricePerWeek = 3000,
                hasAccommodation = false,
                accommodationFee = 0,
                images = listOf(
                    "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?w=600&auto=format&fit=crop&q=80"
                ),
                phone = "09033322110",
                whatsapp = "2349033322110",
                isAvailable = true,
                rating = 4.9,
                totalStudents = 19,
                views = 280,
                isPromoted = false,
                isVerified = true,
                openingHours = "09:00 - 18:00 (Tue-Sun)"
            ),
            SkillMaster(
                masterId = "master_5",
                userId = "user_samuel",
                businessName = "SolarTech Green Energy & Inverters",
                ownerName = "Samuel Ogundipe",
                skillCategory = "Solar Installation",
                skillName = "Solar Panels & Inverter Installation",
                otherSkills = listOf("Lithium Battery Sizing", "Electrical Wiring", "Earthing"),
                yearsExperience = 8,
                description = "Practical rooftop solar installation, inverter load calculations, lithium battery wiring, and troubleshooting for residential & commercial.",
                state = "Lagos",
                lga = "Alimosho",
                area = "Egbeda / Iyana Ipaja",
                address = "18 Akowonjo Road, Egbeda, Alimosho",
                lat = 6.6022,
                lng = 3.2842,
                pricePerMonth = 6500,
                pricePerWeek = 2000,
                hasAccommodation = true,
                accommodationFee = 3500,
                images = listOf(
                    "https://images.unsplash.com/photo-1509391365360-2e959784a276?w=600&auto=format&fit=crop&q=80"
                ),
                phone = "08055566778",
                whatsapp = "2348055566778",
                isAvailable = true,
                rating = 4.7,
                totalStudents = 22,
                views = 195,
                isPromoted = false,
                isVerified = true,
                openingHours = "08:00 - 18:00 (Mon-Sat)"
            )
        )

        // 4. Initial Inquiries
        _inquiries.value = listOf(
            Inquiry(
                inquiryId = "inq_1",
                learnerId = uid,
                learnerName = "Chidi Okafor",
                masterId = "master_1",
                masterName = "Master Ibrahim VIP Cuts",
                skill = "Barber",
                message = "Good day Master Ibrahim, I want to learn professional fades and modern styling. Is accommodation still available?",
                status = "accepted",
                learnerPhone = "08039281726",
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            Inquiry(
                inquiryId = "inq_2",
                learnerId = uid,
                learnerName = "Chidi Okafor",
                masterId = "master_3",
                masterName = "GizmoTech Micro-Soldering",
                skill = "Phone Repair",
                message = "I want to register for the 1-month intensive micro-soldering course. When is next batch starting?",
                status = "pending",
                learnerPhone = "08039281726",
                createdAt = System.currentTimeMillis() - 3600000L * 5
            )
        )

        // 5. Initial Message Logs
        _logs.value = listOf(
            MessageLog(
                logId = "log_1",
                userId = uid,
                customerName = "Bolaji from Lekki",
                incomingMessage = "How much for the classic sneakers in size 43?",
                repliedWith = "Hello boss! Price starts from ₦6,500. Same day dispatch across Lagos. You wan make I pack your order now? 📦",
                timestamp = System.currentTimeMillis() - 1800000L,
                status = "replied",
                aiUsed = true
            ),
            MessageLog(
                logId = "log_2",
                userId = uid,
                customerName = "Blessing (Instagram)",
                incomingMessage = "Are you people legit? I don't want scam please.",
                repliedWith = "No scam zone! Verified vendor with 40+ deliveries, check my Trust Badge. Your order safe with us ✅",
                timestamp = System.currentTimeMillis() - 7200000L,
                status = "replied",
                aiUsed = true
            )
        )

        // 6. Initial Payment Records
        _payments.value = listOf(
            PaymentRecord(
                paymentId = "PAY-INIT-001",
                uid = uid,
                email = _userProfile.value.email,
                plan = "basic",
                amount = 1000,
                method = "opay_secure_link",
                reference = "VOS-BASIC-INIT10",
                status = "confirmed",
                createdAt = System.currentTimeMillis() - 86400000L * 15,
                confirmedAt = System.currentTimeMillis() - 86400000L * 15,
                sqlSynced = true,
                secureLink = "https://vendoros.ng/pay?ref=VOS-BASIC-INIT10",
                maskedAccount = "7081****44"
            )
        )
    }

    // --- User Profile Actions ---
    fun updateUserMode(mode: String) {
        _userProfile.value = _userProfile.value.copy(activeMode = mode)
    }

    fun updateSelectedLocation(state: String, lga: String, area: String) {
        _userProfile.value = _userProfile.value.copy(
            selectedState = state,
            selectedLGA = lga,
            selectedArea = area
        )
    }

    fun toggleAutoReply(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(autoReplyOn = enabled)
    }

    fun toggleAiMode(enabled: Boolean) {
        _userProfile.value = _userProfile.value.copy(aiModeEnabled = enabled)
    }

    fun updateWorkingHours(start: String, end: String) {
        _userProfile.value = _userProfile.value.copy(
            workingHoursStart = start,
            workingHoursEnd = end
        )
    }

    fun updateBusinessName(name: String) {
        _userProfile.value = _userProfile.value.copy(businessName = name)
    }

    // --- Rules Management ---
    fun addRule(rule: AutoRule) {
        _rules.value = listOf(rule) + _rules.value
    }

    fun updateRule(rule: AutoRule) {
        _rules.value = _rules.value.map { if (it.ruleId == rule.ruleId) rule else it }
    }

    fun toggleRuleActive(ruleId: String) {
        _rules.value = _rules.value.map {
            if (it.ruleId == ruleId) it.copy(isActive = !it.isActive) else it
        }
    }

    fun deleteRule(ruleId: String) {
        _rules.value = _rules.value.filter { it.ruleId != ruleId }
    }

    // --- Proofs Management ---
    fun addProof(
        customerName: String,
        customerPhone: String,
        orderId: String,
        photoUrl: String
    ): DeliveryProof {
        val uid = _userProfile.value.uid
        val proofId = "prf_${UUID.randomUUID().toString().take(6)}"
        val maskedPhone = if (customerPhone.length >= 4) {
            "***" + customerPhone.takeLast(4)
        } else {
            "***${customerPhone}"
        }
        val safeOrderId = if (orderId.isNotBlank()) orderId else "ORD-${(10000..99999).random()}"
        val link = "https://vendoros.ng/p/$proofId"

        val newProof = DeliveryProof(
            proofId = proofId,
            vendorId = uid,
            vendorName = _userProfile.value.businessName,
            customerName = customerName,
            customerPhoneFull = customerPhone,
            customerPhoneMasked = maskedPhone,
            orderId = safeOrderId,
            photoUrl = photoUrl,
            status = "pending",
            rating = 5.0,
            comment = "New order delivered, pending customer confirmation.",
            publicLink = link,
            qrData = link,
            createdAt = System.currentTimeMillis()
        )

        _proofs.value = listOf(newProof) + _proofs.value
        _userProfile.value = _userProfile.value.copy(
            totalDeliveries = _userProfile.value.totalDeliveries + 1,
            pendingDeliveries = _userProfile.value.pendingDeliveries + 1
        )
        return newProof
    }

    fun confirmProof(proofId: String) {
        _proofs.value = _proofs.value.map { proof ->
            if (proof.proofId == proofId && proof.status == "pending") {
                proof.copy(
                    status = "confirmed",
                    confirmedAt = System.currentTimeMillis(),
                    comment = "Confirmed by customer! Satisfied with delivery."
                )
            } else {
                proof
            }
        }

        val verifiedCount = _userProfile.value.verifiedDeliveries + 1
        val pendingCount = maxOf(0, _userProfile.value.pendingDeliveries - 1)
        val newScore = minOf(5.0, 4.5 + (verifiedCount * 0.02)).coerceIn(4.0, 5.0)

        _userProfile.value = _userProfile.value.copy(
            verifiedDeliveries = verifiedCount,
            pendingDeliveries = pendingCount,
            trustScore = Math.round(newScore * 10.0) / 10.0
        )
    }

    fun deleteProof(proofId: String) {
        _proofs.value = _proofs.value.filter { it.proofId != proofId }
    }

    // --- Masters / Handwork Marketplace ---
    fun registerMaster(master: SkillMaster) {
        _masters.value = listOf(master) + _masters.value
    }

    fun incrementMasterViews(masterId: String) {
        _masters.value = _masters.value.map {
            if (it.masterId == masterId) it.copy(views = it.views + 1) else it
        }
    }

    fun promoteMaster(masterId: String) {
        _masters.value = _masters.value.map {
            if (it.masterId == masterId) it.copy(isPromoted = true) else it
        }
    }

    // --- Inquiries ---
    fun sendInquiry(masterId: String, masterName: String, skill: String, message: String) {
        val user = _userProfile.value
        val inquiry = Inquiry(
            inquiryId = "inq_${UUID.randomUUID().toString().take(6)}",
            learnerId = user.uid,
            learnerName = user.name,
            masterId = masterId,
            masterName = masterName,
            skill = skill,
            message = message,
            status = "pending",
            learnerPhone = user.phone,
            createdAt = System.currentTimeMillis()
        )
        _inquiries.value = listOf(inquiry) + _inquiries.value
    }

    fun updateInquiryStatus(inquiryId: String, newStatus: String) {
        _inquiries.value = _inquiries.value.map {
            if (it.inquiryId == inquiryId) it.copy(status = newStatus) else it
        }
    }

    // --- Payments ---
    fun recordPayment(payment: PaymentRecord) {
        _payments.value = listOf(payment) + _payments.value
    }

    fun activateProPlan(plan: String) {
        _userProfile.value = _userProfile.value.copy(
            isPro = true,
            proPlan = plan,
            proExpiry = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
        )
    }

    // --- Logs ---
    fun addMessageLog(log: MessageLog) {
        _logs.value = listOf(log) + _logs.value
    }

    companion object {
        @Volatile
        private var instance: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return instance ?: synchronized(this) {
                instance ?: AppRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
