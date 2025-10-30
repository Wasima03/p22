package a22;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class updateableDerby {

    public static void main(String[] args) throws ClassNotFoundException {
        String sqlConsulta = "SELECT nombre, cuenta FROM contadores WHERE nombre='contador1'";

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

        String url = "jdbc:derby:/home/alumno/BBDD/derby/contadores;create=true";

        try (Connection connection = DriverManager.getConnection(url)) {

            try {
                connection.createStatement().execute(
                        "CREATE TABLE contadores (nombre VARCHAR(20) PRIMARY KEY, cuenta INT)"
                );
                System.out.println("Tabla 'contadores' creada correctamente.");
            } catch (Exception e) {
                System.out.println("La tabla ya existe.");
            }

            try {
                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO contadores (nombre, cuenta) VALUES ('contador1', 0)"
                );
                insert.executeUpdate();
                System.out.println("Fila inicial insertada.");
            } catch (Exception e) {
                System.out.println("La fila ya existe.");
            }

            PreparedStatement consulta = connection.prepareStatement(
                    sqlConsulta,
                    ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_UPDATABLE
            );

            int cuenta = 0;

            for (int i = 0; i < 1000; i++) {
                connection.setAutoCommit(false); // Comienza transacción

                ResultSet res = consulta.executeQuery();
                if (res.next()) {
                    cuenta = res.getInt("cuenta") + 1;
                    res.updateInt("cuenta", cuenta);
                    res.updateRow(); 
                } else {
                    break;
                }

                connection.commit();
                connection.setAutoCommit(false);
            }

            connection.setAutoCommit(true);
            System.out.println("Valor final: " + cuenta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


