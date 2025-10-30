package p22;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class ContadorSqlTransaccional {

	public static void main(String[] args) throws ClassNotFoundException {
		// Prueba de concepto de transacción con bloqueo de fila para lectura
		// Sería más fácil en el propio sql poner un set cuenta=cuenta+1 pero ilustramos
		// aquí el problema de concurrencia entre varios procesos.
		// con el for update + transacción conseguimos el bloque de fila y atomicidad
		
		String sqlCreate="create table if not exists contadores(nombre varchar(10) primary key,cuenta int);"; //sentencia sql para crear la tabla
        String sqlInsert = "INSERT IGNORE INTO contadores (nombre, cuenta) VALUES ('contador1', 0);"; //sentencia sql para insertar una fila en la tabla
		String sqlConsulta = "select nombre,cuenta from contadores where nombre='contador1' for update;";
		String sqlActualización = "update contadores set cuenta=? where nombre='contador1';";
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		
		try (Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/adat3?allowPublicKeyRetrieval=true", "dam2", "asdf.1234"))
		{
			PreparedStatement tabla=connection.prepareStatement(sqlCreate);
			PreparedStatement insert=connection.prepareStatement(sqlInsert);
			tabla.execute();
			insert.execute();
			PreparedStatement consulta = connection.prepareStatement(sqlConsulta);
			PreparedStatement actualización = connection.prepareStatement(sqlActualización);
			int cuenta = 0;
			
			for (int i=0; i<1000; i++) {
				connection.setAutoCommit(false);
				ResultSet res = consulta.executeQuery();
				if (res.next()) {
					cuenta = res.getInt(2);
					cuenta++;
					actualización.setInt(1, cuenta);
					actualización.executeUpdate();
				}
				else break;
				connection.commit();
				connection.setAutoCommit(false);
			} // for
			System.out.println("Valor final: " + cuenta);
		} catch (Exception e) {
			e.printStackTrace();
		} // try
	} // main
} // class ContadorSql
