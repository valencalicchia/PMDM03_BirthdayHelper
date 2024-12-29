package com.example.gestordecumples.models;

import androidx.annotation.NonNull;

import java.net.URI;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class Contact implements Serializable {

    private int id;
    private String tipoNotif;
    private String mensaje;
    private String telefono;
    private String fechaNacimiento;
    private String nombre;
    private String imagenId;
    //android.net.Uri not Serializable - java.net.URI SI serializable
    //private Uri rutaImagen;
    private URI rutaImagen;
    private ArrayList<String> setTelefonos = new ArrayList<>();

    public Contact() {}

//    public Contact(int id, String tipoNotif, String mensaje, String telefono, String fechaNacimiento, String nombre, String imagenId,URI rutaImagen,ArrayList<String> setTelefonos){
//        this.id = id;
//        this.tipoNotif = tipoNotif;
//        this.mensaje = mensaje;
//        this.telefono = telefono;
//        this.fechaNacimiento = fechaNacimiento;
//        this.nombre = nombre;
//        this.imagenId = imagenId;
//        this.rutaImagen = rutaImagen;
//        this.setTelefonos = setTelefonos;
//    }

    public Contact(int id, String tipoNotif, String telefono, String nombre, String fechaNacimiento, String imagenId, URI rutaImagen, ArrayList<String> setTelefonos){
        this.id = id;
        this.tipoNotif = tipoNotif;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.nombre = nombre;
        this.imagenId = imagenId;
        this.rutaImagen = rutaImagen;
        this.setTelefonos = setTelefonos;
    }

    public int getId() {
        return id;
    }

    public String getTipoNotif() {
        return tipoNotif;
    }

    public void setTipoNotif(String tipoNotif) {
        this.tipoNotif = tipoNotif;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public URI getRutaImagen() {
        return rutaImagen;
    }

    public ArrayList<String> getSetTelefonos() {
        return setTelefonos;
    }

    @NonNull
    @Override
    public String toString() {
        return "Contacto{" +
                "id=" + id +
                ", tipoNotif='" + tipoNotif + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", telefono='" + telefono + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                ", nombre='" + nombre + '\'' +
                ", imagenId='" + imagenId + '\'' +
                ", rutaImagen=" + rutaImagen +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contacto = (Contact) o;
        return id == contacto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}