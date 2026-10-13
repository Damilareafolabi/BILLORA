package com.example.data

import androidx.room.TypeConverter
import com.example.model.DocumentStatus
import com.example.model.DocumentType
import com.example.model.LineItem
import com.example.model.PaymentMethod
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class Converters {
    @TypeConverter
    fun fromLineItemList(items: List<LineItem>?): String {
        if (items.isNullOrEmpty()) return "[]"
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("description", item.description)
                put("quantity", item.quantity)
                put("unitPrice", item.unitPrice)
                put("discountPercent", item.discountPercent)
            }
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toLineItemList(json: String?): List<LineItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<LineItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LineItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        description = obj.optString("description", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        discountPercent = obj.optDouble("discountPercent", 0.0)
                    )
                )
            }
        } catch (_: Exception) {
            // fallback gracefully
        }
        return list
    }

    @TypeConverter
    fun fromDocumentType(type: DocumentType): String = type.name

    @TypeConverter
    fun toDocumentType(name: String): DocumentType =
        try { DocumentType.valueOf(name) } catch (_: Exception) { DocumentType.INVOICE }

    @TypeConverter
    fun fromDocumentStatus(status: DocumentStatus): String = status.name

    @TypeConverter
    fun toDocumentStatus(name: String): DocumentStatus =
        try { DocumentStatus.valueOf(name) } catch (_: Exception) { DocumentStatus.DRAFT }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? = method?.name

    @TypeConverter
    fun toPaymentMethod(name: String?): PaymentMethod? =
        name?.let { try { PaymentMethod.valueOf(it) } catch (_: Exception) { PaymentMethod.BANK_TRANSFER } }
}
