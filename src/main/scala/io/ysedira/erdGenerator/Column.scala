package io.ysedira.erdGenerator


final case class Column(
                         charOctetLength: String,
                         columnName: String,
                         columnSize: String,
                         dataType: String,
                         decimalDigits: String,
                         isAutoincrement: String,
                         isNullable: String,
                         nullable: String,
                         numPrecRadix: String,
                         ordinalPosition: String,
                         tableName: String,
                         tableSchem: String,
                         typeName: String,
                         isPrimaryKey: Boolean,
                         isForeignKey: Boolean
                       ) {
  def isNull: Boolean = isNullable != "NO"

  def isAutoIncrement: Boolean = isAutoincrement != "NO"

  override def toString: String =
    s"$columnName $dataType ${if (isNull) "<<Null>>"}${if (isAutoIncrement) "<<Auto>>"}"
}

object Column {
  def apply(map: Map[String, String]): Either[String, Column] = for {
    charOctetLength <- map
      .get("CHAR_OCTET_LENGTH")
      .toRight("'CHAR_OCTET_LENGTH' is missing")
    columnName <- map.get("COLUMN_NAME").toRight("'COLUMN_NAME' is missing")
    columnSize <- map.get("COLUMN_SIZE").toRight("'COLUMN_SIZE' is missing")
    dataType <- map.get("DATA_TYPE").toRight("'DATA_TYPE' is missing")
    decimalDigits <- map
      .get("DECIMAL_DIGITS")
      .toRight("'DECIMAL_DIGITS' is missing")
    isAutoincrement <- map
      .get("IS_AUTOINCREMENT")
      .toRight("'IS_AUTOINCREMENT' is missing")
    isNullable <- map.get("IS_NULLABLE").toRight("'IS_NULLABLE' is missing")
    nullable <- map.get("NULLABLE").toRight("'NULLABLE' is missing")
    numPrecRadix <- map
      .get("NUM_PREC_RADIX")
      .toRight("'NUM_PREC_RADIX' is missing")
    ordinalPosition <- map
      .get("ORDINAL_POSITION")
      .toRight("'ORDINAL_POSITION' is missing")
    tableName <- map.get("TABLE_NAME").toRight("'TABLE_NAME' is missing")
    tableSchem <- map.get("TABLE_SCHEM").toRight("'TABLE_SCHEM' is missing")
    typeName <- map.get("TYPE_NAME").toRight("'TYPE_NAME' is missing")
  } yield Column(
    charOctetLength,
    columnName,
    columnSize,
    dataType,
    decimalDigits,
    isAutoincrement,
    isNullable,
    nullable,
    numPrecRadix,
    ordinalPosition,
    tableName,
    tableSchem,
    typeName,
    isPrimaryKey = false,
    isForeignKey = false
  )
}
