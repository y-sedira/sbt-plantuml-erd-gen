package io.ysedira.erdGenerator

class ERD(tables: List[Table], links: List[Link]) {
  private def link(link: io.ysedira.erdGenerator.Link): String =
    s"${link.fktableName} --> ${link.pktableName}"

  private def table(table: Table): String =
    s"""entity ${table.tableName} {
       |${
      table.columns
        .filter(_.isPrimaryKey)
        .map(column)
        .sorted
        .mkString("\t", "\n\t", "")
    }
       |\t....
       |${
      table.columns
        .filterNot(_.isPrimaryKey)
        .map(column)
        .sorted
        .mkString("\t", "\n\t", "")
    }
       |}""".stripMargin

  private def column(column: Column): String =
    "%s: %s %s%s%s%s"
      .format(
        column.columnName,
        column.typeName.toUpperCase,
        if (column.isPrimaryKey) "**<<PK>>**" else "",
        if (column.isForeignKey) "**<<FK>>**" else "",
        if (column.isNull) "<<Null>>" else "",
        if (column.isAutoIncrement) "<<Auto>>" else ""
      )
      .stripMargin

  def generate(): String = {
    s"""
       |@startuml
       |'left to right direction
       |top to bottom direction
       |skinparam linetype ortho
       |
       |!theme plain
       |
       |${tables.map(table).mkString("\n")}
       |
       |
       |${links.map(link).mkString("\n")}
       |@enduml
       |""".stripMargin
  }
}
