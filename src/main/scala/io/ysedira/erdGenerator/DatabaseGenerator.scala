package io.ysedira
package erdGenerator

import net.sourceforge.plantuml.{FileFormat, FileFormatOption}

import java.io.File
import java.nio.file.Files
import java.sql.{Connection, DriverManager, ResultSet}
import scala.annotation.tailrec
import scala.collection.mutable

object DatabaseGenerator {


  def generate(url: String, driver: String, username: String, password: String)(output: File): Unit = {
    Class.forName(driver)
    val connection = DriverManager.getConnection(url, username, password)
    val tablesWithColumns: List[Table] =
      tables(connection)
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

    val links =
      tablesWithColumns
        .flatMap(fksImport(connection, _).map(Link.apply).collect {
          case Right(value) => value
        })
    val str = new ERD(tablesWithColumns, links).generate()
    plantumlExport(str, output)

  }


  private def tables(connection: Connection): List[Either[String, Table]] = {
    drain {
      connection.getMetaData.getTables(null, null, null, Array("TABLE"))
    }.map(Table.apply)

  }

  private def drain(rs: ResultSet) = {
    val _columnNames: Set[String] = columnNames(rs).toSet

    @tailrec
    def _drain(rs: ResultSet, _rows: List[Map[String, String]]): List[Map[String, String]] = {
      if (!rs.next()) {
        _rows
      } else {
        _drain(rs, _rows :+ _columnNames
          .map(n => (n, rs.getString(n)))
          .filter(_._2 != null)
          .filterNot(_._2.isBlank)
          .toMap)
      }
    }

    _drain(rs, List.empty)

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

  def plantumlExport(source: String, output: File) = {
    import net.sourceforge.plantuml.SourceStringReader;

    val reader: SourceStringReader = new SourceStringReader(source);
    val svgFile = new File(output.getAbsolutePath + "/erd.svg")
    val pngFile = new File(output.getAbsolutePath + "/erd.png")
    val plumFile = new File(output.getAbsolutePath + "/erd.puml")

    Files.writeString(plumFile.toPath, source)
    reader.outputImage(Files.newOutputStream(svgFile.toPath), new FileFormatOption(FileFormat.SVG));
    reader.outputImage(Files.newOutputStream(pngFile.toPath), new FileFormatOption(FileFormat.PNG));
    ()
    // Generated files
  }

}