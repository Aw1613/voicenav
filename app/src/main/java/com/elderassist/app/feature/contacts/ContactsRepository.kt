package com.elderassist.app.feature.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.elderassist.app.core.model.ContactCandidate
import com.elderassist.app.core.policy.ActionPolicy

class ContactsRepository(private val context: Context) {

    fun hasReadPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Queries contacts locally without logging PII (names, phone numbers, or IDs).
     */
    fun searchContacts(query: String, limit: Int = 10): List<ContactCandidate> {
        if (!hasReadPermission() || query.isBlank()) return emptyList()

        val results = mutableListOf<ContactCandidate>()
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )
        val selection = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
        val selectionArgs = arrayOf("%${query.trim()}%")
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val keyIdx = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)
            val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val hasPhoneIdx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (cursor.moveToNext() && results.size < limit) {
                val id = if (idIdx >= 0) cursor.getLong(idIdx) else continue
                val lookupKey = if (keyIdx >= 0) cursor.getString(keyIdx).orEmpty() else ""
                val displayName = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty() else ""
                val hasPhone = if (hasPhoneIdx >= 0) cursor.getInt(hasPhoneIdx) > 0 else false
                if (displayName.isNotBlank() && lookupKey.isNotBlank()) {
                    val hint = if (hasPhone) fetchMaskedPhoneHint(id) else null
                    results += ContactCandidate(
                        contactId = id,
                        lookupKey = lookupKey,
                        displayName = displayName,
                        phoneHint = hint
                    )
                }
            }
        }
        return results
    }

    private fun fetchMaskedPhoneHint(contactId: Long): String? {
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val args = arrayOf(contactId.toString())

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            selection,
            args,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val raw = cursor.getString(0).orEmpty()
                if (raw.isNotBlank()) {
                    return ActionPolicy.maskPhoneLast4(raw)
                }
            }
        }
        return null
    }
}
