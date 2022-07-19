package io.ysedira.erdGenerator

  final case class Table(
                          tableName: String,
                          tableSchem: String,
                          tableType: String,
                          columns: List[Column]
                        ) {
    override def toString: String =
      s"""${tableType.toUpperCase} $tableSchem.$tableName
         |${columns.map(_.toString).mkString("\t", "\n\t", "\n")}
         |""".stripMargin
  }

  object Table {
    def apply(map: Map[String, String]): Either[String, Table] = {
      for {
        name <- map.get("TABLE_NAME").toRight("'TABLE_NAME' is missing")
        schema <- map.get("TABLE_SCHEM").toRight("'TABLE_SCHEM' is missing")
        typ <- map.get("TABLE_TYPE").toRight("'TABLE_TYPE' is missing")
      } yield Table(name, schema, typ, List.empty)
    }
  }
