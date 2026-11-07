package com.ham.exchange.data.db

import androidx.room.TypeConverter
import java.math.BigDecimal

class Converters {
    @TypeConverter fun fromBigDecimal(v: BigDecimal?): String? = v?.toPlainString()
    @TypeConverter fun toBigDecimal(v: String?): BigDecimal? = v?.let { BigDecimal(it) }
}
