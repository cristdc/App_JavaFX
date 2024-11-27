package com.example.JAVAFX.CRISTINADIAZCABELLO.controladores;

import com.example.JAVAFX.CRISTINADIAZCABELLO.Dao.*;
import com.example.JAVAFX.CRISTINADIAZCABELLO.modelos.ConexionSingleton;
import com.example.JAVAFX.CRISTINADIAZCABELLO.modelos.Dia;
import com.example.JAVAFX.CRISTINADIAZCABELLO.modelos.DiaEstadoAnimoCR;
import com.example.JAVAFX.CRISTINADIAZCABELLO.modelos.EstadoDeAnimo;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ControladorEstadoAnimo implements Initializable {

    @FXML
    private ImageView imgDescribeTuDia, imgDescripcionDia, imgEmoji, imgSave;
    @FXML
    private Spinner<Integer> spnFuerzaSentimiento, spnGradoProductividad, spnPaciencia;
    @FXML
    private Label txtDiaMes;
    @FXML
    private ComboBox<String> cmbMomentoDia;

    private ControladorDiario cDiario;
    private ControladorDia cDia;
    private ControladorElegirEmoji cElegirEmoji;
    private ControladorPrincipal cPrincipal;

    private Dia dia;
    private DiaEstadoAnimoCR diaEstadoAnimoCR;
    private EstadoDeAnimo estadoDeAnimo;

    private DiaDAOclass diaDAOclass;
    private DiaEstadoAnimoCRDAOclass diaEstadoAnimoCRDAOclass;
    private EstadoDeAnimoDAOclass estadoDeAnimoDAOclass;
    private LocalDate fecha;
    private Connection conexion;

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    public LocalDate getFecha() {
        return fecha;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inicializarCombobox();
        inicializarSpinners();
        conexion = ConexionSingleton.getConexion();
        diaDAOclass = new DiaDAOclass(conexion);
        diaEstadoAnimoCRDAOclass = new DiaEstadoAnimoCRDAOclass(conexion);
        estadoDeAnimoDAOclass = new EstadoDeAnimoDAOclass(conexion);
    }
    private void inicializarCombobox() {
        cmbMomentoDia.getItems().addAll("Mañana", "Tarde", "Noche");
        cmbMomentoDia.getSelectionModel().select(0);
    }
    private void inicializarSpinners() {
        spnFuerzaSentimiento.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10));
        spnGradoProductividad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10));
        spnPaciencia.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10));
    }

    @FXML
    private void save(MouseEvent event) {
        try {
            int fuerzaSentimiento = spnFuerzaSentimiento.getValue();
            int gradoProductividad = spnGradoProductividad.getValue();
            int paciencia = spnPaciencia.getValue();
            String momentoDia = cmbMomentoDia.getValue();
            String emoji = estadoDeAnimo != null ? estadoDeAnimo.getEmoji() : "/img/neutral.png";
            String descripcion = cDiario.getTexto();

            if (estadoDeAnimo == null) {
                estadoDeAnimo = new EstadoDeAnimo(-1, emoji, paciencia, fuerzaSentimiento, gradoProductividad);
                estadoDeAnimoDAOclass.insert(estadoDeAnimo);
            } else {
                estadoDeAnimo.setFuerzaSentimiento(fuerzaSentimiento);
                estadoDeAnimo.setGradoProductividad(gradoProductividad);
                estadoDeAnimo.setPaciencia(paciencia);
                estadoDeAnimo.setEmoji(emoji);
                estadoDeAnimoDAOclass.update(estadoDeAnimo);
            }

            if(diaEstadoAnimoCR == null) {
                diaEstadoAnimoCR = new DiaEstadoAnimoCR(java.sql.Date.valueOf(getFecha()), -1, momentoDia, descripcion);
                diaEstadoAnimoCRDAOclass.insert(diaEstadoAnimoCR);
            } else {
                diaEstadoAnimoCR.setIdEstado(estadoDeAnimo.getIdEstado());
                diaEstadoAnimoCR.setMomentoDia(momentoDia);
                diaEstadoAnimoCR.setFecha(java.sql.Date.valueOf(getFecha()));
                diaEstadoAnimoCR.setDescripcion(descripcion);
                diaEstadoAnimoCRDAOclass.update(diaEstadoAnimoCR);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Se ha guardado correctamente, puedes cerrar sin pérdida de datos.");
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Ha ocurrido un error al guardar los datos.");
            alert.show();
        }
    }

    @FXML
    private void elegirEmoji(MouseEvent event) throws IOException {
        abrirVentana("/com/example/JAVAFX/CRISTINADIAZCABELLO/vistas/ControladorElegirEmoji.fxml", "Controlador Emoji", (loader) -> {
            cElegirEmoji = loader.getController();
            cElegirEmoji.setControladorEnlace(this);
        });
    }
    @FXML
    private void abrirBloc(MouseEvent event) throws IOException {
        if (dia == null) {
            dia = new Dia(java.sql.Date.valueOf(String.valueOf(cPrincipal.selectedDay)), 0, "", false, "");
        }
        if(diaEstadoAnimoCR == null) {
            diaEstadoAnimoCR = new DiaEstadoAnimoCR(java.sql.Date.valueOf(getFecha()), -1, "", "");
        }

        abrirVentana("/com/example/JAVAFX/CRISTINADIAZCABELLO/vistas/ControladorDiario.fxml", "Controlador Diario", (loader) -> {
            cDiario = loader.getController();
            cDiario.setControladorEnlace(this);
            cDiario.setDiaEstadoAnimoCR(diaEstadoAnimoCR);
        });
    }
    @FXML
    private void abrirControladorDia(MouseEvent event) throws IOException {
        if (diaEstadoAnimoCR == null) {
            diaEstadoAnimoCR = new DiaEstadoAnimoCR(java.sql.Date.valueOf(getFecha()), -1, "", "");
        }
        if(estadoDeAnimo == null) {
            estadoDeAnimo = new EstadoDeAnimo(-1, "", 1, 1, 1);
        }

        abrirVentana("/com/example/JAVAFX/CRISTINADIAZCABELLO/vistas/ControladorDia.fxml", "Controlador Dia", (loader) -> {
            cDia = loader.getController();
            cDia.setControladorEnlace(this);
            cDia.setDia(dia);
        });
    }

    private void abrirVentana(String fxmlPath, String titulo, VentanaConfiguracion configuracion) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        configuracion.configurar(loader);

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.setTitle(titulo);
        stage.show();
    }

    public void setControladorEnlace(ControladorPrincipal c) {
        this.cPrincipal = c;
    }
    public void setDia(Dia dia) {
        this.dia = dia;
        txtDiaMes.setText(dia.getFecha().toString());
    }
    public void setDiaEstadoAnimoCR(DiaEstadoAnimoCR diaEstadoAnimoCR) {
        this.diaEstadoAnimoCR = diaEstadoAnimoCR;
    }
    public void setEstadoDeAnimo(EstadoDeAnimo estadoDeAnimo) {
        this.estadoDeAnimo = estadoDeAnimo;
        spnFuerzaSentimiento.getValueFactory().setValue(estadoDeAnimo.getFuerzaSentimiento());
        spnGradoProductividad.getValueFactory().setValue(estadoDeAnimo.getGradoProductividad());
        spnPaciencia.getValueFactory().setValue(estadoDeAnimo.getPaciencia());
        imgEmoji.setImage(new ImageView(estadoDeAnimo.getEmoji()).getImage());
    }
    public void actualizarEmoji(String emoji) {
        imgEmoji.setImage(new ImageView(emoji).getImage());
    }

    public String getCmbMomentoDia() {
        return cmbMomentoDia.getValue();
    }

    @FunctionalInterface
    private interface VentanaConfiguracion {
        void configurar(FXMLLoader loader) throws IOException;
    }
}
