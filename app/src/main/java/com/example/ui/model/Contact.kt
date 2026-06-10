package com.example.ui.model

data class Contact(
    val name: String,
    val phone: String,
    val role: String,
    val avatarEmoji: String,
    val suggestedPrompt: String
)

val MOCK_CONTACTS = listOf(
    Contact(
        name = "Seraphina AI",
        phone = "+1 (800) 555-0199",
        role = "System Companion",
        avatarEmoji = "🎙️",
        suggestedPrompt = "Help me plan my day and review my schedule."
    ),
    Contact(
        name = "John (Landlord)",
        phone = "+1 (212) 555-0143",
        role = "Properties Admin",
        avatarEmoji = "🔑",
        suggestedPrompt = "Ask John to check the plumbing speed and extend our check-out by 3 hours."
    ),
    Contact(
        name = "Dr. Emily (Dentist)",
        phone = "+1 (415) 555-0288",
        role = "Desk Representative",
        avatarEmoji = "🦷",
        suggestedPrompt = "Reschedule tomorrow's dental checkup to Friday morning."
    ),
    Contact(
        name = "Sarah (Co-Author)",
        phone = "+1 (310) 555-0122",
        role = "Creative Lead",
        avatarEmoji = "📚",
        suggestedPrompt = "Apologize for being 30 minutes late and review the book chapter drafts."
    ),
    Contact(
        name = "Michael (Delivery)",
        phone = "+1 (206) 555-0311",
        role = "Logistics Courier",
        avatarEmoji = "📦",
        suggestedPrompt = "Request to leave the delivery package behind the planter box near the door."
    )
)
