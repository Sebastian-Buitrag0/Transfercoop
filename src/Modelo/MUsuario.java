package Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import Controlador.CLogin;
import Vista.VLogin;
import Vista.VUsuario;

public class MUsuario {

	VLogin vLogin = new VLogin();
	NumberFormat prt = NumberFormat.getInstance(new Locale("es", "COL"));
	Conexion conexion = new Conexion();
	Cliente cliente = new Cliente();

	Double retirado;
	Double dinero;

	Connection con = null;
	String accion = cliente.getAccion();

	PreparedStatement ps;
	ResultSet rs;

	public void salir(VUsuario v) {

		VLogin vl = new VLogin();
		MLogin m = new MLogin();
		CLogin c = new CLogin(vl, m);
		vl.setVisible(true);
		v.dispose();
	}

	public void abrirVTransferir(VUsuario v) {
		v.panelP.setVisible(false);
		v.panelT.setVisible(true);

	}

	public void abrirVMovimientos(VUsuario v, MLogin ml) {
		v.panelP.setVisible(false);
		v.panelM.setVisible(true);
		listaTransacciones(v, ml);

	}

	public void volver(VUsuario v) {
		v.panelP.setVisible(true);
		v.panelT.setVisible(false);
		v.panelM.setVisible(false);
	}

	public void mostrarDatos(VUsuario v, MLogin m) {
		try {
			con = conexion.getConection();
			ps = con.prepareStatement("SELECT * FROM usuarios WHERE nID = ?");
			ps.setString(1, m.cliente.getID());
			rs = ps.executeQuery();
			if(rs.next()) {
				m.cliente.setDinero(rs.getDouble("dinero"));
			}
			
		}catch (Exception e) {
			System.out.println(e);
		}
		
		
		NumberFormat nf = NumberFormat.getInstance(new Locale("es", "COL"));
		
		

		v.lblNombre.setText(m.cliente.getNombre() + " " + m.cliente.getApellido());
		v.lblId.setText(m.cliente.getID());
		v.lblFecha.setText(java.time.LocalDateTime.now().toLocalDate().toString());
		v.lblDinero.setText(nf.format(m.cliente.getDinero()));

		v.lblNombre.setText(m.cliente.getNombre() + " " + m.cliente.getApellido());
		v.lblId.setText(m.cliente.getID());
		v.lblDinero.setText(prt.format(m.cliente.getDinero()));
	}

	public void trasferir(VUsuario v, MLogin ml) {
		accion = "Transferencia";
		try {
			con = conexion.getConection();
			ps = con.prepareStatement("SELECT * FROM usuarios WHERE nID = ?");
			ps.setString(1, v.id.getText());

			
			if (v.monto.getText().equals("") || v.id.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "Hay campos vacio");
			} else {
				retirado = Double.parseDouble(v.monto.getText());
				rs = ps.executeQuery();
				
				if (retirado <= 0 || retirado > ml.cliente.getDinero() || rs.next() != true) {
					JOptionPane.showMessageDialog(null, "Valor no validos");
				} else {
					Double dineroU = rs.getDouble("dinero");
					dineroU = dineroU + retirado;
					dinero = ml.cliente.getDinero() - retirado;
					ml.cliente.setDinero(dinero);

					ps = con.prepareStatement("UPDATE usuarios SET dinero = '" + dineroU + "' WHERE nID = ?");
					ps.setString(1, v.id.getText());
					ps.executeUpdate();

					actualizar(ml);

					if (v.id.getText().equals(ml.cliente.getID())) {
						JOptionPane.showMessageDialog(null, "No se puede hacer la transaccion a su propia cuenta");
						v.id.setText(null);
						v.monto.setText(null);
					} else {
						int res = ps.executeUpdate();
						if (res > 0) {
							JOptionPane.showMessageDialog(null, "Transferencia exitosa");
							registrar(ml,v);
							v.id.setText(null);
							v.monto.setText(null);

						} else {
							JOptionPane.showMessageDialog(null, "Ocurrio un error");
							v.id.setText(null);
							v.monto.setText(null);
						}
					}

				}
			}

		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void listaTransacciones(VUsuario v, MLogin ml){
		System.out.println("entrando metodo");
		String a;
		String sql;
		String h;

		
		DefaultTableModel model = new DefaultTableModel();

		model.addColumn("ID");
		model.addColumn("ID destino");
		model.addColumn("Fecha");
		model.addColumn("Accion");
		model.addColumn("Dinero");
		
		v.table.setModel(model);

		a = String.valueOf(ml.cliente.getRango());
			try {
			System.out.println("Try");
//			if(a.equals("A")){
//				sql = "SELECT * FROM movimientos ORDER BY fecha DESC";
//			}else{
//				"SELECT * FROM movimientos WHERE nID= ? OR nID_Des = ? ORDER BY fecha DESC";
//			}
			String usuarios[] = new String[5];
			
			NumberFormat nf = NumberFormat.getInstance(new Locale("es", "COL"));
			con = conexion.getConection();


			
			ps = con.prepareStatement("SELECT * FROM movimientos WHERE nID= ? OR nID_Des = ? ORDER BY fecha DESC");
			ps.setString(1, ml.cliente.getID());
			ps.setString(2, ml.cliente.getID());
			rs = ps.executeQuery();
			rs.next();
		
			System.out.println("Antes del while\nValor rs "+rs.next());
			while(rs.next()){
				System.out.println("Dentro");
				h = nf.format(rs.getDouble("monto"));

				usuarios[0] = rs.getString("nID");
				usuarios[1] = rs.getString("nID_Des");
				usuarios[2] = rs.getString("fecha");
				usuarios[3] = rs.getString("accion");
				usuarios[4] = h;
				System.out.println("adding");
				model.addRow(usuarios);
			}
			v.table.setModel(model);
			v.table.getColumnModel().getColumn(2).setPreferredWidth(150);
			v.table.getTableHeader().setReorderingAllowed(false);
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void actualizar(MLogin ml) {
		try {
			con = conexion.getConection();
			ps = con.prepareStatement("UPDATE usuarios SET dinero = ? WHERE nID = ?");
			ps.setDouble(1, ml.cliente.getDinero());
			ps.setString(2, ml.cliente.getID());
			ps.executeUpdate();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void registrar(MLogin ml, VUsuario v) {
		try {
			con = conexion.getConection();
			ps = con.prepareStatement("INSERT INTO movimientos (nID, fecha, nID_Des, accion, monto) VALUES(?,?,?,?,?)");
			ps.setString(1, ml.cliente.getID());
			ps.setTimestamp(2, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
			ps.setString(3, v.id.getText());
			ps.setString(4, accion);
			ps.setDouble(5, retirado);
			ps.executeUpdate();

		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
