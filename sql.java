package a22;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class sql {
    public static void main(String[] args) throws ClassNotFoundException {

        String sqlCreate = "CREATE TABLE contadores (nombre VARCHAR(20) PRIMARY KEY, cuenta INT)";
        String sqlInsert = "INSERT INTO contadores (nombre, cuenta) VALUES (?, ?)";
        String sqlSelect = "SELECT cuenta FROM contadores WHERE nombre = ?";
        String sqlUpdate = "UPDATE contadores SET cuenta = ? WHERE nombre = ?";

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        String url = "jdbc:derby:/home/alumno/BBDD/derby/contadores;create=true";

        try (Connection conn = DriverManager.getConnection(url)) {

            try {
                conn.createStatement().execute(sqlCreate);
                System.out.println("Tabla 'contadores' creada correctamente.");
            } catch (Exception e) {
                System.out.println("La tabla ya existe.");
            }

            boolean existe = false;
            try (PreparedStatement check = conn.prepareStatement(sqlSelect)) {
                check.setString(1, "contador1");
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    existe = true;
                }
            }

            if (!existe) {
                try (PreparedStatement insert = conn.prepareStatement(sqlInsert)) {
                    insert.setString(1, "contador1");
                    insert.setInt(2, 0);
                    insert.executeUpdate();
                    System.out.println("Fila insertada correctamente.");
                }
            } else {
                System.out.println("La fila ya existe.");
            }

            conn.setAutoCommit(false);

            try {
                for (int i = 0; i < 1000; i++) {
                    int cuenta = 0;

                    // Leer valor actual
                    try (PreparedStatement select = conn.prepareStatement(sqlSelect)) {
                        select.setString(1, "contador1");
                        ResultSet rs = select.executeQuery();
                        if (rs.next()) {
                            cuenta = rs.getInt("cuenta");
                        }
                    }

                    // Actualizar valor
                    try (PreparedStatement update = conn.prepareStatement(sqlUpdate)) {
                        update.setInt(1, cuenta + 1);
                        update.setString(2, "contador1");
                        update.executeUpdate();
                    }
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

            // Mostrar valor final
            try (PreparedStatement select = conn.prepareStatement(sqlSelect)) {
                select.setString(1, "contador1");
                ResultSet rs = select.executeQuery();
                if (rs.next()) {
                    System.out.println("Valor final: " + rs.getInt("cuenta"));
                } else {
                    System.out.println("No se ha encontrado la fila.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


