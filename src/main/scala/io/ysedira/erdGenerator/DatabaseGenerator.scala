package io.ysedira
package erdGenerator

import java.io.File
import java.nio.file.{Files, Paths}
import java.sql.{Connection, DriverManager, ResultSet}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object DatabaseGenerator {

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
        name <- map.get("table_name").toRight("'table_name' is missing")
        schema <- map.get("table_schem").toRight("'table_schem' is missing")
        typ <- map.get("table_type").toRight("'table_type' is missing")
      } yield Table(name, schema, typ, List.empty)
    }
  }

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

  final case class Link(
                         fkName: String,
                         fkcolumnName: String,
                         fktableName: String,
                         fktableSchem: String,
                         pkcolumnName: String,
                         pktableName: String,
                         pktableSchem: String
                       )

  object Link {
    def apply(map: Map[String, String]): Either[String, Link] = for {
      fkName <- map.get("fk_name").toRight("'fk_name' is missing")
      fkcolumnName <- map
        .get("fkcolumn_name")
        .toRight("'fkcolumn_name' is missing")
      fktableName <- map
        .get("fktable_name")
        .toRight("'fktable_name' is missing")
      fktableSchem <- map
        .get("fktable_schem")
        .toRight("'fktable_schem' is missing")
      pkcolumnName <- map
        .get("pkcolumn_name")
        .toRight("'pkcolumn_name' is missing")
      pktableName <- map
        .get("pktable_name")
        .toRight("'pktable_name' is missing")
      pktableSchem <- map
        .get("pktable_schem")
        .toRight("'pktable_schem' is missing")

    } yield Link(
      fkName,
      fkcolumnName,
      fktableName,
      fktableSchem,
      pkcolumnName,
      pktableName,
      pktableSchem
    )
  }

  class ERD(tables: List[Table], links: List[Link]) {
    private def link(link: Link): String =
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
         |${tables.map(table).mkString("\n")}
         |
         |
         |${links.map(link).mkString("\n")}
         |@enduml
         |""".stripMargin
    }
  }

  def main(url: String, driver: String, username: String, password: String)(output: File): Unit = {

    Class.forName(driver)
    val connection = DriverManager.getConnection(url, username, password)
    val tablesWithColumns: List[Table] =
      tables(connection)
        .map(Table.apply)
        .collect { case Right(value) => value }
        .map(table => {
          val pkeys: Set[String] =
            pks(connection, table).flatMap(_.get("column_name")).toSet
          val _fksE: Set[String] =
            fksImport(connection, table).flatMap(_.get("fkcolumn_name")).toSet

          val cols = columns(connection, table)
            .map(Column.apply)
            .collect { case Right(value) => value }
            .map(c =>
              if (pkeys.contains(c.columnName)) c.copy(isPrimaryKey = true)
              else if (_fksE.contains(c.columnName)) c.copy(isForeignKey = true)
              else c
            )
          table.copy(columns = cols)
        })

    //    tablesWithColumns.foreach(println)
    val links =
      tablesWithColumns
        .flatMap(fksImport(connection, _).map(Link.apply).collect {
          case Right(value) => value
        })
    println(output.toPath)
    if (!output.exists()) {
      output.createNewFile()
    }
    Files.writeString(
      output.toPath,
      new ERD(tablesWithColumns, links).generate()
    )

    //
    //    val _pks: ArrayBuffer[ArrayBuffer[Map[String, String]]] =
    //      _tables.map(pks(connection, _))
    //    val _indexes: ArrayBuffer[ArrayBuffer[Map[String, String]]] =
    //      _tables.map(indexes(connection, _))
    //    val _fksI: ArrayBuffer[ArrayBuffer[Map[String, String]]] =
    //      _tables.map(fksImport(connection, _))
    //
    //    pprint2(_tables, Set.empty)
    //    pprint(_pks)
    //    pprint(_indexes)
    //    pprint(_fksI ++ _fksE)
    //    pprint(tablesWithColumns)
    //      Set(
    //        "COLUMN_NAME",
    //        "IS_AUTOINCREMENT",
    //        "IS_NULLABLE",
    //        "TABLE_NAME",
    //        "TABLE_SCHEM",
    //        "TYPE_NAME",
    //        "table_type"
    //      )
    //    )

  }

  private def pprint(
                      maps: List[List[Map[String, String]]],
                      valueexclude: Set[String] = Set.empty
                    ) = {
    maps
      .map(prepPrint(_, valueexclude).mkString("\n"))
      .foreach(println)
  }

  private def pprint2(
                       maps: List[Map[String, String]],
                       valueexclude: Set[String] = Set.empty
                     ): Unit = {
    prepPrint(maps).foreach(println)
  }

  private def prepPrint(
                         maps: List[Map[String, String]],
                         valueexclude: Set[String] = Set.empty
                       ) = {
    maps
      .map(
        _.toList
          .filter(e => valueexclude.isEmpty || valueexclude.contains(e._1))
          .sortBy(_._1)
          .map { case (k, v) => s"$k:$v" }
          .mkString(
            "----------\n\t",
            "\n\t",
            "\n====================================================================="
          )
      )
      .sorted
  }

  private def tables(connection: Connection) = {
    val rs: ResultSet =
      connection.getMetaData.getTables(null, null, null, Array("TABLE"))
    drain(rs)
  }

  private def drain(rs: ResultSet) = {
    val _columnNames: ArrayBuffer[_root_.java.lang.String] = columnNames(rs)
    val _rows = mutable.ArrayBuffer[Map[String, String]]()
    while (rs.next()) {
      _rows.append(
        _columnNames
          .map(n => (n, rs.getString(n)))
          .filter(_._2 != null)
          .filterNot(_._2.isBlank)
          .toMap
      )
    }
    _rows.toList
  }

  private def columnNames(rs: ResultSet) = {
    val count = rs.getMetaData.getColumnCount
    val _columnNames = mutable.ArrayBuffer[String]()
    for (i <- 1 to count) {
      _columnNames.append(rs.getMetaData.getColumnName(i))
    }
    _columnNames
  }

  private def columns(connection: Connection, row: Table) = {
    drain {
      connection.getMetaData.getColumns(
        null,
        row.tableSchem,
        row.tableName,
        null
      )
    } // .map(_ ++ row)
  }

  def pks(
           connection: Connection,
           row: Table
         ): List[Map[String, String]] = {
    drain {
      connection.getMetaData.getPrimaryKeys(
        null,
        row.tableSchem,
        row.tableName
      )
    }
  }

  def indexes(
               connection: Connection,
               row: Map[String, String]
             ): List[Map[String, String]] = {
    drain {
      connection.getMetaData.getIndexInfo(
        null,
        row.getOrElse("table_schem", null),
        row.getOrElse("table_name", null),
        true,
        false
      )
    }.map(_ ++ row)
  }

  def fksExport(
                 connection: Connection,
                 row: Table
               ): List[Map[String, String]] = {
    drain {
      connection.getMetaData.getExportedKeys(
        null,
        row.tableSchem,
        row.tableName
      )
    }
  }

  def fksImport(
                 connection: Connection,
                 row: Table
               ): List[Map[String, String]] = {
    drain {
      connection.getMetaData.getImportedKeys(
        null,
        row.tableSchem,
        row.tableName
      )
    }
  }

}