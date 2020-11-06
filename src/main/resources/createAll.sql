CREATE TABLE IF NOT EXISTS patogeno
 (
  id int not null AUTO_INCREMENT,
  tipo varchar(255) not null unique,
  cantidadEspecie varchar(255) not null ,
  primary key(id)
)

ENGINE = InnoDB;