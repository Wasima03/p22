package p22;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ContadorSqlTransaccionesEmbebida {

    public static void main(String[] args) throws ClassNotFoundException {
    	
        String sqlCreate = "CREATE TABLE contadores (nombre VARCHAR(10) PRIMARY KEY, cuenta INT)";
        String sqlInsert = "MERGE INTO contadores t USING (VALUES (?, ?)) AS v(nombre, cuenta) "
                         + "ON t.nombre = v.nombre "
                         + "WHEN NOT MATCHED THEN INSERT (nombre, cuenta) VALUES (v.nombre, v.cuenta)";
        String sqlConsulta = "SELECT nombre, cuenta FROM contadores WHERE nombre='contador1'";
        String sqlActualizacion = "UPDATE contadores SET cuenta=? WHERE nombre='contador1'";

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        String url = "jdbc:derby:/home/alumno/BBDD/derby/contadores;create=true";

        try (Connection connection = DriverManager.getConnection(url)) {

            try {
                connection.createStatement().execute(sqlCreate);
                System.out.println("Tabla 'contadores' creada correctamente.");
            } catch (Exception e) {
                System.out.println("La tabla ya existe.");
            }

            try (PreparedStatement insert = connection.prepareStatement(sqlInsert)) {
                insert.setString(1, "contador1");
                insert.setInt(2, 0);
                insert.executeUpdate();
                System.out.println("Fila inicial insertada correctamente.");
            } catch (Exception e) {
                System.out.println("Error al insertar la fila inicial (ya existe probablemente).");
            }

            PreparedStatement consulta = connection.prepareStatement(
                    sqlConsulta,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE
            );

            int cuenta = 0;

            connection.setAutoCommit(false); 

            for (int i = 0; i < 1000; i++) {
                ResultSet res = consulta.executeQuery();
                if (res.next()) {
                    cuenta = res.getInt("cuenta") + 1;
                    res.updateInt("cuenta", cuenta);
                    res.updateRow(); 
                } else {
                    break;
                }
                connection.commit();
            }

            connection.setAutoCommit(true);
            System.out.println("Valor final: " + cuenta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



