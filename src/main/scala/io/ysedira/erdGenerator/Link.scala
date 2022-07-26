package io.ysedira.erdGenerator

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
